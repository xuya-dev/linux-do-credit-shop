# LDC Shop

LINUX DO Credit 积分商城 — 基于 Spring Boot 3 + Vue 3 的全栈商城系统，支持虚拟卡密商品交易，集成 LINUX DO OAuth 登录和 LDC Credit 积分支付。

A full-stack credit shop for the LINUX DO community, built with Spring Boot 3 + Vue 3 + Naive UI. Supports virtual card-key product trading, LINUX DO OAuth login, and LDC Credit payment integration.

## 功能特性 / Features

- **用户端** — 极简 SaaS 风格，商品浏览、下单购买、卡密自动发货、订单管理、争议申诉
- **管理端** — 企业级 Dashboard，商品/分类/卡密/订单/争议/公告/用户管理，ECharts 数据看板
- **LINUX DO OAuth** — 一键登录，首次登录自动注册，支持管理员白名单
- **LDC Credit 支付** — Ed25519 签名，异步回调通知，订单自动确认
- **国际化** — 中英文双语界面（i18n）
- **深浅主题** — 明暗主题一键切换
- **Docker 单容器部署** — 前端打包注入 Spring Boot 静态资源，无需 Nginx

## 技术栈 / Tech Stack

| 层 | 技术 |
|---|---|
| 后端 | Java 17, Spring Boot 3.2, MyBatis-Plus, Sa-Token, Hutool, Lombok |
| 前端 | Vue 3, TypeScript, Naive UI, Pinia, Vue Router, Axios, ECharts |
| 数据库 | MySQL 8, Redis 7 |
| 部署 | Docker 多阶段构建, docker-compose |

## 项目结构 / Project Structure

```
ldc-shop/
├── backend/                  # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/dev/xuya/ldcshop/
│       ├── common/           # 统一响应、异常处理
│       ├── config/           # 配置类 (Sa-Token, CORS, MyBatis-Plus)
│       ├── controller/
│       │   ├── user/         # 用户端 API (7 个 Controller)
│       │   └── admin/        # 管理端 API (9 个 Controller)
│       ├── entity/           # 数据库实体 (9 个)
│       ├── mapper/           # MyBatis-Plus Mapper
│       ├── params/           # 请求参数类
│       ├── results/          # 响应结果类
│       ├── service/          # Service 接口 + impl 实现
│       └── util/             # Ed25519Util, OrderUtil, UserContextUtil
├── frontend/                 # Vue 3 前端
│   ├── src/
│   │   ├── api/              # API 调用封装
│   │   ├── i18n/             # 中英文语言包
│   │   ├── stores/           # Pinia 状态管理
│   │   ├── router/           # 路由配置 + 守卫
│   │   ├── layouts/          # UserLayout + AdminLayout
│   │   └── views/
│   │       ├── user/         # 用户端页面 (9 个)
│   │       └── admin/        # 管理端页面 (9 个)
│   └── vite.config.ts
├── sql/
│   └── init.sql              # 建表脚本 (9 张表)
├── Dockerfile                # 三阶段构建 (Node → Maven → JRE)
├── docker-compose.standalone.yml  # 独立模式 (MySQL + Redis + App)
├── docker-compose.external.yml    # 外部模式 (仅 App)
├── deploy.sh                 # Linux 部署脚本
├── deploy.ps1                # Windows 部署脚本
└── .env.example              # 环境变量模板
```

## 快速部署 / Quick Start

### 前置要求 / Prerequisites

- Docker & Docker Compose
- Git

### 1. 克隆仓库 / Clone

```bash
git clone https://github.com/xuya-dev/linux-do-credit-shop.git
cd linux-do-credit-shop
```

### 2. 配置环境变量 / Configure Environment

```bash
cp .env.example .env
```

编辑 `.env`，**必须修改**以下配置：

| 变量 | 说明 |
|---|---|
| `SA_TOKEN_JWT_SECRET` | JWT 密钥，务必修改为随机字符串 |
| `LDC_OAUTH_CLIENT_ID` | LINUX DO OAuth Client ID |
| `LDC_OAUTH_CLIENT_SECRET` | LINUX DO OAuth Client Secret |
| `LDC_OAUTH_REDIRECT_URI` | OAuth 回调地址，格式 `https://your-domain/auth/callback` |
| `LDC_PAYMENT_CLIENT_ID` | LDC Credit 支付 Client ID |
| `LDC_PAYMENT_CLIENT_SECRET` | LDC Credit 支付 Client Secret |
| `LDC_PAYMENT_NOTIFY_URL` | 支付回调地址，需公网可访问 |
| `LDC_PAYMENT_RETURN_URL` | 支付完成后跳转地址 |

### 3. 部署 / Deploy

**Linux:**

```bash
chmod +x deploy.sh

# 独立模式 (自动安装 MySQL + Redis)
./deploy.sh standalone

# 或外部模式 (连接已有 MySQL + Redis)
./deploy.sh external
```

**Windows:**

```powershell
# 独立模式
.\deploy.ps1 standalone

# 或外部模式
.\deploy.ps1 external
```

### 4. 访问 / Access

| 页面 | 地址 |
|---|---|
| 商城首页 | `http://localhost:3000` |
| 管理后台 | `http://localhost:3000/admin` |

首次启动需等待约 30 秒让 MySQL 完成初始化。

## LDC Credit 支付对接

本项目使用官方 LDC 支付协议（Ed25519 签名），对接流程：

1. 在 [LINUX DO Credit 集市中心](https://credit.linux.do/merchant) 创建应用
2. 获取 `Client ID` 和 `Client Secret`，并在应用设置中上传商户 Ed25519 公钥
3. 将凭证填入 `.env` 配置
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

支付成功后，LDC 平台通过 GET 请求回调 `notify_url`，商城验签后自动确认订单。

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

## 端口配置

默认端口为 `3000`（内部和外部一致），可通过 `.env` 中的 `APP_PORT` 修改：

```
APP_PORT=3000
```

## License

MIT
