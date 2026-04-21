package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体 / Product Entity
 * 对应数据库 product 表，支持虚拟商品(卡密)和实物商品
 *
 * @author xuya
 */
@Data
@TableName("ldc_product")
public class Product {

    /** 商品ID / Product ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类ID / Category ID */
    private Long categoryId;

    /** 商品名称 / Product Name */
    private String name;

    /** 商品描述 / Product Description */
    private String description;

    /** 封面图片 / Cover Image */
    private String coverImage;

    /** 商品图片列表(JSON) / Product Images(JSON) */
    private String images;

    /** 积分价格 / Credit Price */
    private BigDecimal price;

    /** 商品类型: 1=虚拟(卡密) 2=实物 / Type: 1=virtual 2=physical */
    private Integer productType;

    /** 库存数量 / Stock Count */
    private Integer stock;

    /** 已售数量 / Sold Count */
    private Integer soldCount;

    /** 状态: 0=下架 1=上架 / Status: 0=off 1=on */
    private Integer status;

    /** 排序值 / Sort Order */
    private Integer sortOrder;

    /** 创建人ID / Created By */
    private Long createdBy;

    /** 创建时间 / Created Time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated Time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除 / Logical Delete */
    @TableLogic
    private Integer deleted;
}
