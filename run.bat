@echo off
setlocal
echo ======================================================================
echo   KTU PBCST304 - Project Management System (Enterprise Architecture)
echo ======================================================================

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH. Please install JDK 17+.
    pause
    exit /b 1
)

REM Ensure bin directory exists
if not exist "bin" mkdir bin

REM Compile all sources
echo [1/2] Compiling Java source files...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" -d bin @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
del sources.txt

REM Launch Application
echo [2/2] Launching PMS Graphical Application...
echo.
echo Login Credentials:
echo   - Manager:   admin      / admin123
echo   - Developer: dev_alice  / alice123
echo   - Developer: dev_bob    / bob123
echo.
java -cp "bin;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" com.pms.Main %*
endlocal
