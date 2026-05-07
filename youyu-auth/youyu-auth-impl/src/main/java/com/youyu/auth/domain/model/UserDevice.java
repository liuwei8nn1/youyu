package com.youyu.auth.domain.model;

import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设备聚合根
 */
@Getter
public class UserDevice extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

	// 供仓储实现回填ID
	@Setter
	@Getter
    private Long id;

	@Getter
    private Long identityId;
    private String deviceUniqueId;
	/**
	 * -- SETTER --
	 *  设置设备名称
	 */
	@Setter
	private String deviceName;
    private String os;
    private String browser;
    private String ip;
    private String userAgent;
    private LocalDateTime loginTime;
    private Integer status; // 1-在线 0-已登出

    public UserDevice() {
    }

    /**
     * 创建新的设备记录
     */
    public static UserDevice create(Long identityId, String deviceUniqueId, String ip, String userAgent) {
        UserDevice device = new UserDevice();
        device.identityId = identityId;
        device.deviceUniqueId = deviceUniqueId;
        device.ip = ip;
        device.userAgent = userAgent;
        device.loginTime = LocalDateTime.now();
        device.status = 1; // 在线
        device.initTime(LocalDateTime.now());
        return device;
    }

    /**
     * 从数据库恢复对象
     */
    public static UserDevice restore(Long id, Long identityId, String deviceUniqueId, String deviceName,
                                     String os, String browser, String ip, String userAgent,
                                     LocalDateTime loginTime, Integer status,
                                     LocalDateTime createTime, LocalDateTime updateTime) {
        UserDevice device = new UserDevice();
        device.id = id;
        device.identityId = identityId;
        device.deviceUniqueId = deviceUniqueId;
        device.deviceName = deviceName;
        device.os = os;
        device.browser = browser;
        device.ip = ip;
        device.userAgent = userAgent;
        device.loginTime = loginTime;
        device.status = status != null ? status : 1;
        device.setCreatedAt(createTime);
        device.setUpdatedAt(updateTime);
        return device;
    }

    /**
     * 更新设备信息（重新登录）
     */
    public void updateLoginInfo(String ip, String userAgent) {
        this.ip = ip;
        this.userAgent = userAgent;
        this.loginTime = LocalDateTime.now();
        this.status = 1;
    }

	/**
     * 标记为离线
     */
    public void markOffline() {
        this.status = 0;
    }

    /**
     * 是否在线
     */
    public boolean isOnline() {
        return this.status != null && this.status == 1;
    }
}
