package com.youyu.auth.infrastructure.persistence.entity;

import java.io.Serial;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.context.UserType;
import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Data;

/**
 * 用户身份数据对象 - 对应user_identity表
 */
@Data
@TableName("user_identity")
public class UserIdentityDO extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
    /** 用户类型 {@link UserType} */
    private Integer userType;
    /** 是否启用 */
    private Boolean enabled;
    
    /** @see #username */
    public static final String USERNAME = "username";
    /** @see #password */
    public static final String PASSWORD = "password";
    /** @see #userType */
    public static final String USER_TYPE = "user_type";
    /** @see #enabled */
    public static final String ENABLED = "enabled";

}
