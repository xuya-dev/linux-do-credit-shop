package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("SELECT IFNULL(SUM(o.total_amount), 0) FROM ldc_order o " +
            "WHERE o.product_id = #{productId} AND o.payment_status = 1 AND o.deleted = 0")
    BigDecimal selectTotalSales(@Param("productId") Long productId);

    /**
     * 扣减库存 / Deduct stock
     * 仅当库存充足时才能扣减成功（乐观锁）
     *
     * @param productId 商品ID / Product ID
     * @param quantity  数量 / Quantity
     * @return 影响行数 / Affected rows
     */
    @Update("UPDATE ldc_product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity} AND deleted = 0")
    int deductStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * 增加已售数量 / Increment sold count
     *
     * @param productId 商品ID / Product ID
     * @param quantity  数量 / Quantity
     * @return 影响行数 / Affected rows
     */
    @Update("UPDATE ldc_product SET sold_count = sold_count + #{quantity} WHERE id = #{productId} AND deleted = 0")
    int incrementSoldCount(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * 恢复库存 / Restore stock (e.g. on order cancellation)
     *
     * @param productId 商品ID / Product ID
     * @param quantity  数量 / Quantity
     * @return 影响行数 / Affected rows
     */
    @Update("UPDATE ldc_product SET stock = stock + #{quantity} WHERE id = #{productId} AND deleted = 0")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * 减少已售数量（下限为0）/ Decrement sold count (floor at 0)
     *
     * @param productId 商品ID / Product ID
     * @param quantity  数量 / Quantity
     * @return 影响行数 / Affected rows
     */
    @Update("UPDATE ldc_product SET sold_count = GREATEST(0, sold_count - #{quantity}) WHERE id = #{productId} AND deleted = 0")
    int decrementSoldCount(@Param("productId") Long productId, @Param("quantity") int quantity);
}
