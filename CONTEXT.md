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
