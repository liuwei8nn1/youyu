package com.youyu.auth.domain.repository;

import java.util.List;

/**
 * 角色-菜单关联仓储接口
 */
public interface RoleMenuRepository {
    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> findMenuIdsByRoleId(Long roleId);

    /**
     * 根据多个角色ID查询菜单ID列表（去重）
     */
    List<Long> findMenuIdsByRoleIds(List<Long> roleIds);

    /**
     * 删除角色的所有菜单关联
     */
    void deleteByRoleId(Long roleId);

    /**
     * 批量插入角色菜单关联
     */
    void batchInsert(Long roleId, List<Long> menuIds);
}
