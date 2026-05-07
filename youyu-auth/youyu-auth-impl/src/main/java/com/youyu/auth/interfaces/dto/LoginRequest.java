package com.youyu.auth.interfaces.dto;

import com.youyu.framework.context.UserType;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {

    /**
     * 设备唯一id
     */
    private String deviceUniqueId;
    
    /**
     * 登录类型：USERNAME / PHONE / EMAIL
     */
    private String loginType;
    
    /**
     * 凭证（用户名/手机号/邮箱）
     */
    private String credential;
    
    /**
     * 密码（仅用户名登录需要）
     */
    private String password;
    
    /**
     * 验证码（仅手机/邮箱登录需要）
     */
    private String verifyCode;
    
    /**
     * 用户类型：1-user, 2-merchant
     * @see UserType
     */
    private Integer userType;
}
