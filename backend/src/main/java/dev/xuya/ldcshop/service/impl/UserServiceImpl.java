package dev.xuya.ldcshop.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.xuya.ldcshop.common.ResultCode;
import dev.xuya.ldcshop.common.exception.BusinessException;
import dev.xuya.ldcshop.common.util.AuditLog;
import dev.xuya.ldcshop.common.util.EntityUtil;
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

    /**
     * 通过 LDC UID 查找用户 / Find user by LDC UID
     */
    @Override
    public User findByLdcUid(String ldcUid) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getLdcUid, ldcUid));
    }

    /**
     * 创建或更新用户 / Create or update a user
     */
    @Override
    public User createOrUpdateUser(User user) {
        if (user.getId() == null) {
            save(user);
        } else {
            updateById(user);
        }
        return user;
    }

    /**
     * 获取用户信息 / Get user info
     */
    @Override
    public UserInfoResult getUserInfo(Long id) {
        User user = EntityUtil.requireNonNull(getById(id), ResultCode.USER_NOT_FOUND);
        return BeanUtil.copyProperties(user, UserInfoResult.class);
    }

    /**
     * 分页查询用户列表 / Paginate user list
     */
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

    /**
     * 更新用户状态 / Update user status
     */
    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = EntityUtil.requireNonNull(getById(id), ResultCode.USER_NOT_FOUND);
        Integer oldStatus = user.getStatus();
        user.setStatus(status);
        updateById(user);
        AuditLog.log("updateUserStatus", "targetUserId=" + id + ", oldStatus=" + oldStatus + ", newStatus=" + status);
    }

    /**
     * 更新用户角色 / Update user role
     */
    @Override
    public void updateUserRole(Long id, String role) {
        User user = EntityUtil.requireNonNull(getById(id), ResultCode.USER_NOT_FOUND);
        String oldRole = user.getRole();
        user.setRole(role);
        updateById(user);
        AuditLog.log("updateUserRole", "targetUserId=" + id + ", oldRole=" + oldRole + ", newRole=" + role);
    }
}
