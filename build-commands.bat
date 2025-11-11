@echo off
setlocal enabledelayedexpansion

echo ===========================================
echo   Building JavaFX app...
echo ===========================================
call mvn clean package
if errorlevel 1 (
    echo [ERROR] Maven build failed!
    pause
    exit /b
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
jlink --module-path "C:\Program Files\Java\jdk-17\jmods;C:\Program Files\javafx-sdk-21.0.8\lib;target\libs" ^
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
xcopy /y "C:\Program Files\javafx-sdk-21.0.8\bin\*.dll" "runtime\bin\"

echo.
echo ===========================================
echo   Packaging app image...
echo ===========================================
jpackage --input target --name SeguraPass ^
  --main-jar password-manager-client-1.0-SNAPSHOT.jar ^
  --main-class com.example.passwordmanagerclient.HelloApplication ^
  --type app-image ^
  --runtime-image runtime
if errorlevel 1 (
    echo [ERROR] jpackage failed!
    pause
    exit /b
)

echo.
echo Build complete! Check the SeguraPass folder.
pause
