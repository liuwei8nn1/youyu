package com.youyu.starter.autoconfigure.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Web 层自动配置
 * <p>
 * 职责：
 * 1. 全局异常处理
 * 2. Web MVC 配置
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({
		com.youyu.framework.context.web.handler.GlobalExceptionHandler.class,
		WebMvcConfig.class
})
public class WebAutoConfiguration {
}
