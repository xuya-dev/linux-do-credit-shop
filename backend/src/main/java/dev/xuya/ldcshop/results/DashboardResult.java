package dev.xuya.ldcshop.results;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计看板结果 / Statistics Dashboard Result
 * 管理端首页数据看板所需的所有统计数据
 *
 * @author xuya
 */
@Data
public class DashboardResult {

    /** 今日订单数 / Today's Order Count */
    private Integer todayOrderCount;

    /** 今日销售额(积分) / Today's Sales Amount */
    private BigDecimal todaySalesAmount;

    /** 总用户数 / Total User Count */
    private Integer totalUserCount;

    /** 总订单数 / Total Order Count */
    private Integer totalOrderCount;

    /** 待处理争议数 / Pending Dispute Count */
    private Integer pendingDisputeCount;

    /** 待发货订单数 / Pending Delivery Count */
    private Integer pendingDeliveryCount;

    /** 近30天每日销售趋势 / Last 30 Days Daily Sales Trend */
    private List<SalesTrendItem> salesTrend;

    /** 订单支付状态分布 / Order Payment Status Distribution */
    private List<StatusDistributionItem> paymentStatusDistribution;

    /** 商品销量排行TOP10 / Product Sales Ranking TOP10 */
    private List<ProductSalesItem> productSalesRank;

    /** 分类销售占比 / Category Sales Distribution */
    private List<CategorySalesItem> categorySalesDistribution;

    /**
     * 销售趋势项 / Sales Trend Item
     */
    @Data
    public static class SalesTrendItem {
        /** 日期 / Date */
        private String date;
        /** 金额 / Amount */
        private BigDecimal amount;
    }

    /**
     * 状态分布项 / Status Distribution Item
     */
    @Data
    public static class StatusDistributionItem {
        /** 状态 / Status */
        private Integer status;
        /** 状态名称 / Status Name */
        private String statusName;
        /** 数量 / Count */
        private Integer count;
    }

    /**
     * 商品销量项 / Product Sales Item
     */
    @Data
    public static class ProductSalesItem {
        /** 商品名称 / Product Name */
        private String productName;
        /** 销售数量 / Quantity */
        private Integer totalQuantity;
        /** 销售金额 / Amount */
        private BigDecimal totalAmount;
    }

    /**
     * 分类销售项 / Category Sales Item
     */
    @Data
    public static class CategorySalesItem {
        /** 分类名称 / Category Name */
        private String categoryName;
        /** 销售金额 / Amount */
        private BigDecimal totalAmount;
    }
}
