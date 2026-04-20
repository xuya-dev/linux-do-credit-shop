package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import dev.xuya.ldcshop.params.ProductParams;
import dev.xuya.ldcshop.results.ProductDetailResult;
import dev.xuya.ldcshop.service.ProductService;
import dev.xuya.ldcshop.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现 / Product Service Implementation
 *
 * @author xuya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public Long createProduct(ProductParams params) {
        Product product = BeanUtil.copyProperties(params, Product.class);
        if (params.getImages() != null) {
            product.setImages(JSONUtil.toJsonStr(params.getImages()));
        }
        product.setCreatedBy(UserContextUtil.getCurrentUserId());
        product.setSoldCount(0);
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        save(product);
        return product.getId();
    }

    @Override
    public void updateProduct(Long id, ProductParams params) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        BeanUtil.copyProperties(params, product, "id", "createdBy", "soldCount");
        if (params.getImages() != null) {
            product.setImages(JSONUtil.toJsonStr(params.getImages()));
        }
        updateById(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        removeById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        product.setStatus(status);
        updateById(product);
    }

    @Override
    public ProductDetailResult getProductDetail(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return convertToDetailResult(product);
    }

    @Override
    public IPage<ProductDetailResult> pageUserProducts(int page, int size, Long categoryId, String keyword, Integer productType) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        buildQueryConditions(wrapper, categoryId, keyword, productType);
        wrapper.orderByAsc(Product::getSortOrder).orderByDesc(Product::getCreatedAt);

        IPage<Product> productPage = productMapper.selectPage(new Page<>(page, size), wrapper);
        return productPage.convert(this::convertToDetailResult);
    }

    @Override
    public IPage<ProductDetailResult> pageAdminProducts(int page, int size, Long categoryId, String keyword, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        buildQueryConditions(wrapper, categoryId, keyword, null);
        wrapper.orderByDesc(Product::getCreatedAt);

        IPage<Product> productPage = productMapper.selectPage(new Page<>(page, size), wrapper);
        return productPage.convert(this::convertToDetailResult);
    }

    /**
     * 构建查询条件 / Build query conditions
     */
    private void buildQueryConditions(LambdaQueryWrapper<Product> wrapper, Long categoryId, String keyword, Integer productType) {
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getDescription, keyword));
        }
        if (productType != null) {
            wrapper.eq(Product::getProductType, productType);
        }
    }

    /**
     * 转换为详情结果 / Convert to detail result
     */
    private ProductDetailResult convertToDetailResult(Product product) {
        ProductDetailResult result = BeanUtil.copyProperties(product, ProductDetailResult.class);
        if (StrUtil.isNotBlank(product.getImages())) {
            result.setImages(JSONUtil.toList(product.getImages(), String.class));
        }
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                result.setCategoryName(category.getName());
            }
        }
        return result;
    }
}
