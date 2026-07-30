package com.youyu.auth.application.service;

import com.youyu.auth.domain.model.UserDevice;
import com.youyu.auth.domain.repository.UserDeviceRepository;
import com.youyu.auth.infrastructure.persistence.redis.AuthRedisService;
import com.youyu.auth.interfaces.dto.DeviceInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备管理应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceManagementService {

    private final UserDeviceRepository userDeviceRepository;
    private final AuthRedisService authRedisService;

    /**
     * 获取当前用户所有在线设备列表
     *
     * @param identityId 用户身份ID
     * @return 设备列表
     */
    public List<DeviceInfoDTO> getOnlineDevices(Long identityId, Integer userType) {
        log.info("获取在线设备列表，identityId: {}", identityId);

        // 查询数据库中状态为在线的设备
        List<UserDevice> devices = userDeviceRepository.findOnlineByIdentityId(identityId);

        // 过滤出 Redis 中也存在的设备（真正在线）- 使用 deviceId（Long类型）
        return devices.stream()
                .filter(device -> authRedisService.isDeviceOnline(identityId, userType, device.getId()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 踢出指定设备
     *
     * @param identityId 用户身份ID
     * @param targetDeviceId 目标设备ID（UserDevice表的id，Long类型）
     */
    @Transactional
    public void kickDevice(Long identityId, Integer userType, Long targetDeviceId) {
        log.info("踢出设备，identityId: {}, targetDeviceId: {}", identityId, targetDeviceId);

        // 1. 删除 Redis key - 使用 deviceId（Long类型）
        authRedisService.markDeviceOffline(identityId,userType,  targetDeviceId);

        // 2. 更新数据库设备状态 - 使用 deviceId（Long类型）
        userDeviceRepository.markOffline(identityId, userType, targetDeviceId);

        log.info("设备踢出成功，identityId: {}, targetDeviceId: {}", identityId, targetDeviceId);
    }

    /**
     * 转换领域对象为 DTO
     */
    private DeviceInfoDTO convertToDTO(UserDevice device) {
        return DeviceInfoDTO.builder()
                .id(device.getId())
                .deviceId(device.getId()) // 使用 UserDevice 表的 id（Long类型）
                .deviceName(device.getDeviceName())
                .os(device.getOs())
                .browser(device.getBrowser())
                .ip(device.getIp())
                .loginTime(device.getLoginTime())
                .status(device.getStatus())
                .build();
    }
}
