# QL - Local Chatbot

A self-contained, one-click local chatbot powered by Qwen-AgentWorld-35B. Downloads the model automatically on first run, runs entirely on your machine with no cloud APIs.

## Requirements

- Python 3.10+
- 32GB unified memory (Apple Silicon or equivalent)
- Metal-capable GPU (Mac) or CUDA GPU (Windows/Linux)

## Run

**macOS / Linux:**
```bash
./start.sh
```

**Windows:**
```
start.bat
```

The first run creates a virtual environment, installs dependencies (including compiling llama-cpp-python with GPU support), and downloads the model (~6GB). Subsequent launches are instant.

A browser window opens automatically with the chat interface.

## Project Structure

- `app.py` - Gradio web UI entry point
- `llm_engine.py` - LLM initialization and prompt formatting
- `model_manager.py` - Automatic model download from Hugging Face
- `requirements.txt` - Python dependencies
- `start.sh` / `start.bat` - One-click bootstrappers

## Model

- **Repo:** unsloth/Qwen-AgentWorld-35B-A3B-GGUF
- **Quantization:** UD-IQ2_XXS (fits within 32GB RAM with room for 8k context)

## Tech Stack

- [llama-cpp-python](https://github.com/abetlen/llama-cpp-python) - Inference backend with Metal/CUDA acceleration
- [huggingface_hub](https://huggingface.co/docs/huggingface_hub) - Model downloader
- [Gradio](https://gradio.app) - Web UI
