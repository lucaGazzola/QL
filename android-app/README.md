# QL Chat - Android

A native Android chatbot powered by Qwen3.5-4B using llama.cpp.

## Features

- Simple chat interface
- Automatic model download on first launch
- Resume support for interrupted downloads
- Local inference using llama.cpp (no internet required after initial download)
- Real-time token generation

## Requirements

- Android 8.0 (API 26) or higher
- ~3GB free storage for model file
- 6GB+ RAM recommended
- Android Studio with NDK support

## Quick Start

### For Development

1. Run the setup script to download llama.cpp:
   ```bash
   ./setup-llama.sh
   ```

2. Open `android-app/` in Android Studio

3. Sync Gradle and build

4. Run on device or emulator

### For Play Store Distribution

The app is ready for Play Store distribution. When you build the release APK:

1. llama.cpp is compiled into the APK (~50-100MB)
2. Users download and install from Play Store
3. On first launch, the app downloads the model (~2.5GB)
4. After download completes, the chat interface appears

**Play Store Notes:**
- APK size: ~50-100MB (within 150MB limit)
- Model download: ~2.5GB (happens after install)
- Download resumes if interrupted
- Works on WiFi and mobile data
- 16KB page size compatible (Android 15+ ready)

## Model

Uses `unsloth/Qwen3.5-4B-GGUF` (Q4_K_XL quantization) from HuggingFace.

The model (~2.5GB) is downloaded automatically on first launch.

## Architecture

- **LlamaEngine** - JNI wrapper around llama.cpp
- **ModelManager** - Handles model download with resume support
- **ChatViewModel** - Manages chat state
- **ChatScreen** - Jetpack Compose UI

## How It Works

1. **First Launch**: App downloads the GGUF model from HuggingFace (~2.5GB)
   - Download shows progress with bytes downloaded
   - Resumes automatically if interrupted
   - Can use WiFi or mobile data
2. **Model Loading**: llama.cpp loads the model into memory (takes 10-30 seconds)
3. **Chat**: User types messages, model generates responses locally

## Troubleshooting

### "Engine not initialized" error
- Ensure llama.cpp was downloaded via `./setup-llama.sh`
- Check that your device has enough RAM (6GB+ recommended)

### Download fails or is slow
- Check your internet connection
- The app will resume from where it left off
- Try switching between WiFi and mobile data

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
