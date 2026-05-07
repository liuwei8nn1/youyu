package com.youyu.ai.sdk.autoconfigure;

import com.youyu.ai.api.client.AiFeignClient;
import com.youyu.ai.sdk.AiServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI Service SDK 自动配置类
 * <p>
 * 只有当以下条件都满足时才生效：
 * 1. AiFeignClient 被注入容器（说明引入了 ai-service-api）
 * 2. Caffeine 库存在（用于本地缓存）
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Configuration
@ConditionalOnBean(AiFeignClient.class)
public class AiServiceSdkAutoConfiguration {

    /**
     * 注册 AI 服务客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public AiServiceClient aiServiceClient(AiFeignClient aiFeignClient) {
        return new AiServiceClient(aiFeignClient);
    }
}
