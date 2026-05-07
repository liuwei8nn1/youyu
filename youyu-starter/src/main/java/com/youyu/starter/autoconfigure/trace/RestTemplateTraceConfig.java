package com.youyu.starter.autoconfigure.trace;

import java.util.ArrayList;
import java.util.List;

import com.youyu.framework.context.*;
import com.youyu.framework.context.web.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 追踪配置
 * <p>
 * 职责：
 * 1. 为 RestTemplate 添加 TraceId 传递拦截器
 * 2. 保证使用 RestTemplate 时的链路追踪完整性
 * 3. 适用于调用外部服务（非 Spring Cloud 微服务）
 * <p>
 * 工作原理：
 * - 当使用 RestTemplate 发起 HTTP 请求时，此拦截器会自动执行
 * - 从 MDC 或 UserContextHolder 获取当前 TraceId
 * - 将 TraceId 添加到 HTTP 请求头中
 * - 下游服务接收到请求后，可以提取并继续使用同一个 TraceId
 */
@Slf4j
@Configuration
@ConditionalOnClass(RestTemplate.class)
public class RestTemplateTraceConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 添加拦截器
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(traceInterceptor());
        restTemplate.setInterceptors(interceptors);
        
        log.info("===========>>>>>>> RestTemplate 已配置 TraceId 传递拦截器");
        return restTemplate;
    }
    
    /**
     * 创建 TraceId 传递拦截器
     */
    @Bean
    public ClientHttpRequestInterceptor traceInterceptor() {
        return (request, body, execution) -> {
            // 1. 获取当前 TraceId
            String traceId = TraceIdGenerator.getCurrentTraceId();
            
            if (traceId != null && !traceId.isEmpty()) {
                // 2. 添加到请求头
                request.getHeaders().add(TraceIdGenerator.TRACE_ID_HEADER, traceId);
                log.debug("RestTemplate 调用传递 TraceId: {} {}", 
                    request.getMethod(), request.getURI());
            } else {
                // 3. 如果没有 TraceId，说明可能在异步线程中
                // 此时不生成新 TraceId，避免 MDC 污染和内存泄漏
                log.warn("RestTemplate 调用未找到 TraceId（可能在异步线程中，请手动传递）: {} {}", 
                    request.getMethod(), request.getURI());
            }
            
            // 4. 添加用户信息
            addUserInfoHeaders(request);
            
            // 5. 执行请求
            return execution.execute(request, body);
        };
    }
    
    /**
     * 添加用户信息到请求头
     */
    private void addUserInfoHeaders(HttpRequest request) {
        UserInfo userInfo = UserContextHolder.getUserInfo();
        
        if (userInfo.isLogin()) {
            request.getHeaders().add(UserContextUtils.USER_ID_HEADER, userInfo.getUserId().toString());
            request.getHeaders().add(UserContextUtils.USER_TYPE_HEADER, userInfo.getUserType().toString());
            request.getHeaders().add(UserContextUtils.USER_NAME_HEADER, userInfo.getUsername());
            request.getHeaders().add(UserContextUtils.USER_ROLES_HEADER, userInfo.getRoles());
            request.getHeaders().add(UserContextUtils.DEVICE_ID_HEADER, userInfo.getDeviceId().toString());
        }
    }
}
