# ============================================================
# LDC Shop 单容器 Dockerfile
# 多阶段构建: 前端Node构建(pnpm) -> 后端Maven构建 -> JRE运行
# Multi-stage: Frontend Node(pnpm) -> Backend Maven -> JRE Runtime
# ============================================================

# ---- 阶段1: 前端构建 / Stage 1: Frontend Build ----
FROM node:20-alpine AS frontend-builder

RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /build/frontend

COPY frontend/pnpm-lock.yaml frontend/package.json ./
COPY frontend/pnpm-workspace.yaml ./
COPY frontend/turbo.json ./
COPY frontend/.npmrc ./

COPY frontend/internal ./internal
COPY frontend/packages ./packages
COPY frontend/scripts ./scripts
COPY frontend/apps/web-ldc-shop/package.json ./apps/web-ldc-shop/package.json
COPY frontend/apps/web-ldc-shop ./apps/web-ldc-shop

RUN pnpm install --frozen-lockfile --registry=https://registry.npmmirror.com

ARG VITE_GLOB_API_URL=/api
ENV VITE_GLOB_API_URL=$VITE_GLOB_API_URL

RUN pnpm run build:shop

# ---- 阶段2: 后端构建 / Stage 2: Backend Build ----
FROM maven:3.9-eclipse-temurin-21 AS backend-builder

WORKDIR /build/backend

COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

COPY backend/src ./src
RUN mvn package -DskipTests -B \
    && mv target/ldc-shop-*.jar target/app.jar

COPY --from=frontend-builder /build/frontend/apps/web-ldc-shop/dist target/static-inject/BOOT-INF/classes/static

RUN cd target \
    && jar uf app.jar -C static-inject BOOT-INF \
    && rm -rf static-inject

# ---- 阶段3: 运行 / Stage 3: Runtime ----
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="xuya <dev@xuya.dev>"
LABEL description="LDC Shop / LINUX DO Credit 商城 (全栈单容器)"
LABEL version="1.0.0"

RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone \
    && apk del tzdata

WORKDIR /app

COPY --from=backend-builder /build/backend/target/app.jar app.jar

RUN addgroup -S ldcshop && adduser -S ldcshop -G ldcshop \
    && mkdir -p /app/logs \
    && chown -R ldcshop:ldcshop /app
USER ldcshop

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:3000/actuator/health || exit 1

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseZGC -XX:+HeapDumpOnOutOfMemoryError"

EXPOSE 3000

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar app.jar"]
