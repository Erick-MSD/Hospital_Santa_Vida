@echo off
setlocal
echo ===============================================
echo     COMPILANDO HOSPITAL SANTA VIDA
echo ===============================================

REM Ruta a JavaFX (ajusta si cambias la version)
set JAVAFX_LIB=C:\Users\Erick\Downloads\openjfx-21.0.8_windows-x64_bin-sdk\javafx-sdk-21.0.8\lib

REM Crear directorio out si no existe
if not exist "out" mkdir out

REM Limpiar solo clases antiguas
echo Limpiando clases anteriores...
for /r out %%f in (*.class) do del /q "%%f" >nul 2>&1

REM Compilar con JavaFX (incluye todos los .java recursivamente)
echo Compilando aplicacion...
for /f "delims=" %%f in ('dir /b /s src\*.java') do (
    set FILE=%%f
    call :compileOne "%%f" || goto :compileError
)
goto :copyResources

:compileOne
javac -d out -cp "lib\mysql-connector-j-8.0.33.jar;%JAVAFX_LIB%\*;src" %1
if errorlevel 1 exit /b 1
exit /b 0

:compileError
echo ------------------------------------------------
echo ERROR EN COMPILACION
echo Revisa el archivo indicado arriba.
echo ------------------------------------------------
pause
exit /b 1

:copyResources
REM Copiar recursos FXML y CSS
echo Copiando recursos FXML y CSS...
if not exist "out\ui" mkdir out\ui
copy /y "src\ui\*.fxml" "out\ui\" >nul
copy /y "src\ui\*.css" "out\ui\" >nul

REM Copiar assets (imágenes)
echo Copiando assets...
if not exist "out\assets" mkdir out\assets
if not exist "out\assets\img" mkdir out\assets\img
copy /y "assets\img\*" "out\assets\img\" >nul

echo.
echo ===============================================
echo     COMPILACION EXITOSA
echo ===============================================
echo Clase principal: HospitalSantaVidaApp
echo.
echo Para ejecutar la aplicacion usa: run.bat
endlocal
pause