package com.youyu.starter.autoconfigure.base;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 基础自动配置
 * <p>
 * 职责：
 * 1. Nacos Sentinel 配置初始化
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.nacos.config.enabled", havingValue = "true")
@Import(com.youyu.framework.NacosSentinelConfigInitializer.class)
public class BaseAutoConfiguration {
}
