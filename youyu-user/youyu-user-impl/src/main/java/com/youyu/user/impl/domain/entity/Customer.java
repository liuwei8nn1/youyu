package com.youyu.user.impl.domain.entity;

import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 外部顾客领域模型
 */
@Getter
@Setter
public class Customer extends BaseDO implements Serializable {

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
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 创建外部顾客
     */
    public static Customer create(Long userId, String username, String phone, String email) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        Customer customer = new Customer();
        customer.userId = userId;
        customer.username = username;
        customer.phone = phone;
        customer.email = email;
        customer.status = 1; // 默认启用
        customer.registerTime = LocalDateTime.now();
        customer.initTime(LocalDateTime.now());
        return customer;
    }
}
