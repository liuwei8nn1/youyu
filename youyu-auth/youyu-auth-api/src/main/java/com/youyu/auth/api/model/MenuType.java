package com.youyu.auth.api.model;

import lombok.Getter;

/**
 * 菜单类型枚举
 * <ul>
 *  <li> {@link #DIRECTORY}  1: 目录
 *  <li> {@link #MENU}  2: 菜单
 *  <li> {@link #BUTTON}  3: 按钮
 * </ul>
 */
@Getter
public enum MenuType {
    
    /**
     * 目录
     */
    DIRECTORY(1, "目录"),
    
    /**
     * 菜单
     */
    MENU(2, "菜单"),
    
    /**
     * 按钮
     */
    BUTTON(3, "按钮");
    
    private final Integer value;
    private final String description;
    
    MenuType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * 根据值获取枚举
     * @param value 值
     * @return 菜单类型枚举
     */
    public static MenuType of(Integer value) {
        if (value == null) {
            return null;
        }
        for (MenuType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的菜单类型值: " + value);
    }
}
