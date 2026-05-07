package com.youyu.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成BCrypt加密密码工具类
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "Admin123";
        String hashed = encoder.encode(password);
        
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希: " + hashed);
        // System.out.println("\nSQL插入语句:");
        // System.out.println("INSERT INTO `user_identity` (`user_id`, `username`, `password`, `user_type`, `enabled`) VALUES");
        // System.out.println("(1, 'platform_admin', '" + hashed + "', 3, 1),");
        // System.out.println("(2, 'enterprise_admin', '" + hashed + "', 2, 1),");
        // System.out.println("(3, 'customer01', '" + hashed + "', 1, 1);");
    }
}
