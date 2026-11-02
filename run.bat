@echo off
setlocal
cd /d "%~dp0"

echo ======================================================================
echo   Enterprise Project Management System
echo ======================================================================
echo.

if not exist "ProjectManagementSystem.class" (
    echo Compiling ProjectManagementSystem.java...
    javac -encoding UTF-8 -cp ".;lib\*" ProjectManagementSystem.java
    if %errorlevel% neq 0 (
        echo.
        echo [ERROR] Compilation failed.
        pause
        exit /b 1
    )
)

echo Starting Application...
echo.
echo ======================================================================
echo  ACCOUNT CREDENTIALS:
echo   - Super Admin : superadmin / superadmin123
echo   - Admin       : admin      / admin123
echo   - User (Dev)  : user       / user1234
echo   - Dev 1       : dev_alice  / alice123
echo   - Dev 2       : dev_bob    / bob12345
echo ======================================================================
echo.

java --enable-native-access=ALL-UNNAMED -cp ".;lib\*" ProjectManagementSystem

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application closed with an exit code: %errorlevel%
    pause
)

endlocal
