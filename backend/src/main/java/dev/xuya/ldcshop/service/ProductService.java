package dev.xuya.ldcshop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.xuya.ldcshop.entity.Product;
import dev.xuya.ldcshop.params.ProductParams;
import dev.xuya.ldcshop.results.ProductDetailResult;

/**
 * 商品服务接口 / Product Service Interface
 *
 * @author xuya
 */
public interface ProductService extends IService<Product> {

    /**
     * 创建商品 / Create product
     *
     * @param params 商品参数 / Product params
     * @return 商品ID / Product ID
     */
    Long createProduct(ProductParams params);

    /**
     * 更新商品 / Update product
     *
     * @param id     商品ID / Product ID
     * @param params 商品参数 / Product params
     */
    void updateProduct(Long id, ProductParams params);

    /**
     * 删除商品（逻辑删除） / Delete product (logical)
     *
     * @param id 商品ID / Product ID
     */
    void deleteProduct(Long id);

    /**
     * 更新商品状态（上架/下架） / Update product status
     *
     * @param id     商品ID / Product ID
     * @param status 状态 / Status
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取商品详情 / Get product detail
     *
     * @param id 商品ID / Product ID
     * @return 商品详情 / Product detail
     */
    ProductDetailResult getProductDetail(Long id);

    /**
     * 分页查询商品列表（用户端，仅上架商品） / Page query product list (user side, on-shelf only)
     *
     * @param page       页码 / Page number
     * @param size       每页数量 / Page size
     * @param categoryId 分类ID（可选） / Category ID (optional)
     * @param keyword    搜索关键词（可选） / Search keyword (optional)
     * @param productType 商品类型（可选） / Product type (optional)
     * @return 分页结果 / Page result
     */
    IPage<ProductDetailResult> pageUserProducts(int page, int size, Long categoryId, String keyword, Integer productType);

    /**
     * 分页查询商品列表（管理端，包含所有状态） / Page query product list (admin side, all statuses)
     *
     * @param page       页码 / Page number
     * @param size       每页数量 / Page size
     * @param categoryId 分类ID（可选） / Category ID (optional)
     * @param keyword    搜索关键词（可选） / Search keyword (optional)
     * @param status     状态（可选） / Status (optional)
     * @return 分页结果 / Page result
     */
    IPage<ProductDetailResult> pageAdminProducts(int page, int size, Long categoryId, String keyword, Integer status);
}
