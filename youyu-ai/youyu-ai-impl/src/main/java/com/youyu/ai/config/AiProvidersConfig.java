package com.youyu.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 提供商配置
 * <p>
 * 从配置文件读取多个 AI 提供商的配置信息
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.providers")
public class AiProvidersConfig {
    
    /**
     * 多个 AI 提供商配置
     * key: 提供商名称（如 siliconflow, zhipu, dashscope）
     * value: 提供商配置
     */
    private Map<String, ProviderConfig> providers = new HashMap<>();
    
    @Data
    public static class ProviderConfig {
        /**
         * API Key
         */
        private String apiKey;
        
        /**
         * Base URL（OpenAI 兼容接口需要）
         */
        private String baseUrl;
        
        /**
         * 模型名称
         */
        private String model;
        
        /**
         * 类型：openai 或 dashscope
         */
        private String type = "openai";
    }
}
