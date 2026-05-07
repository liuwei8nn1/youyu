package com.youyu.framework.web.filter;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.youyu.common.constant.BaseI18nKey;
import com.youyu.common.util.JsonUtil;
import com.youyu.framework.context.I18N;
import com.youyu.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * IP 限流过滤器 (基于 Sentinel 热点参数限流)
 * <p>
 * 工作原理:
 * - 资源名固定,IP 地址作为热点参数传入
 * - Sentinel 自动为每个不同的 IP 创建独立的计数器
 * <p>
 * 配置方式:
 * 1. Nacos 配置 param-flow-rules.json:
 *    {
 *      "resource": "api_rate_limit",
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
 * 注意:
 * - ⚠️ 此 Filter 不会自动生效,需要在 starter 中通过 FilterRegistrationBean 注册
 * - ⚠️ 建议在 Gateway 层统一处理 IP 限流,而不是在每个微服务中配置
 * - ⚠️ 不要使用动态资源名 (如 "ip_limit:" + ip),会导致资源爆炸
 */
@Slf4j
@RequiredArgsConstructor
public class IpRateLimitFilter implements Filter, Ordered {

    private final String resourceName;
    private final int maxRequestsPerSecond;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 获取客户端 IP
        String clientIp = httpRequest.getRemoteAddr();
        
        Entry entry = null;
        try {
            // 使用 Sentinel 热点参数限流
            // - 资源名固定: resourceName
            // - IP 作为第 0 个参数传入
            // - Sentinel 自动为每个不同的 IP 值创建独立计数器
            entry = SphU.entry(resourceName, EntryType.IN, 1, clientIp);
            
            // 通过限流检查,继续执行
            chain.doFilter(request, response);
            
        } catch (BlockException e) {
            // 被限流,返回 429 Too Many Requests
            log.warn("IP 限流触发: ip={}, path={}", clientIp, httpRequest.getRequestURI());
            
            httpResponse.setStatus(HttpStatus.OK.value());
            httpResponse.setContentType("application/json;charset=UTF-8");

            Result<Object> error = Result.error(Result.TOO_MANY_REQUESTS, I18N.msg(BaseI18nKey.SENTINEL_FLOW_CONTROL)).setExt(clientIp);
            String body = JsonUtil.toJson(error);
            httpResponse.getWriter().write(body);
            
        } finally {
            // 退出 Entry,释放资源
            if (entry != null) {
                entry.exit(1, clientIp);
            }
        }
    }

    @Override
    public int getOrder() {
        // 最高优先级,在其它 Filter 之前执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
