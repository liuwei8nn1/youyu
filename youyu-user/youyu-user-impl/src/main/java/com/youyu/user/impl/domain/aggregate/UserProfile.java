package com.youyu.user.impl.domain.aggregate;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户资料聚合根
 * 负责管理用户的个人资料信息（昵称、头像、邮箱等）
 */
@Getter
public class UserProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    private Long identityId; // 关联 user_identity.id（Auth领域主键）
    private String username; // 用户名（用于登录）
    private String nickname;
    private String avatar;
    private String email; // 邮箱（可用于登录）
    private String phone; // 手机号（可用于登录）
    private Integer gender; // 0-未知, 1-男, 2-女
    private LocalDateTime birthday;
    private String signature; // 个性签名
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public UserProfile() {
    }

    /**
     * 创建用户资料
     */
    public static UserProfile create(Long identityId, String username, String nickname, String phone) {
        if (identityId == null || identityId <= 0) {
            throw new IllegalArgumentException("用户身份ID必须大于0");
        }
        if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }

        UserProfile profile = new UserProfile();
        profile.identityId = identityId;
        profile.username = username;
        profile.nickname = nickname != null ? nickname : "用户" + identityId;
        profile.phone = phone;
        profile.gender = 0;
        profile.createTime = LocalDateTime.now();
        profile.updateTime = LocalDateTime.now();
        return profile;
    }

    /**
     * 从持久化恢复
     */
    public static UserProfile restore(Long id, Long identityId, String username, String nickname, String avatar,
                                     String email, String phone, Integer gender,
                                     LocalDateTime birthday, String signature,
                                     LocalDateTime createTime, LocalDateTime updateTime) {
        UserProfile profile = new UserProfile();
        profile.id = id;
        profile.identityId = identityId;
        profile.username = username;
        profile.nickname = nickname;
        profile.avatar = avatar;
        profile.email = email;
        profile.phone = phone;
        profile.gender = gender != null ? gender : 0;
        profile.birthday = birthday;
        profile.signature = signature;
        profile.createTime = createTime;
        profile.updateTime = updateTime;
        return profile;
    }

    /**
     * 更新用户资料
     */
    public void updateProfile(String nickname, String avatar, String email, 
                             String phone, Integer gender, LocalDateTime birthday, String signature) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
        if (avatar != null) {
            this.avatar = avatar;
        }
        if (email != null) {
            this.email = email;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (birthday != null) {
            this.birthday = birthday;
        }
        if (signature != null) {
            this.signature = signature;
        }
        this.updateTime = LocalDateTime.now();
        validate();
    }

    /**
     * 验证数据有效性
     */
    public void validate() {
        if (identityId == null || identityId <= 0) {
            throw new IllegalArgumentException("用户身份ID必须大于0");
        }
        if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }
}
