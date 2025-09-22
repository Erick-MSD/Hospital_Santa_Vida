@echo off
echo ===============================================
echo          COMPILANDO HOSPITAL SANTA VIDA
echo ===============================================

REM Crear directorio out si no existe
if not exist "out" mkdir out

REM Limpiar directorios de destino
echo Limpiando directorios...
if exist "out\*" rmdir /s /q out
mkdir out

REM Compilar con JavaFX
echo Compilando aplicacion...
javac -d out ^
-cp "lib\mysql-connector-j-8.0.33.jar;C:\Users\Erick\Downloads\openjfx-21.0.8_windows-x64_bin-sdk\javafx-sdk-21.0.8\lib\*;src" ^
src\TriageApp.java ^
src\controllers\SimpleLoginController.java ^
src\controllers\TriageController.java ^
src\models\*.java ^
src\dao\*.java ^
src\utils\*.java ^
src\structures\*.java

if %errorlevel% neq 0 (
    echo ERROR EN COMPILACION
    pause
    exit /b 1
)

REM Copiar recursos FXML
echo Copiando recursos...
if not exist "out\ui" mkdir out\ui
copy "src\ui\*.fxml" "out\ui\"

REM Copiar assets (imágenes)
echo Copiando assets...
if not exist "out\assets" mkdir out\assets
if not exist "out\assets\img" mkdir out\assets\img
copy "assets\img\*" "out\assets\img\"

echo.
echo ===============================================
echo          COMPILACION EXITOSA
echo ===============================================
echo.
echo Para ejecutar la aplicacion usa: run.bat
pause