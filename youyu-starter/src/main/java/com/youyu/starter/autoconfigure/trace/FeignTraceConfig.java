package com.youyu.starter.autoconfigure.trace;

import com.youyu.framework.context.*;
import com.youyu.framework.context.web.util.TraceIdGenerator;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 请求拦截器配置
 * <p>
 * 职责：
 * 1. 在 Feign 调用时自动传递 TraceId
 * 2. 传递用户上下文信息（UserId、UserType等）
 * 3. 保证分布式链路追踪的完整性
 * <p>
 * 工作原理：
 * - 当微服务A通过 Feign 调用微服务B时，此拦截器会自动执行
 * - 从 MDC 或 UserContextHolder 获取当前 TraceId
 * - 将 TraceId 添加到 Feign 请求头中
 * - 下游服务接收到请求后，GlobalLogFilter 会提取并继续使用同一个 TraceId
 */
@Slf4j
@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class FeignTraceConfig {

    @Bean
    public RequestInterceptor feignTraceInterceptor() {
        log.info("===========>>>>>>> Feign 已配置 TraceId 传递拦截器");
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1. 获取当前线程的 TraceId（从 MDC 或 UserContextHolder）
                String traceId = TraceIdGenerator.getCurrentTraceId();
                
                if (traceId != null && !traceId.isEmpty()) {
                    // 2. 添加到 Feign 请求头
                    template.header(TraceIdGenerator.TRACE_ID_HEADER, traceId);
                    log.debug("Feign 调用传递 TraceId: {} -> {}", template.method(), template.url());
                } else {
                    // 3. 如果没有 TraceId，说明可能在异步线程中
                    // 此时不生成新 TraceId，避免 MDC 污染和内存泄漏
                    log.warn("Feign 调用未找到 TraceId（可能在异步线程中，请手动传递）: {} -> {}", 
                            template.method(), template.url());
                }
                
                // 4. 传递用户上下文信息
                addUserInfoHeaders(template);
            }
        };
    }

    /**
     * 添加用户信息到请求头
     */
    private void addUserInfoHeaders(RequestTemplate template) {
        UserInfo userInfo = UserContextHolder.getUserInfo();
        
        if (userInfo.isLogin()) {
            template.header(UserContextUtils.USER_ID_HEADER, userInfo.getUserId().toString());
            template.header(UserContextUtils.USER_TYPE_HEADER, userInfo.getUserType().toString());
            template.header(UserContextUtils.USER_NAME_HEADER, userInfo.getUsername());
            template.header(UserContextUtils.USER_ROLES_HEADER, userInfo.getRoles());
            template.header(UserContextUtils.DEVICE_ID_HEADER, userInfo.getDeviceId().toString());
        }
    }
}
