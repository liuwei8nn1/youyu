package com.youyu.user.impl.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 用户资料持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_profile")
public class UserProfileDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long identityId; // 关联 user_identity.id（Auth领域主键）
    private String username; // 用户名（用于登录）
    private String nickname;
    private String avatar;
    private String email; // 邮箱（可用于登录）
    private String phone; // 手机号（可用于登录）
    private Integer gender;
    private LocalDateTime birthday;
    private String signature;
}
