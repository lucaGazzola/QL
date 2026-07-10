---
feature: android-chatbot
status: delivered
specs: []
plans:
  - docs/compose/plans/2026-07-05-android-chatbot.md
branch: main
commits: 3e1eba8..c7e5904
---

# Android Chatbot — Final Report

## What Was Built

A fully functional Android chatbot app that loads the Qwen3.5-4B GGUF model and provides a simple chat interface. The app is ready for Play Store distribution. Users install the APK (~50-100MB), and on first launch the app downloads the model (~2.5GB) with progress display and resume support. After download, the app runs local inference using llama.cpp.

## Architecture

**Components:**
- `MainActivity.kt` - Single-activity entry point with model download flow
- `LlamaEngine.kt` - JNI wrapper around llama.cpp (fully integrated)
- `ModelManager.kt` - Handles model download with resume support
- `ChatViewModel.kt` - Manages chat state and coordinates UI/engine
- `ui/ChatScreen.kt` - Jetpack Compose chat interface
- `ui/MessageBubble.kt` - Individual message bubble composable

**Data Flow:**
1. App launches → check if model exists → download if needed
2. Download shows progress with bytes downloaded, resumes on failure
3. Load model into memory using llama.cpp (10-30 seconds)
4. User types messages, model generates responses locally

**Key Files:**
```
android-app/
├── app/src/main/java/com/ql/chat/
│   ├── MainActivity.kt
│   ├── LlamaEngine.kt
│   ├── ModelManager.kt
│   ├── ChatViewModel.kt
│   └── ui/
│       ├── ChatScreen.kt
│       └── MessageBubble.kt
├── app/src/main/cpp/
│   ├── CMakeLists.txt
│   └── llama-jni.cpp
├── setup-llama.sh
└── app/src/test/java/com/ql/chat/
    ├── ModelManagerTest.kt
    └── ChatViewModelTest.kt
```

## Play Store Distribution

**Ready for Play Store:**
- APK size: ~50-100MB (within 150MB limit)
- Model download: ~2.5GB (happens after install)
- Download resumes if interrupted
- Works on WiFi and mobile data
- No special permissions required
- 16KB page size compatible (Android 15+ ready)

**Build Configuration:**
- `compileSdk = 35`
- `targetSdk = 35`
- `minSdk = 26`
- `ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES = TRUE` (16KB alignment)

**User experience:**
1. Install from Play Store
2. Open app - see download progress
3. Wait for model download (varies by connection)
4. Start chatting

## Usage

### For Development
1. Run `./setup-llama.sh` to download llama.cpp
2. Open in Android Studio, build, and run

### For Production
1. Build release APK with llama.cpp compiled in
2. Upload to Play Store
3. Users install and use

## Verification

- All project files created and verified
- Unit tests for ModelManager (download states, resume, size)
- Unit tests for ChatViewModel
- JNI bindings fully integrated with llama.cpp API
- Download with resume support tested
- Progress display with bytes downloaded
- 16KB page alignment verified for Android 15+ compatibility

## Journey Log

- [lesson] llama.cpp Android integration requires building from source via CMake subdirectory
- [lesson] Model download needs resume support for large files (~2.5GB)
- [lesson] APK can be 50-100MB with llama.cpp, within Play Store limits
- [lesson] First model load takes 10-30 seconds, subsequent loads are faster
- [lesson] Download progress should show both percentage and bytes downloaded
- [lesson] 16KB page alignment required for Android 15+ (API 35+) compatibility
- [lesson] Use ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=TRUE CMake flag for proper alignment

## Source Materials

| File | Role | Notes |
|------|------|-------|
| `docs/compose/plans/2026-07-05-android-chatbot.md` | Implementation plan | Complete |
| `setup-llama.sh` | Setup automation | Downloads llama.cpp |
