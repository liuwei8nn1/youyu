package com.youyu.ai.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 客户端配置 - 多模型支持
 * <p>
 * 使用 Spring AI 的 OpenAiApi 手动创建多个模型提供商的 ChatClient
 * 支持：硅基流动、智谱 GLM、DeepSeek、Moonshot 等 OpenAI 兼容接口
 * <p>
 * 注意：为了支持 Tool Calling、Retry、Observation 等高级功能，
 * 需要注入 Spring AI 自动配置的组件
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiClientConfig {
    
    private final AiProvidersConfig providersConfig;
    
    /**
     * 工具调用管理器（由 Spring AI 自动配置提供）
     */
    private final ToolCallingManager toolCallingManager;
    
    /**
     * 重试模板（由 Spring AI 自动配置提供）
     */
    private final RetryTemplate retryTemplate;
    
    /**
     * 监控注册表（由 Spring Boot Actuator 提供）
     */
    private final ObservationRegistry observationRegistry;
    
    /**
     * 创建所有 AI 提供商的 ChatClient
     * <p>
     * 返回一个 Map，key 为提供商名称，value 为对应的 ChatClient
     * 业务层可以通过注入 Map 来动态选择不同提供商
     *
     * @return Map<String, ChatClient> 提供商名称 -> ChatClient 映射
     */
    @Bean
    public Map<String, ChatClient> chatClients() {
        log.info("开始初始化 AI 客户端配置");
        log.info("配置的提供商数量: {}", providersConfig.getProviders().size());
        
        Map<String, ChatClient> clients = new HashMap<>();
        
        providersConfig.getProviders().forEach((name, provider) -> {
            try {
                log.info("初始化 AI 提供商: {}, 类型: {}, 模型: {}", 
                    name, provider.getType(), provider.getModel());
                
                ChatClient client = createClient(provider);
                clients.put(name, client);
                
                log.info("✓ AI 提供商 [{}] 初始化成功", name);
            } catch (Exception e) {
                log.error("✗ AI 提供商 [{}] 初始化失败: {}", name, e.getMessage());
            }
        });
        
        log.info("AI 客户端配置初始化完成，共 {} 个可用提供商", clients.size());
        return clients;
    }
    
    /**
     * 根据配置创建 ChatClient
     *
     * @param provider 提供商配置
     * @return ChatClient 实例
     */
    private ChatClient createClient(AiProvidersConfig.ProviderConfig provider) {
        if ("dashscope".equalsIgnoreCase(provider.getType())) {
            // 通义千问（DashScope）
            return createDashScopeClient(provider);
        } else {
            // 创建 OpenAI 兼容客户端（硅基流动、智谱、DeepSeek 等）
            return createOpenAiCompatibleClient(provider);
        }
    }
    
    /**
     * 创建通义千问（DashScope）客户端
     * <p>
     * 使用 Spring AI Alibaba 的 DashScope API
     * <p>
     * 支持的模型：
     * - qwen-max（最强能力）
     * - qwen-plus（平衡性能和成本）
     * - qwen-turbo（快速响应）
     * - qwen-long（长文本处理）
     *
     * @param provider 提供商配置
     * @return ChatClient 实例
     */
    private ChatClient createDashScopeClient(AiProvidersConfig.ProviderConfig provider) {
        // 1. 创建 DashScopeApi
        DashScopeApi dashScopeApi = DashScopeApi.builder()
            .apiKey(provider.getApiKey())
            .build();
        
        // 2. 创建 DashScopeChatModel（使用 Builder 模式，注入必要组件）
        DashScopeChatModel chatModel = DashScopeChatModel.builder()
            .dashScopeApi(dashScopeApi)
            .defaultOptions(com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions.builder()
                .model(provider.getModel())
                .temperature(0.7)
                .build())
            .toolCallingManager(toolCallingManager)              // ✅ 支持 Tool Calling
            .retryTemplate(retryTemplate)                        // ✅ 支持自动重试
            .observationRegistry(observationRegistry)            // ✅ 支持监控埋点
            .build();
        
        // 3. 创建 ChatClient
        return ChatClient.create(chatModel);
    }
    
    /**
     * 创建 OpenAI 兼容客户端
     * <p>
     * 使用 OpenAiApi 构建 API 客户端，支持自定义 base-url 和 api-key
     * <p>
     * 支持的提供商：
     * - 硅基流动 (SiliconFlow): https://api.siliconflow.cn/v1
     * - 智谱 GLM (Zhipu): https://open.bigmodel.cn/api/paas/v4
     * - DeepSeek: https://api.deepseek.com/v1
     * - Moonshot (Kimi): https://api.moonshot.cn/v1
     *
     * @param provider 提供商配置
     * @return ChatClient 实例
     */
    private ChatClient createOpenAiCompatibleClient(AiProvidersConfig.ProviderConfig provider) {
        // 1. 使用 Builder 模式创建 OpenAiApi
        OpenAiApi openAiApi = OpenAiApi.builder()
            .baseUrl(provider.getBaseUrl())
            .apiKey(provider.getApiKey())
            .build();
        
        // 2. 创建 ChatOptions（设置模型参数）
        OpenAiChatOptions options = OpenAiChatOptions.builder()
            .model(provider.getModel())
            .temperature(0.7)  // 默认温度值
            .build();
        
        // 3. 使用 Builder 模式创建 OpenAiChatModel（参考 Spring AI 源码，注入所有必要组件）
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(options)
            .toolCallingManager(toolCallingManager)              // ✅ 支持 Tool Calling
            .retryTemplate(retryTemplate)                        // ✅ 支持自动重试
            .observationRegistry(observationRegistry)            // ✅ 支持监控埋点
            .build();
        
        // 4. 创建 ChatClient
        return ChatClient.create(chatModel);
    }
}
