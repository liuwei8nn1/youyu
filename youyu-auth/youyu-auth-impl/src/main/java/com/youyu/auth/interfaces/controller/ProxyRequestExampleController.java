package com.youyu.auth.interfaces.controller;

import com.youyu.auth.api.model.Permission;
import com.youyu.framework.context.web.resolver.ProxyRequest;
import com.youyu.common.model.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProxyRequest 使用示例控制器
 * <p>
 * 展示如何在 Controller 中使用 ProxyRequest 获取请求上下文和用户信息
 *
 * @author LiuWei
 * @since 2026/4/23
 */
@Slf4j
@RestController
@RequestMapping("/proxy-request")
@RequiredArgsConstructor
public class ProxyRequestExampleController {

    /**
     * 示例1：基础用法 - 获取用户信息
     */
    @GetMapping("/user-info")
    @Permission(Permission.LOGIN)
    public Result<UserInfoVO> getUserInfo(ProxyRequest proxyRequest) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(proxyRequest.getUserId());
        vo.setUsername(proxyRequest.getUsername());
        vo.setUserType(proxyRequest.getUserType());
        vo.setLogin(proxyRequest.isLogin());
        vo.setDeviceId(proxyRequest.getDeviceId());
        vo.setTraceId(proxyRequest.getTraceId());
        vo.setRoles(proxyRequest.getRoles());
        
        log.info("查询用户信息: userId={}, username={}", vo.getUserId(), vo.getUsername());
        
        return Result.success(vo);
    }

    /**
     * 示例2：获取请求信息
     */
    @GetMapping("/request-info")
    @Permission(Permission.LOGIN)
    public Result<RequestInfoVO> getRequestInfo(ProxyRequest proxyRequest) {
        RequestInfoVO vo = new RequestInfoVO();
        vo.setClientIp(proxyRequest.getClientIp());
        vo.setRequestUri(proxyRequest.getRequestUri());
        vo.setRequestMethod(proxyRequest.getRequestMethod());
        vo.setUserId(proxyRequest.getUserId());
        
        log.info("请求信息: IP={}, URI={}, Method={}", 
                 vo.getClientIp(), vo.getRequestUri(), vo.getRequestMethod());
        
        return Result.success(vo);
    }

    /**
     * 示例3：与其他参数组合使用
     */
    @GetMapping("/list")
    @Permission(Permission.PLATFORM)
    public Result<String> platformList(ProxyRequest proxyRequest, @Validated QueryDTO query) {
        // 记录操作日志
        log.info("管理员 {} (ID: {}) 查询列表，IP: {}, 查询条件: {}", 
                 proxyRequest.getUsername(),
                 proxyRequest.getUserId(),
                 proxyRequest.getClientIp(),
                 query);
        
        // 业务逻辑...
        String result = "查询成功，共 " + query.getPageSize() + " 条数据";
        
        return Result.success(result);
    }

    /**
     * 示例4：权限控制示例
     */
    @GetMapping("/protected-resource")
    @Permission(Permission.PLATFORM)
    public Result<String> accessProtectedResource(ProxyRequest proxyRequest) {
        // 验证用户是否登录
        if (!proxyRequest.isLogin()) {
            return Result.error("请先登录");
        }
        
        // 根据用户类型进行不同的处理
        Integer userType = proxyRequest.getUserType();
        String message;
        
        if (userType != null && userType == 1) { // ADMIN
            message = "欢迎管理员访问";
        } else if (userType != null && userType == 2) { // merchant
            message = "欢迎员工访问";
        } else {
            message = "欢迎普通用户访问";
        }
        
        return Result.success(message);
    }

    // ==================== DTO 定义 ====================

    @Data
    public static class UserInfoVO {
        private Long userId;
        private String username;
        private Integer userType;
        private Boolean login;
        private Long deviceId;
        private String traceId;
        private String roles;
    }

    @Data
    public static class RequestInfoVO {
        private String clientIp;
        private String requestUri;
        private String requestMethod;
        private Long userId;
    }

    @Data
    public static class QueryDTO {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String keyword;
    }
}
