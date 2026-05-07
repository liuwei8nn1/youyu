package com.youyu.framework.context;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 用户上下文持有者
 * <p>
 * 基于 ThreadLocal 管理当前线程的用户信息
 * <p>
 * 典型使用场景：
 * <pre>{@code
 * // 在 Filter/Interceptor 中设置
 * UserContextHolder.setUserInfo(userInfo);
 * try {
 *     // 业务逻辑
 *     UserInfo info = UserContextHolder.getUserInfo();
 * } finally {
 *     // 清理上下文，防止内存泄漏
 *     UserContextHolder.clear();
 * }
 * }</pre>
 *
 * @author LiuWei
 * @since 2026/4/23
 */
public class UserContextHolder {

    private static final ThreadLocal<UserInfo> USER_INFO_HOLDER = new ThreadLocal<>();

    /**
     * 设置用户信息到当前线程上下文
     *
     * @param userInfo 用户信息，可以为 null
     */
    public static void setUserInfo(@Nullable UserInfo userInfo) {
        USER_INFO_HOLDER.set(userInfo);
    }

    /**
     * 获取当前线程的用户信息
     *
     * @return 用户信息，如果未设置则返回 null
     */
    @NonNull
    public static UserInfo getUserInfo() {
        return USER_INFO_HOLDER.get() != null ? USER_INFO_HOLDER.get() : UserInfo.empty;
    }

    /**
     * 清除当前线程的用户信息
     * <p>
     * <b>重要</b>：必须在请求结束时调用此方法，防止内存泄漏
     */
    public static void clear() {
        USER_INFO_HOLDER.remove();
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return true 表示已登录，false 表示未登录
     */
    public static boolean isLogin() {
        UserInfo userInfo = getUserInfo();
        return userInfo.isLogin();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未登录则返回 null
     */
    @Nullable
    public static Long getUserId() {
        UserInfo userInfo = getUserInfo();
        return userInfo.getUserId();
    }

    /**
     * 获取当前 TraceId
     *
     * @return TraceId，如果不存在则返回 null
     */
    @Nullable
    public static String getTraceId() {
        UserInfo userInfo = getUserInfo();
        return userInfo.getTraceId();
    }
}
