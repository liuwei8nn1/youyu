package com.youyu.user.impl.interfaces.controller;

import com.youyu.common.model.Result;
import com.youyu.user.api.dto.CreateUserRequest;
import com.youyu.user.impl.application.service.UserCreateApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户创建控制器
 * 供 auth-service 调用，用于注册时创建用户
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserCreateController {

    private final UserCreateApplicationService userCreateApplicationService;

    /**
     * 创建用户（供auth服务调用）
     */
    @PostMapping("/create")
    public Result<Long> createUser(@RequestBody CreateUserRequest request) {
        Long userId = userCreateApplicationService.createUser(request);
        return Result.success(userId);
    }
}
