package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.entity.ProductCard;
import dev.xuya.ldcshop.params.CardImportParams;
import dev.xuya.ldcshop.results.CardDetailResult;
import dev.xuya.ldcshop.results.CardListResult;
import dev.xuya.ldcshop.service.ProductCardService;
import dev.xuya.ldcshop.common.util.CryptoUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-卡密管理控制器 / Admin Card Management Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-卡密 / Admin Cards")
public class AdminCardController {

    private final ProductCardService productCardService;
    private final CryptoUtil cryptoUtil;

    /**
     * 分页查询卡密列表 / Paginated list of cards
     */
    @GetMapping
    public R<IPage<CardListResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer status) {
        return R.ok(productCardService.pageCards(page, size, productId, status));
    }

    /**
     * 批量导入卡密 / Batch import cards
     */
    @PostMapping("/import")
    public R<Integer> batchImport(@Valid @RequestBody CardImportParams params) {
        return R.ok(productCardService.batchImport(params));
    }

    /**
     * 删除卡密 / Delete a card
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        productCardService.deleteCard(id);
        return R.ok();
    }

    /**
     * 查询商品可用卡密数量 / Get available card count for a product
     */
    @GetMapping("/available-count")
    public R<Integer> availableCount(@RequestParam Long productId) {
        return R.ok(productCardService.getAvailableCount(productId));
    }

    /**
     * 获取卡密详情 / Get card detail
     * 根据ID查询单张卡密的完整信息，卡密内容会被解密后返回
     * Query a single card by ID; the card content will be decrypted before returning
     */
    @GetMapping("/{id}")
    public R<CardDetailResult> detail(@PathVariable Long id) {
        ProductCard card = productCardService.getById(id);
        if (card == null) {
            throw new BusinessException(ResultCode.CARD_NOT_FOUND);
        }
        CardDetailResult result = new CardDetailResult();
        BeanUtils.copyProperties(card, result);
        result.setCardContent(cryptoUtil.decrypt(card.getCardContent()));
        return R.ok(result);
    }
}
