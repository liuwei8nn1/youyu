package com.youyu.user.impl.domain.repository;

import com.youyu.user.impl.domain.model.Dept;

import java.util.List;
import java.util.Optional;

/**
 * 部门仓储接口
 */
public interface DeptRepository {
    /**
     * 保存部门
     */
    Long save(Dept dept);

    /**
     * 更新部门
     */
    void update(Dept dept);

    /**
     * 根据ID查询
     */
    Optional<Dept> findById(Long id);

    /**
     * 查询所有部门
     */
    List<Dept> listAll();

    /**
     * 根据父ID查询子部门
     */
    List<Dept> findByParentId(Long parentId);

    /**
     * 根据部门编码查询
     */
    Optional<Dept> findByDeptCode(String deptCode);

    /**
     * 删除部门
     */
    boolean removeById(Long id);

    /**
     * 检查是否有子部门
     */
    boolean hasChildren(Long parentId);

    /**
     * 检查部门编码是否存在
     */
    boolean existsByDeptCode(String deptCode);
}
