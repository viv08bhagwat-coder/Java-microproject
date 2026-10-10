@echo off
echo ============================================
echo   Image Converter - Compilation Script
echo ============================================
echo.

if not exist out (
    mkdir out
    echo [OK] Created output directory: out\
)

echo [..] Compiling all packages...
javac -encoding UTF-8 -d out -sourcepath src ^
    src\omni\core\Transformable.java ^
    src\omni\core\Auditable.java ^
    src\omni\core\FileItem.java ^
    src\omni\core\ConvertedItem.java ^
    src\omni\core\ConversionRecord.java ^
    src\omni\core\InvalidFormatException.java ^
    src\omni\core\FileConversionException.java ^
    src\omni\engine\FormatValidator.java ^
    src\omni\engine\MetadataParser.java ^
    src\omni\engine\PdfGenerator.java ^
    src\omni\engine\PdfExtractor.java ^
    src\omni\engine\AnalyzerEngine.java ^
    src\omni\engine\ReportGenerator.java ^
    src\omni\engine\ImageConverter.java ^
    src\omni\storage\ConversionDB.java ^
    src\omni\ui\PdfPreviewDialog.java ^
    src\omni\ui\ConverterFrame.java

if %errorlevel% equ 0 (
    echo.
    echo [OK] Compilation successful!
    echo [OK] Run the application with:  run.bat
    echo      or:  java -cp out omni.ui.ConverterFrame
) else (
    echo.
    echo [FAIL] Compilation FAILED. Check errors above.
)
echo.
pause
