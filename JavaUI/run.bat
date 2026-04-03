@echo off
REM ============================================
REM  NoteGraph Java UI — Build and Run Script
REM  Requires: Java 17+ (JDK)
REM ============================================
setlocal EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "SRC_DIR=%SCRIPT_DIR%src"
set "LIB_DIR=%SCRIPT_DIR%lib"
set "OUT_DIR=%SCRIPT_DIR%out"

REM Build classpath from all JARs in lib/
set "CP="
for %%f in ("%LIB_DIR%\*.jar") do (
    if defined CP (
        set "CP=!CP!;%%~f"
    ) else (
        set "CP=%%~f"
    )
)

echo.
echo ============================================
echo   NoteGraph Desktop Client
echo ============================================
echo.
echo [1/2] Compiling Java sources...

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

REM Find all .java files and write to temp file
dir /s /b "%SRC_DIR%\*.java" > "%SCRIPT_DIR%sources.txt"

javac -d "%OUT_DIR%" -cp "%CP%" @"%SCRIPT_DIR%sources.txt"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    del "%SCRIPT_DIR%sources.txt" 2>nul
    pause
    exit /b 1
)

del "%SCRIPT_DIR%sources.txt" 2>nul

echo [OK] Compilation successful.
echo.
echo [2/2] Launching NoteGraph...
echo       Make sure the Spring Boot backend is running on port 8080.
echo.

java -cp "%OUT_DIR%;%CP%" com.notegraph.ui.NoteGraphApp

endlocal
