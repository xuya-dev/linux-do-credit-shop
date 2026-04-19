package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
            "FROM orders " +
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
    @Select("SELECT payment_status, COUNT(*) AS count FROM orders WHERE deleted = 0 GROUP BY payment_status")
    List<Map<String, Object>> selectPaymentStatusDistribution();

    /**
     * 查询今日订单数和销售额 / Query today's order count and sales
     *
     * @param todayStart 今日开始时间 / Today start
     * @param todayEnd   今日结束时间 / Today end
     * @return 统计结果 / Statistics result
     */
    @Select("SELECT COUNT(*) AS orderCount, IFNULL(SUM(total_amount), 0) AS totalAmount " +
            "FROM orders " +
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
            "FROM orders WHERE payment_status = 1 AND deleted = 0 " +
            "GROUP BY product_id, product_name ORDER BY totalQuantity DESC LIMIT #{limit}")
    List<Map<String, Object>> selectProductSalesRank(@Param("limit") int limit);

    /**
     * 查询分类销售占比 / Query category sales distribution
     *
     * @return 分类销售数据 / Category sales data
     */
    @Select("SELECT c.name AS categoryName, IFNULL(SUM(o.total_amount), 0) AS totalAmount " +
            "FROM orders o " +
            "LEFT JOIN product p ON o.product_id = p.id " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "WHERE o.payment_status = 1 AND o.deleted = 0 " +
            "GROUP BY c.id, c.name ORDER BY totalAmount DESC")
    List<Map<String, Object>> selectCategorySalesDistribution();
}
