package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper / Order Mapper
 *
 * @author xuya
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询指定日期范围内的每日销售额 / Query daily sales within date range
     *
     * @param startDate 开始日期 / Start date
     * @param endDate   结束日期 / End date
     * @return 每日销售额列表 / Daily sales list
     */
    @Select("SELECT DATE(created_at) AS date, IFNULL(SUM(total_amount), 0) AS amount " +
            "FROM ldc_order " +
            "WHERE payment_status = 1 AND deleted = 0 " +
            "AND created_at BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> selectDailySales(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * 查询订单状态分布 / Query order status distribution
     *
     * @return 各状态数量 / Count by status
     */
    @Select("SELECT payment_status, COUNT(*) AS count FROM ldc_order WHERE deleted = 0 GROUP BY payment_status")
    List<Map<String, Object>> selectPaymentStatusDistribution();

    /**
     * 查询今日订单数和销售额 / Query today's order count and sales
     *
     * @param todayStart 今日开始时间 / Today start
     * @param todayEnd   今日结束时间 / Today end
     * @return 统计结果 / Statistics result
     */
    @Select("SELECT COUNT(*) AS orderCount, IFNULL(SUM(total_amount), 0) AS totalAmount " +
            "FROM ldc_order " +
            "WHERE payment_status = 1 AND deleted = 0 " +
            "AND paid_at BETWEEN #{todayStart} AND #{todayEnd}")
    Map<String, Object> selectTodayStats(@Param("todayStart") LocalDateTime todayStart,
                                          @Param("todayEnd") LocalDateTime todayEnd);

    /**
     * 查询商品销量排行 / Query product sales ranking
     *
     * @param limit 排行数量 / Rank limit
     * @return 销量排行 / Sales ranking
     */
    @Select("SELECT product_name, SUM(quantity) AS totalQuantity, SUM(total_amount) AS totalAmount " +
            "FROM ldc_order WHERE payment_status = 1 AND deleted = 0 " +
            "GROUP BY product_id, product_name ORDER BY totalQuantity DESC LIMIT #{limit}")
    List<Map<String, Object>> selectProductSalesRank(@Param("limit") int limit);

    /**
     * 查询分类销售占比 / Query category sales distribution
     *
     * @return 分类销售数据 / Category sales data
     */
    @Select("SELECT c.name AS categoryName, IFNULL(SUM(o.total_amount), 0) AS totalAmount " +
            "FROM ldc_order o " +
            "LEFT JOIN ldc_product p ON o.product_id = p.id " +
            "LEFT JOIN ldc_category c ON p.category_id = c.id " +
            "WHERE o.payment_status = 1 AND o.deleted = 0 " +
            "GROUP BY c.id, c.name ORDER BY totalAmount DESC")
    List<Map<String, Object>> selectCategorySalesDistribution();

    /**
     * 原子确认支付 / Atomically confirm payment
     * 仅当订单为未支付状态时才能确认成功
     *
     * @param outTradeNo    商户业务单号 / Merchant out trade number
     * @param tradeNo       LDC平台交易号 / LDC trade number
     * @param deliveryStatus 发货状态 / Delivery status
     * @param deliveredAt   发货时间 / Delivered time
     * @return 影响行数 / Affected rows
     */
    @Update("UPDATE ldc_order SET payment_status = 1, ldc_trade_no = #{tradeNo}, paid_at = NOW(), " +
            "delivery_status = #{deliveryStatus}, delivered_at = #{deliveredAt} " +
            "WHERE ldc_out_trade_no = #{outTradeNo} AND payment_status = 0 AND deleted = 0")
    int atomicConfirmPayment(@Param("outTradeNo") String outTradeNo,
                             @Param("tradeNo") String tradeNo,
                             @Param("deliveryStatus") int deliveryStatus,
                             @Param("deliveredAt") LocalDateTime deliveredAt);

    /**
     * 查询超时未支付订单 / Query expired unpaid orders
     *
     * @param deadline 截止时间 / Deadline
     * @return 超时订单列表 / Expired order list
     */
    @Select("SELECT id, order_no, product_id, product_type, quantity FROM ldc_order " +
            "WHERE payment_status = 0 AND created_at < #{deadline} AND deleted = 0")
    List<Order> selectExpiredUnpaidOrders(@Param("deadline") LocalDateTime deadline);

    /**
     * 原子关闭订单(仅未支付可关闭) / Atomically close order (only unpaid can be closed)
     *
     * @param id 订单ID / Order ID
     * @return 影响行数 / Affected rows
     */
    @Update("UPDATE ldc_order SET payment_status = 3 WHERE id = #{id} AND payment_status = 0 AND deleted = 0")
    int atomicCloseOrder(@Param("id") Long id);
}
