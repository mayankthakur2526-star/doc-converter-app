@echo off
echo ========================================================
echo Starting DocMorph Document Converter Application...
echo ========================================================

REM Find Maven binary
set "MVN_CMD="
if exist "%USERPROFILE%\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin\mvn.cmd" (
    set "MVN_CMD=%USERPROFILE%\Downloads\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin\mvn.cmd"
) else (
    where mvn >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        set "MVN_CMD=mvn"
    )
)

if "%MVN_CMD%"=="" (
    echo [ERROR] Maven not found on PATH or in Downloads folder.
    echo Please install Apache Maven or run target\doc-converter-app-1.0.0.jar directly with Java.
    pause
    exit /b 1
)

echo Using Maven: %MVN_CMD%
"%MVN_CMD%" spring-boot:run
pause
