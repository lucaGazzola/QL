# QL - Local Chatbot

A local chatbot powered by Qwen3.5-4B using llama.cpp.

## Architecture

- `app.py` - Gradio web UI (custom Blocks layout with chatbot + thinking panel)
- `llm_engine.py` - LLM inference via llama-cpp-python; handles prompt formatting and streaming with think tag parsing
- `model_manager.py` - Downloads GGUF model from HuggingFace on first run
- `models/` - Local model storage (auto-created)
- `start.sh` / `start.bat` - Launch scripts

## Key Details

- **Model**: `unsloth/Qwen3.5-4B-GGUF` (Q4_K_XL quantization)
- **Prompt format**: ChatML (im_start/im_end tokens)
- **UI**: Gradio 3.41.2 Blocks with chatbot (messages format), thinking panel, and toggle checkbox
- **Streaming**: Yields (thinking, answer, in_thinking, thinking_done) tuples
- **Dependencies**: llama-cpp-python, gradio, huggingface_hub

## Features

- Real-time streaming responses
- Thinking indicator (hourglass emoji) during reasoning
- Collapsible thinking panel (toggle via checkbox)
- GPU acceleration (n_gpu_layers=-1)
