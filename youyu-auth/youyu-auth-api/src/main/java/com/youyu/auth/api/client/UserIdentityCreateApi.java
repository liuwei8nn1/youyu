package com.youyu.auth.api.client;

import com.youyu.auth.api.dto.CreateUserIdentityRequest;
import com.youyu.auth.api.dto.CreateUserIdentityResponse;
import com.youyu.common.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户身份创建 Feign 客户端
 * 用于其他微服务调用 youyu-auth 创建用户身份
 */
@FeignClient(name = "youyu-auth", path = "/user", url = "${service.url.youyu-auth:}")
public interface UserIdentityCreateApi {

    /**
     * 创建用户身份
     */
    @PostMapping("/createUserIdentity")
    Result<CreateUserIdentityResponse> createUserIdentity(@RequestBody CreateUserIdentityRequest request);
}
