package com.youyu.framework;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.common.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(name = "spring.cloud.nacos.config.enabled", havingValue = "true")
public class NacosSentinelConfigInitializer {

    private static final Logger logger = LoggerFactory.getLogger(NacosSentinelConfigInitializer.class);

    @Autowired
    private NacosConfigManager nacosConfigManager;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP}")
    private String group;

    @PostConstruct
    public void init() throws Exception {
        initFlowRules();
        initDegradeRules();
        initSystemRules();
        initAuthorityRules();
        initParamFlowRules();
    }

    private void initFlowRules() throws Exception {
        String dataId = appName + "-flow-rules";
        String defaultConfig = loadDefaultConfig("sentinel-default-rules/flow-rules.json");
        publishConfigIfAbsent(dataId, defaultConfig);
    }

    private void initDegradeRules() throws Exception {
        String dataId = appName + "-degrade-rules";
        String defaultConfig = loadDefaultConfig("sentinel-default-rules/degrade-rules.json");
        publishConfigIfAbsent(dataId, defaultConfig);
    }

    private void initSystemRules() throws Exception {
        String dataId = appName + "-system-rules";
        String defaultConfig = loadDefaultConfig("sentinel-default-rules/system-rules.json");
        publishConfigIfAbsent(dataId, defaultConfig);
    }

    private void initAuthorityRules() throws Exception {
        String dataId = appName + "-authority-rules";
        String defaultConfig = loadDefaultConfig("sentinel-default-rules/authority-rules.json");
        publishConfigIfAbsent(dataId, defaultConfig);
    }

    private void initParamFlowRules() throws Exception {
        String dataId = appName + "-param-flow-rules";
        String defaultConfig = loadDefaultConfig("sentinel-default-rules/param-flow-rules.json");
        publishConfigIfAbsent(dataId, defaultConfig);
    }

    /**
     * 从 classpath 加载默认配置文件
     * @param configPath 配置文件路径（相对于 classpath）
     * @return 配置内容，如果文件不存在则返回空数组 "[]"
     */
    private String loadDefaultConfig(String configPath) {
        try {
            ClassPathResource resource = new ClassPathResource(configPath);
            if (!resource.exists()) {
                logger.warn("默认配置文件不存在: {}, 使用空配置", configPath);
                return "[]";
            }
            
            try (InputStream inputStream = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                if (StringUtils.isBlank(content)) {
                    logger.warn("默认配置文件为空: {}, 使用空配置", configPath);
                    return "[]";
                }
                logger.info("成功加载默认配置: {}", configPath);
                return content.trim();
            }
        } catch (Exception e) {
            logger.error("加载默认配置失败: {}, 使用空配置", configPath, e);
            return "[]";
        }
    }

    private void publishConfigIfAbsent(String dataId, String defaultConfig) throws Exception {
        String config = nacosConfigManager.getConfigService().getConfig(dataId, group, 3000);
        if (StringUtils.isBlank(config)) {
            boolean success = nacosConfigManager.getConfigService().publishConfig(dataId, group, defaultConfig, "json");
            if (success) {
                logger.info("成功初始化 Nacos 配置: dataId={}, group={}", dataId, group);
            } else {
                logger.warn("初始化 Nacos 配置失败: dataId={}, group={}", dataId, group);
            }
        } else {
            logger.info("Nacos 配置已存在，跳过初始化: dataId={}, group={}", dataId, group);
        }
    }

}
