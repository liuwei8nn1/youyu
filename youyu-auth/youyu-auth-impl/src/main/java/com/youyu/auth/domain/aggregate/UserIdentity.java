package com.youyu.auth.domain.aggregate;

import com.youyu.common.util.PasswordUtil;
import com.youyu.framework.context.UserType;
import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Setter;

/**
 * 用户身份聚合根 - 统一管理登录凭证
 */
@Getter
@Setter
public class UserIdentity extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Getter
    private String userId;
    @Getter
    private String username;
    @Getter
    private String password;
    /**
     * 用户类型
     * @see UserType
     */
    @Getter
    private Integer userType;
    @Getter
    private Boolean enabled;

    // 包级私有构造函数，供MapStruct使用
    UserIdentity() {
    }

    public static final String USER_ID = "user_id";
    public static final String USER_TYPE = "user_type";


    /**
     * 创建新用户身份
     */
    public static UserIdentity create(String username, String encryptedPassword, UserType userType) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (userType == null) {
            throw new IllegalArgumentException("用户类型不能为空");
        }

        UserIdentity identity = new UserIdentity();
        identity.username = username;
        identity.password = encryptedPassword;
        identity.userType = userType.getValue();
        identity.enabled = true;
        identity.initTime(LocalDateTime.now());
        return identity;
    }

    /**
     * 从数据库恢复聚合根
     */
    public static UserIdentity restore(Long id, String username, String password, 
                                       Integer userType, Boolean enabled,
                                       LocalDateTime createTime, LocalDateTime updateTime) {
        UserIdentity identity = new UserIdentity();
        identity.setId(id);
        identity.username = username;
        identity.password = password;
        identity.userType = userType;
        identity.enabled = enabled != null ? enabled : true;
        identity.setCreatedAt(createTime);
        identity.setUpdatedAt(updateTime);
        return identity;
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword, String encryptedPassword) {
        return com.youyu.common.util.PasswordUtil.matches(rawPassword, encryptedPassword);
    }

    /**
     * 加密密码（用于创建用户时）
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String rawPassword) {
        return PasswordUtil.encode(rawPassword);
    }

    /**
     * 修改密码
     */
    public void changePassword(String newEncryptedPassword) {
        if (newEncryptedPassword == null || newEncryptedPassword.isEmpty()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        this.password = newEncryptedPassword;
    }

    /**
     * 修改密码（包含原密码验证）
     *
     * @param oldRawPassword 原密码（明文）
     * @param newRawPassword 新密码（明文）
     */
    public void changePasswordWithVerification(String oldRawPassword, String newRawPassword) {
        // 1. 验证原密码
        if (oldRawPassword == null || oldRawPassword.isEmpty()) {
            throw new IllegalArgumentException("原密码不能为空");
        }
        if (!verifyPassword(oldRawPassword, this.password)) {
            throw new IllegalArgumentException("原密码错误");
        }

        // 2. 验证新密码
        if (newRawPassword == null || newRawPassword.length() < 6) {
            throw new IllegalArgumentException("新密码长度不能少于6位");
        }

        // 3. 加密并更新
        String encryptedNewPassword = encryptPassword(newRawPassword);
        this.password = encryptedNewPassword;
    }

    /**
     * 禁用用户
     */
    public void disable() {
        if (!this.enabled) {
            throw new IllegalStateException("用户已处于禁用状态");
        }
        this.enabled = false;
    }

    /**
     * 启用用户
     */
    public void enable() {
        if (this.enabled) {
            throw new IllegalStateException("用户已处于启用状态");
        }
        this.enabled = true;
    }

    /**
     * 检查用户是否可用
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.enabled);
    }

    /**
     * 检查是否为平台管理员
     */
    public boolean isPlatformAdmin() {
        return Objects.equals(UserType.PLATFORM.getValue(), this.userType);
    }

    /**
     * 检查是否为企业员工
     */
    public boolean isEnterprise() {
        return Objects.equals(UserType.ENTERPRISE.getValue(), this.userType);
    }

    /**
     * 检查是否为外部顾客
     */
    public boolean isCustomer() {
        return Objects.equals(UserType.CUSTOMER.getValue(), this.userType);
    }
}
