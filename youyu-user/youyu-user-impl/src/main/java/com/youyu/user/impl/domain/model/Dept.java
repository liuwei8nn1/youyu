package com.youyu.user.impl.domain.model;

import com.youyu.framework.datasource.mybatis.BaseDO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门领域模型
 */
@Getter
@Setter
public class Dept extends BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 创建人
     */
    private Long createdBy;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 子部门列表
     */
    private List<Dept> children = new ArrayList<>();

    /**
     * 创建部门
     */
    public static Dept create(Long parentId, String deptName, String deptCode, Integer sortOrder) {
        if (deptName == null || deptName.trim().isEmpty()) {
            throw new IllegalArgumentException("部门名称不能为空");
        }

        Dept dept = new Dept();
        dept.parentId = parentId != null ? parentId : 0L;
        dept.deptName = deptName;
        dept.deptCode = deptCode;
        dept.sortOrder = sortOrder != null ? sortOrder : 0;
        dept.status = 1; // 默认启用
        dept.initTime(LocalDateTime.now());
        return dept;
    }

    /**
     * 添加子部门
     */
    public void addChild(Dept child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }

    /**
     * 更新部门信息
     */
    public void updateInfo(String deptName, String deptCode, String leader, String phone,
                           String email, Integer sortOrder, Integer status) {
        if (deptName != null && !deptName.trim().isEmpty()) {
            this.deptName = deptName;
        }
        if (deptCode != null) {
            this.deptCode = deptCode;
        }
        this.leader = leader;
        this.phone = phone;
        this.email = email;
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (status != null) {
            this.status = status;
        }
    }
}
