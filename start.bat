@echo off
set SCRIPT_DIR=%~dp0
set VENV_DIR=%SCRIPT_DIR%.venv

if not exist "%VENV_DIR%" (
    echo Creating virtual environment...
    python -m venv "%VENV_DIR%"
)

call "%VENV_DIR%\Scripts\activate.bat"

if not exist "%VENV_DIR%\.deps_installed" (
    echo Installing dependencies...
    pip install -r "%SCRIPT_DIR%requirements.txt"
    echo. > "%VENV_DIR%\.deps_installed"
)

python "%SCRIPT_DIR%app.py"
