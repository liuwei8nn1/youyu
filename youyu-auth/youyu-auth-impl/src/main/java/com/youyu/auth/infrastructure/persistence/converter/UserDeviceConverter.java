package com.youyu.auth.infrastructure.persistence.converter;

import com.youyu.auth.domain.aggregate.UserDevice;
import com.youyu.auth.infrastructure.persistence.entity.UserDeviceDO;

/**
 * 用户设备转换器
 */
public class UserDeviceConverter {

    /**
     * 领域对象转实体
     */
    public static UserDeviceDO toEntity(UserDevice device) {
        if (device == null) {
            return null;
        }
        UserDeviceDO entity = new UserDeviceDO();
        entity.setId(device.getId());
        entity.setIdentityId(device.getIdentityId());
        entity.setDeviceUniqueId(device.getDeviceUniqueId()); // 使用 deviceUniqueId
        entity.setDeviceName(device.getDeviceName());
        entity.setOs(device.getOs());
        entity.setBrowser(device.getBrowser());
        entity.setIp(device.getIp());
        entity.setUserAgent(device.getUserAgent());
        entity.setLoginTime(device.getLoginTime());
        entity.setStatus(device.getStatus());
        entity.setCreatedAt(device.getCreatedAt());
        entity.setUpdatedAt(device.getUpdatedAt());
        return entity;
    }

    /**
     * 实体转领域对象
     */
    public static UserDevice toDomain(UserDeviceDO entity) {
        if (entity == null) {
            return null;
        }
        return UserDevice.restore(
                entity.getId(),
                entity.getIdentityId(),
                entity.getDeviceUniqueId(), // 使用 deviceUniqueId
                entity.getDeviceName(),
                entity.getOs(),
                entity.getBrowser(),
                entity.getIp(),
                entity.getUserAgent(),
                entity.getLoginTime(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
