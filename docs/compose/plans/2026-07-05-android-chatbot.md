# Android Chatbot Implementation Plan

> [!NOTE]
> This document may not reflect the current implementation.
> See the final report for up-to-date state:
> [Final Report](../reports/android-chatbot.md)

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create a minimal Android chatbot app that loads Qwen3.5-4B GGUF model and provides a simple chat interface.

**Architecture:** Single-activity Jetpack Compose app with llama.cpp JNI bindings for inference. Model downloaded from HuggingFace on first launch. Minimal UI: message list + text input.

**Tech Stack:** Kotlin, Jetpack Compose, llama.cpp (JNI), HuggingFace Hub API, OkHttp for downloads

---

## File Structure

```
android-app/
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/ql/chat/
│   │   │   ├── MainActivity.kt
│   │   │   ├── LlamaEngine.kt
│   │   │   ├── ModelManager.kt
│   │   │   ├── ChatViewModel.kt
│   │   │   └── ui/
│   │   │       ├── ChatScreen.kt
│   │   │       └── MessageBubble.kt
│   │   ├── cpp/
│   │   │   ├── CMakeLists.txt
│   │   │   ├── llama-jni.cpp
│   │   │   └── llama.h (prebuilt header)
│   │   └── res/
│   │       └── values/
│   │           ├── strings.xml
│   │           └── themes.xml
│   └── src/test/java/com/ql/chat/
│       ├── ModelManagerTest.kt
│       └── ChatViewModelTest.kt
├── build.gradle.kts (project-level)
├── settings.gradle.kts
└── gradle.properties
```

---

### Task 1: Project Setup

**Files:**
- Create: `android-app/build.gradle.kts` (project)
- Create: `android-app/settings.gradle.kts`
- Create: `android-app/gradle.properties`
- Create: `android-app/app/build.gradle.kts`
- Create: `android-app/app/src/main/AndroidManifest.xml`
- Create: `android-app/app/src/main/res/values/strings.xml`
- Create: `android-app/app/src/main/res/values/themes.xml`

- [ ] **Step 1: Create project-level build.gradle.kts**

```kotlin
// android-app/build.gradle.kts
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
```

- [ ] **Step 2: Create settings.gradle.kts**

```kotlin
// android-app/settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "QL"
include(":app")
```

- [ ] **Step 3: Create gradle.properties**

```properties
# android-app/gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: Create app/build.gradle.kts**

```kotlin
// android-app/app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ql.chat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ql.chat"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Networking for model download
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

- [ ] **Step 5: Create AndroidManifest.xml**

```xml
<!-- android-app/app/src/main/AndroidManifest.xml -->
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        android:maxSdkVersion="28" />

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.QL">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.QL">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 6: Create resource files**

```xml
<!-- android-app/app/src/main/res/values/strings.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">QL Chat</string>
</resources>
```

```xml
<!-- android-app/app/src/main/res/values/themes.xml -->
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.QL" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

- [ ] **Step 7: Create MainActivity.kt placeholder**

```kotlin
// android-app/app/src/main/java/com/ql/chat/MainActivity.kt
package com.ql.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    // ChatScreen will be added in Task 5
                }
            }
        }
    }
}
```

- [ ] **Step 8: Verify project builds**

Run from `android-app/`:
```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

---

### Task 2: JNI Bindings for llama.cpp

**Files:**
- Create: `android-app/app/src/main/cpp/CMakeLists.txt`
- Create: `android-app/app/src/main/cpp/llama-jni.cpp`
- Create: `android-app/app/src/main/java/com/ql/chat/LlamaEngine.kt`

- [ ] **Step 1: Create CMakeLists.txt**

```cmake
# android-app/app/src/main/cpp/CMakeLists.txt
cmake_minimum_required(VERSION 3.22.1)
project("ql-chat")

# llama.cpp will be built from source or linked as prebuilt
# For now, we define the JNI bridge that will call into llama.cpp

add_library(ql-jni SHARED
    llama-jni.cpp
)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

# Link against llama.cpp (to be provided as prebuilt or built from source)
# target_link_libraries(ql-jni llama)

target_link_libraries(ql-jni
    android
    log
    ${JNI_LIBRARIES}
)
```

- [ ] **Step 2: Create llama-jni.cpp**

```cpp
// android-app/app/src/main/cpp/llama-jni.cpp
#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "LlamaEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global engine handle (will be replaced with actual llama.cpp context)
static jlong g_engine_handle = 0;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_ql_chat_LlamaEngine_nativeInit(
    JNIEnv *env,
    jobject thiz,
    jstring model_path,
    jint n_ctx
) {
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("Initializing engine with model: %s, context: %d", path, n_ctx);

    // TODO: Replace with actual llama_init_from_file()
    // llama_model *model = llama_init_from_file(path, params);
    // g_engine_handle = reinterpret_cast<jlong>(model);

    env->ReleaseStringUTFChars(model_path, path);

    // Return dummy handle for now
    return 1L;
}

JNIEXPORT void JNICALL
Java_com_ql_chat_LlamaEngine_nativeFree(
    JNIEnv *env,
    jobject thiz,
    jlong handle
) {
    LOGI("Freeing engine handle: %ld", handle);
    // TODO: Replace with actual llama_free()
    // llama_model *model = reinterpret_cast<llama_model*>(handle);
    // llama_free(model);
}

JNIEXPORT jstring JNICALL
Java_com_ql_chat_LlamaEngine_nativeGenerate(
    JNIEnv *env,
    jobject thiz,
    jlong handle,
    jstring prompt,
    jint max_tokens
) {
    const char *prompt_str = env->GetStringUTFChars(prompt, nullptr);
    LOGI("Generating with prompt length: %zu, max_tokens: %d",
         strlen(prompt_str), max_tokens);

    // TODO: Replace with actual llama completion
    // For now, return a placeholder response
    std::string response = "Hello! I'm a placeholder response. "
                           "The actual llama.cpp integration needs to be completed.";

    env->ReleaseStringUTFChars(prompt, prompt_str);

    return env->NewStringUTF(response.c_str());
}

} // extern "C"
```

- [ ] **Step 3: Create LlamaEngine.kt**

```kotlin
// android-app/app/src/main/java/com/ql/chat/LlamaEngine.kt
package com.ql.chat

class LlamaEngine {
    init {
        System.loadLibrary("ql-jni")
    }

    private external fun nativeInit(modelPath: String, nCtx: Int): Long
    private external fun nativeFree(handle: Long)
    private external fun nativeGenerate(handle: Long, prompt: String, maxTokens: Int): String

    private var handle: Long = 0L

    fun init(modelPath: String, nCtx: Int = 4096) {
        handle = nativeInit(modelPath, nCtx)
    }

    fun generate(prompt: String, maxTokens: Int = 2048): String {
        if (handle == 0L) {
            throw IllegalStateException("Engine not initialized")
        }
        return nativeGenerate(handle, prompt, maxTokens)
    }

    fun free() {
        if (handle != 0L) {
            nativeFree(handle)
            handle = 0L
        }
    }

    protected fun finalize() {
        free()
    }
}
```

- [ ] **Step 4: Verify JNI compiles**

Run from `android-app/`:
```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL (may have linker warnings for missing llama.cpp symbols)

---

### Task 3: Model Manager

**Files:**
- Create: `android-app/app/src/main/java/com/ql/chat/ModelManager.kt`
- Create: `android-app/app/src/test/java/com/ql/chat/ModelManagerTest.kt`

- [ ] **Step 1: Write ModelManager tests**

```kotlin
// android-app/app/src/test/java/com/ql/chat/ModelManagerTest.kt
package com.ql.chat

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class ModelManagerTest {
    private lateinit var tempDir: File

    @Before
    fun setup() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "ql-test-${System.currentTimeMillis()}")
        tempDir.mkdirs()
    }

    @Test
    fun `modelExists returns false when model file missing`() {
        val manager = ModelManager(tempDir)
        assertFalse(manager.modelExists())
    }

    @Test
    fun `modelExists returns true when model file present`() {
        val modelFile = File(tempDir, ModelManager.MODEL_FILENAME)
        modelFile.createNewFile()
        val manager = ModelManager(tempDir)
        assertTrue(manager.modelExists())
    }

    @Test
    fun `getModelPath returns correct path`() {
        val manager = ModelManager(tempDir)
        val expected = File(tempDir, ModelManager.MODEL_FILENAME).absolutePath
        assertEquals(expected, manager.getModelPath())
    }

    @Test
    fun `getModelUrl returns HuggingFace URL`() {
        val manager = ModelManager(tempDir)
        assertTrue(manager.getModelUrl().contains("huggingface.co"))
        assertTrue(manager.getModelUrl().contains(ModelManager.MODEL_FILENAME))
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run from `android-app/`:
```bash
./gradlew test --tests "com.ql.chat.ModelManagerTest"
```
Expected: FAIL with "Unresolved reference: ModelManager"

- [ ] **Step 3: Implement ModelManager**

```kotlin
// android-app/app/src/main/java/com/ql/chat/ModelManager.kt
package com.ql.chat

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ModelManager(private val modelsDir: File) {

    companion object {
        const val MODEL_REPO = "unsloth/Qwen3.5-4B-GGUF"
        const val MODEL_FILENAME = "Qwen3.5-4B-UD-Q4_K_XL.gguf"
        private const val BASE_URL = "https://huggingface.co"
    }

    init {
        modelsDir.mkdirs()
    }

    fun modelExists(): Boolean {
        return File(modelsDir, MODEL_FILENAME).exists()
    }

    fun getModelPath(): String {
        return File(modelsDir, MODEL_FILENAME).absolutePath
    }

    fun getModelUrl(): String {
        return "$BASE_URL/$MODEL_REPO/resolve/main/$MODEL_FILENAME"
    }

    @Throws(IOException::class)
    fun downloadModel(
        onProgress: ((percent: Int) -> Unit)? = null
    ): String {
        val outputFile = File(modelsDir, MODEL_FILENAME)
        if (outputFile.exists()) {
            return outputFile.absolutePath
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(getModelUrl())
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Download failed: ${response.code}")
        }

        val body = response.body ?: throw IOException("Empty response body")
        val contentLength = body.contentLength()
        var downloaded = 0L

        body.byteStream().use { input ->
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    if (contentLength > 0) {
                        val progress = ((downloaded * 100) / contentLength).toInt()
                        onProgress?.invoke(progress)
                    }
                }
            }
        }

        return outputFile.absolutePath
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run from `android-app/`:
```bash
./gradlew test --tests "com.ql.chat.ModelManagerTest"
```
Expected: PASS (4 tests)

- [ ] **Step 5: Commit**

```bash
cd android-app
git add app/src/main/java/com/ql/chat/ModelManager.kt app/src/test/java/com/ql/chat/ModelManagerTest.kt
git commit -m "feat: add model manager with HuggingFace download support"
```

---

### Task 4: Chat ViewModel

**Files:**
- Create: `android-app/app/src/main/java/com/ql/chat/ChatViewModel.kt`
- Create: `android-app/app/src/test/java/com/ql/chat/ChatViewModelTest.kt`

- [ ] **Step 1: Write ChatViewModel tests**

```kotlin
// android-app/app/src/test/java/com/ql/chat/ChatViewModelTest.kt
package com.ql.chat

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {
    private lateinit var viewModel: ChatViewModel

    @Before
    fun setup() {
        viewModel = ChatViewModel()
    }

    @Test
    fun `initial state has empty messages`() {
        assertTrue(viewModel.messages.isEmpty())
    }

    @Test
    fun `initial state is not loading`() {
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `sendMessage adds user message`() {
        viewModel.sendMessage("Hello")
        assertEquals(1, viewModel.messages.size)
        assertEquals("Hello", viewModel.messages[0].content)
        assertEquals("user", viewModel.messages[0].role)
    }

    @Test
    fun `messages are immutable list`() {
        viewModel.sendMessage("Hello")
        val size1 = viewModel.messages.size
        viewModel.sendMessage("World")
        val size2 = viewModel.messages.size
        assertTrue(size2 > size1)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run from `android-app/`:
```bash
./gradlew test --tests "com.ql.chat.ChatViewModelTest"
```
Expected: FAIL with "Unresolved reference: ChatViewModel"

- [ ] **Step 3: Implement ChatViewModel**

```kotlin
// android-app/app/src/main/java/com/ql/chat/ChatViewModel.kt
package com.ql.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val role: String,
    val content: String
)

class ChatViewModel : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    val isLoading = mutableStateOf(false)

    private var engine: LlamaEngine? = null
    private var modelManager: ModelManager? = null

    fun initEngine(modelPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val eng = LlamaEngine()
            eng.init(modelPath)
            engine = eng
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || isLoading.value) return

        _messages.add(ChatMessage("user", content))
        isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val eng = engine
                if (eng == null) {
                    withContext(Dispatchers.Main) {
                        _messages.add(ChatMessage("assistant", "Engine not initialized"))
                        isLoading.value = false
                    }
                    return@launch
                }

                val response = eng.generate(content)
                withContext(Dispatchers.Main) {
                    _messages.add(ChatMessage("assistant", response))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _messages.add(ChatMessage("assistant", "Error: ${e.message}"))
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading.value = false
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine?.free()
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run from `android-app/`:
```bash
./gradlew test --tests "com.ql.chat.ChatViewModelTest"
```
Expected: PASS (4 tests)

- [ ] **Step 5: Commit**

```bash
cd android-app
git add app/src/main/java/com/ql/chat/ChatViewModel.kt app/src/test/java/com/ql/chat/ChatViewModelTest.kt
git commit -m "feat: add chat ViewModel with message state management"
```

---

### Task 5: Chat UI

**Files:**
- Create: `android-app/app/src/main/java/com/ql/chat/ui/MessageBubble.kt`
- Create: `android-app/app/src/main/java/com/ql/chat/ui/ChatScreen.kt`
- Modify: `android-app/app/src/main/java/com/ql/chat/MainActivity.kt`

- [ ] **Step 1: Create MessageBubble composable**

```kotlin
// android-app/app/src/main/java/com/ql/chat/ui/MessageBubble.kt
package com.ql.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ql.chat.ChatMessage

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                fontSize = 16.sp
            )
        }
    }
}
```

- [ ] **Step 2: Create ChatScreen composable**

```kotlin
// android-app/app/src/main/java/com/ql/chat/ui/ChatScreen.kt
package com.ql.chat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ql.chat.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QL Chat") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(viewModel.messages) { message ->
                    MessageBubble(message = message)
                }
            }

            // Loading indicator
            if (viewModel.isLoading.value) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    enabled = !viewModel.isLoading.value
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    enabled = inputText.isNotBlank() && !viewModel.isLoading.value
                ) {
                    Text("Send")
                }
            }
        }
    }
}
```

- [ ] **Step 3: Update MainActivity to use ChatScreen**

```kotlin
// android-app/app/src/main/java/com/ql/chat/MainActivity.kt
package com.ql.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.ql.chat.ui.ChatScreen
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize model manager and engine
        val modelsDir = File(filesDir, "models")
        val modelManager = ModelManager(modelsDir)

        if (modelManager.modelExists()) {
            viewModel.initEngine(modelManager.getModelPath())
        }
        // TODO: Handle model download on first launch

        setContent {
            MaterialTheme {
                Surface {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}
```

- [ ] **Step 4: Verify UI compiles**

Run from `android-app/`:
```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
cd android-app
git add app/src/main/java/com/ql/chat/ui/ app/src/main/java/com/ql/chat/MainActivity.kt
git commit -m "feat: add minimal chat UI with message bubbles and input"
```

---

### Task 6: Integration - Model Download Flow

**Files:**
- Modify: `android-app/app/src/main/java/com/ql/chat/MainActivity.kt`

- [ ] **Step 1: Add download state handling to MainActivity**

```kotlin
// android-app/app/src/main/java/com/ql/chat/MainActivity.kt
package com.ql.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import com.ql.chat.ui.ChatScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modelsDir = File(filesDir, "models")
        val modelManager = ModelManager(modelsDir)

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var isDownloading by remember { mutableStateOf(false) }
            var downloadProgress by remember { mutableIntStateOf(0) }
            var downloadError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                if (!modelManager.modelExists()) {
                    isDownloading = true
                    try {
                        withContext(Dispatchers.IO) {
                            modelManager.downloadModel { progress ->
                                downloadProgress = progress
                            }
                        }
                        viewModel.initEngine(modelManager.getModelPath())
                    } catch (e: Exception) {
                        downloadError = e.message
                    } finally {
                        isDownloading = false
                    }
                } else {
                    viewModel.initEngine(modelManager.getModelPath())
                }
            }

            MaterialTheme {
                Surface {
                    when {
                        isDownloading -> {
                            DownloadScreen(progress = downloadProgress)
                        }
                        downloadError != null -> {
                            ErrorScreen(
                                message = downloadError ?: "Unknown error",
                                onRetry = {
                                    downloadError = null
                                    scope.launch {
                                        isDownloading = true
                                        try {
                                            withContext(Dispatchers.IO) {
                                                modelManager.downloadModel { progress ->
                                                    downloadProgress = progress
                                                }
                                            }
                                            viewModel.initEngine(modelManager.getModelPath())
                                        } catch (e: Exception) {
                                            downloadError = e.message
                                        } finally {
                                            isDownloading = false
                                        }
                                    }
                                }
                            )
                        }
                        else -> {
                            ChatScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadScreen(progress: Int) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material3.Text(
            text = "Downloading model...",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
        )
        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(16.dp)
        )
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = androidx.compose.ui.Modifier.fillMaxWidth()
        )
        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(8.dp)
        )
        androidx.compose.material3.Text(text = "$progress%")
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material3.Text(
            text = "Download failed",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
        )
        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(8.dp)
        )
        androidx.compose.material3.Text(text = message)
        androidx.compose.foundation.layout.Spacer(
            modifier = androidx.compose.ui.Modifier.height(16.dp)
        )
        androidx.compose.material3.Button(onClick = onRetry) {
            androidx.compose.material3.Text("Retry")
        }
    }
}
```

- [ ] **Step 2: Verify complete app compiles**

Run from `android-app/`:
```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
cd android-app
git add app/src/main/java/com/ql/chat/MainActivity.kt
git commit -m "feat: add model download flow with progress and error handling"
```

---

### Task 7: README and Documentation

**Files:**
- Create: `android-app/README.md`

- [ ] **Step 1: Create README**

```markdown
# QL Chat - Android

A minimal Android chatbot powered by Qwen3.5-4B using llama.cpp.

## Features

- Simple chat interface
- Automatic model download on first launch
- Local inference (no internet required after initial download)

## Requirements

- Android 8.0 (API 26) or higher
- ~3GB free storage for model file
- 6GB+ RAM recommended

## Building

1. Open `android-app/` in Android Studio
2. Sync Gradle
3. Build and run on device or emulator

## Model

Uses `unsloth/Qwen3.5-4B-GGUF` (Q4_K_XL quantization) from HuggingFace.

The model (~2.5GB) is downloaded automatically on first launch.

## Architecture

- **LlamaEngine** - JNI wrapper around llama.cpp
- **ModelManager** - Handles model download and caching
- **ChatViewModel** - Manages chat state
- **ChatScreen** - Jetpack Compose UI

## Current Status

- [x] Project setup
- [x] JNI bindings (placeholder)
- [x] Model download
- [x] Chat UI
- [ ] Full llama.cpp integration
```

- [ ] **Step 2: Commit**

```bash
cd android-app
git add README.md
git commit -m "docs: add Android app README"
```

---

## Summary

| Task | Description | Dependencies |
|------|-------------|--------------|
| 1 | Project setup | None |
| 2 | JNI bindings | Task 1 |
| 3 | Model manager | Task 1 |
| 4 | Chat ViewModel | Task 1 |
| 5 | Chat UI | Task 4 |
| 6 | Integration | Tasks 2, 3, 5 |
| 7 | Documentation | All |

**Note:** The JNI bindings in Task 2 are placeholders. Full llama.cpp integration requires either:
1. Building llama.cpp from source for Android (complex)
2. Using a prebuilt llama.cpp library for Android
3. Using an existing Android wrapper like `llama-android`

The current plan provides a working structure that can be enhanced with actual llama.cpp integration later.
