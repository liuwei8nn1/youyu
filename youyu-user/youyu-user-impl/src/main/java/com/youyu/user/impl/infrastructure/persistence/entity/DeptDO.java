package com.youyu.user.impl.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.Bean;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门数据对象
 */
@Data
@TableName("sys_dept")
public class DeptDO implements Bean<Long>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 逻辑删除时间戳
     */
    private Long deletedAt;

    public static final String ID = "id";
    public static final String PARENT_ID = "parent_id";
    public static final String DEPT_CODE = "dept_code";
    public static final String SORT_ORDER = "sort_order";
}
