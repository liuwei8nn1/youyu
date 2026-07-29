package com.youyu.auth.application.service;

import com.youyu.auth.domain.aggregate.Role;
import com.youyu.auth.domain.entity.UserRole;
import com.youyu.auth.domain.repository.RoleRepository;
import com.youyu.auth.domain.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户-角色管理应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleApplicationService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    /**
     * 为用户分配角色
     *
     * @param userIdentityId 用户身份ID
     * @param userId         用户业务ID
     * @param userType       用户类型
     * @param roleId         角色ID
     */
    @Transactional
    public void assignRoleToUser(Long userIdentityId, Long userId, Integer userType, Long roleId) {
        // 1. 验证角色存在且可用
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));
        role.validateActive();

        // 2. 检查是否已分配
        if (userRoleRepository.existsByUserIdentityIdAndRoleId(userIdentityId, roleId)) {
            log.warn("用户 {} 已经拥有角色 {}", userIdentityId, roleId);
            return;
        }

        // 3. 创建用户-角色关联
        UserRole userRole = UserRole.create(userIdentityId, userId, userType, roleId);
        userRoleRepository.save(userRole);

        log.info("为用户 {} 分配角色 {} 成功", userIdentityId, roleId);
    }

    /**
     * 批量为用户分配角色
     *
     * @param userIdentityId 用户身份ID
     * @param userId         用户业务ID
     * @param userType       用户类型
     * @param roleIds        角色ID列表
     */
    @Transactional
    public void assignRolesToUser(Long userIdentityId, Long userId, Integer userType, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        // 验证所有角色存在且可用
        for (Long roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));
            role.validateActive();
        }

        // 过滤掉已存在的关联
        List<Long> newRoleIds = roleIds.stream()
                .filter(roleId -> !userRoleRepository.existsByUserIdentityIdAndRoleId(userIdentityId, roleId))
                .collect(Collectors.toList());

        if (newRoleIds.isEmpty()) {
            log.warn("用户 {} 的所有角色都已分配", userIdentityId);
            return;
        }

        // 批量创建关联
        List<UserRole> userRoles = newRoleIds.stream()
                .map(roleId -> UserRole.create(userIdentityId, userId, userType, roleId))
                .collect(Collectors.toList());

        userRoleRepository.batchSave(userRoles);

        log.info("为用户 {} 批量分配 {} 个角色成功", userIdentityId, newRoleIds.size());
    }

    /**
     * 撤销用户的角色
     *
     * @param userIdentityId 用户身份ID
     * @param roleId         角色ID
     */
    @Transactional
    public void revokeRoleFromUser(Long userIdentityId, Long roleId) {
        userRoleRepository.deleteByUserIdentityIdAndRoleId(userIdentityId, roleId);
        log.info("撤销用户 {} 的角色 {} 成功", userIdentityId, roleId);
    }

    /**
     * 撤销用户的所有角色
     *
     * @param userIdentityId 用户身份ID
     */
    @Transactional
    public void revokeAllRolesFromUser(Long userIdentityId) {
        userRoleRepository.deleteByUserIdentityId(userIdentityId);
        log.info("撤销用户 {} 的所有角色成功", userIdentityId);
    }

    /**
     * 查询用户的角色ID列表
     *
     * @param userIdentityId 用户身份ID
     * @return 角色ID列表
     */
    public List<Long> getUserRoleIds(Long userIdentityId) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdentityId(userIdentityId);
        return userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户ID和用户类型查询角色ID列表
     *
     * @param userId   用户业务ID
     * @param userType 用户类型
     * @return 角色ID列表
     */
    public List<Long> getUserRoleIdsByUserIdAndType(Long userId, Integer userType) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndType(userId, userType);
        return userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
    }

    /**
     * 查询拥有指定角色的用户身份ID列表
     *
     * @param roleId 角色ID
     * @return 用户身份ID列表
     */
    public List<Long> getUsersByRoleId(Long roleId) {
        List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
        return userRoles.stream()
                .map(UserRole::getUserIdentityId)
                .collect(Collectors.toList());
    }

    /**
     * 统计角色的用户数量
     *
     * @param roleId 角色ID
     * @return 用户数量
     */
    public int countUsersByRoleId(Long roleId) {
        return userRoleRepository.countByRoleId(roleId);
    }

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userIdentityId 用户身份ID
     * @param roleId         角色ID
     * @return true-拥有，false-不拥有
     */
    public boolean hasRole(Long userIdentityId, Long roleId) {
        return userRoleRepository.existsByUserIdentityIdAndRoleId(userIdentityId, roleId);
    }
}
