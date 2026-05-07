package com.youyu.gateway.filter;

import com.youyu.common.model.Result;
import com.youyu.framework.context.Env;
import com.youyu.gateway.config.ApiSignProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * API 签名验证过滤器（全局过滤器）
 * <p>
 * 执行顺序：第一个执行（order = -2），在 JwtFilter 之前
 * <p>
 * 职责：
 * 1. 校验请求签名，防止请求被篡改
 * 2. 校验时间戳，防止重放攻击
 * <p>
 * 签名算法：HMAC-SHA256(AppSecret, AppId + Timestamp + Nonce + Path)
 */
@Slf4j
@Component
public class ApiSignatureFilter implements GlobalFilter, Ordered {

    private final ApiSignProperties apiSignProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiSignatureFilter(ApiSignProperties apiSignProperties) {
        this.apiSignProperties = apiSignProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 如果未开启验签，直接放行
        if (!apiSignProperties.isEnabled()
                || apiSignProperties.getSecrets().isEmpty()
                || Env.inner()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String appId = request.getHeaders().getFirst("X-App-Id");
        
        // 根据 AppId 获取对应的密钥
        String secret = apiSignProperties.getSecrets().get(appId);
        if (!StringUtils.hasText(secret)) {
            log.warn("Unknown AppId: {}", appId);
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid AppId");
        }
        String timestamp = request.getHeaders().getFirst("X-Timestamp");
        String nonce = request.getHeaders().getFirst("X-Nonce");
        String signature = request.getHeaders().getFirst("X-Signature");

        // 基础参数校验
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(timestamp) || 
            !StringUtils.hasText(nonce) || !StringUtils.hasText(signature)) {
            return writeErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Missing signature headers");
        }

        // 时间戳校验（防重放）
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis() / 1000;
            if (Math.abs(currentTime - requestTime) > apiSignProperties.getTimeWindow()) {
                return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Request expired");
            }
        } catch (NumberFormatException e) {
            return writeErrorResponse(exchange, HttpStatus.BAD_REQUEST, "Invalid timestamp format");
        }

        // 签名校验
        String path = request.getURI().getPath();
        String signStr = appId + timestamp + nonce + path;
        String calculatedSign = calculateHmacSha256(signStr, secret);

        if (!signature.equals(calculatedSign)) {
            log.warn("API Signature verification failed for path: {}", path);
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Invalid signature");
        }

        return chain.filter(exchange);
    }

    /**
     * 计算 HMAC-SHA256 签名
     */
    private String calculateHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }

    /**
     * 写入错误响应
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error("SIGN_ERROR", message);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Serialize error response failed", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 在 JwtFilter (-1) 之前执行
        return -2;
    }
}
