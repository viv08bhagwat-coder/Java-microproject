@echo off
echo ============================================
echo   Image Converter - Application Launcher
echo ============================================
echo.

if not exist out\omni\ui\ConverterFrame.class (
    echo [WARN] Not compiled yet. Running compile.bat first...
    call compile.bat
    if %errorlevel% neq 0 exit /b 1
)

echo [..] Launching Image Converter Workstation GUI...
java -cp out omni.ui.ConverterFrame
