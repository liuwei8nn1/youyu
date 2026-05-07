package com.youyu.auth.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新Token响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {
    
    private String accessToken;   // 新的 Access Token
    private String refreshToken;  // 新的 Refresh Token（可选，滚动刷新时返回）
}
