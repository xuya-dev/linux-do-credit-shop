package dev.xuya.ldcshop.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 订单工具类 / Order Utility
 * 用于生成订单号等
 *
 * @author xuya
 */
public class OrderUtil {

    /** 订单号前缀 / Order Number Prefix */
    private static final String ORDER_PREFIX = "LDC";

    /**
     * 生成唯一订单号 / Generate unique order number
     * 格式: LDC + 年月日时分秒 + 6位随机数
     * Format: LDC + yyyyMMddHHmmss + 6-digit random
     *
     * @return 订单号 / Order number
     */
    public static String generateOrderNo() {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
        return ORDER_PREFIX + datetime + random;
    }

    /**
     * 生成商户业务单号 / Generate merchant out trade number
     *
     * @return 业务单号 / Out trade number
     */
    public static String generateOutTradeNo() {
        return "OT" + IdUtil.fastSimpleUUID().substring(0, 16).toUpperCase();
    }
}
