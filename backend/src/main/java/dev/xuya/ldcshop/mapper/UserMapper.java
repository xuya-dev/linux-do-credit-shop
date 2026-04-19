package dev.xuya.ldcshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.xuya.ldcshop.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper / User Mapper
 * 用户表数据访问层
 *
 * @author xuya
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
