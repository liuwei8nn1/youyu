package com.youyu.auth.infrastructure.persistence.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录模式配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.login-mode")
public class LoginModeConfig {

    /**
     * 全局登录模式：MULTI / SINGLE / MAX:n (如 MAX:3)
     */
    private String global = "MULTI";
    
    /**
     * 解析登录模式
     *
     * @return 登录模式枚举
     */
    public LoginMode parseGlobalMode() {
        return LoginMode.parse(global);
    }

    /**
     * 登录模式枚举
     */

    @Getter
    public enum LoginMode {
        MULTI,      // 无限制多端登录
        SINGLE,     // 唯一登录，新登录踢旧登录
        MAX;        // 最多同时登录 n 端

        @Setter
        private int maxDevices;

        LoginMode() {
            this.maxDevices = Integer.MAX_VALUE;
        }

	    /**
         * 解析登录模式字符串
         *
         * @param modeStr 模式字符串（MULTI / SINGLE / MAX:3）
         * @return 登录模式枚举
         */
        public static LoginMode parse(String modeStr) {
            if (modeStr == null || modeStr.trim().isEmpty()) {
                return MULTI;
            }

            String upper = modeStr.toUpperCase().trim();

            if ("MULTI".equals(upper)) {
                return MULTI;
            } else if ("SINGLE".equals(upper)) {
                return SINGLE;
            } else if (upper.startsWith("MAX:")) {
                try {
                    int max = Integer.parseInt(upper.substring(4));
                    if (max <= 0) {
                        throw new IllegalArgumentException("MAX 模式的设备数量必须大于0");
                    }
                    LoginMode mode = MAX;
                    mode.maxDevices = max;
                    return mode;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("无效的 MAX 模式配置: " + modeStr);
                }
            } else {
                throw new IllegalArgumentException("无效的登录模式: " + modeStr);
            }
        }
    }
}
