package com.youyu.user.impl.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.youyu.framework.datasource.mybatis.LogicDeleteBaseDO;
import lombok.Data;

import java.io.Serial;

/**
 * 平台管理员资料数据对象
 */
@Data
@TableName("platform_user")
public class PlatformUserDO extends LogicDeleteBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联user_identity.id
     */
    private Long identityId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    public static final String ID = "id";
    public static final String IDENTITY_ID = "identity_id";
    public static final String USERNAME = "username";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
}
