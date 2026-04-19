package dev.xuya.ldcshop.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.common.R;
import dev.xuya.ldcshop.results.CategoryResult;
import dev.xuya.ldcshop.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端-分类控制器 / User Category Controller
 *
 * @author xuya
 */
@RestController
@RequestMapping("/api/user/categories")
@RequiredArgsConstructor
@Tag(name = "用户端-分类 / User Categories")
public class UserCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public R<List<CategoryResult>> list() {
        return R.ok(categoryService.listEnabledCategories());
    }
}
