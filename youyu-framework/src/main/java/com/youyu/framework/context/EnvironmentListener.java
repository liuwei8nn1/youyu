package com.youyu.framework.context;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * 应用环境准备监听器
 * <p>
 * 职责：
 * 1. 在 Spring Boot 应用启动时自动检测并初始化当前环境
 * 2. 根据 spring.profiles.active 配置设置 Env.CURRENT
 */
@Slf4j
public class EnvironmentListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String[] activeProfiles = event.getEnvironment().getActiveProfiles();
        Env.init(activeProfiles);
        
        // 从Environment中读取应用名
        String appName = event.getEnvironment().getProperty("spring.application.name", "unknown-app");
        Env.setAppName(appName);
        
        log.info("===========>>>>>>> 环境初始化完成，当前环境: {}, 应用名: {}", Env.CURRENT.getLabel(), appName);
    }
}
