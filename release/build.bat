@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
    echo Usage: build-client.bat VERSION
    exit /b 1
)

set VERSION=%~1
set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%\.."
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "JAVAFX_HOME=C:\Program Files\javafx-sdk-21.0.12"
set "INNO_DIR=C:\Program Files\Inno Setup 7"

if not exist "%JAVA_HOME%\bin\jlink.exe" (
    echo JDK not found.
    exit /b 1
)

if not exist "%JAVAFX_HOME%\lib" (
    echo JavaFX SDK not found.
    exit /b 1
)

if not exist "%INNO_DIR%\ISCC.exe" (
    echo Inno Setup not found.
    exit /b 1
)

echo ===========================================
echo   Building JavaFX app...
echo ===========================================
call mvn clean package
if errorlevel 1 (
    echo [ERROR] Maven build failed!
    pause
    exit /b
)
if not exist "target\password-manager-client.jar" (
    echo [ERROR] Built JAR not found!
    exit /b 1
)

echo.
echo ===========================================
echo   Removing old runtime...
echo ===========================================
rmdir /s /q runtime
rmdir /s /q SeguraPass

echo.
echo ===========================================
echo   Creating runtime image with jlink...
echo ===========================================
"%JAVA_HOME%\bin\jlink.exe" --module-path "%JAVA_HOME%\jmods;%JAVAFX_HOME%\lib;target\libs" ^
  --add-modules java.base,java.desktop,java.logging,javafx.controls,javafx.fxml,javafx.graphics,java.net.http,jdk.crypto.ec ^
  --output runtime ^
  --strip-debug --no-header-files --no-man-pages --compress=2
if errorlevel 1 (
    echo [ERROR] jlink failed!
    pause
    exit /b
)

echo.
echo ===========================================
echo   Copying JavaFX DLLs...
echo ===========================================
xcopy /y "%JAVAFX_HOME%\bin\*.dll" "runtime\bin\"
if errorlevel 1 (
    echo [ERROR] Failed copying JavaFX DLLs!
    exit /b 1
)

echo.
echo ===========================================
echo   Packaging app image...
echo ===========================================
"%JAVA_HOME%\bin\jpackage.exe" --input target --name SeguraPass ^
  --main-jar password-manager-client.jar ^
  --main-class com.example.passwordmanagerclient.HelloApplication ^
  --type app-image ^
  --runtime-image runtime ^
  --icon "%SCRIPT_DIR%icon.ico"
if errorlevel 1 (
    echo [ERROR] jpackage failed!
    pause
    exit /b
)

echo.
echo ===========================================
echo   Build complete.
echo ===========================================

rmdir /s /q "%SCRIPT_DIR%Output"

echo.
echo ===========================================
echo   Creating installer...
echo ===========================================
"%INNO_DIR%\ISCC.exe" ^
    /DMyAppVersion=%VERSION% ^
    "%SCRIPT_DIR%inno-script.iss"

if errorlevel 1 (
    echo [ERROR] Inno Setup failed!
    pause
    exit /b
)

pause
popd
endlocal
