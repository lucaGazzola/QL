#!/bin/bash
# Setup script for llama.cpp integration
# Run this before building the Android app

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LLAMA_DIR="${SCRIPT_DIR}/llama.cpp"

echo "Setting up llama.cpp for Android..."

# Check if llama.cpp already exists
if [ -d "$LLAMA_DIR" ]; then
    echo "llama.cpp directory already exists at: $LLAMA_DIR"
    echo "Updating to latest version..."
    cd "$LLAMA_DIR"
    git pull
else
    echo "Cloning llama.cpp..."
    cd "$SCRIPT_DIR"
    git clone --depth 1 https://github.com/ggerganov/llama.cpp.git
fi

echo ""
echo "llama.cpp setup complete!"
echo ""
echo "To build the Android app:"
echo "  1. Open android-app/ in Android Studio"
echo "  2. Sync Gradle"
echo "  3. Build and run"
echo ""
echo "Note: First build will take a while as it compiles llama.cpp from source."
