package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.ProductParams;
import dev.xuya.ldcshop.results.ProductDetailResult;
import dev.xuya.ldcshop.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-商品管理控制器 / Admin Product Management Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-商品 / Admin Products")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public R<IPage<ProductDetailResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(productService.pageAdminProducts(page, size, categoryId, keyword, status));
    }

    @GetMapping("/{id}")
    public R<ProductDetailResult> detail(@PathVariable Long id) {
        return R.ok(productService.getProductDetail(id));
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody ProductParams params) {
        return R.ok(productService.createProduct(params));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ProductParams params) {
        productService.updateProduct(id, params);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return R.ok();
    }
}
