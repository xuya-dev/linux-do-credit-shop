package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xuya.ldcshop.common.util.I18nUtil;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.common.util.AuditLog;
import dev.xuya.ldcshop.common.util.EntityUtil;
import dev.xuya.ldcshop.entity.OrderCard;
import dev.xuya.ldcshop.entity.Product;
import dev.xuya.ldcshop.entity.ProductCard;
import dev.xuya.ldcshop.mapper.OrderCardMapper;
import dev.xuya.ldcshop.mapper.ProductCardMapper;
import dev.xuya.ldcshop.mapper.ProductMapper;
import dev.xuya.ldcshop.params.CardImportParams;
import dev.xuya.ldcshop.results.CardListResult;
import dev.xuya.ldcshop.service.ProductCardService;
import dev.xuya.ldcshop.common.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 卡密服务实现 / Product Card Service Implementation
 *
 * @author xuya
 */
@Service
@RequiredArgsConstructor
public class ProductCardServiceImpl extends ServiceImpl<ProductCardMapper, ProductCard> implements ProductCardService {

    private final ProductCardMapper productCardMapper;
    private final OrderCardMapper orderCardMapper;
    private final ProductMapper productMapper;
    private final CryptoUtil cryptoUtil;

    /**
     * 批量导入卡密 / Batch import cards
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchImport(CardImportParams params) {
        Product product = EntityUtil.requireNonNull(productMapper.selectById(params.getProductId()), ResultCode.PRODUCT_NOT_FOUND);
        if (product.getProductType() != 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        List<ProductCard> cards = new ArrayList<>();
        for (String content : params.getCards()) {
            ProductCard card = new ProductCard();
            card.setProductId(params.getProductId());
            card.setCardContent(cryptoUtil.encrypt(content.trim()));
            card.setStatus(0);
            card.setCreatedAt(LocalDateTime.now());
            cards.add(card);
        }
        // 批量插入 / Batch insert
        saveBatch(cards);
        AuditLog.log("batchImport", "productId=" + params.getProductId() + ", count=" + cards.size());
        return cards.size();
    }

    /**
     * 分页查询卡密列表 / Paginate card list
     */
    @Override
    public IPage<CardListResult> pageCards(int page, int size, Long productId, Integer status) {
        LambdaQueryWrapper<ProductCard> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(ProductCard::getProductId, productId);
        }
        if (status != null) {
            wrapper.eq(ProductCard::getStatus, status);
        }
        wrapper.orderByDesc(ProductCard::getCreatedAt);
        IPage<ProductCard> cardPage = productCardMapper.selectPage(new Page<>(page, size), wrapper);
        return cardPage.convert(card -> BeanUtil.copyProperties(card, CardListResult.class));
    }

    /**
     * 获取可用卡密数量 / Get available card count
     */
    @Override
    public int getAvailableCount(Long productId) {
        return (int) count(new LambdaQueryWrapper<ProductCard>()
                .eq(ProductCard::getProductId, productId)
                .eq(ProductCard::getStatus, 0));
    }

    /**
     * 为订单分配卡密 / Allocate cards for an order
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allocateCards(Long orderId, Long productId, int quantity) {
        List<Long> candidateIds = productCardMapper.selectAvailableIds(productId, quantity);
        if (candidateIds.size() < quantity) {
            throw new BusinessException(ResultCode.CARD_STOCK_INSUFFICIENT);
        }

        LocalDateTime now = LocalDateTime.now();
        int allocated = 0;
        for (Long cardId : candidateIds) {
            int rows = productCardMapper.atomicAllocate(cardId, productId, orderId);
            if (rows > 0) {
                allocated++;
                OrderCard orderCard = new OrderCard();
                orderCard.setOrderId(orderId);
                orderCard.setCardId(cardId);
                orderCard.setCreatedAt(now);
                orderCardMapper.insert(orderCard);
            }
            if (allocated >= quantity) break;
        }

        if (allocated < quantity) {
            throw new BusinessException(ResultCode.CARD_STOCK_INSUFFICIENT);
        }
        AuditLog.log("allocateCards", "orderId=" + orderId + ", quantity=" + allocated);
    }

    /**
     * 删除卡密 / Delete a card
     * 已售出的卡密不允许删除 / Sold cards cannot be deleted
     */
    @Override
    public void deleteCard(Long id) {
        ProductCard card = EntityUtil.requireNonNull(getById(id), ResultCode.CARD_NOT_FOUND);
        if (card.getStatus() == 1) {
            throw new BusinessException(4004, I18nUtil.get("card.sold_cannot_delete"));
        }
        removeById(id);
        AuditLog.log("deleteCard", "cardId=" + id + ", productId=" + card.getProductId());
    }
}
