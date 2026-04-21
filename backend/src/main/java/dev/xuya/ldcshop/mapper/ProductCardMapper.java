package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.ProductCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品卡密 Mapper / Product Card Mapper
 *
 * @author xuya
 */
@Mapper
public interface ProductCardMapper extends BaseMapper<ProductCard> {

    /**
     * 查询指定商品可用的卡密ID列表 / Query available card IDs for a product
     *
     * @param productId 商品ID / Product ID
     * @param limit     数量限制 / Limit count
     * @return 卡密ID列表 / Card ID list
     */
    @Select("SELECT id FROM ldc_product_card WHERE product_id = #{productId} AND status = 0 LIMIT #{limit}")
    List<Long> selectAvailableIds(@Param("productId") Long productId, @Param("limit") int limit);

    /**
     * 原子分配卡密 / Atomically allocate a card to an order
     * 仅当卡密状态为可用(0)时才能分配成功
     *
     * @param cardId    卡密ID / Card ID
     * @param productId 商品ID / Product ID
     * @param orderId   订单ID / Order ID
     * @return 影响行数 / Affected rows
     */
    @Update("UPDATE ldc_product_card SET status = 1, order_id = #{orderId}, sold_at = NOW() " +
            "WHERE id = #{cardId} AND status = 0 AND product_id = #{productId}")
    int atomicAllocate(@Param("cardId") Long cardId, @Param("productId") Long productId, @Param("orderId") Long orderId);

    /**
     * 批量释放卡密（退回可用状态）/ Batch release cards back to available status
     *
     * @param cardIds 卡密ID列表 / Card ID list
     * @return 影响行数 / Affected rows
     */
    @Update("<script>" +
            "UPDATE ldc_product_card SET status = 0, order_id = NULL, sold_at = NULL " +
            "WHERE id IN " +
            "<foreach collection='cardIds' item='id' open='(' separator=',' close=')'>" +
            "  #{id}" +
            "</foreach>" +
            " AND status = 1" +
            "</script>")
    int batchReleaseCards(@Param("cardIds") List<Long> cardIds);
}
