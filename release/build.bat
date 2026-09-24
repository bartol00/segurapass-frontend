@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
    echo Usage: build-client.bat VERSION
    exit /b 1
)

set VERSION=%~1
set "MSIX_VERSION=%VERSION%.0"

set "SCRIPT_DIR=%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "JAVAFX_HOME=C:\Program Files\javafx-sdk-21.0.12"

set "MSIX_IDENTITY_NAME=SeguraPass.SeguraPass"
set "MSIX_PUBLISHER=CN=2EF34D9D-EB84-4A69-8104-D6EA254DDCC7"

set "MAKEAPPX="
for /f "delims=" %%F in ('where /r "%ProgramFiles(x86)%\Windows Kits\10\bin" MakeAppx.exe 2^>nul') do (
    set "MAKEAPPX=%%F"
)
if not defined MAKEAPPX (
    echo [ERROR] MakeAppx.exe not found.
    echo Install the Windows SDK.
    exit /b 1
)

echo Using MakeAppx:
echo %MAKEAPPX%

if not exist "%JAVA_HOME%\bin\jlink.exe" (
    echo [ERROR] jlink not found.
    exit /b 1
)

if not exist "%JAVA_HOME%\bin\jpackage.exe" (
    echo [ERROR] jpackage not found.
    exit /b 1
)

if not exist "%JAVAFX_HOME%\lib" (
    echo [ERROR] JavaFX SDK not found.
    exit /b 1
)

if not exist "msix\AppxManifest.xml.template" (
    echo [ERROR] MSIX manifest template not found.
    exit /b 1
)

if not exist "msix\Assets" (
    echo [ERROR] MSIX assets directory not found.
    exit /b 1
)

echo.
echo ===========================================
echo   Removing old directories...
echo ===========================================
rmdir /s /q runtime 2>nul
rmdir /s /q SeguraPass 2>nul
rmdir /s /q build\msix-staging 2>nul
rmdir /s /q Output 2>nul

pause

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

pushd "%SCRIPT_DIR%\.."

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
echo   Packaging app image...
echo ===========================================
"%JAVA_HOME%\bin\jpackage.exe" --input target --name SeguraPass ^
  --main-jar password-manager-client.jar ^
  --main-class com.example.passwordmanagerclient.HelloApplication ^
  --type app-image ^
  --runtime-image "%SCRIPT_DIR%\runtime" ^
  --icon "%SCRIPT_DIR%\icon.ico" ^
  --dest release
if errorlevel 1 (
    echo [ERROR] jpackage failed!
    pause
    exit /b
)
popd
if not exist "SeguraPass\SeguraPass.exe" (
    echo [ERROR] SeguraPass.exe not found after jpackage.
    exit /b 1
)

echo.
echo ===========================================
echo   Build complete.
echo ===========================================

echo.
echo ===========================================
echo   Creating MSIX...
echo ===========================================

mkdir "build\msix-staging"
mkdir "build\msix-staging\app"
mkdir "build\msix-staging\Assets"

xcopy /E /I /Y "SeguraPass\*" "build\msix-staging\app\"
if errorlevel 1 (
    echo [ERROR] Failed copying application image
    exit /b 1
)

xcopy /E /I /Y "msix\Assets\*" "build\msix-staging\Assets\"
if errorlevel 1 (
    echo [ERROR] Failed copying MSIX assets
    exit /b 1
)

REM ==========================================================
REM Generate manifest
REM ==========================================================

echo.
echo ===========================================
echo   Generating AppxManifest.xml...
echo ===========================================

powershell -NoProfile -Command ^
    "$content = Get-Content 'msix\AppxManifest.xml.template' -Raw; " ^
    "$content = $content.Replace('__VERSION__', '%MSIX_VERSION%'); " ^
    "$content = $content.Replace('__IDENTITY_NAME__', '%MSIX_IDENTITY_NAME%'); " ^
    "$content = $content.Replace('__PUBLISHER__', '%MSIX_PUBLISHER%'); " ^
    "Set-Content -Path 'build\msix-staging\AppxManifest.xml' -Value $content -Encoding UTF8"

if errorlevel 1 (
    echo [ERROR] Failed generating AppxManifest.xml
    pause
    exit /b 1
)
if not exist "build\msix-staging\AppxManifest.xml" (
    echo [ERROR] AppxManifest.xml was not created
    pause
    exit /b 1
)

REM ==========================================================
REM Create MSIX package
REM ==========================================================

echo.
echo ===========================================
echo   Running MakeAppx...
echo ===========================================

mkdir "Output" 2>nul

"%MAKEAPPX%" pack ^
    /d "build\msix-staging" ^
    /p "Output\SeguraPass-%VERSION%.msix" ^
    /o

if errorlevel 1 (
    echo [ERROR] MakeAppx failed
    pause
    exit /b 1
)
if not exist "Output\SeguraPass-%VERSION%.msix" (
    echo [ERROR] MSIX package was not created
    pause
    exit /b 1
)

echo.
echo ===========================================
echo   MSIX BUILD COMPLETE
echo ===========================================
echo.
echo Package:
echo   Output\SeguraPass-%VERSION%.msix
echo.
echo Version:
echo   %MSIX_VERSION%
echo.

REM ==========================================================
REM Create locally signed copy
REM ==========================================================

echo.
echo ===========================================
echo   Creating local signed MSIX...
echo ===========================================

set "SIGNTOOL="
for /f "delims=" %%F in ('where /r "%ProgramFiles(x86)%\Windows Kits\10\bin" SignTool.exe 2^>nul') do (
    set "SIGNTOOL=%%F"
)
if not defined SIGNTOOL (
    echo [ERROR] SignTool.exe not found
    echo Install the Windows SDK
    pause
    exit /b 1
)

echo Using SignTool:
echo %SIGNTOOL%

set "DEV_CERT=msix\cert\SeguraPass-DevCert.pfx"
set "DEV_CERT_PASSWORD=your-local-password"

if not exist "%DEV_CERT%" (
    echo [ERROR] Development certificate not found:
    echo %DEV_CERT%
    pause
    exit /b 1
)

mkdir "Output\local" 2>nul

copy /Y ^
    "Output\SeguraPass-%VERSION%.msix" ^
    "Output\local\SeguraPass-%VERSION%-local.msix"

if errorlevel 1 (
    echo [ERROR] Failed creating local MSIX copy
    pause
    exit /b 1
)

echo.
echo ===========================================
echo   Signing local MSIX...
echo ===========================================

"%SIGNTOOL%" sign ^
    /fd SHA256 ^
    /f "%DEV_CERT%" ^
    /p "%DEV_CERT_PASSWORD%" ^
    "Output\local\SeguraPass-%VERSION%-local.msix"

if errorlevel 1 (
    echo [ERROR] Failed signing local MSIX
    pause
    exit /b 1
)

echo.
echo ===========================================
echo   Verifying local MSIX signature...
echo ===========================================

"%SIGNTOOL%" verify ^
    /pa ^
    /v ^
    "Output\local\SeguraPass-%VERSION%-local.msix"

if errorlevel 1 (
    echo [ERROR] Local MSIX signature verification failed.
    pause
    exit /b 1
)

echo.
echo ===========================================
echo   MSIX BUILD COMPLETE
echo ===========================================
echo.
echo Store package (unsigned):
echo   Output\SeguraPass-%VERSION%.msix
echo.
echo Local package (signed):
echo   Output\local\SeguraPass-%VERSION%-local.msix
echo.
echo Version:
echo   %MSIX_VERSION%
echo.

endlocal
