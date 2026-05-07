package com.youyu.framework.context.web.util;

import com.youyu.framework.context.UserInfo;
import com.youyu.framework.context.UserContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求上下文工具类
 * <p>
 * 提供便捷的静态方法获取当前请求的用户信息
 * <p>
 * 使用示例：
 * <pre>{@code
 * Long userId = RequestContextUtil.getCurrentUserId();
 * String traceId = RequestContextUtil.getCurrentTraceId();
 * UserInfo userInfo = RequestContextUtil.getCurrentUserInfo();
 * }</pre>
 */
public class RequestContextUtil {

    /**
     * 获取当前请求的 HttpServletRequest
     *
     * @return HttpServletRequest，如果不在请求上下文中则返回 null
     */
    @Nullable
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未登录或不在请求上下文中则返回 null
     */
    @Nullable
    public static Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        return UserContextUtils.extractUserId(request);
    }

    /**
     * 获取当前 TraceId
     *
     * @return TraceId
     */
    @Nullable
    public static String getCurrentTraceId() {
        HttpServletRequest request = getCurrentRequest();
        return UserContextUtils.extractTraceId(request);
    }

    /**
     * 获取当前用户完整信息
     *
     * @return 用户信息对象
     */
    @NonNull
    public static UserInfo getCurrentUserInfo() {
        HttpServletRequest request = getCurrentRequest();
        return UserContextUtils.extract(request);
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isLogin() {
        Long userId = getCurrentUserId();
        return userId != null;
    }

    /**
     * 获取当前请求的 URI
     *
     * @return 请求 URI，如果不在请求上下文中则返回 null
     */
    @Nullable
    public static String getCurrentUri() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return request.getRequestURI();
    }

    /**
     * 获取当前请求的 IP 地址
     *
     * @return 客户端 IP，如果不在请求上下文中则返回 null
     */
    @Nullable
    public static String getCurrentClientIp() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // X-Forwarded-For 可能包含多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
