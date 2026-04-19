package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 商品 Mapper / Product Mapper
 *
 * @author xuya
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 查询商品总销售额 / Query total sales amount
     *
     * @param productId 商品ID / Product ID
     * @return 销售额 / Sales amount
     */
    @Select("SELECT IFNULL(SUM(o.total_amount), 0) FROM orders o " +
            "WHERE o.product_id = #{productId} AND o.payment_status = 1 AND o.deleted = 0")
    BigDecimal selectTotalSales(@Param("productId") Long productId);
}
