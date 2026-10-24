@echo off
setlocal
title Student Central Launcher

cd /d "%~dp0"

echo ==========================================================
echo        Student Central - Microservices System Launcher    
echo ==========================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-all.ps1" %*

if %ERRORLEVEL% equ 0 goto :SUCCESS

echo.
echo [ERROR] Launcher exited with error code %ERRORLEVEL%.
pause
exit /b %ERRORLEVEL%

:SUCCESS
exit /b 0
