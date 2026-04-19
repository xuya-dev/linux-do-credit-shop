package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.DisputeMapper;
import dev.xuya.ldcshop.mapper.OrderMapper;
import dev.xuya.ldcshop.mapper.UserMapper;
import dev.xuya.ldcshop.results.DashboardResult;
import dev.xuya.ldcshop.service.StatisticsService;
import dev.xuya.ldcshop.entity.Dispute;
import dev.xuya.ldcshop.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现 / Statistics Service Implementation
 * 提供管理端数据看板所需的统计数据
 *
 * @author xuya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final DisputeMapper disputeMapper;

    @Override
    public DashboardResult getDashboard() {
        DashboardResult result = new DashboardResult();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 今日统计 / Today's stats
        Map<String, Object> todayStats = orderMapper.selectTodayStats(todayStart, todayEnd);
        if (todayStats != null) {
            result.setTodayOrderCount(((Number) todayStats.getOrDefault("orderCount", 0)).intValue());
            result.setTodaySalesAmount(new BigDecimal(todayStats.getOrDefault("totalAmount", "0").toString()));
        } else {
            result.setTodayOrderCount(0);
            result.setTodaySalesAmount(BigDecimal.ZERO);
        }

        // 总用户数 / Total users
        result.setTotalUserCount((int) userMapper.selectCount(
                new LambdaQueryWrapper<User>()));

        // 总订单数（已支付） / Total orders (paid)
        result.setTotalOrderCount((int) orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getPaymentStatus, Order.PAYMENT_PAID)));

        // 待处理争议 / Pending disputes
        result.setPendingDisputeCount((int) disputeMapper.selectCount(
                new LambdaQueryWrapper<Dispute>().eq(Dispute::getStatus, Dispute.STATUS_PENDING)));

        // 待发货订单 / Pending delivery
        result.setPendingDeliveryCount((int) orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getPaymentStatus, Order.PAYMENT_PAID)
                        .eq(Order::getDeliveryStatus, Order.DELIVERY_PENDING)
                        .eq(Order::getProductType, 2)));

        // 近30天销售趋势 / Last 30 days sales trend
        LocalDateTime startDate = LocalDate.now().minusDays(29).atStartOfDay();
        List<Map<String, Object>> dailySales = orderMapper.selectDailySales(startDate, todayEnd);
        result.setSalesTrend(dailySales.stream().map(m -> {
            DashboardResult.SalesTrendItem item = new DashboardResult.SalesTrendItem();
            item.setDate(m.get("date").toString());
            item.setAmount(new BigDecimal(m.get("amount").toString()));
            return item;
        }).collect(Collectors.toList()));

        // 订单支付状态分布 / Payment status distribution
        List<Map<String, Object>> statusDist = orderMapper.selectPaymentStatusDistribution();
        Map<Integer, String> statusNames = Map.of(
                0, "待支付 / Pending",
                1, "已支付 / Paid",
                2, "已退款 / Refunded"
        );
        result.setPaymentStatusDistribution(statusDist.stream().map(m -> {
            DashboardResult.StatusDistributionItem item = new DashboardResult.StatusDistributionItem();
            Integer status = ((Number) m.get("payment_status")).intValue();
            item.setStatus(status);
            item.setStatusName(statusNames.getOrDefault(status, "未知 / Unknown"));
            item.setCount(((Number) m.get("count")).intValue());
            return item;
        }).collect(Collectors.toList()));

        // 商品销量排行TOP10 / Product sales ranking TOP10
        List<Map<String, Object>> salesRank = orderMapper.selectProductSalesRank(10);
        result.setProductSalesRank(salesRank.stream().map(m -> {
            DashboardResult.ProductSalesItem item = new DashboardResult.ProductSalesItem();
            item.setProductName(m.get("product_name").toString());
            item.setTotalQuantity(((Number) m.get("totalQuantity")).intValue());
            item.setTotalAmount(new BigDecimal(m.get("totalAmount").toString()));
            return item;
        }).collect(Collectors.toList()));

        // 分类销售占比 / Category sales distribution
        List<Map<String, Object>> categoryDist = orderMapper.selectCategorySalesDistribution();
        result.setCategorySalesDistribution(categoryDist.stream().map(m -> {
            DashboardResult.CategorySalesItem item = new DashboardResult.CategorySalesItem();
            item.setCategoryName(m.get("categoryName") != null ? m.get("categoryName").toString() : "未分类 / Uncategorized");
            item.setTotalAmount(new BigDecimal(m.get("totalAmount").toString()));
            return item;
        }).collect(Collectors.toList()));

        return result;
    }
}
