package com.youyu.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.context.UserType;
import com.youyu.framework.datasource.mybatis.LogicDeleteBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class RoleDO extends LogicDeleteBaseDO {

    private String roleCode;
    private String roleName;
    private String description;
    /**
     * 用户类型
     * @see UserType
     */
    private Integer userType;
    private Integer status;
    private Integer sortOrder;

    // ==================== 字段常量定义 ====================
    public static final String ROLE_CODE = "role_code";
    public static final String ROLE_NAME = "role_name";
    public static final String DESCRIPTION = "description";
    public static final String USER_TYPE = "user_type";
    public static final String STATUS = "status";
    public static final String SORT_ORDER = "sort_order";
}
