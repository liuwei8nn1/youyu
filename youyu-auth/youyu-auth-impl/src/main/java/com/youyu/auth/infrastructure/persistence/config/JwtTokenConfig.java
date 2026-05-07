package com.youyu.auth.infrastructure.persistence.config;

import com.youyu.auth.api.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author LiuWei
 * @since 2026/4/22
 */
@Configuration
public class JwtTokenConfig {

	@Value("${jwt.secret}")
	String secret;
	/**
	 *  Access Token 有效期（秒）
	 *  默认有效期为 15 分钟，单位秒数
	 */
	@Value("${jwt.access-token-ttl}")
	Long accessTokenTtl = 60 * 15L;
	/**
	 * Refresh Token 有效期（秒）
	 *  默认有效期为 15 天，单位秒数
	 */
	@Value("${jwt.refresh-token-ttl}")
	Long refreshTokenTtl = 60 * 60 * 24 * 15L;

	@Bean
	public JwtTokenProvider jwtTokenProvider(){
		return new JwtTokenProvider(this.secret,this.accessTokenTtl,this.refreshTokenTtl);
	}

}
