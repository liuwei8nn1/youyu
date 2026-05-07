package com.youyu.user.impl.domain.model;

import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业员工资料领域模型
 */
@Getter
@Setter
public class Employee extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关联user_identity.id（Auth领域主键，用于授权）
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
     * 部门ID
     */
    private Long deptId;

    /**
     * 职位
     */
    private String position;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 入职时间
     */
    private LocalDateTime hireDate;

    /**
     * 创建企业员工资料
     */
    public static Employee create(Long identityId, String username, String phone, String email) {
        if (identityId == null || identityId <= 0) {
            throw new IllegalArgumentException("用户身份ID必须大于0");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        Employee employee = new Employee();
        employee.identityId = identityId;
        employee.username = username;
        employee.phone = phone;
        employee.email = email;
        employee.status = 1; // 默认启用
        employee.hireDate = LocalDateTime.now();
        employee.initTime(LocalDateTime.now());
        return employee;
    }
}
