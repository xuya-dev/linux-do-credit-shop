package dev.xuya.ldcshop.task;

import dev.xuya.ldcshop.common.config.LdcShopProperties;
import dev.xuya.ldcshop.entity.Order;
import dev.xuya.ldcshop.mapper.OrderMapper;
import dev.xuya.ldcshop.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 未支付订单自动关闭定时任务 / Auto-close Unpaid Orders Scheduled Task
 *
 * @author xuya
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoCloseTask {

    private static final String LOCK_KEY = "ldc:task:auto-close-lock";
    private static final long LOCK_TTL_SECONDS = 55;

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final LdcShopProperties properties;
    private final StringRedisTemplate redisTemplate;

    /**
     * 关闭超时未支付订单 / Close expired unpaid orders
     * 使用 Redis 分布式锁确保多实例环境下只有一个节点执行
     */
    @Scheduled(fixedRateString = "${ldc-shop.order.auto-close-interval:60000}")
    public void closeExpiredOrders() {
        if (!tryAcquireLock()) {
            return;
        }

        try {
            int timeoutMinutes = properties.getOrder().getAutoCloseMinutes();
            LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);

            List<Order> expiredOrders = orderMapper.selectExpiredUnpaidOrders(deadline);
            if (expiredOrders.isEmpty()) {
                return;
            }

            log.info("Found {} expired unpaid orders, closing...", expiredOrders.size());

            for (Order order : expiredOrders) {
                int rows = orderMapper.atomicCloseOrder(order.getId());
                if (rows > 0) {
                    if (order.getProductType() != null && order.getProductType() == 2) {
                        productMapper.restoreStock(order.getProductId(), order.getQuantity());
                    }
                    log.info("Auto-closed expired order: {}, productId: {}, qty: {}",
                            order.getOrderNo(), order.getProductId(), order.getQuantity());
                }
            }
        } finally {
            releaseLock();
        }
    }

    /**
     * 尝试获取分布式锁 / Try to acquire distributed lock
     *
     * @return 是否获取成功 / Whether lock was acquired
     */
    private boolean tryAcquireLock() {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * 释放分布式锁 / Release distributed lock
     */
    private void releaseLock() {
        redisTemplate.delete(LOCK_KEY);
    }
}
