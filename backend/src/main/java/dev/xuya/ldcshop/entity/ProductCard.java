package dev.xuya.ldcshop.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 卡密实体 / Product Card Entity
 * 对应数据库 product_card 表，虚拟商品的卡密/激活码池
 *
 * @author xuya
 */
@Data
@TableName("ldc_product_card")
public class ProductCard {

    /** 卡密ID / Card ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID / Product ID */
    private Long productId;

    /** 卡密内容 / Card Content */
    private String cardContent;

    /** 状态: 0=可用 1=已售 2=已禁用 / Status: 0=available 1=sold 2=disabled */
    private Integer status;

    /** 关联订单ID / Related Order ID */
    private Long orderId;

    /** 售出时间 / Sold Time */
    private LocalDateTime soldAt;

    /** 创建时间 / Created Time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
