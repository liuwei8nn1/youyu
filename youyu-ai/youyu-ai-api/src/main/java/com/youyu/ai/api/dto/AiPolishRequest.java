package com.youyu.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 文本润色请求 DTO
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPolishRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待润色的原始文本
     */
    private String originalText;

    /**
     * 润色风格（可选）
     * - professional: 专业正式
     * - casual: 轻松随意
     * - marketing: 营销推广
     * - concise: 简洁明了
     */
    private String style;

    /**
     * 目标语言（可选，默认中文）
     */
    private String targetLanguage;

    /**
     * 最大长度限制（可选）
     */
    private Integer maxLength;
}
