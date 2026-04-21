package dev.xuya.ldcshop.params;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品创建/编辑参数 / Product Create/Update Params
 * 管理员创建或编辑商品时提交的参数
 *
 * @author xuya
 */
@Data
public class ProductParams {

    /** 分类ID / Category ID */
    private Long categoryId;

    /** 商品名称 / Product Name */
    @NotBlank(message = "{validation.product_name_required}")
    private String name;

    /** 商品描述 / Product Description */
    private String description;

    /** 封面图片URL / Cover Image URL */
    private String coverImage;

    /** 商品图片URL列表 / Product Image URLs */
    private List<String> images;

    /** 积分价格 / Credit Price */
    @NotNull(message = "{validation.price_required}")
    @Positive(message = "{validation.price_positive}")
    private BigDecimal price;

    /** 商品类型: 1=虚拟(卡密) 2=实物 / Type: 1=virtual 2=physical */
    @NotNull(message = "{validation.product_type_required}")
    @Min(value = 1, message = "{validation.product_type_invalid}")
    @Max(value = 2, message = "{validation.product_type_invalid}")
    private Integer productType;

    /** 库存数量 / Stock Count */
    private Integer stock;

    /** 状态: 0=下架 1=上架 / Status: 0=off 1=on */
    private Integer status;

    /** 排序值 / Sort Order */
    private Integer sortOrder;
}
