package com.youyu.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * 使用BCrypt算法进行密码加密和验证
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
        // 防止实例化
    }

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码（明文）
     * @return 加密后的密码
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码（明文）
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return false;
        }
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
