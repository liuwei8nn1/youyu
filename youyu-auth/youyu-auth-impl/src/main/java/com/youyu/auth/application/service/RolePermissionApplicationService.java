package com.youyu.auth.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.youyu.auth.domain.entity.Menu;
import com.youyu.auth.domain.aggregate.Role;
import com.youyu.auth.domain.entity.UserRole;
import com.youyu.auth.domain.repository.MenuRepository;
import com.youyu.auth.domain.repository.RoleMenuRepository;
import com.youyu.auth.domain.repository.RoleRepository;
import com.youyu.auth.domain.repository.UserRoleRepository;
import com.youyu.auth.infrastructure.persistence.entity.RoleDO;
import com.youyu.common.util.CollectionUtil;
import com.youyu.framework.context.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 角色权限菜单应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionApplicationService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final UserRoleRepository userRoleRepository;  // 新增：本地查询用户角色

    /**
     * 全量菜单树缓存（按用户类型区分）
     * Key: userType, Value: 菜单树列表
     * 过期时间：10分钟
     */
    private final Cache<Integer, List<Menu>> menuTreeCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10)
            .build();

    // ==================== 角色管理 ====================

    /**
     * 创建角色
     */
    @Transactional
    public Long createRole(String roleCode, String roleName, String description, Integer userType, Integer sortOrder) {
        if (roleRepository.existsByRoleCode(roleCode)) {
            throw new IllegalArgumentException("角色编码已存在: " + roleCode);
        }

        Role role = new Role();
        role.setRoleCode(roleCode);
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setUserType(userType != null ? userType : UserType.ENTERPRISE.getValue());
        role.setStatus(1);
        role.setSortOrder(sortOrder != null ? sortOrder : 0);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return roleRepository.save(role);
    }

    /**
     * 更新角色
     */
    @Transactional
    public void updateRole(Long roleId, String roleName, String description, Integer userType, Integer sortOrder) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));

        role.updateInfo(roleName, description, userType, sortOrder);
        roleRepository.update(role);
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));

        // 检查是否有用户关联
        if (roleRepository.isUsedByUsers(roleId)) {
            throw new IllegalStateException("角色已被用户使用,无法删除");
        }

        roleRepository.removeById(roleId);
    }

    /**
     * 查询所有角色
     */
    public List<Role> getAllRoles() {
        return roleRepository.listAll();
    }

    /**
     * 根据用户类型查询角色
     */
    public List<Role> getRolesByUserType(Integer userType) {
        return roleRepository.findByUserType(userType);
    }

    /**
     * 分页查询角色
     */
    public Page<Role> getRolesPage(Page<RoleDO> page, Integer userType) {
        return roleRepository.findPage(page, userType);
    }

    // ==================== 菜单权限管理 ====================

    /**
     * 为角色分配菜单
     */
    @Transactional
    public void assignMenusToRole(Long roleId, List<Long> menuIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleId));

        // 1. 删除该角色的所有菜单关联
        roleMenuRepository.deleteByRoleId(roleId);

        // 2. 批量插入新的菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            roleMenuRepository.batchInsert(roleId, menuIds);
        }

        log.info("为角色 {} 分配菜单完成，菜单数量: {}", roleId, menuIds != null ? menuIds.size() : 0);
    }

    /**
     * 查询角色的菜单
     */
    public List<Menu> getRoleMenus(Long roleId) {
        // 1. 根据 roleId 查询关联的 menu_ids
        List<Long> menuIds = roleMenuRepository.findMenuIdsByRoleId(roleId);

        if (CollectionUtils.isEmpty(menuIds)) {
            return Collections.emptyList();
        }

        // 2. 根据 menu_ids 查询菜单
        List<Menu> allMenus = menuRepository.findAllAndOrder();
        List<Menu> roleMenus = allMenus.stream()
                .filter(menu -> menuIds.contains(menu.getId()))
                .collect(Collectors.toList());

        // 3. 构建树形结构（包含所有类型：目录、菜单、按钮）
        return buildMenuTree(roleMenus, 0L);
    }

    /**
     * 查询所有菜单（用于分配时选择）
     */
    public List<Menu> getAllMenusForAssignment() {
        return menuRepository.listAll();
    }

    // ==================== 菜单管理 ====================

    /**
     * 创建菜单
     */
    @Transactional
    public Long createMenu(Long parentId, String name, String path, String component,
                           String icon, String permissionCode, Integer type, Integer targetUserType,
                           Integer visible, Integer sortOrder, String redirect) {
        Menu menu = new Menu();
        menu.setParentId(parentId != null ? parentId : 0L);
        menu.setName(name);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setIcon(icon);
        menu.setPermissionCode(permissionCode);
        menu.setType(type != null ? type : com.youyu.auth.api.model.MenuType.MENU.getValue());
        menu.setTargetUserType(targetUserType != null ? targetUserType : UserType.CUSTOMER.getValue());
        menu.setVisible(visible != null ? visible : 1);
        menu.setStatus(1);
        menu.setSortOrder(sortOrder != null ? sortOrder : 0);
        menu.setRedirect(redirect);
        menu.setCreatedAt(LocalDateTime.now());
        menu.setUpdatedAt(LocalDateTime.now());
        return menuRepository.save(menu);
    }

    /**
     * 更新菜单
     */
    @Transactional
    public void updateMenu(Long menuId, String name, String path, String component,
                          String icon, String permissionCode, Integer targetUserType,
                          Integer visible, Integer sortOrder, String redirect) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + menuId));

        // 更新菜单信息
        menu.setName(name);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setIcon(icon);
        menu.setPermissionCode(permissionCode);
        if (targetUserType != null) {
            menu.setTargetUserType(targetUserType);
        }
        menu.setVisible(visible);
        menu.setSortOrder(sortOrder);
        menu.setRedirect(redirect);

        menuRepository.update(menu);
    }

    /**
     * 删除菜单
     */
    @Transactional
    public void deleteMenu(Long menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + menuId));

        // 检查是否有子菜单
        if (menuRepository.hasChildren(menuId)) {
            throw new IllegalStateException("菜单下存在子菜单,无法删除");
        }

        menuRepository.removeById(menuId);
    }

    /**
     * 获取所有菜单(树形结构，带缓存)
     */
    public List<Menu> getAllMenus() {
        // 默认查询企业用户类型的菜单
        return getAllMenusByUserType(UserType.ENTERPRISE.getValue());
    }

    /**
     * 根据用户类型获取菜单树（带缓存）
     */
    public List<Menu> getAllMenusByUserType(Integer userType) {
        // 先从缓存中获取
        List<Menu> cachedMenus = menuTreeCache.getIfPresent(userType);
        if (cachedMenus != null) {
            log.debug("菜单树缓存命中，userType: {}", userType);
            return cachedMenus;
        }

        // 缓存未命中，从数据库查询并构建
        log.debug("菜单树缓存未命中，从数据库查询，userType: {}", userType);
        List<Menu> allMenus = menuRepository.findByTargetUserType(userType);
        List<Menu> menuTree = buildMenuTree(allMenus, 0L);

        // 存入缓存
        menuTreeCache.put(userType, menuTree);
        return menuTree;
    }

    /**
     * 根据用户ID获取菜单(从 Auth 领域本地查询)
     */
    public List<Menu> getUserMenus(Long userId, Integer userType) {
        // 从 Auth 领域本地查询用户角色
        List<UserRole> userRoles = userRoleRepository.findByUserIdAndType(userId, userType);
        
        if (userRoles.isEmpty()) {
            log.warn("用户 {} 没有分配角色，返回空菜单列表", userId);
            return Collections.emptyList();
        }
        
        // 提取角色ID列表
        List<Long> roleIds = CollectionUtil.toList(userRoles, UserRole::getRoleId);
        
        log.debug("用户 {} 分配了 {} 个角色", userId, roleIds.size());
        
        // todo 还可以优化，比如只有一个角色时（多角色还是得过滤，因为大多是单角色，只缓存单角色的菜单树，角色修改也好维护缓存），直接缓存该角色的菜单树
        // 根据角色ID查询关联的菜单ID（使用上面定义的 roleIds）
        List<Long> menuIds = roleMenuRepository.findMenuIdsByRoleIds(roleIds);

        if (CollectionUtils.isEmpty(menuIds)) {
            log.warn("用户 {} 的角色没有关联任何菜单，返回空菜单列表", userId);
            return Collections.emptyList();
        }

        // 从缓存中获取全量菜单树，然后直接在树上过滤
        List<Menu> allMenusTree = getAllMenusByUserType(userType);
        
        // 将menuIds转为Set提升查找性能
        Set<Long> menuIdSet = new HashSet<>(menuIds);
        
        // 直接在树上过滤（递归过滤，保留有权限的节点及其父节点）
        return filterMenuTree(allMenusTree, menuIdSet);
    }

    /**
     * 获取用户的所有权限码
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 权限码列表
     */
    public List<String> getUserPermissions(Long userId, Integer userType) {
        // 1. 获取用户的菜单列表
        List<Menu> userMenus = getUserMenus(userId, userType);
        
        if (CollectionUtils.isEmpty(userMenus)) {
            log.debug("用户 {} 没有菜单权限，返回空权限列表", userId);
            return Collections.emptyList();
        }
        
        // 2. 提取所有菜单的权限码（排除空值和DIRECTORY类型的菜单）
        List<String> permissions = userMenus.stream()
                .filter(menu -> menu.getPermissionCode() != null && !menu.getPermissionCode().isEmpty())
                .map(Menu::getPermissionCode)
                .distinct() // 去重
                .collect(Collectors.toList());
        
        log.debug("用户 {} 的权限码数量: {}", userId, permissions.size());
        return permissions;
    }

    /**
     * 直接在菜单树上过滤（递归过滤，保留有权限的节点）
     * 注意：不会修改缓存中的原始对象，会创建新的Menu对象
     * 
     * @param menuTree 原始菜单树
     * @param menuIdSet 有权限的菜单ID集合
     * @return 过滤后的菜单树（包含所有类型：目录、菜单、按钮）
     */
    private List<Menu> filterMenuTree(List<Menu> menuTree, Set<Long> menuIdSet) {
        if (CollectionUtils.isEmpty(menuTree)) {
            return Collections.emptyList();
        }

        List<Menu> result = new ArrayList<>();
        for (Menu menu : menuTree) {
            // 递归过滤子菜单
            List<Menu> filteredChildren = filterMenuTree(menu.getChildren(), menuIdSet);
            
            // 如果当前菜单有权限，或者其子菜单有权限，则保留
            if (menuIdSet.contains(menu.getId()) || !filteredChildren.isEmpty()) {
                // 创建新的Menu对象，避免修改缓存中的原始对象
                Menu newMenu = copyMenu(menu);
                newMenu.setChildren(filteredChildren);
                result.add(newMenu);
            }
        }
        return result;
    }

    /**
     * 复制Menu对象（浅拷贝，除了children）
     */
    private Menu copyMenu(Menu source) {
        Menu target = new Menu();
        target.setId(source.getId());
        target.setParentId(source.getParentId());
        target.setName(source.getName());
        target.setPath(source.getPath());
        target.setComponent(source.getComponent());
        target.setIcon(source.getIcon());
        target.setPermissionCode(source.getPermissionCode());
        target.setType(source.getType());
        target.setTargetUserType(source.getTargetUserType());
        target.setVisible(source.getVisible());
        target.setStatus(source.getStatus());
        target.setSortOrder(source.getSortOrder());
        target.setRedirect(source.getRedirect());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    /**
     * 构建菜单树
     */
    private List<Menu> buildMenuTree(List<Menu> allMenus, Long parentId) {
        if (CollectionUtils.isEmpty(allMenus)) {
            return Collections.emptyList();
        }

        List<Menu> result = new ArrayList<>();
        for (Menu menu : allMenus) {
            if (menu.getParentId().equals(parentId)) {
                // 递归设置子菜单
                List<Menu> children = buildMenuTree(allMenus, menu.getId());
                menu.setChildren(children);
                result.add(menu);
            }
        }
        return result;
    }
}
