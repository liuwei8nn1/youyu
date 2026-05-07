package com.youyu.framework.context;

import com.youyu.common.util.NumberUtil;
import com.youyu.common.util.StringUtil;
import com.youyu.framework.context.web.util.TraceIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 用户上下文工具类
 * <p>
 * 提供从 HTTP 请求中提取用户信息并自动设置到 UserContextHolder 的功能
 * <p>
 * 网关需要在转发请求时添加以下请求头：
 * <ul>
 *   <li>X-User-Id: 用户ID</li>
 *   <li>X-User-Name: 用户名</li>
 *   <li>X-User-Type: 用户类型（user/merchant/ADMIN）</li>
 *   <li>X-User-Roles: 角色列表（逗号分隔）</li>
 *   <li>X-Device-Id: 设备ID</li>
 *   <li>X-Trace-Id: 链路追踪ID</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 在 Filter 中
 * public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) {
 *     HttpServletRequest request = (HttpServletRequest) req;
 *     try {
 *         // 提取并设置用户信息到 ThreadLocal
 *         UserInfo userInfo = UserContextUtils.extractAndSet(request);
 *         
 *         // 业务逻辑...
 *         
 *     } finally {
 *         // 清理上下文
 *         UserContextHolder.clear();
 *     }
 * }
 * }</pre>
 *
 * @author LiuWei
 * @since 2026/4/23
 */
public class UserContextUtils {

    /** 用户ID请求头 */
    public static final String USER_ID_HEADER = "X-User-Id";
    /** 用户名请求头 */
    public static final String USER_NAME_HEADER = "X-User-Name";
    /** 用户类型请求头 */
    public static final String USER_TYPE_HEADER = "X-User-Type";
    /** 用户角色请求头 */
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    /** 设备ID请求头 */
    public static final String DEVICE_ID_HEADER = "X-Device-Id";

    /**
     * 从 HTTP 请求中提取用户信息并设置到 UserContextHolder
     * <p>
     * 这是最常用的方法，一次性完成提取和设置
     *
     * @param request HTTP 请求
     * @return 提取的用户信息对象
     */
    @NonNull
    public static UserInfo extractAndSet(@Nullable HttpServletRequest request) {
        UserInfo userInfo = extract(request);
        UserContextHolder.setUserInfo(userInfo);
        return userInfo;
    }

    /**
     * 从 HTTP 请求中提取用户信息（不设置到 ThreadLocal）
     *
     * @param request HTTP 请求
     * @return 用户信息对象，如果请求为空或缺少用户ID则返回空的 UserInfo
     */
    @NonNull
    public static UserInfo extract(@Nullable HttpServletRequest request) {
        if (request == null) {
            return UserInfo.builder().build();
        }

        String userIdStr = request.getHeader(USER_ID_HEADER);
        if (StringUtil.isEmpty(userIdStr)) {
            return UserInfo.builder().build();
        }

        Long userId = NumberUtil.getLong(userIdStr);
        String username = request.getHeader(USER_NAME_HEADER);
        Integer userType = NumberUtil.getInt(request.getHeader(USER_TYPE_HEADER));
        String roles = request.getHeader(USER_ROLES_HEADER);
        Long deviceId = NumberUtil.getLong(request.getHeader(DEVICE_ID_HEADER));
        String traceId = request.getHeader(TraceIdGenerator.TRACE_ID_HEADER);

        return UserInfo.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .roles(roles)
                .deviceId(deviceId)
                .traceId(traceId)
                .build();
    }

    /**
     * 从 HTTP 请求中提取用户ID
     *
     * @param request HTTP 请求
     * @return 用户ID，如果不存在则返回 null
     */
    @Nullable
    public static Long extractUserId(@Nullable HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userIdStr = request.getHeader(USER_ID_HEADER);
        return StringUtil.isEmpty(userIdStr) ? null : NumberUtil.getLong(userIdStr);
    }

    /**
     * 从 HTTP 请求中提取 TraceId
     * <p>
     *
     * @param request HTTP 请求
     */
    @Nullable
    public static String extractTraceId(@Nullable HttpServletRequest request) {
        return TraceIdGenerator.getTraceId(request);
    }
}
