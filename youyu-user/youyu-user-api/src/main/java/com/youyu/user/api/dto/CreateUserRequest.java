package com.youyu.user.api.dto;

import lombok.Data;

/**
 * 创建用户请求DTO（供auth服务调用）
 */
@Data
public class CreateUserRequest {
    /**
     * 用户ID（关联user_identity.id）
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户类型：1-customer, 2-enterprise, 3-platform
     */
    private Integer userType;
}
