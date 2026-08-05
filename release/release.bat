for /f %%i in ('git describe --tags --abbrev^=0') do set VERSION=%%i



echo ===========================================
echo   Latest version: %VERSION%
echo ===========================================

git checkout %VERSION%
if errorlevel 1 exit /b 1

echo ===========================================
echo   Calling build.bat %VERSION%
echo ===========================================


for /f "skip=1 tokens=1" %%i in ('
    certutil -hashfile "Output\SeguraPass-Setup.exe" SHA256
') do (
    set SHA256=%%i
    goto doneHash
)
:doneHash
echo ===========================================
echo   Calculated hash for SeguraPass-Setup.exe %SHA256%
echo ===========================================

pushd "%cd%\.."
for /f %%i in ('
    powershell -ExecutionPolicy Bypass -File get-protocol-version.ps1
') do set PROTOCOL_VERSION=%%i
echo ===========================================
echo   Fetched protocol version: %PROTOCOL_VERSION%
echo ===========================================
popd

powershell -ExecutionPolicy Bypass ^
    -File "json-generation.ps1" ^
    -Version "%VERSION%" ^
    -Protocol "%PROTOCOL_VERSION%" ^
    -Sha "%SHA256%" ^
    -Output "metadata.json"
if errorlevel 1 (
    echo [ERROR] Metadata generation failed!
    exit /b 1
)
