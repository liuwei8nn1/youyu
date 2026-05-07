package com.youyu.auth.interfaces.controller;

import com.youyu.auth.api.model.Permission;
import com.youyu.auth.application.service.AdminUserService;
import com.youyu.common.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     * @param reason 禁用原因
     * @return 操作结果
     */
    @PostMapping("disableUser")
    @Permission(Permission.PLATFORM)
    public Result<Void> disableUser(@RequestParam(value = "userId") Long userId,
                                    @RequestParam(value = "userType") Integer userType,
                                    @RequestParam(value = "reason", required = false) String reason) {
        try {
            adminUserService.disableUser(userId, userType, reason);
            return Result.success();
        } catch (Exception e) {
            log.error("禁用用户失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解除用户禁用
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("enableUser")
    @Permission(Permission.PLATFORM)
    public Result<Void> enableUser(@RequestParam(value = "userId") Long userId,
                                   @RequestParam(value = "userType") Integer userType) {
        try {
            adminUserService.enableUser(userId, userType);
            return Result.success();
        } catch (Exception e) {
            log.error("解除用户禁用失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改全局登录模式
     *
     * @param mode 登录模式（MULTI / SINGLE / MAX:n）
     * @return 操作结果
     */
    @PutMapping("updateLoginMode")
    @Permission(Permission.PLATFORM)
    public Result<Void> updateLoginMode(@RequestParam(value = "mode") String mode) {
        try {
            adminUserService.updateGlobalLoginMode(mode);
            return Result.success();
        } catch (Exception e) {
            log.error("修改登录模式失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
