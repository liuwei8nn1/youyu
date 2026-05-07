package com.youyu.ai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI 文本润色响应 DTO
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPolishResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 润色后的文本
     */
    private String polishedText;

    /**
     * 使用的模型名称
     */
    private String modelName;

    /**
     * 消耗的 Token 数量
     */
    private Integer tokensUsed;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTimeMs;
}
