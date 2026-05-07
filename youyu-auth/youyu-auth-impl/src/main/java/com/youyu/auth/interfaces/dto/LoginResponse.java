package com.youyu.auth.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String accessToken;   // Access Token
    private String refreshToken;  // Refresh Token
    private Long userId;
    private String username;
    private Integer userType;
    private List<String> roles;
}
