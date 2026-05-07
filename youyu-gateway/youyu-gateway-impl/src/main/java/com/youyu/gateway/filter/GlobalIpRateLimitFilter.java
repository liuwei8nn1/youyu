package com.youyu.gateway.filter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.youyu.gateway.util.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局 IP 限流过滤器 (基于 Sentinel 热点参数限流)
 * <p>
 * 职责:
 * 1. 对所有请求进行 IP 维度的限流
 * 2. 使用 Sentinel ParamFlowRule (热点参数限流) 实现
 * 3. 被限流的请求返回 429 Too Many Requests
 * <p>
 * 工作原理:
 * - 资源名固定: "global_ip_limit"
 * - IP 地址作为热点参数传入
 * - Sentinel 自动为每个不同的 IP 创建独立的计数器
 * <p>
 * 配置方式:
 * 1. Nacos 配置 param-flow-rules.json:
 *    {
 *      "resource": "global_ip_limit",
 *      "grade": 1,
 *      "count": 50,
 *      "paramIdx": 0,
 *      "limitApp": "default"
 *    }
 * 2. application.yml 添加数据源:
 *    sentinel:
 *      datasource:
 *        param-flow:
 *          nacos:
 *            data-id: ${spring.application.name}-param-flow-rules
 *            rule-type: param-flow
 * <p>
 * 优点:
 * - ✅ Dashboard 只显示一个资源,便于管理
 * - ✅ 可以看到每个 IP 的实时统计数据
 * - ✅ 支持特殊 IP 的特殊配置 (白名单/黑名单)
 * - ✅ 规则可通过 Nacos 动态调整
 * <p>
 * 注意:
 * - ⚠️ 不要使用动态资源名 (如 "ip_limit:" + ip),会导致资源爆炸
 * - ⚠️ 此 Filter 应在业务 Filter 之前执行 (order 值要小)
 */
@Slf4j
@Component
public class GlobalIpRateLimitFilter implements GlobalFilter, Ordered {

    /**
     * 全局 IP 限流资源名 (固定,不随 IP 变化)
     */
    private static final String GLOBAL_IP_LIMIT_RESOURCE = "global_ip_limit";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 获取客户端 IP
        String clientIp = exchange.getRequest().getRemoteAddress()
            .getAddress().getHostAddress();

        Entry entry = null;
        try {
            // 2. 使用 Sentinel 热点参数限流
            //    - 资源名固定: GLOBAL_IP_LIMIT_RESOURCE
            //    - IP 作为第 0 个参数传入
            //    - Sentinel 自动为每个不同的 IP 值创建独立计数器
            entry = SphU.entry(GLOBAL_IP_LIMIT_RESOURCE, 
                com.alibaba.csp.sentinel.EntryType.IN, 1, clientIp);

            // 3. 通过限流检查,继续执行
            return chain.filter(exchange);

        } catch (BlockException e) {
            // 4. 被限流,返回 429 Too Many Requests
            log.warn("IP 限流触发: ip={}, path={}, BlockException类型={}", clientIp, exchange.getRequest().getPath(), e.getClass().getSimpleName());
            return WebUtil.writeTooManyRequests(exchange.getResponse(), clientIp);
        } finally {
            // 5. 退出 Entry,释放资源
            if (entry != null) {
                entry.exit(1, clientIp);
            }
        }
    }


    @Override
    public int getOrder() {
        // 在业务 Filter 之前执行,但在全局日志 Filter 之后
        return -90;
    }
}
