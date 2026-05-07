package com.youyu.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;
}
