package com.youyu.auth.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youyu.auth.domain.aggregate.Role;
import com.youyu.auth.infrastructure.persistence.entity.RoleDO;
import com.youyu.framework.context.UserType;
import com.youyu.framework.datasource.mybatis.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储接口
 */
public interface RoleRepository {

    /**
     * 更新角色
     * @param role
     * @return
     */
    int update(Role role);
    /**
     * 保存角色
     */
    Long save(Role role);

    /**
     * 根据ID查询角色
     */
    Optional<Role> findById(Long id);

    /**
     * 批量根据ID查询角色（避免N+1）
     * @param ids 角色ID列表
     * @return 角色列表
     */
    List<Role> findByIds(List<Long> ids);

    /**
     * 根据角色编码查询角色
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 查询所有角色
     */
    List<Role> listAll();

    /**
     * 根据状态查询角色
     */
    List<Role> findByStatus(Integer status);

    /**
     * 删除角色
     */
    boolean removeById(Long id);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 根据用户类型查询角色
     * @param userType 用户类型值
     * @see UserType
     */
    List<Role> findByUserType(Integer userType);

    /**
     * 批量根据角色编码查询角色（避免N+1）
     * @param roleCodes 角色编码列表
     * @return 角色列表
     */
    List<Role> findByRoleCodes(List<String> roleCodes);

    /**
     * 批量根据角色编码和用户类型查询角色（避免N+1）
     * @param roleCodes 角色编码列表
     * @param userType 用户类型
     * @return 角色列表
     */
    List<Role> findByRoleCodesAndUserType(List<String> roleCodes, Integer userType);

    /**
     * 分页查询角色
     * @param page MyBatis-Plus 分页对象
     * @param userType 用户类型（可选）
     * @return 分页结果
     */
    Page<Role> findPage(Page<RoleDO> page, Integer userType);

    /**
     * 检查角色是否被使用(有用户关联)
     */
    boolean isUsedByUsers(Long roleId);
}
