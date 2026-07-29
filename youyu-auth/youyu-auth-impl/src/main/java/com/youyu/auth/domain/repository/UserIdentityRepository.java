package com.youyu.auth.domain.repository;

import com.youyu.auth.domain.aggregate.UserIdentity;

import java.util.Optional;

/**
 * 用户身份仓储接口
 */
public interface UserIdentityRepository {
    
    /**
     * 保存用户身份
     */
    void save(UserIdentity userIdentity);
    
    /**
     * 更新用户身份
     */
    void update(UserIdentity userIdentity);
    
    /**
     * 根据ID查询
     */
    Optional<UserIdentity> findById(Long id);

    /**
     * 根据用户ID和用户类型查询
     */
    Optional<UserIdentity> findByUserIdAndType(Long id, Integer userType);
    
    /**
     * 删除用户身份
     */
    void delete(Long id);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username, Integer userType);

    /**
     * 根据用户名和用户类型查询
     */
    Optional<UserIdentity> findByUsernameAndType(String username, Integer userType);
}
