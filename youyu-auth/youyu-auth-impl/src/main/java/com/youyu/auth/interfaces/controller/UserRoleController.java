package com.youyu.auth.interfaces.controller;

import com.youyu.auth.application.service.UserRoleApplicationService;
import com.youyu.auth.domain.repository.UserIdentityRepository;
import com.youyu.common.model.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户-角色管理控制器（Auth 领域）
 * <p>
 * 职责：管理用户与角色的授权关系
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/user-role")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleApplicationService userRoleApplicationService;
    private final UserIdentityRepository userIdentityRepository;  // 新增：查询用户身份

    /**
     * 为用户分配单个角色
     *
     * @param request 分配请求
     * @return 操作结果
     */
    @PostMapping("/assign")
    public Result<Void> assignRole(@RequestBody AssignRoleRequest request) {
        userRoleApplicationService.assignRoleToUser(
                request.getUserIdentityId(),
                request.getUserId(),
                request.getUserType(),
                request.getRoleId()
        );
        return Result.success();
    }

    /**
     * 为用户批量分配角色
     *
     * @param request 批量分配请求
     * @return 操作结果
     */
    @PostMapping("/assignBatch")
    public Result<Void> assignRoles(@RequestBody AssignRolesRequest request) {
        // 如果没有提供 userIdentityId，则根据 userId + userType 查询
        Long userIdentityId = request.getUserIdentityId();
        if (userIdentityId == null) {
            userIdentityId = getUserIdentityId(request.getUserId(), request.getUserType());
        }
        
        userRoleApplicationService.assignRolesToUser(
                userIdentityId,
                request.getUserId(),
                request.getUserType(),
                request.getRoleIds()
        );
        return Result.success();
    }

    /**
     * 撤销用户的指定角色
     *
     * @param request 撤销请求
     * @return 操作结果
     */
    @PostMapping("/revoke")
    public Result<Void> revokeRole(@RequestBody RevokeRoleRequest request) {
        userRoleApplicationService.revokeRoleFromUser(
                request.getUserIdentityId(),
                request.getRoleId()
        );
        return Result.success();
    }

    /**
     * 撤销用户的所有角色
     *
     * @param userIdentityId 用户身份ID
     * @return 操作结果
     */
    @PostMapping("/revokeAll")
    public Result<Void> revokeAllRoles(@RequestParam Long userIdentityId) {
        userRoleApplicationService.revokeAllRolesFromUser(userIdentityId);
        return Result.success();
    }

    /**
     * 查询用户的角色ID列表（通过 userIdentityId）
     *
     * @param userIdentityId 用户身份ID
     * @return 角色ID列表
     */
    @GetMapping("/roles/byIdentity")
    public Result<List<Long>> getUserRolesByIdentity(@RequestParam Long userIdentityId) {
        List<Long> roleIds = userRoleApplicationService.getUserRoleIds(userIdentityId);
        return Result.success(roleIds);
    }

    /**
     * 查询用户的角色ID列表（通过 userId + userType）
     *
     * @param userId   用户业务ID
     * @param userType 用户类型
     * @return 角色ID列表
     */
    @GetMapping("/roles/byUser")
    public Result<List<Long>> getUserRolesByUser(
            @RequestParam Long userId,
            @RequestParam Integer userType
    ) {
        List<Long> roleIds = userRoleApplicationService.getUserRoleIdsByUserIdAndType(userId, userType);
        return Result.success(roleIds);
    }

    /**
     * 查询拥有指定角色的用户列表
     *
     * @param roleId 角色ID
     * @return 用户身份ID列表
     */
    @GetMapping("/users/byRole")
    public Result<List<Long>> getUsersByRole(@RequestParam Long roleId) {
        List<Long> userIdentityIds = userRoleApplicationService.getUsersByRoleId(roleId);
        return Result.success(userIdentityIds);
    }

    /**
     * 统计角色的用户数量
     *
     * @param roleId 角色ID
     * @return 用户数量
     */
    @GetMapping("/count/byRole")
    public Result<Integer> countUsersByRole(@RequestParam Long roleId) {
        int count = userRoleApplicationService.countUsersByRoleId(roleId);
        return Result.success(count);
    }

    /**
     * 检查用户是否拥有指定角色
     *
     * @param userIdentityId 用户身份ID
     * @param roleId         角色ID
     * @return true-拥有，false-不拥有
     */
    @GetMapping("/hasRole")
    public Result<Boolean> hasRole(
            @RequestParam Long userIdentityId,
            @RequestParam Long roleId
    ) {
        boolean hasRole = userRoleApplicationService.hasRole(userIdentityId, roleId);
        return Result.success(hasRole);
    }

    /**
     * 根据 userId + userType 获取 userIdentityId
     */
    private Long getUserIdentityId(Long userId, Integer userType) {
        return userIdentityRepository.findByUserIdAndType(userId, userType)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: userId=" + userId + ", userType=" + userType))
                .getId();
    }

    // ==================== DTO类 ====================

    /**
     * 分配单个角色请求
     */
    @Data
    public static class AssignRoleRequest {
        /**
         * 用户身份ID (user_identity.id)
         */
        private Long userIdentityId;

        /**
         * 用户业务ID
         */
        private Long userId;

        /**
         * 用户类型
         */
        private Integer userType;

        /**
         * 角色ID
         */
        private Long roleId;
    }

    /**
     * 批量分配角色请求
     */
    @Data
    public static class AssignRolesRequest {
        /**
         * 用户身份ID (user_identity.id)
         */
        private Long userIdentityId;

        /**
         * 用户业务ID
         */
        private Long userId;

        /**
         * 用户类型
         */
        private Integer userType;

        /**
         * 角色ID列表
         */
        private List<Long> roleIds;
    }

    /**
     * 撤销角色请求
     */
    @Data
    public static class RevokeRoleRequest {
        /**
         * 用户身份ID (user_identity.id)
         */
        private Long userIdentityId;

        /**
         * 角色ID
         */
        private Long roleId;
    }
}
