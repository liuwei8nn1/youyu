package com.youyu.gateway.config;

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
	@Value("${jwt.access-token-ttl:900}")
	Long accessTokenTtl;
	@Value("${jwt.refresh-token-ttl:2592000}")
	Long refreshTokenTtl;

	@Bean
	public JwtTokenProvider jwtTokenProvider(){
		return new JwtTokenProvider(this.secret,this.accessTokenTtl,this.refreshTokenTtl);
	}

}
