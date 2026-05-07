package com.youyu.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.common.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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

/**
 * Gateway Sentinel Nacos 配置初始化器
 * <p>
 * 职责：
 * 1. 在应用启动时，检查 Nacos 中是否存在 Sentinel 规则配置
 * 2. 如果不存在，则从本地默认配置文件读取并写入 Nacos
 * 3. 这样可以在首次部署时自动初始化配置，后续通过 Nacos 控制台动态调整
 * <p>
 * 注意：
 * - 仅在 Nacos Config 启用时生效
 * - Gateway 使用特殊的 GatewayFlowRule，与普通微服务的 FlowRule 不同
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.cloud.nacos.config.enabled", havingValue = "true")
public class GatewayNacosSentinelConfigInitializer {

    @Autowired
    private NacosConfigManager nacosConfigManager;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.cloud.nacos.config.group:DEFAULT_GROUP}")
    private String group;

    @PostConstruct
    public void init() throws Exception {
        log.info("========== 开始初始化 Gateway Sentinel Nacos 配置 ==========");
        
        // 1. Gateway 流控规则 (GatewayFlowRule)
        initGatewayFlowRules();
        
        // 2. 系统级限流规则 (SystemRule) - 保护整个应用
        initSystemRules();
        
        // 3. 热点参数限流规则 (ParamFlowRule) - 用于全局IP限流等
        initParamFlowRules();
        
        log.info("========== Gateway Sentinel Nacos 配置初始化完成 ==========");
    }

    /**
     * 初始化 Gateway 流控规则
     */
    private void initGatewayFlowRules() throws Exception {
        String dataId = appName + "-gateway-flow-rules";
        String defaultConfig = loadDefaultConfig("sentinel-default-rules/gateway-flow-rules.json");
        publishConfigIfAbsent(dataId, defaultConfig);
    }

    /**
     * 初始化系统级限流规则
     * <p>
     * 系统级限流用于保护整个应用不被压垮,基于以下指标:
     * - QPS: 每秒请求数
     * - RT: 平均响应时间
     * - Thread: 并发线程数
     * - CPU: CPU 使用率
     * - Load: 系统负载 (仅 Linux)
     */
    private void initSystemRules() throws Exception {
        String dataId = appName + "-system-rules";
        String defaultConfig = loadDefaultConfig("sentinel-default-rules/gateway-system-rules.json");
        publishConfigIfAbsent(dataId, defaultConfig);
    }

    /**
     * 初始化热点参数限流规则
     * <p>
     * 热点参数限流用于实现按维度限流,如:
     * - 全局 IP 限流
     * - 用户ID 限流
     * - 商品ID 限流等
     */
    private void initParamFlowRules() throws Exception {
        String dataId = appName + "-param-flow-rules";
        String defaultConfig = loadDefaultConfig("sentinel-default-rules/gateway-param-flow-rules.json");
        publishConfigIfAbsent(dataId, defaultConfig);
    }

    /**
     * 从 classpath 加载默认配置文件
     *
     * @param configPath 配置文件路径（相对于 classpath）
     * @return 配置内容，如果文件不存在则返回空数组 "[]"
     */
    private String loadDefaultConfig(String configPath) {
        try {
            ClassPathResource resource = new ClassPathResource(configPath);
            if (!resource.exists()) {
                log.warn("默认配置文件不存在: {}, 使用空配置", configPath);
                return "[]";
            }

            try (InputStream inputStream = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                if (StringUtils.isBlank(content)) {
                    log.warn("默认配置文件为空: {}, 使用空配置", configPath);
                    return "[]";
                }
                log.info("成功加载默认配置: {}", configPath);
                return content.trim();
            }
        } catch (Exception e) {
            log.error("加载默认配置失败: {}, 使用空配置", configPath, e);
            return "[]";
        }
    }

    private void publishConfigIfAbsent(String dataId, String defaultConfig) throws Exception {
        String config = nacosConfigManager.getConfigService().getConfig(dataId, group, 3000);
        if (StringUtils.isBlank(config)) {
            boolean success = nacosConfigManager.getConfigService().publishConfig(dataId, group, defaultConfig, "json");
            if (success) {
                log.info("成功初始化 Nacos 配置: dataId={}, group={}", dataId, group);
            } else {
                log.warn("初始化 Nacos 配置失败: dataId={}, group={}", dataId, group);
            }
        } else {
            log.info("Nacos 配置已存在，跳过初始化: dataId={}, group={}", dataId, group);
        }
    }
}
