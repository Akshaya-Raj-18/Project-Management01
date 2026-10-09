# KTU PBCST304 - Project Management System PowerShell Runner
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  KTU PBCST304 - Project Management System (Enterprise Architecture)  " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$files = (Get-ChildItem -Path "src" -Filter "*.java" -Recurse).FullName
[System.IO.File]::WriteAllLines("sources.txt", $files)

Write-Host "[1/2] Compiling Java source files..." -ForegroundColor Yellow
cmd.exe /c "javac -encoding UTF-8 -cp lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar -d bin @sources.txt"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit 1
}
Remove-Item "sources.txt" -Force

Write-Host "[2/2] Launching PMS Graphical Application..." -ForegroundColor Green
Write-Host "Login Credentials:" -ForegroundColor Yellow
Write-Host "  - Manager:   admin      / admin123"
Write-Host "  - Developer: dev_alice  / alice123"
Write-Host "  - Developer: dev_bob    / bob123"

cmd.exe /c "java -cp bin;lib\sqlite-jdbc.jar;lib\slf4j-api.jar;lib\slf4j-simple.jar com.pms.Main $args"
