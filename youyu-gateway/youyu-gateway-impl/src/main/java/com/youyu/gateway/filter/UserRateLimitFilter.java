package com.youyu.gateway.filter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.youyu.framework.context.UserInfo;
import com.youyu.gateway.util.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.youyu.framework.context.UserInfo.JWT_USERINFO;

/**
 * 用户维度限流过滤器 (基于 Sentinel 热点参数限流)
 * <p>
 * 职责:
 * 1. 对已登录用户进行用户ID维度的限流
 * 2. 使用 Sentinel ParamFlowRule (热点参数限流) 实现
 * 3. 未登录用户跳过此限流(由 GlobalIpRateLimitFilter 处理)
 * <p>
 * 工作原理:
 * - 资源名固定: "user_rate_limit"
 * - 参数: "routeId:userId" 组合键 (如 "seckill-service:1001")
 * - Sentinel 自动为每个不同的组合键创建独立计数器
 * - 实现按路由+用户的细粒度限流
 * <p>
 * 执行顺序:
 * - order = 1,在 JwtFilter (order=-1) 之后执行
 * - 确保 UserInfo 已设置,可以获取 userId
 * <p>
 * 配置方式:
 * 1. Nacos 配置 param-flow-rules.json:
 *    {
 *      "resource": "user_rate_limit",
 *      "grade": 1,
 *      "count": 5,
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
 * 架构设计:
 * - Gateway 层: 粗粒度限流 (IP、用户ID)
 *   ├─ GlobalIpRateLimitFilter: IP限流 (防刷)
 *   └─ UserRateLimitFilter: 用户限流
 * - 微服务层: 细粒度限流 (可选,默认关闭)
 *   └─ IpRateLimitFilter (base-starter): 特殊场景启用
 * <p>
 * 注意:
 * - ⚠️ 必须在 JwtFilter 之后执行,否则无法获取 UserInfo
 * - ⚠️ 未登录用户(userId=null)会跳过此限流
 * - ⚠️ Sentinel ParamFlowRule 只支持单参数索引,但可以将多个值组合成单个参数
 */
@Slf4j
@Component
public class UserRateLimitFilter implements GlobalFilter, Ordered {

    /**
     * 用户限流资源名 (固定)
     */
    private static final String USER_RATE_LIMIT_RESOURCE = "user_rate_limit";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 从 attributes 获取 UserInfo (由 JwtFilter 设置)
        UserInfo userInfo = exchange.getAttribute(JWT_USERINFO);
        
        // 2. 未登录用户跳过此限流,由 GlobalIpRateLimitFilter 处理
        if (userInfo == null || userInfo.getUserId() == null) {
            return chain.filter(exchange);
        }
        
        Long userId = userInfo.getUserId();
        
        // 获取路由ID
        // org.springframework.cloud.gateway.route.Route
        // Object obj = exchange.getAttribute(
        //         "org.springframework.cloud.gateway.support.ServerWebExchangeUtils.gatewayRoute"
        // );
        // String routeId = "unknown";
        // if(obj instanceof Route route){
        //     routeId = route.getId();
        // }
        
        Entry entry = null;
        try {
            // 使用 Sentinel 热点参数限流
            entry = SphU.entry(USER_RATE_LIMIT_RESOURCE, com.alibaba.csp.sentinel.EntryType.IN, 1, userId);

            // 通过限流检查,继续执行
            return chain.filter(exchange);
            
        } catch (BlockException e) {
            // 被限流,返回 429 Too Many Requests
            log.warn("用户限流触发: userId={}, path={}",userId, exchange.getRequest().getPath(), e);
            return WebUtil.writeTooManyRequests(exchange.getResponse(), userId);
        } finally {
            // 退出 Entry,释放资源
            if (entry != null) {
                entry.exit(1, userId);
            }
        }
    }

    @Override
    public int getOrder() {
        // 在 JwtFilter (order=-1) 之后执行,确保 UserInfo 已设置
        // 但在业务 Filter 之前执行
        return 1;
    }
}
