package dev.xuya.ldcshop.results;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类结果 / Category Result
 *
 * @author xuya
 */
@Data
public class CategoryResult {

    /** 分类ID / Category ID */
    private Long id;

    /** 分类名称 / Category Name */
    private String name;

    /** 分类图标 / Category Icon */
    private String icon;

    /** 排序值 / Sort Order */
    private Integer sortOrder;

    /** 状态 / Status */
    private Integer status;

    /** 该分类下商品数量 / Product Count in Category */
    private Integer productCount;

    /** 创建时间 / Created Time */
    private LocalDateTime createdAt;
}
