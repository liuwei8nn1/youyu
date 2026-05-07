package com.youyu.auth.interfaces.controller;

import com.youyu.auth.api.dto.CreateUserIdentityRequest;
import com.youyu.auth.api.dto.CreateUserIdentityResponse;
import com.youyu.auth.application.service.UserIdentityApplicationService;
import com.youyu.common.model.Result;
import com.youyu.framework.context.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户身份管理Controller（供内部服务调用）
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserIdentityApplicationService userIdentityApplicationService;

    /**
     * 创建用户身份（供内部服务调用）
     */
    @PostMapping("/createIdentity")
    public Result<CreateUserIdentityResponse> createUserIdentity(@RequestBody CreateUserIdentityRequest request) {
        log.info("内部服务创建用户身份，username: {}, userType: {}", request.getUsername(), request.getUserType());

        // 1. 参数校验
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }

        Integer userType = request.getUserType() != null ? request.getUserType() : UserType.CUSTOMER.getValue();

        CreateUserIdentityResponse response = userIdentityApplicationService.createUserIdentity(request);
        return Result.success(response);
    }

    /**
     * 修改密码
     */
    @PutMapping("/changePassword")
    public Result<Void> changePassword(@RequestParam Long identityId,
                                       @RequestParam Integer userType,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        userIdentityApplicationService.changePassword(identityId, oldPassword, newPassword);
        return Result.success();
    }



}
