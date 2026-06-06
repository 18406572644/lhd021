@echo off
chcp 65001 >nul
echo ========================================
echo    社区闲置物资互助系统 - 停止服务
echo ========================================
echo.

echo 正在停止所有服务...
docker-compose down

echo [√] 服务已停止
echo.
pause
