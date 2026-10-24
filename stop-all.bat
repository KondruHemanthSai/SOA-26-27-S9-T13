@echo off
setlocal
title Student Central - Stopping Services

cd /d "%~dp0"

echo ==========================================================
echo        Stopping All Student Central Services...           
echo ==========================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-all.ps1"

echo.
pause
