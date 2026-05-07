package com.youyu.ai.application.service;

import com.youyu.ai.api.dto.AiGenerateRequest;
import com.youyu.ai.api.dto.AiGenerateResponse;
import com.youyu.ai.api.dto.AiPolishRequest;
import com.youyu.ai.api.dto.AiPolishResponse;

/**
 * AI 应用服务接口
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
public interface AiApplicationService {

    /**
     * 文本润色
     *
     * @param request 润色请求
     * @return 润色结果
     */
    AiPolishResponse polishText(AiPolishRequest request);

    /**
     * 文本生成
     *
     * @param request 生成请求
     * @return 生成结果
     */
    AiGenerateResponse generateText(AiGenerateRequest request);
}
