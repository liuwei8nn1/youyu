package com.youyu.ai.sdk;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.youyu.ai.api.client.AiFeignClient;
import com.youyu.ai.api.dto.*;
import com.youyu.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * AI 服务客户端
 * <p>
 * 提供本地缓存和重试机制
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@Slf4j
@RequiredArgsConstructor
public class AiServiceClient {

    private final AiFeignClient aiFeignClient;

    /**
     * 本地缓存 - 用于缓存润色结果（避免重复调用）
     */
    private final Cache<String, AiPolishResponse> polishCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();

    /**
     * 文本润色（带缓存）
     *
     * @param request 润色请求
     * @return 润色结果
     */
    public AiPolishResponse polishText(AiPolishRequest request) {
        // 生成缓存 key
        String cacheKey = generateCacheKey(request);
        
        // 尝试从缓存获取
        AiPolishResponse cached = polishCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("命中润色缓存: {}", cacheKey);
            return cached;
        }
        
        // 调用远程服务
        log.debug("调用 AI 服务进行文本润色");
        Result<AiPolishResponse> result = aiFeignClient.polishText(request);
        
        if (result.isSuccess() && result.getData() != null) {
            // 存入缓存
            polishCache.put(cacheKey, result.getData());
            return result.getData();
        } else {
            throw new RuntimeException("AI 服务调用失败: " + result.getMessage());
        }
    }

    /**
     * 文本生成
     *
     * @param request 生成请求
     * @return 生成结果
     */
    public AiGenerateResponse generateText(AiGenerateRequest request) {
        log.debug("调用 AI 服务进行文本生成");
        Result<AiGenerateResponse> result = aiFeignClient.generateText(request);
        
        if (result.isSuccess() && result.getData() != null) {
            return result.getData();
        } else {
            throw new RuntimeException("AI 服务调用失败: " + result.getMessage());
        }
    }

    /**
     * 生成缓存 key
     */
    private String generateCacheKey(AiPolishRequest request) {
        return String.format("%s:%s:%s:%d",
            request.getOriginalText(),
            request.getStyle() != null ? request.getStyle() : "default",
            request.getTargetLanguage() != null ? request.getTargetLanguage() : "zh",
            request.getMaxLength() != null ? request.getMaxLength() : 0
        );
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        polishCache.invalidateAll();
        log.info("AI 润色缓存已清除");
    }
}
