@echo off
setlocal enabledelayedexpansion

for /f %%i in ('git describe --tags --abbrev^=0') do set VERSION=%%i
echo ===========================================
echo   Latest version: %VERSION%
echo ===========================================

git checkout %VERSION%
if errorlevel 1 exit /b 1

set "VERSION=%VERSION:v=%"

echo ===========================================
echo   Calling build.bat %VERSION%
echo ===========================================
call "build.bat" "%VERSION%"
if errorlevel 1 (
    echo [ERROR] Build failed!
    exit /b 1
)

pushd "%cd%\.."
for /f %%i in ('
    powershell -ExecutionPolicy Bypass -File "release\get-protocol-version.ps1"
') do set PROTOCOL_VERSION=%%i
echo ===========================================
echo   Fetched protocol version: %PROTOCOL_VERSION%
echo ===========================================
popd

python "release.py" ^
    "%VERSION%" ^
    "%PROTOCOL_VERSION%" ^
    "Output\SeguraPass-Setup.exe"
