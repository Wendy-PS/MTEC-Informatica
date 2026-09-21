@echo off
title Gerando o executavel MTEC Informatica
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0construir.ps1"
set RESULTADO=%ERRORLEVEL%
echo.
if "%RESULTADO%"=="0" (
    echo Abrindo a pasta com o executavel...
    start "" explorer "%~dp0SAIDA"
)
pause
exit /b %RESULTADO%
