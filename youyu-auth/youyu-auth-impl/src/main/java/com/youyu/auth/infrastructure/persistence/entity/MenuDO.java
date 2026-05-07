package com.youyu.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.context.UserType;
import com.youyu.framework.datasource.mybatis.LogicDeleteBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class MenuDO extends LogicDeleteBaseDO {

    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private String permissionCode;
    /**
     * 菜单类型: 1-目录, 2-菜单, 3-按钮
     * @see com.youyu.auth.api.model.MenuType
     */
    private Integer type;
    /**
     * 目标用户类型
     * @see com.youyu.framework.context.UserType
     */
    private Integer targetUserType;
    private Integer visible;
    private Integer status;
    private Integer sortOrder;
    private String redirect;

    // ==================== 字段常量定义 ====================
    public static final String PARENT_ID = "parent_id";
    public static final String NAME = "name";
    public static final String PATH = "path";
    public static final String COMPONENT = "component";
    public static final String ICON = "icon";
    public static final String PERMISSION_CODE = "permission_code";
    public static final String TYPE = "type";
    public static final String TARGET_USER_TYPE = "target_user_type";
    public static final String VISIBLE = "visible";
    public static final String STATUS = "status";
    public static final String SORT_ORDER = "sort_order";
    public static final String REDIRECT = "redirect";
}
