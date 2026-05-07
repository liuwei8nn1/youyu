package com.youyu.auth.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设备表实体
 */
@Data
@TableName("user_device")
public class UserDeviceDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long identityId;

    private Integer userType;

    private String deviceUniqueId;

    private String deviceName;

    private String os;

    private String browser;

    private String ip;

    private String userAgent;

    private LocalDateTime loginTime;

    private Integer status;

    public static final String ID = "id";
    public static final String IDENTITY_ID = "identity_id";
    public static final String STATUS = "status";
    public static final String USER_TYPE = "user_type";
    public static final String DEVICE_UNIQUE_ID = "device_unique_id";
    public static final String LOGIN_TIME = "login_time";





}
