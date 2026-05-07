package com.youyu.starter.autoconfigure.warn;

import com.youyu.framework.warn.core.MsgWarnChannel;
import com.youyu.framework.warn.listener.ApplicationClosedListener;
import com.youyu.framework.warn.listener.ApplicationStartedListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;

/**
 * 告警监听器配置类
 * <p>
 * 职责：注册应用启停监听器Bean
 */
@Configuration(proxyBeanMethods = false)
public class WarnListenerConfiguration {

	/**
	 * 注册应用启动监听器
	 */
	@Bean
	public ApplicationListener<ApplicationReadyEvent> applicationStartedListener(@Autowired(required = false) MsgWarnChannel msgWarnChannel) {
		return new ApplicationStartedListener(msgWarnChannel);
	}

	/**
	 * 注册应用关闭监听器
	 */
	@Bean
	public ApplicationListener<ContextClosedEvent> applicationClosedListener(@Autowired(required = false) MsgWarnChannel msgWarnChannel) {
		return new ApplicationClosedListener(msgWarnChannel);
	}
}
