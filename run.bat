@echo off
setlocal
cd /d "%~dp0"

echo ======================================================================
echo   Enterprise Project Management System (PMS)
echo ======================================================================
echo.

REM Verify Java installation
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in your system PATH.
    echo Please install Java Development Kit (JDK 17 or higher).
    pause
    exit /b 1
)

REM Ensure bin directory exists
if not exist "bin" mkdir "bin"

REM Compile ProjectManagementSystem.java into bin directory
echo [1/2] Compiling ProjectManagementSystem.java...
javac -encoding UTF-8 -cp ".;lib\*" -d bin ProjectManagementSystem.java
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

echo [2/2] Launching Application...
echo.
echo ======================================================================
echo  DEFAULT LOGIN CREDENTIALS:
echo   - Super Admin : superadmin / superadmin123
echo   - Admin       : admin      / admin123
echo   - User (Dev)  : user       / user1234
echo   - Developer 1 : dev_alice  / alice123
echo   - Developer 2 : dev_bob    / bob12345
echo ======================================================================
echo.

java --enable-native-access=ALL-UNNAMED -cp "bin;lib\*" ProjectManagementSystem

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application terminated unexpectedly.
    pause
)

endlocal
