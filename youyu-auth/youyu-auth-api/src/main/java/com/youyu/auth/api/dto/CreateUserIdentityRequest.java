package com.youyu.auth.api.dto;

import lombok.Data;

/**
 * 创建用户身份请求DTO（内部服务调用）
 */
@Data
public class CreateUserIdentityRequest {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（明文）
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户类型: 1-CUSTOMER, 2-ENTERPRISE, 3-PLATFORM
     */
    private Integer userType;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
