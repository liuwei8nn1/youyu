package com.youyu.starter.autoconfigure.web;

import com.youyu.framework.context.web.resolver.ProxyRequestArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置类
 * <p>
 * 职责：
 * 1. 注册自定义的参数解析器（ProxyRequestArgumentResolver）
 * 2. 支持在 Controller 方法参数中直接注入 ProxyRequest
 * <p>
 * 工作原理：
 * - Spring MVC 启动时会调用 addArgumentResolvers 方法
 * - 将 ProxyRequestArgumentResolver 添加到解析器列表中
 * - 当 Controller 方法参数类型为 ProxyRequest 时，自动使用该解析器进行注入
 *
 * @author LiuWei
 * @since 2026/4/23
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 添加自定义参数解析器
     *
     * @param resolvers 参数解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 添加 ProxyRequest 参数解析器
        resolvers.add(new ProxyRequestArgumentResolver());
    }
}
