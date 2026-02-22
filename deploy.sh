#!/bin/bash
# ============================================================
# 微生活券吧 - 生产环境部署脚本 (Linux/Mac)
# ============================================================

set -e

echo "========================================"
echo "  微生活券吧 - 生产环境部署"
echo "========================================"
echo

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[ERROR] Docker 未安装${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}[ERROR] Docker Compose 未安装${NC}"
    exit 1
fi

# 检查 .env 文件
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}[WARN] .env 文件不存在，从模板创建...${NC}"
    cp .env.example .env
    echo -e "${RED}[ACTION REQUIRED] 请编辑 .env 文件配置以下必填项:${NC}"
    echo "  - WECHAT_APPID"
    echo "  - WECHAT_SECRET"
    echo "  - WECHAT_PAY_MCHID"
    echo "  - WECHAT_PAY_KEY"
    echo "  - JWT_SECRET"
    exit 1
fi

# 检查必要的环境变量
source .env
if [ -z "$WECHAT_APPID" ] || [ "$WECHAT_APPID" = "wx1234567890abcdef" ]; then
    echo -e "${RED}[ERROR] 请在 .env 中配置正确的 WECHAT_APPID${NC}"
    exit 1
fi

echo -e "${GREEN}[1/6] 拉取最新镜像...${NC}"
docker-compose pull mysql redis

echo -e "${GREEN}[2/6] 构建后端镜像...${NC}"
docker-compose build backend

echo -e "${GREEN}[3/6] 启动数据库服务...${NC}"
docker-compose up -d mysql redis
echo "等待数据库就绪 (30s)..."
sleep 30

echo -e "${GREEN}[4/6] 启动后端服务...${NC}"
docker-compose up -d backend
echo "等待后端启动 (60s)..."
sleep 60

echo -e "${GREEN}[5/6] 构建前端...${NC}"
# 构建 H5 生产版本
if [ -d "wsh-miniapp" ]; then
    cd wsh-miniapp
    npm install && npm run build:h5 || echo -e "${YELLOW}[WARN] H5 构建跳过${NC}"
    cd ..
fi
# 构建管理后台
if [ -d "wsh-admin" ]; then
    cd wsh-admin
    npm install && npm run build || echo -e "${YELLOW}[WARN] 管理后台构建跳过${NC}"
    cd ..
fi

echo -e "${GREEN}[6/6] 启动 Nginx...${NC}"
docker-compose up -d nginx

echo
echo -e "${GREEN}========================================"
echo "  部署完成！"
echo "========================================${NC}"
echo
docker-compose ps

echo
echo "服务地址:"
echo "  - H5 入口:     http://localhost (或配置的域名)"
echo "  - 运营后台:    http://localhost/admin/"
echo "  - 后端 API:    http://localhost:9000"
echo "  - 健康检查:    http://localhost/health"
echo
echo "日志查看:"
echo "  docker-compose logs -f backend"
echo
