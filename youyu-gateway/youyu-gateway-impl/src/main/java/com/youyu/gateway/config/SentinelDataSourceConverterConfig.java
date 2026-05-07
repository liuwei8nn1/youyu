package com.youyu.gateway.config;

import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.alibaba.cloud.sentinel.gateway.SentinelGatewayAutoConfiguration;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * @see SentinelAutoConfiguration.SentinelConverterConfiguration.SentinelJsonConfiguration
 * @see com.alibaba.cloud.sentinel.gateway.SentinelGatewayAutoConfiguration
 * Sentinel 数据源转换器配置
 * <p>
 * 为文件数据源提供 JSON 转换器 Bean
 * 注意：Nacos 数据源会自动处理转换，无需此配置
 */
@Configuration
public class SentinelDataSourceConverterConfig {

    // /**
    //  * Gateway 流控规则转换器 (GatewayFlowRule)
    //  * 注意：GatewayRuleManager 要求返回 Set 类型
    //  */
    // @Bean("sentinel-json-gw-flow-converter")
    // public Converter<String, Set<GatewayFlowRule>> sentinelJsonGwFlowConverter() {
    //     return source -> {
    //         return JSON.parseObject(source, new TypeReference<HashSet<GatewayFlowRule>>() {});
    //     };
    // }

    // /**
    //  * 系统级限流规则转换器 (SystemRule)
    //  */
    // @Bean("sentinel-json-system-converter")
    // public Converter<String, HashSet<SystemRule>> sentinelJsonSystemConverter() {
    //     return source -> JSON.parseObject(source, new TypeReference<HashSet<SystemRule>>() {});
    // }
    //
    // /**
    //  * 热点参数限流规则转换器 (ParamFlowRule)
    //  */
    // @Bean("sentinel-json-param-flow-converter")
    // public Converter<String, HashSet<ParamFlowRule>> sentinelJsonParamFlowConverter() {
    //     return source -> JSON.parseObject(source, new TypeReference<HashSet<ParamFlowRule>>() {});
    // }
    //
    // /**
    //  * API 定义转换器 (ApiDefinition)
    //  */
    // @Bean("sentinel-json-api-converter")
    // public Converter<String, HashSet<ApiDefinition>> sentinelJsonApiConverter() {
    //     return source -> JSON.parseObject(source, new TypeReference<HashSet<ApiDefinition>>() {});
    // }
}
