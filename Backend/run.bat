@echo off
REM ============================================
REM  NoteGraph Spring Boot Backend — Run Script
REM  Requires: Java 17+ (JDK), Maven Wrapper
REM ============================================

echo.
echo ============================================
echo   NoteGraph Backend  (Spring Boot)
echo ============================================
echo.
echo Starting server on http://localhost:8080 ...
echo.

call "%~dp0mvnw.cmd" spring-boot:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Backend failed to start. Check logs above.
    pause
)
