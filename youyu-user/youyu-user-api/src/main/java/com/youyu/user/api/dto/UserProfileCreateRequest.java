package com.youyu.user.api.dto;

import lombok.Data;

/**
 * 用户资料创建请求DTO（供auth服务调用）
 */
@Data
public class UserProfileCreateRequest {
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
     * 用户类型：1-平台用户, 2-企业用户
     */
    private Integer userType;
}
