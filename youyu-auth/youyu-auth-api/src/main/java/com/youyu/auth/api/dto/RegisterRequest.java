package com.youyu.auth.api.dto;

import lombok.Data;

/**
 * 用户注册请求DTO
 */
@Data
public class RegisterRequest {
    /**
     * 用户名（必填）
     */
    private String username;

    /**
     * 密码（必填，明文）
     */
    private String password;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 邮箱（可选）
     */
    private String email;

    /**
     * 用户类型：1-普通用户, 2-商户, 3-管理员
     */
    private Integer userType;

    /**
     * 昵称（可选）
     */
    private String nickname;
}
