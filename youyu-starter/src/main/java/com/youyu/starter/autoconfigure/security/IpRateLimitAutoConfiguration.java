package com.youyu.starter.autoconfigure.security;

import com.youyu.framework.web.filter.IpRateLimitFilter;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * IP 限流自动配置
 * <p>
 * 职责:
 * 1. 条件化注册 IpRateLimitFilter
 * 2. 提供配置属性支持
 * <p>
 * 使用方式:
 * 1. 在 application.yml 中启用:
 *    demo:
 *      ip-rate-limit:
 *        enabled: true
 *        resource-name: api_rate_limit
 *        max-requests-per-second: 50
 * <p>
 * 注意:
 * - ⚠️ 此配置默认关闭,建议在 Gateway 层统一处理 IP 限流
 * - ⚠️ 仅在特殊场景下(如直连微服务)才启用此配置
 * - ⚠️ 需要配合 Sentinel 热点参数限流规则使用
 */
@Configuration
@ConditionalOnProperty(name = "demo.ip-rate-limit.enabled", havingValue = "true")
public class IpRateLimitAutoConfiguration {

    /**
     * 注册 IP 限流过滤器
     */
    @Bean
    public FilterRegistrationBean<IpRateLimitFilter> ipRateLimitFilter(
            IpRateLimitProperties properties) {
        
        FilterRegistrationBean<IpRateLimitFilter> registration = new FilterRegistrationBean<>();
        
        // 创建 Filter 实例
        IpRateLimitFilter filter = new IpRateLimitFilter(
            properties.getResourceName(),
            properties.getMaxRequestsPerSecond()
        );
        
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(properties.getOrder());
        registration.setName("ipRateLimitFilter");
        
        return registration;
    }

    /**
     * IP 限流配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "demo.ip-rate-limit")
    public IpRateLimitProperties ipRateLimitProperties() {
        return new IpRateLimitProperties();
    }

    /**
     * IP 限流配置属性类
     */
    @Data
    public static class IpRateLimitProperties {
        
        /**
         * 是否启用 IP 限流 (默认 false)
         */
        private boolean enabled = false;
        
        /**
         * 资源名称 (默认 api_rate_limit)
         */
        private String resourceName = "api_rate_limit";
        
        /**
         * 每个 IP 每秒最大请求数 (默认 50)
         */
        private int maxRequestsPerSecond = 50;
        
        /**
         * Filter 执行顺序 (默认最高优先级)
         */
        private int order = Ordered.HIGHEST_PRECEDENCE;
    }
}
