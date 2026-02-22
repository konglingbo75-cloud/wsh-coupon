# 微生活券吧 - 部署清单

## 项目状态总览

| 模块 | 状态 | 说明 |
|------|------|------|
| 后端服务 (wsh-backend) | ✅ 完成 | Maven 构建成功，83个Java类 |
| 小程序前端 (wsh-miniapp) | ✅ 完成 | 26个Vue3页面 |
| H5 开发构建 | ✅ 验证通过 | `npm run dev:h5` |
| 微信小程序构建 | ✅ 验证通过 | `npm run dev:mp-weixin` |
| Docker 配置 | ✅ 已创建 | docker-compose.yml |
| 数据库脚本 | ✅ 完成 | Flyway 迁移脚本 |

---

## 部署前准备清单

### 1. 微信配置（必须）

- [ ] 微信公众平台注册小程序
- [ ] 获取 AppID 和 AppSecret
- [ ] 配置服务器域名（request 合法域名）
- [ ] 配置业务域名（用于 H5 嵌入）

### 2. 微信支付配置（必须）

- [ ] 申请微信支付商户号
- [ ] 完成商户认证
- [ ] 获取商户号 (mch_id)
- [ ] 获取 API 密钥 (api_key)
- [ ] 配置支付授权目录
- [ ] 开通分账功能（用于服务费扣除）

### 3. 服务器环境

- [ ] 云服务器（推荐配置：2核4G以上）
- [ ] 域名（已备案）
- [ ] SSL 证书（HTTPS 必须）
- [ ] Docker + Docker Compose 安装

---

## 快速部署步骤

### 方式一：Docker Compose 一键部署（推荐）

```bash
# 1. 克隆代码到服务器
git clone <your-repo> /opt/wsh
cd /opt/wsh

# 2. 配置环境变量
cp .env.example .env
vim .env  # 填写微信AppID、支付商户号等

# 3. 启动服务
chmod +x deploy.sh
./deploy.sh

# 4. 查看日志
docker-compose logs -f
```

### 方式二：分步手动部署

#### 2.1 数据库

```bash
# 启动 MySQL 和 Redis
docker-compose up -d mysql redis

# 等待数据库就绪后，Flyway 会自动执行迁移
```

#### 2.2 后端服务

```bash
# 构建并启动后端
docker-compose up -d --build backend

# 验证启动
curl http://localhost:9000/actuator/health
```

#### 2.3 小程序发布

```bash
cd wsh-miniapp

# 构建微信小程序生产版本
npm run build:mp-weixin

# 输出目录：dist/build/mp-weixin
# 使用微信开发者工具上传并提交审核
```

---

## 环境配置说明

### 必填环境变量

| 变量名 | 说明 | 获取方式 |
|--------|------|----------|
| WECHAT_APPID | 小程序 AppID | 微信公众平台 → 开发 → 开发管理 |
| WECHAT_SECRET | 小程序 AppSecret | 微信公众平台 → 开发 → 开发管理 |
| WECHAT_PAY_MCHID | 微信支付商户号 | 微信支付商户平台 |
| WECHAT_PAY_KEY | API 密钥 | 微信支付商户平台 → 账户中心 → API安全 |
| JWT_SECRET | JWT 签名密钥 | 自行生成32位以上随机字符串 |

### 数据库配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| MYSQL_ROOT_PASSWORD | wsh@2026!Root | MySQL root 密码 |
| MYSQL_PASSWORD | wsh@2026!App | 应用数据库密码 |
| REDIS_PASSWORD | wsh@2026!Redis | Redis 密码 |

---

## 服务端口说明

| 服务 | 端口 | 用途 |
|------|------|------|
| Nginx | 80/443 | Web 入口 |
| Backend | 9000 | 后端 API |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

---

## 小程序提审清单

### 提交审核前检查

- [ ] 所有页面功能测试通过
- [ ] 登录授权流程正常
- [ ] 支付流程测试通过（使用微信支付沙箱）
- [ ] 核销功能测试通过
- [ ] 无 console.log 输出
- [ ] 无测试数据残留
- [ ] 隐私政策配置完成
- [ ] 用户协议页面准备

### 审核注意事项

1. **类目选择**：生活服务 → 生活服务综合
2. **功能介绍**：说明是会员权益聚合平台
3. **测试账号**：提供消费者和商家两个测试账号
4. **截图准备**：主要功能页面截图

---

## 监控与运维

### 日志查看

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务
docker-compose logs -f backend
docker-compose logs -f mysql
```

### 服务重启

```bash
# 重启单个服务
docker-compose restart backend

# 重启所有服务
docker-compose restart
```

### 数据备份

```bash
# 备份 MySQL 数据
docker exec wsh-mysql mysqldump -u root -p wsh_prod > backup_$(date +%Y%m%d).sql

# 备份 Redis 数据
docker exec wsh-redis redis-cli -a $REDIS_PASSWORD BGSAVE
```

---

## 常见问题

### Q: 后端启动失败，提示数据库连接失败

检查 MySQL 是否已启动并就绪：
```bash
docker-compose ps mysql
docker-compose logs mysql
```

### Q: 微信登录失败

1. 检查 AppID 和 AppSecret 是否正确
2. 检查服务器域名是否已配置到微信后台
3. 检查后端日志获取详细错误信息

### Q: 支付回调失败

1. 确认回调地址已配置 HTTPS
2. 确认商户号和 API 密钥正确
3. 检查分账功能是否已开通

---

## 技术支持

如遇问题，请提供以下信息：
1. 错误日志（`docker-compose logs backend`）
2. 操作步骤
3. 环境信息（服务器配置、Docker 版本）
