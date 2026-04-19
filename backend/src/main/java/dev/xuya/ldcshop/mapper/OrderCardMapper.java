package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.OrderCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单卡密关联 Mapper / Order Card Relation Mapper
 *
 * @author xuya
 */
@Mapper
public interface OrderCardMapper extends BaseMapper<OrderCard> {
}
