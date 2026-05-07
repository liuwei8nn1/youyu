package com.youyu.framework.context.web.resolver;

import java.util.List;
import java.util.Locale;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youyu.framework.context.I18N;
import com.youyu.framework.context.*;
import com.youyu.framework.context.web.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * 代理请求上下文对象
 * <p>
 * 可在 Controller 方法参数中直接注入，提供便捷的请求上下文访问
 * <p>
 * 使用示例：
 * <pre>{@code
 * @GetMapping("/list")
 * public Result<Page<VO>> list(ProxyRequest proxyRequest, @Validated QueryDTO query) {
 *     // 获取 HttpServletRequest
 *     HttpServletRequest request = proxyRequest.getRequest();
 *     
 *     // 获取 HttpServletResponse
 *     HttpServletResponse response = proxyRequest.getResponse();
 *     
 *     // 获取当前用户信息
 *     UserInfo userInfo = proxyRequest.getUserInfo();
 *     Long userId = proxyRequest.getUserId();
 *     
 *     // 后续可扩展更多便捷方法...
 *     
 *     return Result.success(service.list(query));
 * }
 * }</pre>
 *
 * @author LiuWei
 * @since 2026/4/23
 */
@Getter
public class ProxyRequest {

    /**
     * HTTP 请求对象
     */
    private final HttpServletRequest request;

    /**
     * HTTP 响应对象
     */
    private final HttpServletResponse response;

    /**
     * 当前用户信息（从 UserContextHolder 获取）
     */
    @NonNull
    private final UserInfo userInfo;

    /**
     * 构造函数
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    public ProxyRequest(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.userInfo = UserContextHolder.getUserInfo();
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，未登录返回 null
     */
    public Long getUserId() {
        return userInfo.getUserId();
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名，未登录返回 null
     */
    public String getUsername() {
        return userInfo.getUsername();
    }

    /**
     * 获取当前用户类型
     *
     * @see UserType
     * @return 用户类型，未登录返回 null
     */
    public Integer getUserType() {
        return userInfo.getUserType();
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return true-已登录，false-未登录
     */
    public boolean isLogin() {
        return userInfo.isLogin();
    }

    /**
     * 获取设备ID
     *
     * @return 设备ID，未登录返回 null
     */
    public Long getDeviceId() {
        return userInfo.getDeviceId();
    }

    /**
     * 获取追踪ID
     *
     * @return 追踪ID
     */
    public String getTraceId() {
        return userInfo.getTraceId();
    }

    /**
     * 获取角色列表
     *
     * @return 角色列表（逗号分隔），未登录返回 null
     */
    public String getRoles() {
        return userInfo.getRoles();
    }

    /**
     * 获取角色列表
     *
     * @return 角色列表，空列表表示无角色(放回的空列表是不可变的，不会被修改)
     */
    public List<String> getRoleList() {
        return userInfo.getRoleList();
    }

    /**
     * 获取国际化需要使用的语种
     */
    public Locale getLocale() {
        return I18N.localeSupplier.get();
    }

    // ==================== 后续可扩展的方法 ====================

    /**
     * 获取客户端IP地址
     *
     * @return IP地址
     */
    public String getClientIp() {
        return ServletUtil.getClientIP(request);
    }

    /**
     * 获取请求URI
     *
     * @return URI
     */
    public String getRequestUri() {
        return request.getRequestURI();
    }

    /**
     * 获取请求方法
     *
     * @return HTTP方法（GET/POST等）
     */
    public String getRequestMethod() {
        return request.getMethod();
    }


    transient String deviceUniqueId;
    /**
     * 根据请求头信息生成唯一id
     * @return 设备唯一id
     */
    public String getDeviceUniqueId(){
	    if (deviceUniqueId == null) {
            deviceUniqueId = ServletUtil.getDeviceUniqueId(request);
	    }
        return deviceUniqueId;
    }

    public String getUserAgent(){
        return request.getHeader(ServletUtil.USER_AGENT);
    }

    // ==================== 分页相关方法 ====================

    /**
     * 获取 MyBatis-Plus 分页对象
     * 如果前端没传分页信息则默认是第一页，每页10条
     *
     * @return IPage 对象
     */
    public <T> Page<T> getPage() {
        String pageParam = request.getParameter("page");
        String pageSizeParam = request.getParameter("pageSize");
        
        int current = 1;
        int size = 10;
        
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                current = Integer.parseInt(pageParam);
                if (current <= 0) {
                    current = 1;
                }
            } catch (NumberFormatException e) {
                current = 1;
            }
        }
        
        if (pageSizeParam != null && !pageSizeParam.isEmpty()) {
            try {
                size = Integer.parseInt(pageSizeParam);
                if (size <= 0) {
                    size = 10;
                }
            } catch (NumberFormatException e) {
                size = 10;
            }
        }
        
        return new Page<>(current, size);
    }

}
