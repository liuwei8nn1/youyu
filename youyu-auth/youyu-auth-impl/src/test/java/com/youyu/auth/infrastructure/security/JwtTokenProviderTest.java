package com.youyu.auth.infrastructure.security;

import com.youyu.auth.api.JwtTokenProvider;
import org.junit.Assert;
import org.junit.Test;

public class JwtTokenProviderTest {

	@Test
	public void testGenerateToken() throws InterruptedException {
		// 测试生成 JWT 逻辑
		// JWT HMAC-SHA 算法要求密钥至少 256 位(32字节)
		String secret = "this-is-a-secure-jwt-secret-key-for-testing-purpose"; // 48字节,满足要求
		JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(secret, 60 * 60 * 24 * 30, 60 * 60 * 24 * 15 );
		// JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(secret, 1, 10 );
		String accessToken = jwtTokenProvider.generateAccessToken(1L, null, "admin", 1, "ROLE_ADMIN", 123456L);
		// eyJhbGciOiJIUzM4NCJ9.eyJyb2xlcyI6IlJPTEVfQURNSU4iLCJ1c2VyVHlwZSI6MSwidXNlck5hbWUiOiJhZG1pbiIsInVzZXJJZCI6MSwiZGV2aWNlSWQiOiIxMjM0NTYiLCJpYXQiOjE3NzY4NTM3ODAsImV4cCI6MTc3OTQ0NTc4MH0.wSyLO9OipycZP7OxDtnDUvf1-2nhObeHUWvdDCg5PZ8v5iI6VAQeCEgMavF-VDvE
		// Thread.sleep(1500);
		jwtTokenProvider.validateToken(accessToken);
		System.out.println(accessToken);
		Assert.assertNotNull(accessToken);
	}

}