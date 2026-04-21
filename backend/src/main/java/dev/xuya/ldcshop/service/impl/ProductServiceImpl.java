package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.util.AuditLog;
import dev.xuya.ldcshop.common.util.EntityUtil;
import dev.xuya.ldcshop.entity.Category;
import dev.xuya.ldcshop.entity.Product;
import dev.xuya.ldcshop.mapper.CategoryMapper;
import dev.xuya.ldcshop.mapper.ProductMapper;
import dev.xuya.ldcshop.params.ProductParams;
import dev.xuya.ldcshop.results.ProductDetailResult;
import dev.xuya.ldcshop.service.ProductCardService;
import dev.xuya.ldcshop.service.ProductService;
import dev.xuya.ldcshop.common.util.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final ProductCardService productCardService;

    /**
     * 创建商品 / Create a product
     * 虚拟商品库存由卡密数量自动计算 / Virtual product stock is auto-calculated from card inventory
     */
    @Override
    public Long createProduct(ProductParams params) {
        Product product = BeanUtil.copyProperties(params, Product.class);
        if (params.getImages() != null) {
            product.setImages(JSONUtil.toJsonStr(params.getImages()));
        }
        product.setCreatedBy(UserContextUtil.getCurrentUserId());
        product.setSoldCount(0);
        // 虚拟商品: 库存由卡密数量自动计算 / Virtual products: stock is auto-calculated from card inventory
        if (product.getProductType() == 1) {
            product.setStock(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        save(product);
        AuditLog.log("createProduct", "productId=" + product.getId() + ", productName=" + product.getName());
        return product.getId();
    }

    /**
     * 更新商品 / Update a product
     */
    @Override
    public void updateProduct(Long id, ProductParams params) {
        Product product = EntityUtil.requireNonNull(getById(id), ResultCode.PRODUCT_NOT_FOUND);
        BeanUtil.copyProperties(params, product, "id", "createdBy", "soldCount");
        if (params.getImages() != null) {
            product.setImages(JSONUtil.toJsonStr(params.getImages()));
        }
        // 虚拟商品: 忽略提交的库存值 / Virtual products: ignore submitted stock, keep 0
        if (product.getProductType() == 1) {
            product.setStock(0);
        }
        updateById(product);
        AuditLog.log("updateProduct", "productId=" + id + ", productName=" + product.getName());
    }

    /**
     * 删除商品 / Delete a product
     */
    @Override
    public void deleteProduct(Long id) {
        Product product = EntityUtil.requireNonNull(getById(id), ResultCode.PRODUCT_NOT_FOUND);
        removeById(id);
        AuditLog.log("deleteProduct", "productId=" + id + ", productName=" + product.getName());
    }

    /**
     * 更新商品状态 / Update product status
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = EntityUtil.requireNonNull(getById(id), ResultCode.PRODUCT_NOT_FOUND);
        product.setStatus(status);
        updateById(product);
    }

    /**
     * 获取商品详情 / Get product detail
     */
    @Override
    public ProductDetailResult getProductDetail(Long id) {
        Product product = EntityUtil.requireNonNull(getById(id), ResultCode.PRODUCT_NOT_FOUND);
        return convertToDetailResult(product);
    }

    /**
     * 用户端商品分页查询 / User-side product pagination
     */
    @Override
    public IPage<ProductDetailResult> pageUserProducts(int page, int size, Long categoryId, String keyword, Integer productType) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        buildQueryConditions(wrapper, categoryId, keyword, productType);
        wrapper.orderByAsc(Product::getSortOrder).orderByDesc(Product::getCreatedAt);

        IPage<Product> productPage = productMapper.selectPage(new Page<>(page, size), wrapper);
        return convertProductPage(productPage);
    }

    /**
     * 管理端商品分页查询 / Admin-side product pagination
     */
    @Override
    public IPage<ProductDetailResult> pageAdminProducts(int page, int size, Long categoryId, String keyword, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        buildQueryConditions(wrapper, categoryId, keyword, null);
        wrapper.orderByDesc(Product::getCreatedAt);

        IPage<Product> productPage = productMapper.selectPage(new Page<>(page, size), wrapper);
        return convertProductPage(productPage);
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
     * 批量转换商品分页 / Convert product page in batch
     */
    private IPage<ProductDetailResult> convertProductPage(IPage<Product> page) {
        List<Product> products = page.getRecords();
        if (products.isEmpty()) {
            return page.convert(p -> null);
        }

        Map<Long, Category> catMap = resolveCategoryMap(products);
        return page.convert(product -> toDetailResult(product, catMap));
    }

    /**
     * 转换单个商品为详情结果 / Convert single product to detail result
     */
    private ProductDetailResult convertToDetailResult(Product product) {
        Map<Long, Category> catMap = product.getCategoryId() != null
                ? categoryMapper.selectBatchIds(Set.of(product.getCategoryId())).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c))
                : Map.of();
        return toDetailResult(product, catMap);
    }

    /**
     * 统一的商品详情转换方法 / Unified product-to-detail-result conversion
     *
     * @param product 商品实体 / Product entity
     * @param catMap  分类映射 / Category map
     * @return 商品详情结果 / Product detail result
     */
    private ProductDetailResult toDetailResult(Product product, Map<Long, Category> catMap) {
        ProductDetailResult result = BeanUtil.copyProperties(product, ProductDetailResult.class);
        if (StrUtil.isNotBlank(product.getImages())) {
            result.setImages(JSONUtil.toList(product.getImages(), String.class));
        }
        if (product.getCategoryId() != null) {
            Category category = catMap.get(product.getCategoryId());
            if (category != null) {
                result.setCategoryName(category.getName());
            }
        }
        if (product.getProductType() == 1) {
            result.setStock(productCardService.getAvailableCount(product.getId()));
        }
        return result;
    }

    /**
     * 解析商品列表关联的分类映射 / Resolve category map for product list
     */
    private Map<Long, Category> resolveCategoryMap(List<Product> products) {
        Set<Long> catIds = products.stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        return catIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(catIds).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));
    }
}
