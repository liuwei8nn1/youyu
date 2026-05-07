package com.youyu.auth.interfaces.controller;

import com.youyu.auth.api.dto.RegisterRequest;
import com.youyu.auth.api.dto.RegisterResponse;
import com.youyu.auth.api.model.Permission;
import com.youyu.auth.application.service.RegisterApplicationService;
import com.youyu.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户注册控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterApplicationService registerApplicationService;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册响应
     */
    @PostMapping("/register")
    @Permission(Permission.NONE)
    public Result<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = registerApplicationService.register(request);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            log.warn("注册参数校验失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error("注册失败: " + e.getMessage());
        }
    }
}
