# QL - Local Chatbot

A local chatbot powered by Qwen3.5-4B using llama.cpp.

## Platforms

- **Web** - Gradio-based chat UI (`app.py`)
- **Android** - Native Android app (`android-app/`)

## Architecture

### Web

- `app.py` - Gradio web UI (custom Blocks layout with chatbot + thinking panel)
- `llm_engine.py` - LLM inference via llama-cpp-python; handles prompt formatting and streaming with think tag parsing
- `model_manager.py` - Downloads GGUF model from HuggingFace on first run
- `models/` - Local model storage (auto-created)
- `start.sh` / `start.bat` - Launch scripts

### Android

- `android-app/` - Native Android chatbot app
  - `MainActivity.kt` - Single-activity entry point with model download flow
  - `LlamaEngine.kt` - JNI wrapper around llama.cpp (fully integrated)
  - `ModelManager.kt` - Handles model download with resume support
  - `ChatViewModel.kt` - Manages chat state and coordinates UI/engine
  - `ui/ChatScreen.kt` - Jetpack Compose chat interface
  - `ui/MessageBubble.kt` - Individual message bubble composable
  - `llama-jni.cpp` - JNI bridge to llama.cpp
  - `setup-llama.sh` - Downloads llama.cpp source

## Key Details

- **Model**: `unsloth/Qwen3.5-4B-GGUF` (Q4_K_XL quantization)
- **Prompt format**: ChatML (im_start/im_end tokens)
- **UI**: Gradio 3.41.2 Blocks with chatbot (messages format), thinking panel, and toggle checkbox
- **Streaming**: Yields (thinking, answer, in_thinking, thinking_done) tuples
- **Dependencies**: llama-cpp-python, gradio, huggingface_hub

### Android Build

- **compileSdk**: 35
- **targetSdk**: 35
- **minSdk**: 26 (Android 8.0)
- **NDK**: arm64-v8a, x86_64
- **16KB compatible**: Yes (ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=TRUE)

## Build & Test

### Web
```bash
./start.sh  # or start.bat on Windows
```

### Android
```bash
# First time: download llama.cpp source
cd android-app && ./setup-llama.sh

# Build
./gradlew assembleDebug

# Run tests
./gradlew test

# Run specific tests
./gradlew test --tests "com.ql.chat.ModelManagerTest"
./gradlew test --tests "com.ql.chat.ChatViewModelTest"
```

## Android Dependencies

- Compose BOM: `2024.01.00`
- OkHttp: `4.12.0` (model download)
- Kotlin: `1.9.20`
- AGP: `8.2.0`
- CMake: `3.22.1`
- NDK STL: `c++_shared`

## Android Data Flow

1. App launches → check if model exists → download if needed
2. Download shows progress (percentage + bytes), resumes on failure
3. Load model into memory via llama.cpp (10-30 seconds first load)
4. User types messages, model generates responses locally

## Lessons Learned

- llama.cpp Android integration requires building from source via CMake subdirectory
- Model download needs resume support for large files (~2.5GB)
- APK is 50-100MB with llama.cpp, within Play Store limits
- 16KB page alignment required for Android 15+ (API 35+): use `ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=TRUE` CMake flag
- Download progress should show both percentage and bytes downloaded

## Features

### Web

- Real-time streaming responses
- Thinking indicator (hourglass emoji) during reasoning
- Collapsible thinking panel (toggle via checkbox)
- GPU acceleration (n_gpu_layers=-1)

### Android

- Simple chat interface
- Automatic model download on first launch
- Resume support for interrupted downloads
- Local inference using llama.cpp
- Real-time token generation
