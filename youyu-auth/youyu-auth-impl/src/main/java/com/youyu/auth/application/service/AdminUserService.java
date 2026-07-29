package com.youyu.auth.application.service;

import com.youyu.auth.domain.aggregate.UserDevice;
import com.youyu.auth.domain.repository.UserDeviceRepository;
import com.youyu.auth.infrastructure.persistence.config.LoginModeConfig;
import com.youyu.auth.infrastructure.persistence.redis.AuthRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台管理用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AuthRedisService authRedisService;
    private final UserDeviceRepository userDeviceRepository;
    private final LoginModeConfig loginModeConfig;
    private final LoginApplicationService loginApplicationService;

    /**
     * 禁用用户
     *
     * @param identityId 用户身份ID
     * @param reason 禁用原因
     */
    @Transactional
    public void disableUser(Long identityId, Integer userType,  String reason) {
        log.info("禁用用户，identityId: {}, reason: {}", identityId, reason);

        // 1. 设置 Redis 禁用标记
        authRedisService.disableUser(identityId, userType, reason);

        // 2. 退出所有设备
        logoutAllDevices(identityId, userType);

        log.info("用户禁用成功，identityId: {}", identityId);
    }

    /**
     * 解除用户禁用
     *
     * @param identityId 用户身份ID
     */
    public void enableUser(Long identityId,Integer userType) {
        log.info("解除用户禁用，identityId: {}", identityId);

        // 删除 Redis 禁用标记
        authRedisService.enableUser(identityId, userType);

        log.info("用户解除禁用成功，identityId: {}", identityId);
    }

    /**
     * 修改全局登录模式
     *
     * @param mode 登录模式（MULTI / SINGLE / MAX:n）
     */
    public void updateGlobalLoginMode(String mode) {
        log.info("修改全局登录模式，mode: {}", mode);

        // 验证模式有效性
        LoginModeConfig.LoginMode.parse(mode);

        // 更新配置（实际应更新 Nacos 配置中心）
        loginModeConfig.setGlobal(mode);

        log.info("全局登录模式修改成功，mode: {}", mode);
    }

    /**
     * 退出用户所有设备
     *
     * @param identityId 用户身份ID
     */
    private void logoutAllDevices(Long identityId,Integer userType) {
        log.info("退出用户所有设备，identityId: {}", identityId);

        // 查询所有在线设备
        List<UserDevice> onlineDevices = userDeviceRepository.findOnlineByIdentityId(identityId);

        // 批量删除 Redis key
        List<Long> deviceIds = onlineDevices.stream()
                .map(UserDevice::getId)
                .collect(Collectors.toList());
        if (!deviceIds.isEmpty()) {
            authRedisService.batchMarkDeviceOffline(identityId, userType, deviceIds);
        }

        // 更新数据库所有设备状态为离线
        for (UserDevice device : onlineDevices) {
            userDeviceRepository.markOffline(identityId, userType, device.getId());
        }

        log.info("退出用户所有设备成功，identityId: {}, count: {}", identityId, onlineDevices.size());
    }
}
