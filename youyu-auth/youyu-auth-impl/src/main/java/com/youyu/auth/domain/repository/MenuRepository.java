package com.youyu.auth.domain.repository;

import com.youyu.auth.domain.entity.Menu;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 菜单仓储接口
 */
public interface MenuRepository {

    /**
     * 保存菜单
     */
    Long save(Menu menu);

    /**
     * 更新菜单
     */
    void update(Menu menu);

    /**
     * 根据ID查询菜单
     */
    Optional<Menu> findById(Long id);

    /**
     * 查询所有菜单
     */
    List<Menu> findAllAndOrder();

    /**
     * 查询所有菜单(无排序)
     */
    List<Menu> listAll();

    /**
     * 根据父ID查询子菜单
     */
    List<Menu> findByParentId(Long parentId);

    /**
     * 根据权限码查询菜单
     */
    List<Menu> findByPermissionCode(String permissionCode);

    /**
     * 根据权限码集合查询菜单
     */
    List<Menu> findByPermissionCodes(Set<String> permissionCodes);

    /**
     * 查询可见的菜单
     */
    List<Menu> findVisibleMenus();

    /**
     * 根据目标用户类型查询菜单
     * @param targetUserType 目标用户类型值
     * @see com.youyu.common.enums.UserType
     */
    List<Menu> findByTargetUserType(Integer targetUserType);

    /**
     * 检查是否有子菜单
     */
    boolean hasChildren(Long parentId);

    /**
     * 根据ID删除菜单
     */
    boolean removeById(Long id);
}
