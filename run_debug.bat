@echo off
setlocal
echo ===============================================
echo     EJECUTANDO HOSPITAL SANTA VIDA (DEBUG)
echo ===============================================

REM Ruta a JavaFX
set JAVAFX_LIB=C:\Users\Erick\Downloads\openjfx-21.0.8_windows-x64_bin-sdk\javafx-sdk-21.0.8\lib

REM Verificar que este compilado
if not exist "out\HospitalSantaVidaApp.class" (
    echo ERROR: La aplicacion no esta compilada.
    echo Ejecuta compile.bat primero.
    pause
    exit /b 1
)

echo Verificando JavaFX...
if not exist "%JAVAFX_LIB%" (
    echo ERROR: JavaFX no encontrado en: %JAVAFX_LIB%
    pause
    exit /b 1
)

echo Iniciando aplicacion...
echo.
echo NOTAS:
echo - Si aparece un error de base de datos, es normal (cierra el dialogo)
echo - La ventana puede tardar unos segundos en aparecer
echo - Busca la ventana en la barra de tareas si no la ves
echo.

set CP=out;lib\mysql-connector-j-8.0.33.jar
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp "%CP%" HospitalSantaVidaApp

echo.
echo ===============================================
echo La aplicacion ha terminado.
echo ===============================================
pause