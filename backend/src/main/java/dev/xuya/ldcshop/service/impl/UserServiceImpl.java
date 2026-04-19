package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.mapper.UserMapper;
import dev.xuya.ldcshop.results.UserInfoResult;
import dev.xuya.ldcshop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现 / User Service Implementation
 *
 * @author xuya
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User findByLdcUid(String ldcUid) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getLdcUid, ldcUid));
    }

    @Override
    public User createOrUpdateUser(User user) {
        if (user.getId() == null) {
            save(user);
        } else {
            updateById(user);
        }
        return user;
    }

    @Override
    public UserInfoResult getUserInfo(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return BeanUtil.copyProperties(user, UserInfoResult.class);
    }

    @Override
    public IPage<UserInfoResult> pageUsers(int page, int size, String keyword, String role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (StrUtil.isNotBlank(role)) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> userPage = page(new Page<>(page, size), wrapper);
        return userPage.convert(u -> BeanUtil.copyProperties(u, UserInfoResult.class));
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        updateById(user);
    }

    @Override
    public void updateUserRole(Long id, String role) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setRole(role);
        updateById(user);
    }
}
