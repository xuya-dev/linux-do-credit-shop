package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.entity.Category;
import dev.xuya.ldcshop.entity.Product;
import dev.xuya.ldcshop.mapper.CategoryMapper;
import dev.xuya.ldcshop.mapper.ProductMapper;
import dev.xuya.ldcshop.params.CategoryParams;
import dev.xuya.ldcshop.results.CategoryResult;
import dev.xuya.ldcshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现 / Category Service Implementation
 *
 * @author xuya
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;

    @Override
    public Long createCategory(CategoryParams params) {
        Category category = BeanUtil.copyProperties(params, Category.class);
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        save(category);
        return category.getId();
    }

    @Override
    public void updateCategory(Long id, CategoryParams params) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        BeanUtil.copyProperties(params, category, "id");
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        // 检查分类下是否有商品 / Check if category has products
        long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException(4001, "该分类下存在商品，无法删除 / Category has products, cannot delete");
        }
        removeById(id);
    }

    @Override
    public List<CategoryResult> listEnabledCategories() {
        List<Category> categories = list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
        return categories.stream().map(this::convertToResult).collect(Collectors.toList());
    }

    @Override
    public IPage<CategoryResult> pageCategories(int page, int size) {
        IPage<Category> categoryPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
        return categoryPage.convert(this::convertToResult);
    }

    private CategoryResult convertToResult(Category category) {
        CategoryResult result = BeanUtil.copyProperties(category, CategoryResult.class);
        long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, category.getId())
                        .eq(Product::getStatus, 1));
        result.setProductCount((int) count);
        return result;
    }
}
