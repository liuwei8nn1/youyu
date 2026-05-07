package com.youyu.ai.api.client;

import com.youyu.ai.api.dto.*;
import com.youyu.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI 服务 Feign 客户端
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@FeignClient(name = "ai-service", path = "/ai")
public interface AiFeignClient {

    /**
     * 文本润色
     *
     * @param request 润色请求
     * @return 润色结果
     */
    @PostMapping("/polish")
    Result<AiPolishResponse> polishText(@RequestBody AiPolishRequest request);

    /**
     * 文本生成
     *
     * @param request 生成请求
     * @return 生成结果
     */
    @PostMapping("/generate")
    Result<AiGenerateResponse> generateText(@RequestBody AiGenerateRequest request);
}
