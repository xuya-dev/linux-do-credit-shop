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
import dev.xuya.ldcshop.entity.Category;
import dev.xuya.ldcshop.entity.Product;
import dev.xuya.ldcshop.mapper.CategoryMapper;
import dev.xuya.ldcshop.mapper.ProductMapper;
import dev.xuya.ldcshop.params.CategoryParams;
import dev.xuya.ldcshop.results.CategoryResult;
import dev.xuya.ldcshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务实现 / Category Service Implementation
 *
 * @author xuya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;

    /**
     * 创建分类 / Create a category
     */
    @Override
    public Long createCategory(CategoryParams params) {
        Category category = BeanUtil.copyProperties(params, Category.class);
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        save(category);
        AuditLog.log("createCategory", "categoryId=" + category.getId() + ", categoryName=" + category.getName());
        return category.getId();
    }

    /**
     * 更新分类 / Update a category
     */
    @Override
    public void updateCategory(Long id, CategoryParams params) {
        Category category = EntityUtil.requireNonNull(getById(id), ResultCode.CATEGORY_NOT_FOUND);
        BeanUtil.copyProperties(params, category, "id");
        updateById(category);
        AuditLog.log("updateCategory", "categoryId=" + id + ", categoryName=" + category.getName());
    }

    /**
     * 删除分类 / Delete a category
     */
    @Override
    public void deleteCategory(Long id) {
        Category category = EntityUtil.requireNonNull(getById(id), ResultCode.CATEGORY_NOT_FOUND);
        // 检查分类下是否有商品 / Check if category has products
        long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException(4001, I18nUtil.get("category.has_products"));
        }
        removeById(id);
        AuditLog.log("deleteCategory", "categoryId=" + id + ", categoryName=" + category.getName());
    }

    /**
     * 获取启用的分类列表 / List enabled categories
     */
    @Override
    public List<CategoryResult> listEnabledCategories() {
        List<Category> categories = list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
        return batchConvertToResults(categories);
    }

    /**
     * 分页查询分类 / Paginate categories
     */
    @Override
    public IPage<CategoryResult> pageCategories(int page, int size) {
        IPage<Category> categoryPage = page(new Page<>(page, size),
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder));
        Map<Long, Long> countMap = batchCountProducts(categoryPage.getRecords());
        return categoryPage.convert(cat -> {
            CategoryResult result = BeanUtil.copyProperties(cat, CategoryResult.class);
            result.setProductCount(countMap.getOrDefault(cat.getId(), 0L).intValue());
            return result;
        });
    }

    /**
     * 批量转换分类结果 / Batch convert category results
     */
    private List<CategoryResult> batchConvertToResults(List<Category> categories) {
        if (categories.isEmpty()) return List.of();
        Map<Long, Long> countMap = batchCountProducts(categories);
        return categories.stream().map(cat -> {
            CategoryResult result = BeanUtil.copyProperties(cat, CategoryResult.class);
            result.setProductCount(countMap.getOrDefault(cat.getId(), 0L).intValue());
            return result;
        }).collect(Collectors.toList());
    }

    /**
     * 批量统计分类下商品数量 / Batch count products under categories
     */
    private Map<Long, Long> batchCountProducts(List<Category> categories) {
        if (categories.isEmpty()) return Map.of();
        List<Long> catIds = categories.stream().map(Category::getId).collect(Collectors.toList());
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .in(Product::getCategoryId, catIds)
                        .eq(Product::getStatus, 1)
                        .select(Product::getCategoryId));
        return products.stream().collect(Collectors.groupingBy(Product::getCategoryId, Collectors.counting()));
    }
}
