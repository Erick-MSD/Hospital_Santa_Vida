@echo off
echo ===============================================
echo       EJECUTANDO HOSPITAL SANTA VIDA
echo ===============================================

REM Verificar que este compilado
if not exist "out\TriageApp.class" (
    echo ERROR: La aplicacion no esta compilada
    echo Ejecuta compile.bat primero
    pause
    exit /b 1
)

echo Iniciando aplicacion de Triage...
java -cp "out;lib\mysql-connector-j-8.0.33.jar;C:\Users\Erick\Downloads\openjfx-21.0.8_windows-x64_bin-sdk\javafx-sdk-21.0.8\lib\*" ^
--module-path "C:\Users\Erick\Downloads\openjfx-21.0.8_windows-x64_bin-sdk\javafx-sdk-21.0.8\lib" ^
--add-modules javafx.controls,javafx.fxml ^
TriageApp

pause