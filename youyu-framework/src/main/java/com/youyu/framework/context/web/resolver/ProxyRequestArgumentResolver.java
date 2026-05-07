package com.youyu.framework.context.web.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * ProxyRequest 参数解析器
 * <p>
 * 职责：
 * 1. 识别 Controller 方法参数中的 ProxyRequest 类型
 * 2. 自动从请求中提取 HttpServletRequest 和 HttpServletResponse
 * 3. 创建并返回 ProxyRequest 实例
 * <p>
 * 工作原理：
 * - Spring MVC 在调用 Controller 方法前，会遍历所有注册的 HandlerMethodArgumentResolver
 * - 当发现方法参数类型为 ProxyRequest 时，此解析器会被触发
 * - 从 NativeWebRequest 中提取原生的 Servlet 请求和响应对象
 * - 构造 ProxyRequest 实例并注入到方法参数中
 *
 * @author LiuWei
 * @since 2026/4/23
 */
@Slf4j
public class ProxyRequestArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否支持该参数类型
     *
     * @param parameter 方法参数
     * @return true-支持 ProxyRequest 类型
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(ProxyRequest.class);
    }

    /**
     * 解析参数值
     *
     * @param parameter     方法参数
     * @param mavContainer  ModelAndView 容器
     * @param webRequest    Web 请求
     * @param binderFactory 数据绑定工厂
     * @return ProxyRequest 实例
     */
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        // 从 NativeWebRequest 中提取原生的 Servlet 对象
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        if (request == null || response == null) {
            log.warn("无法获取 HttpServletRequest 或 HttpServletResponse，当前环境可能不是 Servlet 环境");
            return null;
        }

        // 创建并返回 ProxyRequest 实例
        return new ProxyRequest(request, response);
    }
}
