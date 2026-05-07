package com.youyu.auth.sdk.autoconfigure;

import com.youyu.auth.api.client.AuthFeignClient;
import com.youyu.auth.sdk.AuthServiceClient;
import com.youyu.auth.sdk.aspect.PermissionCheckAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auth Service SDK 自动配置类
 * <p>
 * 只有当以下条件都满足时才生效：
 * 1. AuthFeignClient 被注入容器（说明引入了 auth-service-api）
 * 2. Caffeine 库存在（用于本地缓存）
 * 3. OpenFeign 库存在（用于 Feign 客户端）
 */
@Configuration
@ConditionalOnBean(com.youyu.auth.api.client.AuthFeignClient.class)  // 只有 AuthFeignClient 存在时才生效
public class AuthServiceSdkAutoConfiguration {

    /**
     * 注册权限检查切面
     */
    @Bean
    @ConditionalOnMissingBean
    public PermissionCheckAspect permissionCheckAspect(AuthServiceClient authServiceClient) {
        return new PermissionCheckAspect(authServiceClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthServiceClient authServiceClient(AuthFeignClient authFeignClient) {
        return new AuthServiceClient(authFeignClient);
    }
}
