package com.youyu.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 文本生成请求 DTO
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 生成提示词
     */
    private String prompt;

    /**
     * 生成主题（可选）
     */
    private String topic;

    /**
     * 生成长度（可选，默认中等）
     * - short: 短文本
     * - medium: 中等长度
     * - long: 长文本
     */
    private String length;

    /**
     * 创造性程度（0-1，默认 0.7）
     */
    private Double temperature;

    /**
     * 最大 Token 数（可选）
     */
    private Integer maxTokens;
}
