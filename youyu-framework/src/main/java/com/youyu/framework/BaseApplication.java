package com.youyu.framework;

import com.youyu.framework.context.EnvironmentListener;
import org.springframework.boot.SpringApplication;

/**
 * 基础启动类
 * <p>
 * 提供统一的启动方法，自动注册 EnvironmentListener
 *
 * @author LiuWei
 * @since 2026/4/20
 */
public abstract class BaseApplication {
	protected static void startup(Class<?> primarySource, String[] args) {
		SpringApplication application = new SpringApplication(primarySource);
		application.addListeners(new EnvironmentListener());
		application.run(args);
	}
}
