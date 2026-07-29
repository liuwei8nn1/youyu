package com.youyu.auth.domain.aggregate;

import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 角色聚合根
 */
@Getter
@Setter
@Accessors(chain = true)
public class Role extends BaseDO {

    /**
     * 角色编码(唯一)
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 用户类型: 0-UNKNOWN, 1-CUSTOMER, 2-ENTERPRISE, 3-PLATFORM
     * @see com.youyu.common.enums.UserType
     */
    private Integer userType;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 禁用角色
     */
    public void disable() {
        this.status = 0;
    }

    /**
     * 启用角色
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 更新角色信息
     */
    public void updateInfo(String roleName, String description, Integer sortOrder) {
        this.roleName = roleName;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    /**
     * 更新角色信息（包含userType）
     */
    public void updateInfo(String roleName, String description, Integer userType, Integer sortOrder) {
        this.roleName = roleName;
        this.description = description;
        if (userType != null) {
            this.userType = userType;
        }
        this.sortOrder = sortOrder;
    }

    /**
     * 验证角色是否可用
     */
    public void validateActive() {
        if (this.status == null || this.status != 1) {
            throw new IllegalStateException("角色已禁用，无法进行操作");
        }
    }
}
