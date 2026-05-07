package com.youyu.auth.api.client;

import com.youyu.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 认证服务 Feign 客户端
 * <p>
 * 用于其他微服务调用 youyu-auth 获取用户权限信息
 * <p>
 * 工作模式:
 * - 如果配置了 service.url.youyu-auth: 直接使用该 URL(K8s 环境,负载均衡由 K8s 处理)
 * - 如果未配置 service.url.youyu-auth: 使用 name 从注册中心查找(传统微服务架构,客户端负载均衡)
 */
@FeignClient(name = "youyu-auth", path = "/role-permission", url = "${service.url.youyu-auth:}")
public interface AuthFeignClient {

    /**
     * 获取用户的权限码列表
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 权限码列表
     */
    @GetMapping("/getUserPermissions")
    Result<List<String>> getUserPermissions(@RequestParam("userId") Long userId,
                                             @RequestParam("userType") Integer userType);
}
