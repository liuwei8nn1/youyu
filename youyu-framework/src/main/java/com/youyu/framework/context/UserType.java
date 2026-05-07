package com.youyu.framework.context;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户类型枚举
 *
 * <p>用于区分不同业务场景的用户类型：
 * <ul>
 *   <li>{@link #UNKNOWN}  0: 未知类型，通常用于未登录或异常情况</li>
 *   <li>{@link #CUSTOMER}  1: C端顾客，用于商城、秒杀等面向消费者的业务</li>
 *   <li>{@link #ENTERPRISE}  2: B端企业员工，用于企业后台管理系统</li>
 *   <li>{@link #PLATFORM}  3: 平台管理员，用于平台运营管理</li>
 * </ul>
 *
 * <p><b>注意：</b>
 * <ul>
 *   <li>新增用户类型时，需要在数据库 {@code sys_role} 表的 {@code user_type} 字段同步添加</li>
 *   <li>菜单权限通过 {@code target_user_type} 字段控制可见范围</li>
 * </ul>
 */
@Getter
public enum UserType {
    /**
     * 未知用户类型
     * <p>value: 0
     * <p>通常用于未登录或异常情况，不建议在业务中使用
     */
    UNKNOWN("unknown", "未知用户类型"),
    
    /**
     * 外部顾客
     * <p>value: 1
     * <p>适用场景：商城购物、参与秒杀等C端业务
     */
    CUSTOMER("customer", "外部顾客"),

    /**
     * 企业员工
     * <p>value: 2
     * <p>适用场景：企业后台管理、订单处理等B端业务
     */
    ENTERPRISE("enterprise", "企业员工"),

    /**
     * 平台管理员
     * <p>value: 3
     * <p>适用场景：平台运营管理、系统配置等
     */
    PLATFORM("platform", "平台管理员");

    @EnumValue
    private final Integer value;
    private final String code;
    private final String description;

    UserType(String code, String description) {
        this.value = ordinal();
        this.code = code;
        this.description = description;
    }

    public final static UserType[] CACHE = values();

    public static UserType fromCode(String code) {
        for (UserType type : CACHE) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的用户类型: " + code);
    }

    public static UserType of(Integer value) {
        if (value == null || value < 0 || value >= CACHE.length) {
            return UserType.UNKNOWN;
        }
        return CACHE[value];
    }

}
