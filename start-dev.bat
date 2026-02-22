@echo off
REM ============================================================
REM 微生活券吧 - 本地开发快速启动脚本 (Windows)
REM ============================================================

echo ========================================
echo   微生活券吧 - 本地开发环境启动
echo ========================================
echo.

REM 检查 Docker
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker 未安装，请先安装 Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop/
    pause
    exit /b 1
)

REM 检查 Docker 是否运行
docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker 未运行，请启动 Docker Desktop
    pause
    exit /b 1
)

echo [1/4] 检查环境配置...
if not exist ".env" (
    echo [INFO] 创建 .env 文件 (从模板复制)
    copy .env.example .env
    echo [WARN] 请编辑 .env 文件配置微信AppID等参数
)

echo [2/4] 启动基础服务 (MySQL + Redis)...
docker-compose up -d mysql redis
echo 等待数据库就绪...
timeout /t 30 /nobreak >nul

echo [3/4] 启动后端服务...
docker-compose up -d backend
echo 等待后端启动...
timeout /t 30 /nobreak >nul

echo [4/4] 检查服务状态...
docker-compose ps

echo.
echo ========================================
echo   启动完成！
echo ========================================
echo.
echo 服务地址:
echo   - 后端 API:    http://localhost:9000
echo   - Swagger 文档: http://localhost:9000/swagger-ui/index.html
echo   - MySQL:       localhost:3306
echo   - Redis:       localhost:6379
echo.
echo 小程序开发:
echo   cd wsh-miniapp ^&^& npm run dev:h5      (H5 开发)
echo   cd wsh-miniapp ^&^& npm run dev:mp-weixin (微信小程序)
echo.
echo 微信开发者工具导入路径:
echo   %cd%\wsh-miniapp\dist\dev\mp-weixin
echo.
pause
