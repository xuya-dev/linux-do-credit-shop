package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.entity.OrderCard;
import dev.xuya.ldcshop.entity.ProductCard;
import dev.xuya.ldcshop.mapper.OrderCardMapper;
import dev.xuya.ldcshop.mapper.ProductCardMapper;
import dev.xuya.ldcshop.params.CardImportParams;
import dev.xuya.ldcshop.service.ProductCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCardServiceImpl extends ServiceImpl<ProductCardMapper, ProductCard> implements ProductCardService {

    private final ProductCardMapper productCardMapper;
    private final OrderCardMapper orderCardMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchImport(CardImportParams params) {
        List<ProductCard> cards = new ArrayList<>();
        for (String content : params.getCards()) {
            ProductCard card = new ProductCard();
            card.setProductId(params.getProductId());
            card.setCardContent(content.trim());
            card.setStatus(0);
            card.setCreatedAt(LocalDateTime.now());
            cards.add(card);
        }
        // 批量插入 / Batch insert
        saveBatch(cards);
        log.info("批量导入卡密成功 / Batch import cards success: productId={}, count={}",
                params.getProductId(), cards.size());
        return cards.size();
    }

    @Override
    public IPage<ProductCard> pageCards(int page, int size, Long productId, Integer status) {
        LambdaQueryWrapper<ProductCard> wrapper = new LambdaQueryWrapper<>();
        if (productId != null) {
            wrapper.eq(ProductCard::getProductId, productId);
        }
        if (status != null) {
            wrapper.eq(ProductCard::getStatus, status);
        }
        wrapper.orderByDesc(ProductCard::getCreatedAt);
        return productCardMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public int getAvailableCount(Long productId) {
        return (int) count(new LambdaQueryWrapper<ProductCard>()
                .eq(ProductCard::getProductId, productId)
                .eq(ProductCard::getStatus, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void allocateCards(Long orderId, Long productId, int quantity) {
        // 查询可用卡密 / Query available cards
        List<ProductCard> availableCards = list(new LambdaQueryWrapper<ProductCard>()
                .eq(ProductCard::getProductId, productId)
                .eq(ProductCard::getStatus, 0)
                .last("LIMIT " + quantity));

        if (availableCards.size() < quantity) {
            throw new BusinessException(ResultCode.CARD_STOCK_INSUFFICIENT);
        }

        LocalDateTime now = LocalDateTime.now();
        for (ProductCard card : availableCards) {
            // 更新卡密状态 / Update card status
            card.setStatus(1);
            card.setOrderId(orderId);
            card.setSoldAt(now);
            updateById(card);

            // 创建订单卡密关联 / Create order-card relation
            OrderCard orderCard = new OrderCard();
            orderCard.setOrderId(orderId);
            orderCard.setCardId(card.getId());
            orderCard.setCreatedAt(now);
            orderCardMapper.insert(orderCard);
        }
        log.info("卡密分配成功 / Cards allocated: orderId={}, quantity={}", orderId, quantity);
    }

    @Override
    public void deleteCard(Long id) {
        ProductCard card = getById(id);
        if (card == null) {
            throw new BusinessException(ResultCode.CARD_NOT_FOUND);
        }
        if (card.getStatus() == 1) {
            throw new BusinessException(4004, "已售出的卡密不可删除 / Sold card cannot be deleted");
        }
        removeById(id);
    }
}
