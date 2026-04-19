#!/usr/bin/env bash
# ============================================================
# LDC Shop 一键部署脚本 / One-Click Deploy Script
#
# 使用方法 / Usage:
#   chmod +x deploy.sh
#   ./deploy.sh standalone    # 独立模式, 自动安装 MySQL + Redis
#   ./deploy.sh external      # 外部模式, 连接已有 MySQL + Redis
#   ./deploy.sh stop          # 停止
#   ./deploy.sh restart       # 重启
#   ./deploy.sh status        # 状态
#   ./deploy.sh logs          # 日志
#   ./deploy.sh build         # 重新构建
#   ./deploy.sh down          # 删除全部(含数据)
# ============================================================

set -e

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()  { echo -e "${CYAN}[INFO]  $1${NC}"; }
ok()    { echo -e "${GREEN}[OK]    $1${NC}"; }
warn()  { echo -e "${YELLOW}[WARN]  $1${NC}"; }
err()   { echo -e "${RED}[ERROR] $1${NC}"; }

ensure_env() {
    [ -f .env ] && { ok ".env 已加载"; return; }
    warn ".env 不存在, 从 .env.example 复制 / .env not found, copying from .env.example"
    cp .env.example .env
    warn "请编辑 .env 后重新运行 / Please edit .env and re-run"
    exit 1
}

check_docker() {
    docker info > /dev/null 2>&1 && { ok "Docker 运行中"; return; }
    err "Docker 未运行或未安装"; exit 1
}

ACTION="${1:-standalone}"
echo -e "\n${CYAN}========================================${NC}"
echo -e "${CYAN}  LDC Shop 部署工具 / Deploy Tool${NC}"
echo -e "${CYAN}========================================${NC}\n"
check_docker

case "$ACTION" in
    standalone)
        info "独立模式 / Standalone: MySQL + Redis + App"
        ensure_env
        docker compose -f docker-compose.standalone.yml --env-file .env up --build -d
        echo ""; ok "部署成功! / Deploy successful!"; echo ""
        info "访问地址: http://localhost"
        info "管理后台: http://localhost/admin"
        info "API 文档: http://localhost/api/doc.html"
        echo ""; warn "首次启动需等待 MySQL 初始化 (约30秒)"
        ;;
    external)
        info "外部模式 / External: App only"
        ensure_env
        docker compose -f docker-compose.external.yml --env-file .env up --build -d
        echo ""; ok "部署成功! / Deploy successful!"
        info "访问地址: http://localhost"
        ;;
    stop)
        info "停止服务..."; docker compose -f docker-compose.standalone.yml --env-file .env down 2>/dev/null || true
        docker compose -f docker-compose.external.yml --env-file .env down 2>/dev/null || true; ok "已停止"
        ;;
    restart)
        info "重启..."; docker compose -f docker-compose.standalone.yml --env-file .env restart 2>/dev/null || true
        docker compose -f docker-compose.external.yml --env-file .env restart 2>/dev/null || true; ok "已重启"
        ;;
    status)
        docker compose -f docker-compose.standalone.yml --env-file .env ps 2>/dev/null || true
        docker compose -f docker-compose.external.yml --env-file .env ps 2>/dev/null || true
        ;;
    logs)
        S="${2:-}"; docker compose -f docker-compose.standalone.yml --env-file .env logs -f --tail 100 $S 2>/dev/null || \
        docker compose -f docker-compose.external.yml --env-file .env logs -f --tail 100 $S 2>/dev/null || true
        ;;
    build)
        info "重新构建..."; docker compose -f docker-compose.standalone.yml --env-file .env build --no-cache; ok "构建完成"
        ;;
    down)
        warn "将删除所有容器和数据! / This deletes all containers and data!"
        read -p "确认? (y/N): " c
        [ "$c" = "y" ] || [ "$c" = "Y" ] || { info "已取消"; exit 0; }
        docker compose -f docker-compose.standalone.yml --env-file .env down -v 2>/dev/null || true
        docker compose -f docker-compose.external.yml --env-file .env down -v 2>/dev/null || true; ok "已清理"
        ;;
    *)
        echo "用法: $0 {standalone|external|stop|restart|status|logs|build|down}"; exit 1
        ;;
esac
