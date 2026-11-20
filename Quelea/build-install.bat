@echo off
setlocal

set "ISCC=%ProgramFiles%\Inno Setup 7\ISCC.exe"
if not exist "%ISCC%" (
    >&2 echo Error: Inno Setup 7 compiler not found at "%ISCC%".
    >&2 echo Install Inno Setup 7 in "%ProgramFiles%\Inno Setup 7" and try again.
    exit /b 1
)

"%ISCC%" "/DMyAppVersion=%~1" /Q "quelea64.iss"
exit /b %ERRORLEVEL%
