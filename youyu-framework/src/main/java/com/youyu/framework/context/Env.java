package com.youyu.framework.context;

import lombok.Getter;
import org.apache.commons.lang3.*;
import org.jspecify.annotations.Nullable;

/**
 * 系统运行环境的区分枚举
 * <p>
 * 职责：
 * 1. 定义系统部署环境（本地、开发、测试、预发、生产）
 * 2. 提供环境标识字符（用于订单号前缀）
 * 3. 根据 Spring Profile 自动检测当前环境
 * 4. 提供便捷的环境判断方法
 */
@Getter
public enum Env {
    /** 生产环境 */
    PROD("prod", "生产环境", 'R'),
    /** 预发布环境 */
    UAT("uat", "预发布环境", 'P'),
    /** 测试环境 */
    TEST("test", "测试环境", 'T'),
    /** 开发环境（共用开发环境） */
    DEV("dev", "开发环境", 'D'),
    /** 本地环境（本地开发环境） */
    LOCAL("local", "本地环境", 'L');

    /** 环境标识字符串（对应 Spring Profile） */
    public final String value;
    
    /** 环境描述 */
    public final String label;
    
    /** 环境标识字符（用于订单号前缀，1个字符） */
    public final char code;

    Env(String value, String label, char code) {
        this.value = value;
        this.label = label;
        this.code = code;
    }

    /**
     * 根据环境标识字符串获取枚举
     *
     * @param value 环境标识字符串
     * @return 对应的枚举值，未找到返回 null
     */
    public static Env of(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (Env env : values()) {
            if (env.value.equalsIgnoreCase(value)) {
                return env;
            }
        }
        return null;
    }

    /** 当前的环境类型（默认为生产环境） */
    public static Env CURRENT = PROD;

    /** 当前应用名（从 spring.application.name 读取）
     * -- GETTER --
     *  获取应用名
     *
     * @return 应用名
     */
    @Getter
    private static String appName = "unknown-app";

    /**
     * 是否在本地环境
     */
    public static boolean inLocal() {
        return CURRENT == LOCAL;
    }

    /**
     * 是否在开发环境
     */
    public static boolean inDev() {
        return CURRENT == DEV;
    }

    /**
     * 是否在测试环境
     */
    public static boolean inTest() {
        return CURRENT == TEST;
    }

    /**
     * 是否在预发布环境
     */
    public static boolean inUat() {
        return CURRENT == UAT;
    }

    /**
     * 是否在生产环境
     */
    public static boolean inProduction() {
        return CURRENT == PROD;
    }

    /**
     * 是否在企业内部环境（非UAT、非正式环境）
     */
    public static boolean inner() {
        return !outer();
    }

    /**
     * 是否在企业外部环境（UAT、正式环境）
     */
    public static boolean outer() {
        return CURRENT == PROD || CURRENT == UAT;
    }

    /**
     * 基于指定的环境 profile 配置初始化 Env
     *
     * @param activeProfiles Spring 激活的 profiles
     * @return 当前环境
     */
    public static Env init(@Nullable final String[] activeProfiles) {
        Env current = CURRENT;
        if (ArrayUtils.isNotEmpty(activeProfiles)) {
            for (Env env : values()) {
                if (Strings.CI.containsAny(env.value, activeProfiles)) {
                    CURRENT = current = env;
                    break;
                }
            }
        }
        System.err.println("自动检测到的当前环境为：" + current.getLabel() + " (" + current.getValue() + ")");
        return current;
    }

    /**
     * 获取当前环境的标识字符
     *
     * @return 环境标识字符（如：'R' 表示生产环境）
     */
    public static char getCurrentEnvCode() {
        return CURRENT.getCode();
    }

    /**
     * 设置应用名（由 EnvironmentListener 调用）
     *
     * @param name 应用名
     */
    public static void setAppName(String name) {
        appName = name != null ? name : "unknown-app";
    }

}
