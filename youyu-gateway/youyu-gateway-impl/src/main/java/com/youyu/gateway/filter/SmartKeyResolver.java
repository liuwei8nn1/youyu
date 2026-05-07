package com.youyu.gateway.filter;

import com.youyu.framework.context.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.youyu.framework.context.UserInfo.JWT_USERINFO;


/**
 * 智能限流 Key 解析器
 * <p>
 * 职责:
 * 1. 优先使用用户ID作为限流维度 (已登录用户)
 * 2. 降级使用客户端 IP (未登录用户)
 * <p>
 * 使用场景:
 * - 配合 RequestRateLimiter 实现按用户/IP 限流
 * - 解决未登录用户没有 X-User-Id 的问题
 * <p>
 * 配置示例:
 * <pre>
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *         - id: order-service
 *           filters:
 *             - name: RequestRateLimiter
 *               args:
 *                 redis-rate-limiter.replenishRate: 5
 *                 redis-rate-limiter.burstCapacity: 10
 *                 key-resolver: "#{@smartKeyResolver}"
 * </pre>
 * <p>
 * 注意:
 * - JwtFilter 会将 UserInfo 存入 exchange.attributes
 * - 此 Resolver 在 JwtFilter 之后执行,可以获取到 UserInfo
 */
@Slf4j
@Component("smartKeyResolver")
public class SmartKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        // 1. 优先从 attributes 获取 UserInfo (由 JwtFilter 设置)
        UserInfo userInfo = exchange.getAttribute(JWT_USERINFO);
        
        if (userInfo != null && userInfo.getUserId() != null) {
            // 已登录: 使用用户ID作为限流维度
            return Mono.just("user:" + userInfo.getUserId());
        }
        
        // 2. 降级: 使用客户端 IP 作为限流维度
        String clientIp = exchange.getRequest().getRemoteAddress()
            .getAddress().getHostAddress();
        return Mono.just("ip:" + clientIp);
    }
}
