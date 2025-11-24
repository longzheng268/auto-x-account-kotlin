@echo off
REM 启动脚本 - Windows

cd /d "%~dp0"
gradle run --console=plain
pause
