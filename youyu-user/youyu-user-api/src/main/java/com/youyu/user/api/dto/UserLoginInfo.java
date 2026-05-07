package com.youyu.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录信息 DTO
 * 供 auth-service 调用时返回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态：0-禁用, 1-正常, 2-锁定
     */
    private Integer status;
}
