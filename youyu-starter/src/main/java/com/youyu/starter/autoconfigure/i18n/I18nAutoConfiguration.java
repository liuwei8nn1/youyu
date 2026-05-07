package com.youyu.starter.autoconfigure.i18n;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 国际化自动配置
 * <p>
 * 职责：
 * 1. 初始化 I18N 消息源
 */
@Configuration(proxyBeanMethods = false)
@Import(com.youyu.framework.context.I18nConfig.class)
public class I18nAutoConfiguration {
}
