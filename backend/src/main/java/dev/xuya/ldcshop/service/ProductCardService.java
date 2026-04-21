package dev.xuya.ldcshop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.xuya.ldcshop.entity.ProductCard;
import dev.xuya.ldcshop.params.CardImportParams;
import dev.xuya.ldcshop.results.CardListResult;

/**
 * 卡密服务接口 / Product Card Service Interface
 *
 * @author xuya
 */
public interface ProductCardService extends IService<ProductCard> {

    /**
     * 批量导入卡密 / Batch import cards
     *
     * @param params 导入参数 / Import params
     * @return 导入成功数量 / Successfully imported count
     */
    int batchImport(CardImportParams params);

    /**
     * 分页查询卡密列表（脱敏）/ Page query card list (masked)
     *
     * @param page      页码 / Page number
     * @param size      每页数量 / Page size
     * @param productId 商品ID（可选） / Product ID (optional)
     * @param status    状态（可选） / Status (optional)
     * @return 分页结果 / Page result
     */
    IPage<CardListResult> pageCards(int page, int size, Long productId, Integer status);

    /**
     * 获取商品可用卡密数量 / Get available card count for product
     *
     * @param productId 商品ID / Product ID
     * @return 可用数量 / Available count
     */
    int getAvailableCount(Long productId);

    /**
     * 分配卡密给订单 / Allocate cards to order
     *
     * @param orderId   订单ID / Order ID
     * @param productId 商品ID / Product ID
     * @param quantity  数量 / Quantity
     */
    void allocateCards(Long orderId, Long productId, int quantity);

    /**
     * 删除卡密 / Delete card
     *
     * @param id 卡密ID / Card ID
     */
    void deleteCard(Long id);
}
