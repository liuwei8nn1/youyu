package com.youyu.auth.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.youyu.auth.api.model.MenuType;
import com.youyu.framework.context.UserType;
import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 菜单实体
 */
@Getter
@Setter
@Accessors(chain = true)
public class Menu extends BaseDO {

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 关联的权限编码
     */
    private String permissionCode;

    /**
     * 菜单类型: 1-目录, 2-菜单, 3-按钮
     * @see MenuType
     */
    private Integer type;

    /**
     * 目标用户类型
     * @see UserType
     */
    private Integer targetUserType;

    /**
     * 是否可见: 0-隐藏, 1-显示
     */
    private Integer visible;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 重定向路径
     */
    private String redirect;

    /**
     * 子菜单列表(用于树形结构)
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Menu> children = new ArrayList<>();

    /**
     * 是否为菜单类型
     */
    public boolean isMenu() {
        return type != null && type.equals(MenuType.MENU.getValue());
    }

    /**
     * 是否为按钮类型
     */
    public boolean isButton() {
        return type != null && type.equals(MenuType.BUTTON.getValue());
    }

    /**
     * 是否为目录类型
     */
    public boolean isDirectory() {
        return type != null && type.equals(MenuType.DIRECTORY.getValue());
    }

    /**
     * 隐藏菜单
     */
    public void hide() {
        this.visible = 0;
    }

    /**
     * 显示菜单
     */
    public void show() {
        this.visible = 1;
    }

    /**
     * 禁用菜单
     */
    public void disable() {
        this.status = 0;
    }

    /**
     * 启用菜单
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 添加子菜单
     */
    public void addChild(Menu child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }

    /**
     * 获取子菜单列表(返回不可变列表)
     */
    public List<Menu> getChildren() {
        return children != null ? List.copyOf(children) : List.of();
    }

    /**
     * 设置子菜单列表
     */
    public void setChildren(List<Menu> children) {
        this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
    }
}
