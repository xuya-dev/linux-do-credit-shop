package dev.xuya.ldcshop.results;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情结果 / Product Detail Result
 * 返回给前端的商品详细信息
 *
 * @author xuya
 */
@Data
public class ProductDetailResult {

    /** 商品ID / Product ID */
    private Long id;

    /** 分类ID / Category ID */
    private Long categoryId;

    /** 分类名称 / Category Name */
    private String categoryName;

    /** 商品名称 / Product Name */
    private String name;

    /** 商品描述 / Product Description */
    private String description;

    /** 封面图片 / Cover Image */
    private String coverImage;

    /** 商品图片列表 / Product Images */
    private List<String> images;

    /** 积分价格 / Credit Price */
    private BigDecimal price;

    /** 商品类型: 1=虚拟 2=实物 / Type */
    private Integer productType;

    /** 库存数量 / Stock Count */
    private Integer stock;

    /** 已售数量 / Sold Count */
    private Integer soldCount;

    /** 状态 / Status */
    private Integer status;

    /** 排序值 / Sort Order */
    private Integer sortOrder;

    /** 创建时间 / Created Time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated Time */
    private LocalDateTime updatedAt;
}
