package com.youyu.user.impl.interfaces.controller;

import com.youyu.common.model.Result;
import com.youyu.user.api.dto.AddressDTO;
import com.youyu.user.api.dto.UserLoginInfo;
import com.youyu.user.api.dto.UserProfileCreateRequest;
import com.youyu.user.impl.application.service.UserProfileApplicationService;
import com.youyu.user.impl.application.service.UserQueryApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户查询控制器
 * 供 auth-service 调用，用于登录时查询用户信息
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryApplicationService userQueryApplicationService;
    private final UserProfileApplicationService userProfileApplicationService;

    /**
     * 根据用户名查询用户登录信息
     * 供 auth-service 调用
     */
    @GetMapping("/by-username")
    public Result<UserLoginInfo> getByUsername(@RequestParam(name = "username") String username,
                                               @RequestParam(name = "userType")  Integer userType) {
        log.debug("查询用户，username: {}, userType: {}", username, userType);
        return userQueryApplicationService.findByUsername(username)
                .map(Result::success)
                .orElseGet(() -> Result.error("用户不存在"));
    }

    /**
     * 根据手机号查询用户登录信息
     * 供 auth-service 调用
     */
    @GetMapping("/by-phone/{phone}")
    public Result<UserLoginInfo> getByPhone(@PathVariable String phone,
                                            @RequestParam Integer userType) {
        log.debug("查询用户，phone: {}, userType: {}", phone, userType);
        return userQueryApplicationService.findByPhone(phone)
                .map(Result::success)
                .orElseGet(() -> Result.error("用户不存在"));
    }

    /**
     * 根据邮箱查询用户登录信息
     * 供 auth-service 调用
     */
    @GetMapping("/by-email/{email}")
    public Result<UserLoginInfo> getByEmail(@PathVariable String email,
                                            @RequestParam Integer userType) {
        log.debug("查询用户，email: {}, userType: {}", email, userType);
        return userQueryApplicationService.findByEmail(email)
                .map(Result::success)
                .orElseGet(() -> Result.error("用户不存在"));
    }

    /**
     * 查询用户的默认收货地址
     * 供 order-service 调用
     *
     * @param userId 用户ID
     * @return 默认收货地址
     */
    @GetMapping("/default-address/{userId}")
    public Result<AddressDTO> getDefaultAddress(@PathVariable Long userId) {
        return userQueryApplicationService.findDefaultAddress(userId)
                .map(Result::success)
                .orElseGet(() -> Result.error("未找到默认收货地址"));
    }

    // ==================== 注册相关接口 ====================

    /**
     * 检查用户名是否存在
     */
    @PostMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam(value = "username") String username,
                                         @RequestParam(value = "userType", required = false, defaultValue = "1") Integer userType) {
        boolean exists = userProfileApplicationService.existsByUsername(username, userType);
        return Result.success(exists);
    }

    /**
     * 检查手机号是否存在
     */
    @PostMapping("/check-phone")
    public Result<Boolean> checkPhone(@RequestParam(value = "phone") String phone,
                                      @RequestParam(value = "userType", required = false, defaultValue = "1") Integer userType) {
        boolean exists = userProfileApplicationService.existsByPhone(phone, userType);
        return Result.success(exists);
    }

    /**
     * 检查邮箱是否存在
     */
    @PostMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam(value = "email") String email,
                                      @RequestParam(value = "userType", required = false, defaultValue = "1") Integer userType) {
        boolean exists = userProfileApplicationService.existsByEmail(email, userType);
        return Result.success(exists);
    }

    /**
     * 创建用户资料（供auth服务调用）
     */
    @PostMapping("/create-profile")
    public Result<Long> createUserProfile(@RequestBody UserProfileCreateRequest request) {
        Long userId = userProfileApplicationService.registerCustomer(request);
        return Result.success(userId);
    }
}
