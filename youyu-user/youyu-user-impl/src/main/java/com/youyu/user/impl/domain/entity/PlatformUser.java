package com.youyu.user.impl.domain.entity;

import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 平台管理员资料领域模型
 */
@Getter
@Setter
public class PlatformUser extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联user_identity.id
     */
    private Long userId;

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

    /**
     * 创建平台管理员资料
     */
    public static PlatformUser create(Long userId, String username, String phone, String email) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        PlatformUser platformUser = new PlatformUser();
        platformUser.userId = userId;
        platformUser.username = username;
        platformUser.phone = phone;
        platformUser.email = email;
        platformUser.status = 1; // 默认启用
        platformUser.initTime(LocalDateTime.now());
        return platformUser;
    }
}
