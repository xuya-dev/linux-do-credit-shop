# ============================================================
# LDC Shop 一键部署脚本 (Windows PowerShell)
#
# 使用方法 / Usage:
#   .\deploy.ps1 standalone    # 独立模式, 自动安装 MySQL + Redis
#   .\deploy.ps1 external      # 外部模式, 连接已有 MySQL + Redis
#   .\deploy.ps1 stop          # 停止
#   .\deploy.ps1 restart       # 重启
#   .\deploy.ps1 status        # 状态
#   .\deploy.ps1 logs          # 日志
#   .\deploy.ps1 build         # 重新构建
#   .\deploy.ps1 down          # 删除全部(含数据)
# ============================================================

param(
    [Parameter(Position=0)]
    [ValidateSet("standalone", "external", "stop", "restart", "status", "logs", "build", "down")]
    [string]$Action = "standalone"
)

$ErrorActionPreference = "Stop"
function WI($m) { Write-Host "[INFO]  $m" -ForegroundColor Cyan }
function WO($m) { Write-Host "[OK]    $m" -ForegroundColor Green }
function WW($m) { Write-Host "[WARN]  $m" -ForegroundColor Yellow }
function WE($m) { Write-Host "[ERROR] $m" -ForegroundColor Red }

function Ensure-Env {
    if (Test-Path ".env") { WO ".env 已加载"; return }
    WW ".env 不存在, 从 .env.example 复制"
    Copy-Item ".env.example" ".env"
    WW "请编辑 .env 后重新运行"; exit 1
}

function Check-Docker {
    try { $null = docker info 2>&1; WO "Docker 运行中" }
    catch { WE "Docker 未运行或未安装"; exit 1 }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LDC Shop 部署工具 / Deploy Tool" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Check-Docker

switch ($Action) {
    "standalone" {
        WI "独立模式 / Standalone: MySQL + Redis + App"
        Ensure-Env
        docker compose -f docker-compose.standalone.yml --env-file .env build --no-cache
        docker compose -f docker-compose.standalone.yml --env-file .env up -d
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""; WO "部署成功!"; Write-Host ""
            WI "访问地址: http://localhost:3000"
            WI "管理后台: http://localhost:3000/admin"
            WI "API 文档:  http://localhost:3000/api/doc.html"
            Write-Host ""; WW "首次启动需等待 MySQL 初始化 (约30秒)"
        }
    }
    "external" {
        WI "外部模式 / External: App only"
        Ensure-Env
        docker compose -f docker-compose.external.yml --env-file .env build --no-cache
        docker compose -f docker-compose.external.yml --env-file .env up -d
        if ($LASTEXITCODE -eq 0) { Write-Host ""; WO "部署成功!"; WI "访问地址: http://localhost:3000" }
    }
    "stop" {
        WI "停止服务..."; docker compose -f docker-compose.standalone.yml --env-file .env down 2>$null
        docker compose -f docker-compose.external.yml --env-file .env down 2>$null; WO "已停止"
    }
    "restart" {
        WI "重启..."; docker compose -f docker-compose.standalone.yml --env-file .env restart 2>$null
        docker compose -f docker-compose.external.yml --env-file .env restart 2>$null; WO "已重启"
    }
    "status" {
        docker compose -f docker-compose.standalone.yml --env-file .env ps 2>$null
        docker compose -f docker-compose.external.yml --env-file .env ps 2>$null
    }
    "logs" {
        $svc = if ($args.Count -gt 0) { $args[0] } else { "" }
        docker compose -f docker-compose.standalone.yml --env-file .env logs -f --tail 100 $svc 2>$null
        docker compose -f docker-compose.external.yml --env-file .env logs -f --tail 100 $svc 2>$null
    }
    "build" {
        WI "重新构建..."; docker compose -f docker-compose.standalone.yml --env-file .env build --no-cache; WO "构建完成"
    }
    "down" {
        WW "将删除所有容器和数据! / This deletes all containers and data!"
        $c = Read-Host "确认? (y/N)"
        if ($c -eq 'y' -or $c -eq 'Y') {
            docker compose -f docker-compose.standalone.yml --env-file .env down -v 2>$null
            docker compose -f docker-compose.external.yml --env-file .env down -v 2>$null; WO "已清理"
        } else { WI "已取消" }
    }
}
