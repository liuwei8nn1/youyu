package com.youyu.auth.domain.repository;

import com.youyu.auth.domain.entity.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * 用户-角色关联仓储接口
 */
public interface UserRoleRepository {

    /**
     * 保存用户-角色关联
     */
    void save(UserRole userRole);

    /**
     * 批量保存用户-角色关联
     */
    void batchSave(List<UserRole> userRoles);

    /**
     * 根据ID查询
     */
    Optional<UserRole> findById(Long id);

    /**
     * 根据用户身份ID查询角色列表
     */
    List<UserRole> findByUserIdentityId(Long userIdentityId);

    /**
     * 根据用户ID和用户类型查询角色列表
     */
    List<UserRole> findByUserIdAndType(Long userId, Integer userType);

    /**
     * 根据角色ID查询用户列表
     */
    List<UserRole> findByRoleId(Long roleId);

    /**
     * 删除用户-角色关联
     */
    void deleteById(Long id);

    /**
     * 根据用户身份ID和角色ID删除
     */
    void deleteByUserIdentityIdAndRoleId(Long userIdentityId, Long roleId);

    /**
     * 根据用户身份ID删除所有关联
     */
    void deleteByUserIdentityId(Long userIdentityId);

    /**
     * 根据角色ID删除所有关联
     */
    void deleteByRoleId(Long roleId);

    /**
     * 检查用户是否拥有指定角色
     */
    boolean existsByUserIdentityIdAndRoleId(Long userIdentityId, Long roleId);

    /**
     * 统计角色的用户数量
     */
    int countByRoleId(Long roleId);
}
