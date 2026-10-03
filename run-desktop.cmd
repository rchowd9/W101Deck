@echo off
setlocal

where java >nul 2>nul
if errorlevel 1 (
    echo Java was not found. Install JDK 17 or newer and add it to PATH.
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven was not found. Install Maven and add it to PATH.
    exit /b 1
)

call mvn clean javafx:run
exit /b %ERRORLEVEL%
