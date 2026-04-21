package dev.xuya.ldcshop.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 审计日志工具 / Audit Log Utility
 * 统一管理员操作日志格式
 *
 * @author xuya
 */
@Slf4j
public final class AuditLog {

    private AuditLog() {}

    /**
     * 记录管理员操作日志 / Log admin operation
     *
     * @param action 操作名称 / Action name
     * @param target 操作目标描述 / Target description
     */
    public static void log(String action, String target) {
        log.info("[AUDIT] {} performed by userId={}, {}", action, UserContextUtil.getCurrentUserId(), target);
    }
}
