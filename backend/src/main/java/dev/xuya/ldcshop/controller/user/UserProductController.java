package dev.xuya.ldcshop.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.results.ProductDetailResult;
import dev.xuya.ldcshop.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端-商品控制器 / User Product Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/user/products")
@RequiredArgsConstructor
@Tag(name = "用户端-商品 / User Products")
public class UserProductController {

    private final ProductService productService;

    /**
     * 商品列表 / Product list
     */
    @GetMapping
    public R<IPage<ProductDetailResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer productType) {
        return R.ok(productService.pageUserProducts(page, size, categoryId, keyword, productType));
    }

    /**
     * 商品详情 / Product detail
     */
    @GetMapping("/{id}")
    public R<ProductDetailResult> detail(@PathVariable Long id) {
        return R.ok(productService.getProductDetail(id));
    }
}
