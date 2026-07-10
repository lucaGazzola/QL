# QL - Local Chatbot

A self-contained, one-click local chatbot powered by Qwen3.5-4B. Downloads the model automatically on first run, runs entirely on your machine with no cloud APIs.

## Platforms

- **Web** - Gradio-based chat UI (`app.py`)
- **Android** - Native Android app (`android-app/`)

## Requirements

### Web

- Python 3.10+
- 8GB unified memory (Apple Silicon or equivalent)
- Metal-capable GPU (Mac) or CUDA GPU (Windows/Linux)

### Android

- Android 8.0 (API 26) or higher
- 6GB+ RAM recommended
- ~3GB free storage for model file
- Android Studio with NDK support (for building)

## Run

### Web

**macOS / Linux:**
```bash
./start.sh
```

**Windows:**
```
start.bat
```

The first run creates a virtual environment, installs dependencies (including compiling llama-cpp-python with GPU support), and downloads the model (~3GB). Subsequent launches are instant.

A browser window opens automatically with the chat interface.

### Android

1. Run the setup script to download llama.cpp:
   ```bash
   cd android-app
   ./setup-llama.sh
   ```

2. Open `android-app/` in Android Studio

3. Sync Gradle and build

4. Run on device or emulator

See [`android-app/README.md`](android-app/README.md) for details.

## Project Structure

### Web

- `app.py` - Gradio web UI entry point
- `llm_engine.py` - LLM initialization and prompt formatting
- `model_manager.py` - Automatic model download from Hugging Face
- `requirements.txt` - Python dependencies
- `start.sh` / `start.bat` - One-click bootstrappers

### Android

- `android-app/` - Native Android chatbot app
  - `app/src/main/java/com/ql/chat/` - Kotlin source code
  - `app/src/main/cpp/` - llama.cpp JNI bindings
  - `setup-llama.sh` - Downloads llama.cpp source

## Model

- **Repo:** unsloth/Qwen3.5-4B-GGUF
- **Quantization:** Q4_K_XL (fits within 8GB RAM with room for 8k context)

## Tech Stack

### Web

- [llama-cpp-python](https://github.com/abetlen/llama-cpp-python) - Inference backend with Metal/CUDA acceleration
- [huggingface_hub](https://huggingface.co/docs/huggingface_hub) - Model downloader
- [Gradio](https://gradio.app) - Web UI

### Android

- [llama.cpp](https://github.com/ggerganov/llama.cpp) - Inference backend (via JNI)
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI toolkit
- [OkHttp](https://square.github.io/okhttp/) - Model download with resume support
