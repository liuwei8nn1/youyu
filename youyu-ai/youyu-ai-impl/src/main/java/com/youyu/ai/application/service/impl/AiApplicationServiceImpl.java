package com.youyu.ai.application.service.impl;

import com.youyu.ai.api.dto.*;
import com.youyu.ai.application.service.AiApplicationService;
import com.youyu.ai.domain.service.AiTextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AI 应用服务实现
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiApplicationServiceImpl implements AiApplicationService {

    private final AiTextService aiTextService;

    @Override
    public AiPolishResponse polishText(AiPolishRequest request) {
        log.debug("执行文本润色: originalText length={}", 
            request.getOriginalText() != null ? request.getOriginalText().length() : 0);
        
        return aiTextService.polishText(request);
    }

    @Override
    public AiGenerateResponse generateText(AiGenerateRequest request) {
        log.debug("执行文本生成: prompt length={}", 
            request.getPrompt() != null ? request.getPrompt().length() : 0);
        
        return aiTextService.generateText(request);
    }
}
