package com.youyu.user.impl.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youyu.user.impl.domain.model.Employee;
import com.youyu.user.impl.infrastructure.persistence.entity.EmployeeDO;

import java.util.List;
import java.util.Optional;

/**
 * 企业员工资料仓储接口
 */
public interface EmployeeRepository {
    /**
     * 保存企业员工资料
     */
    Long save(Employee employee);

    /**
     * 更新企业员工资料
     */
    void update(Employee employee);

    /**
     * 根据ID查询
     */
    Optional<Employee> findById(Long id);

    /**
     * 根据用户ID查询
     */
    Optional<Employee> findByUserId(Long userId);

    /**
     * 根据用户名查询
     */
    Optional<Employee> findByUsername(String username);

    /**
     * 根据手机号查询
     */
    Optional<Employee> findByPhone(String phone);

    /**
     * 根据邮箱查询
     */
    Optional<Employee> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 分页查询员工列表
     */
    Page<Employee> listPage(Page<EmployeeDO> page, String keyword, Long deptId, Integer status);

    /**
     * 根据ID删除（逻辑删除）
     */
    void removeById(Long id);

    /**
     * 获取所有员工
     */
    List<Employee> listAll();
}
