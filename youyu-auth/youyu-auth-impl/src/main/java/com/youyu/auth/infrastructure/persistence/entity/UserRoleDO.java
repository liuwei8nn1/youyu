package com.youyu.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 用户-角色关联表 DO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_role")
public class UserRoleDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     */
    private Integer userType;

    /**
     * 角色ID（引用 sys_role.id）
     */
    private Long roleId;

    // ==================== 字段常量 ====================

    public static final String USER_IDENTITY_ID = "user_identity_id";
    public static final String USER_ID = "user_id";
    public static final String USER_TYPE = "user_type";
    public static final String ROLE_ID = "role_id";
}
