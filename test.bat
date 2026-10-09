@echo off
setlocal
echo ======================================================================
echo   KTU PBCST304 - Running Syllabus Automated Verification Tests
echo ======================================================================

if not exist "bin" mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" -d bin @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
del sources.txt

java -cp "bin;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar" com.pms.OOPVerificationTest
pause
endlocal
