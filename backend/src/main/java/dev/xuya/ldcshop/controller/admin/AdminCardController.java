package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.entity.ProductCard;
import dev.xuya.ldcshop.params.CardImportParams;
import dev.xuya.ldcshop.service.ProductCardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    /**
     * 卡密列表 / Card list
     */
    @GetMapping
    public R<IPage<ProductCard>> list(
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
     * 删除卡密 / Delete card
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        productCardService.deleteCard(id);
        return R.ok();
    }

    /**
     * 查询商品可用卡密数量 / Get available card count
     */
    @GetMapping("/available-count")
    public R<Integer> availableCount(@RequestParam Long productId) {
        return R.ok(productCardService.getAvailableCount(productId));
    }
}
