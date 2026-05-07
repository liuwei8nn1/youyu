package com.youyu.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.youyu.framework.context.UserContextUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gateway Sentinel 限流配置
 * <p>
 * 职责：
 * 1. 初始化 Gateway 限流规则（从 Nacos 加载，本地提供默认值）
 * 2. 配置 API 分组（可选，用于更精细的限流控制）
 * <p>
 * 限流维度：
 * - 路由级别限流：针对每个路由的总 QPS
 * - IP 级别限流：防止单个 IP 刷接口
 * - 用户ID 级别限流：防止单个用户频繁请求
 * <p>
 * 注意：
 * - Gateway 使用单机模式限流，性能最优
 * - 如需分布式精准限流，建议使用业务层的 Redis 方案
 */
@Slf4j
@Configuration
public class GatewaySentinelConfig {

    @Value("${spring.cloud.nacos.config.enabled}")
    private Boolean enable = false;

    @PostConstruct
    public void init() {
        log.info("========== 初始化 Gateway Sentinel 配置 ==========");
        
        // 1. 初始化 API 分组（可选）
        initApiDefinitions();
        
        // 2. 限流规则加载策略:
        //    - 如果启用了 Nacos: 由 GatewayNacosSentinelConfigInitializer 写入默认值到 Nacos,
        //      Sentinel 数据源会自动从 Nacos 加载,此方法不执行
        //    - 如果未启用 Nacos: 从本地 JSON 文件加载默认规则
        if (!enable) {
            log.info("Nacos 未启用,从本地 JSON 文件加载限流规则");
            initFlowRulesFromJson();
        } else {
            log.info("Nacos 已启用,限流规则将从 Nacos 自动加载");
        }
        
        log.info("========== Gateway Sentinel 配置初始化完成 ==========");
    }


    /**
     * 初始化 API 分组定义
     * <p>
     * API 分组可以将多个路径归为一组，然后对整个组进行限流
     * 例如：将所有订单相关接口归为 "order-api" 组
     */
    private void initApiDefinitions() {
        Set<ApiDefinition> definitions = new HashSet<>();

        // 示例：订单相关 API 分组
        ApiDefinition orderApi = new ApiDefinition("order-api")
            .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                add(new ApiPathPredicateItem().setPattern("/api/order/**"));
                add(new ApiPathPredicateItem().setPattern("/api/seckill/**"));
            }});
        definitions.add(orderApi);

        // 示例：商品相关 API 分组
        ApiDefinition productApi = new ApiDefinition("product-api")
            .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                add(new ApiPathPredicateItem().setPattern("/api/product/**"));
            }});
        definitions.add(productApi);

        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        log.info("API 分组定义已加载: {}", definitions.size());
    }

    /**
     * 从本地 JSON 文件加载限流规则 (仅在未启用 Nacos 时执行)
     * <p>
     * 此方法读取 sentinel-default-rules/gateway-flow-rules.json 并加载规则
     */
    private void initFlowRulesFromJson() {
        try {
            // 读取 JSON 文件
            ClassPathResource resource = new ClassPathResource("sentinel-default-rules/gateway-flow-rules.json");
            if (!resource.exists()) {
                log.warn("限流规则配置文件不存在: sentinel-default-rules/gateway-flow-rules.json");
                return;
            }

            // 解析 JSON
            String jsonContent = new String(resource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            List<GatewayFlowRule> ruleList = com.alibaba.fastjson2.JSON.parseArray(jsonContent, GatewayFlowRule.class);
            
            if (ruleList != null && !ruleList.isEmpty()) {
                // 转换为 Set
                Set<GatewayFlowRule> rules = new HashSet<>(ruleList);
                GatewayRuleManager.loadRules(rules);
                log.info("从本地 JSON 文件加载限流规则成功: {} 条", rules.size());
            } else {
                log.warn("限流规则配置文件为空");
            }
        } catch (Exception e) {
            log.error("从本地 JSON 文件加载限流规则失败", e);
        }
    }

    /**
     * 限流规则配置示例 (仅供参考,不实际执行)
     * <p>
     * 重要说明:
     * - 此方法仅作为配置示例,展示如何通过代码编写限流规则
     * - 实际环境中,应该使用 JSON 文件或从 Nacos 动态加载
     * <p>
     * 限流维度说明:
     * 1. 路由级别限流: resource=路由ID, 不设置 paramItem
     * 2. IP 级别限流: resource=路由ID, paramItem.parseStrategy=0 (CLIENT_IP)
     * 3. 用户ID 限流: resource=路由ID, paramItem.parseStrategy=2 (HEADER), fieldName=X-User-Id
     * <p>
     * 完整的默认规则请参考: sentinel-default-rules/gateway-flow-rules.json
     */
    @SuppressWarnings("unused")
    private void initFlowRulesExample() {
        Set<GatewayFlowRule> rules = new HashSet<>();
    
        // ========================================
        // 示例1: 路由级别限流 (保护后端服务)
        // ========================================
        // 订单服务: 总 QPS = 1000
        rules.add(new GatewayFlowRule("order-service")
            .setCount(1000)
            .setIntervalSec(1));
    
        // 秒杀服务: 总 QPS = 500 (更严格)
        rules.add(new GatewayFlowRule("seckill-service")
            .setCount(500)
            .setIntervalSec(1));
    
        // ========================================
        // 示例2: IP 级别限流 (防止单 IP 刷接口)
        // ========================================
        // parseStrategy=0 表示按客户端 IP 限流
        // 订单服务: 单 IP 每秒最多 50 次
        rules.add(new GatewayFlowRule("order-service")
            .setCount(50)
            .setIntervalSec(1)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
            ));
    
        // 认证服务: 单 IP 每秒最多 5 次 (防止暴力破解)
        rules.add(new GatewayFlowRule("auth-public")
            .setCount(5)
            .setIntervalSec(1)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_CLIENT_IP)
            ));
    
        // ========================================
        // 示例3: 用户ID 级别限流 (防止单用户频繁请求)
        // ========================================
        // parseStrategy=2 表示从 Header 获取参数
        // fieldName 指定 Header 名称 (由 JwtFilter 设置)
        // 注意: 未登录用户没有 X-User-Id,不会触发此限流规则
            
        // 订单服务: 单用户每秒最多 5 次
        rules.add(new GatewayFlowRule("order-service")
            .setCount(5)
            .setIntervalSec(1)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName(UserContextUtils.USER_ID_HEADER)
            ));
    
        // 秒杀服务: 单用户每秒最多 1 次 (严格限制)
        rules.add(new GatewayFlowRule("seckill-service")
            .setCount(1)
            .setIntervalSec(1)
            .setParamItem(new GatewayParamFlowItem()
                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                .setFieldName(UserContextUtils.USER_ID_HEADER)
            ));
    
        // 注意: 此处不执行 GatewayRuleManager.loadRules(rules)
        // 实际规则由 Nacos 数据源自动加载
    }
}
