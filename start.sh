#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
VENV_DIR="$SCRIPT_DIR/.venv"

if [ ! -d "$VENV_DIR" ]; then
    echo "Creating virtual environment..."
    python3 -m venv "$VENV_DIR"
fi

source "$VENV_DIR/bin/activate"

if [ ! -f "$VENV_DIR/.deps_installed" ]; then
    echo "Installing dependencies (this may take a while on first run)..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        CMAKE_ARGS="-DGGML_METAL=on" pip install -r "$SCRIPT_DIR/requirements.txt"
    else
        pip install -r "$SCRIPT_DIR/requirements.txt"
    fi
    touch "$VENV_DIR/.deps_installed"
fi

python "$SCRIPT_DIR/app.py"
