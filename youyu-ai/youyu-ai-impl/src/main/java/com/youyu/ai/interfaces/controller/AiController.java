package com.youyu.ai.interfaces.controller;

import com.youyu.ai.api.dto.AiGenerateRequest;
import com.youyu.ai.api.dto.AiGenerateResponse;
import com.youyu.ai.api.dto.AiPolishRequest;
import com.youyu.ai.api.dto.AiPolishResponse;
import com.youyu.ai.application.service.AiApplicationService;
import com.youyu.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI 服务 Controller
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiApplicationService aiApplicationService;

    /**
     * 文本润色
     *
     * @param request 润色请求
     * @return 润色结果
     */
    @PostMapping("/polish")
    public Result<AiPolishResponse> polishText(@RequestBody AiPolishRequest request) {
        try {
            log.info("收到文本润色请求: {}", request);
            AiPolishResponse response = aiApplicationService.polishText(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("文本润色失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 文本生成
     *
     * @param request 生成请求
     * @return 生成结果
     */
    @PostMapping("/generate")
    public Result<AiGenerateResponse> generateText(@RequestBody AiGenerateRequest request) {
        try {
            log.info("收到文本生成请求: {}", request);
            AiGenerateResponse response = aiApplicationService.generateText(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("文本生成失败", e);
            return Result.error(e.getMessage());
        }
    }
}
