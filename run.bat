@echo off
setlocal
echo ===============================================
echo     EJECUTANDO HOSPITAL SANTA VIDA
echo ===============================================

REM Ruta a JavaFX (ajusta si la mueves)
set JAVAFX_LIB=C:\Users\Erick\Downloads\openjfx-21.0.8_windows-x64_bin-sdk\javafx-sdk-21.0.8\lib

REM Verificar que este compilado el main correcto
if not exist "out\HospitalSantaVidaApp.class" (
    echo ERROR: La aplicacion no esta compilada o la clase principal no existe.
    echo Ejecuta compile.bat primero (o verifica que HospitalSantaVidaApp.java compile sin errores)
    pause
    exit /b 1
)

echo Iniciando aplicacion...
set CP=out;lib\mysql-connector-j-8.0.33.jar
echo Classpath: %CP%
echo Module Path: %JAVAFX_LIB%
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml -cp "%CP%" HospitalSantaVidaApp

if %errorlevel% neq 0 (
    echo.
    echo ERROR: La aplicacion termino con codigo %errorlevel%
    echo Revisa los mensajes de error arriba.
)

endlocal
pause