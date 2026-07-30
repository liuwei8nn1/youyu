package com.youyu.auth.domain.model;

import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 用户-角色关联实体（DDD: Auth 领域）
 * 表示授权关系：某个用户被赋予了某个角色
 */
@Getter
@Setter
@Accessors(chain = true)
public class UserRole extends BaseDO {

    /**
     * 用户身份ID（引用 user_identity.id）
     */
    private Long userIdentityId;

    /**
     * 用户业务ID（冗余字段，便于查询）
     */
    private Long userId;

    /**
     * 用户类型: 0-UNKNOWN, 1-CUSTOMER, 2-ENTERPRISE, 3-PLATFORM
     * @see com.youyu.framework.context.UserType
     */
    private Integer userType;

    /**
     * 角色ID（引用 sys_role.id）
     */
    private Long roleId;

    /**
     * 工厂方法：创建用户-角色关联
     */
    public static UserRole create(Long userIdentityId, Long userId, Integer userType, Long roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserIdentityId(userIdentityId);
        userRole.setUserId(userId);
        userRole.setUserType(userType);
        userRole.setRoleId(roleId);
        return userRole;
    }

    /**
     * 验证关联的有效性
     */
    public void validate() {
        if (userIdentityId == null || userIdentityId <= 0) {
            throw new IllegalArgumentException("用户身份ID不能为空");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (userType == null) {
            throw new IllegalArgumentException("用户类型不能为空");
        }
        if (roleId == null || roleId <= 0) {
            throw new IllegalArgumentException("角色ID不能为空");
        }
    }
}
