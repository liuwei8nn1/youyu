package com.youyu.starter.autoconfigure.logging;


import com.youyu.framework.context.web.filter.GlobalLogFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 请求日志自动配置类
 * <p>
 * 职责：
 * 1. 自动注册 GlobalLogFilter
 * 2. 支持通过配置开关控制是否启用
 * 3. 配置 Filter 的 URL patterns 和优先级
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RequestLoggingAutoConfiguration {

    /**
     * 创建 GlobalLogFilter Bean
     * <p>
     * 让 Spring 管理 Filter 实例，使 @Value 注解能够正常注入配置值
     *
     * @return GlobalLogFilter 实例
     */
    @Bean
    @ConditionalOnMissingBean(GlobalLogFilter.class)
    public GlobalLogFilter globalLogFilter() {
        return new GlobalLogFilter();
    }

    /**
     * 注册全局日志过滤器
     * <p>
     * 只有在以下条件下才会注册：
     * 1. logging.request.enabled = true（或未配置，默认 true）
     * 2. 容器中不存在 GlobalLogFilter Bean
     *
     * @param globalLogFilter 由 Spring 管理的 Filter 实例
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<GlobalLogFilter> globalLogFilterRegistration(GlobalLogFilter globalLogFilter) {
        FilterRegistrationBean<GlobalLogFilter> registration = new FilterRegistrationBean<>();
        
        // 使用 Spring 管理的 Filter 实例（@Value 注解已注入）
        registration.setFilter(globalLogFilter);
        
        // 配置 URL patterns
        registration.addUrlPatterns("/*");
        
        // 设置优先级（最高优先级，确保在其他 Filter 之前执行）
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        
        // 设置 Filter 名称
        registration.setName("globalLogFilter");
        
        return registration;
    }
}
