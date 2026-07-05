import os
from huggingface_hub import hf_hub_download

MODEL_REPO = "unsloth/Qwen3.6-27B-MTP-GGUF"
MODEL_FILENAME = "Qwen3.6-27B-Q3_K_S.gguf"
MODELS_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "models")


def get_model_path() -> str:
    os.makedirs(MODELS_DIR, exist_ok=True)
    model_path = os.path.join(MODELS_DIR, MODEL_FILENAME)

    if os.path.exists(model_path):
        print(f"Model already exists at {model_path}")
        return model_path

    print(f"Downloading {MODEL_FILENAME} from {MODEL_REPO}...")
    print("This may take a while on first launch.")

    downloaded_path = hf_hub_download(
        repo_id=MODEL_REPO,
        filename=MODEL_FILENAME,
        local_dir=MODELS_DIR,
        local_dir_use_symlinks=False,
    )

    print("Download complete.")
    return downloaded_path
