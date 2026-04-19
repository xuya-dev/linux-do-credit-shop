# ============================================================
# LDC Shop 单容器 Dockerfile
# 多阶段构建: 前端Node构建 -> 后端Maven构建 -> JRE运行
# Multi-stage: Frontend Node -> Backend Maven -> JRE Runtime
# ============================================================

# ---- 阶段1: 前端构建 / Stage 1: Frontend Build ----
FROM node:20-alpine AS frontend-builder

WORKDIR /build/frontend

# 复制前端依赖文件 / Copy frontend dependency files
COPY frontend/package.json frontend/package-lock.json* ./

# 安装依赖 / Install dependencies
RUN npm ci --registry=https://registry.npmmirror.com

# 复制前端源码并构建 / Copy frontend source and build
COPY frontend/ .

# API 基础路径（相对路径，由同一个服务提供） / API base path (relative, served by same container)
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# ---- 阶段2: 后端构建 / Stage 2: Backend Build ----
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /build/backend

# 先复制 pom.xml 利用 Docker 缓存加速依赖下载
# Copy pom.xml first to leverage Docker cache for dependencies
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# 复制后端源码并构建 / Copy backend source and build
COPY backend/src ./src
RUN mvn package -DskipTests -B \
    && mv target/ldc-shop-*.jar target/app.jar

# 将前端构建产物注入 jar 的静态资源目录 (原地更新, 保留 MANIFEST.MF)
# Inject frontend build into jar's static resources (in-place update, preserves MANIFEST.MF)
COPY --from=frontend-builder /build/frontend/dist target/static-inject/BOOT-INF/classes/static

RUN cd target \
    && jar uf app.jar -C static-inject BOOT-INF \
    && rm -rf static-inject

# ---- 阶段3: 运行 / Stage 3: Runtime ----
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="xuya <dev@xuya.dev>"
LABEL description="LDC Shop / LINUX DO Credit 商城 (全栈单容器)"
LABEL version="1.0.0"

# 时区和基础工具 / Timezone and basic tools
RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

WORKDIR /app

# 从构建阶段复制 jar (含前端静态文件) / Copy jar (includes frontend static files)
COPY --from=backend-builder /build/backend/target/app.jar app.jar

# 创建非 root 用户 / Create non-root user
RUN addgroup -S ldcshop && adduser -S ldcshop -G ldcshop \
    && mkdir -p /app/logs \
    && chown -R ldcshop:ldcshop /app
USER ldcshop

# 健康检查 / Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:3000/api/auth/authorize-url || exit 1

# JVM 参数 (可通过环境变量覆盖) / JVM options (overridable via env)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

EXPOSE 3000

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
