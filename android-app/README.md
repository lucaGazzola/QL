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
