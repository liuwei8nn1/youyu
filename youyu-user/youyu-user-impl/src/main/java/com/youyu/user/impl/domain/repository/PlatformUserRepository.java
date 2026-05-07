package com.youyu.user.impl.domain.repository;

import com.youyu.user.impl.domain.model.PlatformUser;

import java.util.Optional;

/**
 * 平台管理员资料仓储接口
 */
public interface PlatformUserRepository {
    /**
     * 保存平台管理员资料
     */
    Long save(PlatformUser platformUser);

    /**
     * 根据identityId查询
     */
    Optional<PlatformUser> findByIdentityId(Long userId);

    /**
     * 根据用户名查询
     */
    Optional<PlatformUser> findByUsername(String username);

    /**
     * 根据手机号查询
     */
    Optional<PlatformUser> findByPhone(String phone);

    /**
     * 根据邮箱查询
     */
    Optional<PlatformUser> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
