package com.youyu.framework.warn.core;

import lombok.Getter;

/**
 * 告警平台类型枚举
 * <p>
 * 支持多种告警平台，便于后续扩展
 */
@Getter
public enum WarnPlatform {
    /** 企业微信 */
    WECHAT("wechat", "企业微信"),
    /** 钉钉（预留扩展） */
    DINGTALK("dingtalk", "钉钉");

    /** 平台代码 */
    private final String code;
    /** 平台名称 */
    private final String name;

    WarnPlatform(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据平台代码获取枚举
     *
     * @param code 平台代码
     * @return 对应的枚举值，未找到返回null
     */
    public static WarnPlatform of(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (WarnPlatform platform : values()) {
            if (platform.code.equalsIgnoreCase(code)) {
                return platform;
            }
        }
        return null;
    }
}
