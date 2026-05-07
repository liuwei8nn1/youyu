package com.youyu.ai.infrastructure.ai;

import com.youyu.ai.api.dto.*;
import com.youyu.ai.domain.service.AiTextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI 文本服务实现 - 多模型支持
 * <p>
 * 支持动态选择不同 AI 提供商（硅基流动、智谱、DeepSeek 等）
 * <p>
 * 使用方式：
 * 1. 默认使用第一个配置的提供商
 * 2. 可以通过 getChatClient(providerName) 指定提供商
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiTextServiceImpl implements AiTextService {

    /**
     * 所有 AI 提供商的 ChatClient 映射
     * key: 提供商名称（如 siliconflow, zhipu, deepseek）
     * value: 对应的 ChatClient 实例
     */
    private final Map<String, ChatClient> chatClients;

    /**
     * 获取默认的 ChatClient（使用第一个可用的提供商）
     */
    private ChatClient getDefaultChatClient() {
        if (chatClients.isEmpty()) {
            throw new IllegalStateException("未配置任何 AI 提供商，请在 application.yml 中配置 ai.providers");
        }
        // 返回第一个可用的客户端
        return chatClients.values().iterator().next();
    }

    @Override
    public AiPolishResponse polishText(AiPolishRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 构建润色提示词
        String prompt = buildPolishPrompt(request);
        
        // 获取默认 ChatClient
        ChatClient chatClient = getDefaultChatClient();
        
        log.debug("调用 AI 模型进行文本润色");
        
        // 使用 ChatClient 进行调用
        String result = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        // 构建响应
        return AiPolishResponse.builder()
            .polishedText(result)
            .modelName("default")
            .tokensUsed(estimateTokens(prompt + result))
            .processingTimeMs(processingTime)
            .build();
    }

    @Override
    public AiGenerateResponse generateText(AiGenerateRequest request) {
        long startTime = System.currentTimeMillis();
        
        // 构建生成提示词
        String prompt = buildGeneratePrompt(request);
        
        // 获取默认 ChatClient
        ChatClient chatClient = getDefaultChatClient();
        
        log.debug("调用 AI 模型进行文本生成");
        
        // 使用 ChatClient 进行调用
        String result = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        // 构建响应
        return AiGenerateResponse.builder()
            .generatedText(result)
            .modelName("default")
            .tokensUsed(estimateTokens(prompt + result))
            .processingTimeMs(processingTime)
            .build();
    }

    /**
     * 根据提供商名称获取 ChatClient
     * <p>
     * 业务层可以调用此方法动态选择模型
     *
     * @param providerName 提供商名称（如 siliconflow, zhipu）
     * @return ChatClient 实例
     */
    public ChatClient getChatClient(String providerName) {
        ChatClient client = chatClients.get(providerName);
        if (client == null) {
            throw new IllegalArgumentException(
                "不支持的 AI 提供商: " + providerName + 
                "，可用提供商: " + chatClients.keySet()
            );
        }
        return client;
    }

    /**
     * 构建润色提示词
     */
    private String buildPolishPrompt(AiPolishRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("请对以下文本进行润色优化。\n\n");
        
        if (request.getStyle() != null && !request.getStyle().isEmpty()) {
            prompt.append("风格要求: ").append(getStyleDescription(request.getStyle())).append("\n");
        }
        
        if (request.getTargetLanguage() != null && !request.getTargetLanguage().isEmpty()) {
            prompt.append("目标语言: ").append(request.getTargetLanguage()).append("\n");
        }
        
        if (request.getMaxLength() != null && request.getMaxLength() > 0) {
            prompt.append("长度限制: 不超过 ").append(request.getMaxLength()).append(" 字\n");
        }
        
        prompt.append("\n原始文本:\n").append(request.getOriginalText());
        prompt.append("\n\n请直接输出润色后的文本，不要添加任何解释。");
        
        return prompt.toString();
    }

    /**
     * 构建生成提示词
     */
    private String buildGeneratePrompt(AiGenerateRequest request) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(request.getPrompt());
        
        if (request.getTopic() != null && !request.getTopic().isEmpty()) {
            prompt.append("\n主题: ").append(request.getTopic());
        }
        
        if (request.getLength() != null && !request.getLength().isEmpty()) {
            prompt.append("\n长度要求: ").append(getLengthDescription(request.getLength()));
        }
        
        return prompt.toString();
    }

    /**
     * 获取风格描述
     */
    private String getStyleDescription(String style) {
        return switch (style.toLowerCase()) {
            case "professional" -> "专业正式，适合商务场合";
            case "casual" -> "轻松随意，口语化表达";
            case "marketing" -> "营销推广风格，有吸引力";
            case "concise" -> "简洁明了，去除冗余";
            default -> "自然流畅";
        };
    }

    /**
     * 获取长度描述
     */
    private String getLengthDescription(String length) {
        return switch (length.toLowerCase()) {
            case "short" -> "简短（100字以内）";
            case "medium" -> "中等长度（100-300字）";
            case "long" -> "详细长篇（300字以上）";
            default -> "中等长度";
        };
    }

    /**
     * 估算 Token 数量（简化版）
     * 实际应该使用 AI 提供商返回的准确数据
     */
    private int estimateTokens(String text) {
        // 中文约 1.5-2 字符 = 1 token
        // 英文约 4 字符 = 1 token
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        int chineseChars = 0;
        int otherChars = 0;
        
        for (char c : text.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                chineseChars++;
            } else {
                otherChars++;
            }
        }
        
        return (int) Math.ceil(chineseChars / 1.5) + (int) Math.ceil(otherChars / 4.0);
    }
}
