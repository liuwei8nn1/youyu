package com.youyu.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建用户身份响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserIdentityResponse {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;
}
