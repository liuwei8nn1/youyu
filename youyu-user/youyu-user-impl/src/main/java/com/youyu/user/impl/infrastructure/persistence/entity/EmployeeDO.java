package com.youyu.user.impl.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.youyu.framework.datasource.mybatis.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业员工资料数据对象
 */
@Data
@TableName("employee")
public class EmployeeDO extends LogicDeleteBaseDO {

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

    public static final String ID = "id";
    public static final String IDENTITY_ID = "identity_id";
    public static final String USERNAME = "username";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String DEPT_ID = "dept_id";
    public static final String STATUS = "status";
}
