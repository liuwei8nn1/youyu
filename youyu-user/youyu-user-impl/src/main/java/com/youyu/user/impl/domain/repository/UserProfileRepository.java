package com.youyu.user.impl.domain.repository;

import com.youyu.user.impl.domain.model.UserProfile;

import java.util.Optional;

/**
 * 用户资料仓储接口
 */
public interface UserProfileRepository {

    /**
     * 保存用户资料
     */
    void save(UserProfile userProfile);

    /**
     * 根据用户身份ID查询
     */
    Optional<UserProfile> findByIdentityId(Long identityId);

    /**
     * 根据ID查询(别名方法,与findByIdentityId相同)
     */
    default Optional<UserProfile> findById(Long identityId) {
        return findByIdentityId(identityId);
    }

    /**
     * 根据用户名查询
     */
    Optional<UserProfile> findByUsername(String username);

    /**
     * 根据手机号查询
     */
    Optional<UserProfile> findByPhone(String phone);

    /**
     * 根据邮箱查询
     */
    Optional<UserProfile> findByEmail(String email);

    /**
     * 更新用户资料
     */
    void update(UserProfile userProfile);
}
