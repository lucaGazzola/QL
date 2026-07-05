# QL Chat - Android

A minimal Android chatbot powered by Qwen3.5-4B using llama.cpp.

## Features

- Simple chat interface
- Automatic model download on first launch
- Local inference using llama.cpp (no internet required after initial download)
- Real-time token generation

## Requirements

- Android 8.0 (API 26) or higher
- ~3GB free storage for model file
- 6GB+ RAM recommended
- Android Studio with NDK support

## Quick Start

1. Run the setup script to download llama.cpp:
   ```bash
   ./setup-llama.sh
   ```

2. Open `android-app/` in Android Studio

3. Sync Gradle and build

4. Run on device or emulator

## Model

Uses `unsloth/Qwen3.5-4B-GGUF` (Q4_K_XL quantization) from HuggingFace.

The model (~2.5GB) is downloaded automatically on first launch.

## Architecture

- **LlamaEngine** - JNI wrapper around llama.cpp
- **ModelManager** - Handles model download and caching
- **ChatViewModel** - Manages chat state
- **ChatScreen** - Jetpack Compose UI

## How It Works

1. **First Launch**: App downloads the GGUF model from HuggingFace (~2.5GB)
2. **Model Loading**: llama.cpp loads the model into memory (takes 10-30 seconds)
3. **Chat**: User types messages, model generates responses locally

## Troubleshooting

### "Engine not initialized" error
- Ensure llama.cpp was downloaded via `./setup-llama.sh`
- Check that your device has enough RAM (6GB+ recommended)

### Slow performance
- First inference is slower as the model loads
- Subsequent messages should be faster

### Build fails
- Ensure Android Studio has NDK installed
- Check that CMake 3.22.1+ is available

## Development

The app uses a hybrid approach:
- If llama.cpp source is present, it builds and links against it
- If not, it runs in placeholder mode with dummy responses

This allows development without the full llama.cpp build, but you need the real library for actual inference.
