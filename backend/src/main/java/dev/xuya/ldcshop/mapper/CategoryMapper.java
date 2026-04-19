package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper / Category Mapper
 *
 * @author xuya
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
