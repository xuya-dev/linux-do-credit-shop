package dev.xuya.ldcshop.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.params.CategoryParams;
import dev.xuya.ldcshop.results.CategoryResult;
import dev.xuya.ldcshop.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-分类管理控制器 / Admin Category Management Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@SaCheckRole("admin")
@Tag(name = "管理端-分类 / Admin Categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public R<IPage<CategoryResult>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(categoryService.pageCategories(page, size));
    }

    @PostMapping
    public R<Long> create(@Valid @RequestBody CategoryParams params) {
        return R.ok(categoryService.createCategory(params));
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryParams params) {
        categoryService.updateCategory(id, params);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return R.ok();
    }
}
