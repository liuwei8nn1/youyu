package com.youyu.auth.interfaces.controller;

import com.youyu.auth.api.model.Permission;
import com.youyu.auth.application.service.DeviceManagementService;
import com.youyu.auth.application.service.LoginApplicationService;
import com.youyu.auth.infrastructure.persistence.redis.AuthRedisService;
import com.youyu.auth.interfaces.dto.DeviceInfoDTO;
import com.youyu.auth.interfaces.dto.LoginRequest;
import com.youyu.auth.interfaces.dto.LoginResponse;
import com.youyu.auth.interfaces.dto.RefreshTokenResponse;
import com.youyu.common.model.Result;
import com.youyu.framework.context.UserInfo;
import com.youyu.framework.context.web.resolver.ProxyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
public class AuthController {

    private final LoginApplicationService loginApplicationService;
    private final DeviceManagementService deviceManagementService;
    private final AuthRedisService authRedisService;

    /**
     * 用户登录
     *
     * @param request   登录请求
     * @return 登录响应（包含JWT Token）
     */
    @PostMapping("/login")
    @Permission(Permission.NONE)
    public Result<LoginResponse> login(ProxyRequest q, LoginRequest request) {
        if (request.getDeviceUniqueId() == null) {
            request.setDeviceUniqueId(q.getDeviceUniqueId());
        }
        LoginResponse response = loginApplicationService.login(request, q.getClientIp(), q.getUserAgent());
        return Result.success(response);
    }

    /**
     * 刷新 Token
     * <p>
     * 注意：deviceId 从 Refresh Token 中解析，不需要前端传递
     *
     * @param refreshToken Refresh Token
     * @return 新的 Token
     */
    @PostMapping("/refresh")
    @Permission(Permission.NONE)
    public Result<RefreshTokenResponse> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        try {
            RefreshTokenResponse response = loginApplicationService.refreshToken(refreshToken);
            return Result.success(response);
        } catch (Exception e) {
            log.error("刷新Token失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出（当前设备）
     * @return 操作结果
     */
    @PostMapping("/logout")
    @Permission(Permission.LOGIN)
    public Result<Void> logout(ProxyRequest q) {
        try {
            UserInfo userInfo = q.getUserInfo();
            loginApplicationService.logout(userInfo.getUserId(),userInfo.getUserType(),userInfo.getDeviceId());
            return Result.success();
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退出所有设备
     *
     * @return 操作结果
     */
    @PostMapping("/logout-all")
    @Permission(Permission.LOGIN)
    public Result<Void> logoutAll(ProxyRequest q) {
        try {
            UserInfo userInfo = q.getUserInfo();
            loginApplicationService.logoutAll(userInfo.getUserId(), userInfo.getUserType(), q.getDeviceId());
            return Result.success();
        } catch (Exception e) {
            log.error("退出所有设备失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户所有在线设备列表
     *
     * @return 设备列表
     */
    @GetMapping("/sessions")
    @Permission(Permission.LOGIN)
    public Result<List<DeviceInfoDTO>> getSessions(ProxyRequest q) {
        try {
            List<DeviceInfoDTO> devices = deviceManagementService.getOnlineDevices(q.getUserId(), q.getUserType());
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取设备列表失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 踢出指定设备
     *
     * @param targetDeviceId 目标设备ID（UserDevice表的id，Long类型）
     * @return 操作结果
     */
    @PostMapping("kickSession")
    public Result<Void> kickSession(ProxyRequest q, @RequestParam Long targetDeviceId) {
        try {
            deviceManagementService.kickDevice(q.getUserId(), q.getUserType(), targetDeviceId);
            return Result.success();
        } catch (Exception e) {
            log.error("踢出设备失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // ==================== 在线状态查询接口 ====================

    /**
     * 检查用户是否在线
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return true=在线，false=离线
     */
    @GetMapping("checkPresence")
    @Permission(Permission.EMP)
    public Result<Boolean> checkPresence(@RequestParam Long userId,
                                         @RequestParam Integer userType) {
        try {
            boolean online = authRedisService.isUserOnline(userId, userType);
            return Result.success(online);
        } catch (Exception e) {
            log.error("检查在线状态失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户最后活跃时间
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 最后活跃时间戳（毫秒），null表示离线
     */
    @GetMapping("getLastActiveTime")
    @Permission(Permission.EMP)
    public Result<Long> getLastActiveTime(@RequestParam Long userId,
                                          @RequestParam Integer userType) {
        try {
            Long lastActiveTime = authRedisService.getLastActiveTime(userId, userType);
            return Result.success(lastActiveTime);
        } catch (Exception e) {
            log.error("获取最后活跃时间失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户最后活跃信息（包含设备ID和时间戳）
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 最后活跃信息，null表示离线
     */
    @GetMapping("getPresenceInfo")
    @Permission(Permission.EMP)
    public Result<AuthRedisService.PresenceInfo> getPresenceInfo(@RequestParam Long userId,
                                                                 @RequestParam Integer userType) {
        try {
            AuthRedisService.PresenceInfo info = authRedisService.getLastActiveInfo(userId, userType);
            return Result.success(info);
        } catch (Exception e) {
            log.error("获取在线状态信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
