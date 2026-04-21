# LDC Shop

LINUX DO Credit 积分商城 — 基于 Spring Boot 3 + Vue 3 的全栈商城系统，支持虚拟卡密商品交易，集成 LINUX DO OAuth 登录和 LDC Credit 积分支付。

A full-stack credit shop for the LINUX DO community, built with Spring Boot 3 + Vue 3 + Naive UI. Supports virtual card-key product trading, LINUX DO OAuth login, and LDC Credit payment integration.

## 功能特性 / Features

- **用户端** — 极简 SaaS 风格，商品浏览、下单购买、卡密自动发货、订单管理、争议申诉
- **管理端** — 企业级 Dashboard，商品/分类/卡密/订单/争议/公告/用户管理，ECharts 数据看板
- **LINUX DO OAuth** — 一键登录，首次登录自动注册，支持管理员白名单
- **LDC Credit 支付** — Ed25519 签名验证，异步回调通知，订单自动确认，支持退款
- **安全** — AES-256-GCM 卡密加密、CORS、支付回调签名验证、Redis 限流、MDC 链路追踪
- **国际化** — 中英文双语界面（i18n），用户端完整支持
- **数据库迁移** — Flyway 版本化迁移，开箱即用
- **深浅主题** — 明暗主题一键切换
- **Docker 单容器部署** — 前端打包注入 Spring Boot 静态资源，无需 Nginx

## 技术栈 / Tech Stack

| 层 | 技术 |
|---|---|
| 后端 | Java 21, Spring Boot 3.2, MyBatis-Plus, Sa-Token JWT, Flyway, Virtual Threads, ZGC |
| 前端 | Vue 3, TypeScript, Naive UI, Pinia, Vue Router, Axios, ECharts |
| 数据库 | MySQL 8, Redis 7 |
| 部署 | Docker 多阶段构建, docker-compose |

## 项目结构 / Project Structure

```
ldc-shop/
├── backend/                  # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/dev/xuya/ldcshop/
│       ├── common/           # 统一响应、异常处理、工具类、拦截器
│       ├── config/           # 配置类 (Sa-Token, CORS, MyBatis-Plus, Security)
│       ├── controller/
│       │   ├── user/         # 用户端 API
│       │   └── admin/        # 管理端 API
│       ├── entity/           # 数据库实体
│       ├── mapper/           # MyBatis-Plus Mapper
│       ├── params/           # 请求参数 DTO
│       ├── results/          # 响应结果 DTO
│       ├── service/          # Service 接口 + impl 实现
│       └── task/             # 定时任务 (订单自动关闭)
├── frontend/                 # Vue 3 前端 (pnpm monorepo)
│   ├── apps/web-ldc-shop/    # 商城应用
│   └── packages/             # 共享包
├── sql/
│   └── init.sql              # 全量建表脚本 (含索引和外键)
├── Dockerfile                # 三阶段构建 (Node → Maven → JRE)
├── docker-compose.standalone.yml  # 独立模式 (MySQL + Redis + App)
├── docker-compose.external.yml    # 外部模式 (仅 App)
├── deploy.sh                 # Linux 部署脚本
├── deploy.ps1                # Windows 部署脚本
└── .env.example              # 环境变量模板
```

---

## 安装部署 / Installation

### 前置要求 / Prerequisites

- [Docker](https://docs.docker.com/get-docker/) & Docker Compose v2+
- [Git](https://git-scm.com/)
- 至少 2GB 可用内存

### 第 1 步：克隆仓库 / Step 1: Clone

```bash
git clone https://github.com/xuya-dev/linux-do-credit-shop.git
cd linux-do-credit-shop
```

### 第 2 步：获取 OAuth 凭证 / Step 2: Get OAuth Credentials

1. 前往 [LINUX DO Connect](https://connect.linux.do/) 创建一个 OAuth 应用
2. **回调地址 (Redirect URI)** 填写: `https://your-domain/auth/callback`
3. 记录下 **Client ID** 和 **Client Secret**

> OAuth 配置是登录流程的前提，必须在 `.env` 中配置才能使用。

### 第 3 步：配置环境变量 / Step 3: Configure Environment

```bash
cp .env.example .env
```

编辑 `.env`，以下是完整说明：

```bash
# ============================================================
# 必须修改 / MUST CHANGE
# ============================================================

# 运行环境: dev 或 prod
SPRING_PROFILES_ACTIVE=prod

# JWT 密钥 — 请修改为随机字符串!
# Generate: openssl rand -hex 32
SA_TOKEN_JWT_SECRET=please-change-this-to-a-random-secret

# LINUX DO OAuth — 在 connect.linux.do 创建应用获取
LDC_OAUTH_CLIENT_ID=your-client-id
LDC_OAUTH_CLIENT_SECRET=your-client-secret
LDC_OAUTH_REDIRECT_URI=https://your-domain/auth/callback

# 管理员用户名 — 首次登录时自动设为管理员 (逗号分隔)
LDC_ADMIN_USERNAMES=your-username

# 卡密加密密钥 — AES-256-GCM 加密卡密内容
# Generate: openssl rand -hex 16
CARD_ENCRYPT_SECRET=please-change-this-encryption-key

# CORS 允许的前端域名 (逗号分隔)
CORS_ALLOWED_ORIGINS=https://your-domain

# ============================================================
# 数据库 — 独立模式可用默认值，外部模式需修改
# ============================================================
MYSQL_HOST=mysql            # standalone 模式不要改
MYSQL_PORT=3306
MYSQL_DATABASE=ldc_shop
MYSQL_USERNAME=root
MYSQL_ROOT_PASSWORD=change-this-mysql-password
MYSQL_PASSWORD=change-this-mysql-password

REDIS_HOST=redis            # standalone 模式不要改
REDIS_PORT=6379
REDIS_PASSWORD=change-this-redis-password
REDIS_DATABASE=0

# ============================================================
# 可选修改 / OPTIONAL
# ============================================================
APP_PORT=3000               # 对外端口
ORDER_AUTO_CLOSE_MINUTES=30 # 未支付订单自动关闭(分钟)
JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseZGC  # JDK 21 ZGC
```

> 密钥生成命令：
> ```bash
> # JWT 密钥 (64字符)
> openssl rand -hex 32
> 
> # 卡密加密密钥 (32字符)
> openssl rand -hex 16
> ```

### 第 4 步：选择部署模式 / Step 4: Choose Deploy Mode

#### 模式 A：独立模式（推荐新手）

自动创建 MySQL + Redis + App 三个容器，开箱即用。

**Linux:**
```bash
chmod +x deploy.sh
./deploy.sh standalone
```

**Windows:**
```powershell
.\deploy.ps1 standalone
```

**或手动:**
```bash
docker compose -f docker-compose.standalone.yml --env-file .env build
docker compose -f docker-compose.standalone.yml --env-file .env up -d
```

#### 模式 B：外部模式

连接已有的 MySQL 和 Redis，仅部署应用容器。

需要在 `.env` 中配置外部 MySQL/Redis 地址：
```bash
MYSQL_HOST=your-mysql-host
MYSQL_PASSWORD=your-mysql-password
REDIS_HOST=your-redis-host
REDIS_PASSWORD=your-redis-password
```

然后部署：
```bash
# Linux
./deploy.sh external

# 或手动
docker compose -f docker-compose.external.yml --env-file .env build
docker compose -f docker-compose.external.yml --env-file .env up -d
```

### 第 5 步：等待启动 / Step 5: Wait for Startup

首次启动约需 **1-3 分钟**（MySQL 初始化 + Maven 下载依赖 + 前端构建）。

```bash
# 查看构建日志
./deploy.sh logs

# 等待健康检查通过
curl http://localhost:3000/actuator/health
```

看到 `{"status":"UP"}` 表示启动成功。

### 第 6 步：访问应用 / Step 6: Access

| 页面 | 地址 |
|---|---|
| 商城首页 | `http://localhost:3000` |
| 管理后台 | `http://localhost:3000/admin` |

点击 **LINUX DO 登录** 按钮，使用 LINUX DO 账号登录。你在 `.env` 中配置的管理员用户名会自动获得管理员权限。

### 第 7 步：配置支付（管理后台）/ Step 7: Configure Payment

登录管理后台后，进入 **系统设置** 页面，配置 LDC Credit 支付参数：

| 配置项 | 说明 |
|---|---|
| LDC 支付 Client ID | 在 [LDC Credit 商户中心](https://credit.linux.do/merchant) 创建应用获取 |
| LDC 支付 Client Secret | 同上 |
| Ed25519 商户私钥 | Base64 编码的私钥，用于签名 |
| LDC 平台公钥 | Base64 编码的公钥，用于验签 |
| 支付网关地址 | 默认 `https://credit.linux.do/epay` |
| 异步通知地址 | 需公网可访问，如 `https://your-domain/api/order/notify` |
| 同步跳转地址 | 支付完成后跳转，如 `https://your-domain/orders` |

> 支付配置存储在数据库中，通过管理后台随时修改，无需重启。

---

## 部署命令参考 / Deploy Commands

```bash
./deploy.sh standalone   # 独立模式部署
./deploy.sh external     # 外部模式部署
./deploy.sh stop         # 停止服务
./deploy.sh restart      # 重启服务
./deploy.sh status       # 查看状态
./deploy.sh logs         # 查看日志 (实时)
./deploy.sh build        # 重新构建镜像
./deploy.sh down         # 删除所有容器和数据
```

## 配置职责划分 / Config Responsibility

| 配置类别 | 管理方式 | 原因 |
|---|---|---|
| OAuth、管理员用户名 | `.env` 环境变量 | 登录前就必须可用 |
| 支付、商店信息 | 管理后台设置页 | 登录后通过 UI 管理，修改无需重启 |
| MySQL、Redis、JVM | `.env` 环境变量 | 基础设施配置 |

## LDC Credit 支付对接

本项目使用官方 LDC 支付协议（Ed25519 签名），对接流程：

1. 在 [LINUX DO Credit 商户中心](https://credit.linux.do/merchant) 创建应用
2. 获取 `Client ID` 和 `Client Secret`，并上传商户 Ed25519 公钥
3. 在管理后台 **系统设置** 页面填入凭证
4. 商城自动通过 `/pay/submit.php` 发起积分流转请求
5. 用户在 LDC 平台完成认证后，异步回调通知商城确认订单

### 签名算法

1. 取除 `sign` 以外的所有非空请求参数
2. 按参数名 ASCII 字典序排列
3. 拼接为 `k1=v1&k2=v2...` 格式
4. 将 `Client Secret` 追加到末尾
5. 使用商户 Ed25519 私钥签名
6. Base64 编码作为 `sign` 参数

### 异步通知

支付成功后，LDC 平台通过 POST 请求回调 `notify_url`，商城验签后自动确认订单。

## 本地开发 / Local Development

### 后端

```bash
cd backend
# 需要本地 JDK 21 + MySQL 8 + Redis 7
mvn spring-boot:run
```

### 前端

```bash
cd frontend
pnpm install
pnpm run dev:shop
```

## License

MIT
