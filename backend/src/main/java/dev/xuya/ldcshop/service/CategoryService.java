package dev.xuya.ldcshop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.xuya.ldcshop.entity.Category;
import dev.xuya.ldcshop.params.CategoryParams;
import dev.xuya.ldcshop.results.CategoryResult;

import java.util.List;

/**
 * 分类服务接口 / Category Service Interface
 *
 * @author xuya
 */
public interface CategoryService extends IService<Category> {

    /**
     * 创建分类 / Create category
     */
    Long createCategory(CategoryParams params);

    /**
     * 更新分类 / Update category
     */
    void updateCategory(Long id, CategoryParams params);

    /**
     * 删除分类 / Delete category
     */
    void deleteCategory(Long id);

    /**
     * 获取所有启用的分类列表 / Get all enabled categories
     */
    List<CategoryResult> listEnabledCategories();

    /**
     * 分页查询分类（管理端） / Page query categories (admin)
     */
    IPage<CategoryResult> pageCategories(int page, int size);
}
