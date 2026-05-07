package com.youyu.auth.api.model;

import java.util.Set;

import jakarta.annotation.Nullable;
import lombok.Getter;

/**
 * 权限级别枚举
 * <p>
 * 维护每个权限级别允许访问的用户类型集合，用于快速权限判断（空间换时间）
 */
@Getter
public enum PermissionLevel {
    
    /**
     * 无需权限,直接放行
     */
    NONE(0,  Set.of()),
    
    /**
     * 只需登录即可（任意登录用户）
     */
    LOGIN(1, Set.of(com.youyu.framework.context.UserType.CUSTOMER, com.youyu.framework.context.UserType.ENTERPRISE, com.youyu.framework.context.UserType.PLATFORM)),

    /**
     * 顾客权限（仅外部顾客）
     */
    CUSTOMER(2, Set.of(com.youyu.framework.context.UserType.CUSTOMER)),

    /**
     * 员工权限（企业员工 + 平台管理员都可以访问）
     */
    EMP(3, Set.of(com.youyu.framework.context.UserType.ENTERPRISE, com.youyu.framework.context.UserType.PLATFORM)),

    /**
     * 企业专属权限（仅企业员工）
     */
    ENTERPRISE(4, Set.of(com.youyu.framework.context.UserType.ENTERPRISE)),

    /**
     * 平台管理员专属权限（仅平台管理员）
     */
    PLATFORM(5, Set.of(com.youyu.framework.context.UserType.PLATFORM));

    private final int level;
    private final Set<com.youyu.framework.context.UserType> allowedUserTypes;
    
    PermissionLevel(int level, Set<com.youyu.framework.context.UserType> allowedUserTypes) {
        this.level = level;
        this.allowedUserTypes = allowedUserTypes;
    }
    
    /**
     * 根据 code 获取枚举
     */
    public static PermissionLevel fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return NONE;
        }
        for (PermissionLevel level : values()) {
            if (level.name().equalsIgnoreCase(code)) {
                return level;
            }
        }
        return NONE;
    }

    /**
     * 检查指定的用户类型是否有此权限级别的访问权
     *
     * @param userType 用户类型
     * @return true-有权限, false-无权限
     */
    public boolean isAllowed(com.youyu.framework.context.UserType userType) {
        if (userType == null) {
            return this == NONE;
        }
        // NONE 所有人都可以
        if (this == NONE) {
            return true;
        }
        return allowedUserTypes.contains(userType);
    }
    
    /**
     * 静态方法：检查用户是否满足任一权限级别要求
     *
     * @param requiredLevels 要求的权限级别集合
     * @param userType 用户类型
     * @return true-满足权限, false-不满足
     */
    public static boolean hasPermission(Set<PermissionLevel> requiredLevels, @Nullable com.youyu.framework.context.UserType userType) {
        // 未登录用户只能访问 NONE
        if (userType == null) {
            return requiredLevels.contains(NONE);
        }
        
        // 遍历所有要求的权限级别，只要满足其中一个即可
        for (PermissionLevel requiredLevel : requiredLevels) {
            if (requiredLevel.isAllowed(userType)) {
                return true;
            }
        }
        return false;
    }
}
