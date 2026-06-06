@echo off
chcp 65001 >nul
echo ========================================
echo    社区闲置物资互助系统 - 一键启动
echo ========================================
echo.

echo [1/4] 检查Docker是否运行...
docker version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)
echo [√] Docker运行正常
echo.

echo [2/4] 停止并清理旧容器...
docker-compose down
echo [√] 清理完成
echo.

echo [3/4] 构建并启动所有服务...
echo    前端端口: 2021
echo    后端端口: 6021
echo    数据库端口: 3306
echo.
docker-compose up -d --build
echo.

echo [4/4] 等待服务启动...
echo    数据库初始化可能需要30-60秒...
timeout /t 5 /nobreak >nul
echo.

echo ========================================
echo    启动完成！
echo ========================================
echo.
echo 访问地址:
echo   前端: http://localhost:2021
echo   后端API: http://localhost:6021/api
echo   接口文档: http://localhost:6021/api/doc.html
echo.
echo 查看日志: docker-compose logs -f
echo 停止服务: docker-compose down
echo.
pause
