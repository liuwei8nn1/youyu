package com.youyu.auth.domain.repository;

import com.youyu.auth.domain.model.UserDevice;

import java.util.List;
import java.util.Optional;

/**
 * 用户设备仓储接口
 */
public interface UserDeviceRepository {

    /**
     * 保存设备记录（新增或更新）
     */
    void save(UserDevice device);

    /**
     * 根据用户身份ID和设备唯一ID查询
     */
    Optional<UserDevice> findByIdentityIdAndDeviceUniqueId(Long identityId, String deviceUniqueId);

    /**
     * 根据ID查询设备
     */
    Optional<UserDevice> findById(Long id);

    /**
     * 查询用户的所有设备（按登录时间倒序）
     */
    List<UserDevice> findByIdentityIdOrderByLoginTimeDesc(Long identityId);

    default List<UserDevice> findOnlineByIdentityId(Long identityId){
        return this.findOnlineByIdentityId(identityId,null);
    }

    /**
     * 查询用户的在线设备
     */
    List<UserDevice> findOnlineByIdentityId(Long identityId, Long excludeDeviceId);

    /**
     * 更新设备信息
     */
    void update(UserDevice device);

    /**
     * 删除设备记录
     */
    void delete(Long id);

    /**
     * 标记设备为离线
     */
    void markOffline(Long identityId,Integer userType, Long deviceId);
}
