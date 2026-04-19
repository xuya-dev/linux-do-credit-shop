package dev.xuya.ldcshop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.xuya.ldcshop.entity.User;
import dev.xuya.ldcshop.results.UserInfoResult;

/**
 * 用户服务接口 / User Service Interface
 *
 * @author xuya
 */
public interface UserService {

    /**
     * 根据LDC UID查找用户 / Find user by LDC UID
     */
    User findByLdcUid(String ldcUid);

    /**
     * 创建或更新用户 / Create or update user
     */
    User createOrUpdateUser(User user);

    /**
     * 获取用户信息 / Get user info
     */
    UserInfoResult getUserInfo(Long id);

    /**
     * 分页查询用户 / Page query users
     */
    IPage<UserInfoResult> pageUsers(int page, int size, String keyword, String role);

    /**
     * 更新用户状态 / Update user status
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 更新用户角色 / Update user role
     */
    void updateUserRole(Long id, String role);
}
