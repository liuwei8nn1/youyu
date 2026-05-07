package com.youyu.auth.interfaces.controller;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youyu.auth.api.model.Permission;
import com.youyu.auth.application.service.RolePermissionApplicationService;
import com.youyu.auth.domain.model.Menu;
import com.youyu.auth.domain.model.Role;
import com.youyu.common.model.Result;
import com.youyu.framework.context.UserType;
import com.youyu.framework.context.web.resolver.ProxyRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 角色权限菜单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/role-permission")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionApplicationService rolePermissionService;

    // ==================== 角色管理 ====================

    /**
     * 创建角色
     */
    @PostMapping("createRole")
    public Result<Long> createRole(ProxyRequest q, CreateRoleRequest request) {
        Long roleId = rolePermissionService.createRole(
                request.getRoleCode(),
                request.getRoleName(),
                request.getDescription(),
                request.getUserType(),
                request.getSortOrder()
        );
        return Result.success(roleId);
    }

    /**
     * 更新角色
     */
    @PostMapping("updateRole")
    public Result<Void> updateRole(UpdateRoleRequest request) {
        rolePermissionService.updateRole(
                request.getRoleId(),
                request.getRoleName(),
                request.getDescription(),
                request.getUserType(),
                request.getSortOrder()
        );
        return Result.success();
    }

    /**
     * 删除角色
     */
    @PostMapping("deleteRole")
    public Result<Void> deleteRole(@RequestParam(name = "roleId") Long roleId) {
        rolePermissionService.deleteRole(roleId);
        return Result.success();
    }

    /**
     * 查询所有角色
     */
    @GetMapping("roles")
    public Result<List<Role>> getAllRoles() {
        List<Role> roles = rolePermissionService.getAllRoles();
        return Result.success(roles);
    }
    
    /**
     * 根据用户类型查询角色
     */
    @GetMapping("/roles/byUserType")
    public Result<List<Role>> getRolesByUserType(@RequestParam(name = "userType") Integer userType) {
        List<Role> roles = rolePermissionService.getRolesByUserType(userType);
        return Result.success(roles);
    }

    /**
     * 分页查询角色
     */
    @GetMapping("/roles/page")
    public Result<Page<Role>> getRolesPage(ProxyRequest q, @RequestParam(name = "userType", required = false) Integer userType) {
        Page<Role> pageResult = rolePermissionService.getRolesPage(q.getPage(), userType);
        return Result.success(pageResult);
    }

    // ==================== 角色菜单管理 ====================

    /**
     * 为角色分配菜单
     */
    @PostMapping("assignMenus")
    public Result<Void> assignMenus(@RequestBody AssignMenusRequest request) {
        rolePermissionService.assignMenusToRole(request.getRoleId(), request.getMenuIds());
        return Result.success();
    }

    /**
     * 查询角色的菜单
     */
    @GetMapping("getRoleMenus")
    public Result<List<Menu>> getRoleMenus(@RequestParam(name = "roleId") Long roleId) {
        List<Menu> menus = rolePermissionService.getRoleMenus(roleId);
        return Result.success(menus);
    }

    /**
     * 查询所有菜单（用于分配时选择）
     */
    @GetMapping("/menus/all")
    public Result<List<Menu>> getAllMenusForAssignment() {
        List<Menu> menus = rolePermissionService.getAllMenusForAssignment();
        return Result.success(menus);
    }
    @GetMapping("/getUserPermissions")
    public Result<List<String>> getUserPermissions(@RequestParam("userId") Long userId,
                                            @RequestParam("userType") Integer userType){
        List<String> permissions = rolePermissionService.getUserPermissions(userId, userType);
        return Result.success(permissions);
    }

    // ==================== 菜单管理 ====================

    /**
     * 创建菜单
     */
    @PostMapping("/menus")
    public Result<Long> createMenu(@RequestBody CreateMenuRequest request) {
        Long menuId = rolePermissionService.createMenu(
                request.getParentId(),
                request.getName(),
                request.getPath(),
                request.getComponent(),
                request.getIcon(),
                request.getPermissionCode(),
                request.getType(),
                request.getTargetUserType(),
                request.getVisible(),
                request.getSortOrder(),
                request.getRedirect()
        );
        return Result.success(menuId);
    }

    /**
     * 更新菜单
     */
    @PostMapping("updateMenu")
    public Result<Void> updateMenu(@RequestParam(name = "menuId")  Long menuId, @RequestBody UpdateMenuRequest request) {
        rolePermissionService.updateMenu(
                menuId,
                request.getName(),
                request.getPath(),
                request.getComponent(),
                request.getIcon(),
                request.getPermissionCode(),
                request.getTargetUserType(),
                request.getVisible(),
                request.getSortOrder(),
                request.getRedirect()
        );
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @PostMapping("deleteMenu")
    public Result<Void> deleteMenu(@RequestParam(name = "menuId") Long menuId) {
        rolePermissionService.deleteMenu(menuId);
        return Result.success();
    }

    /**
     * 获取所有菜单(树形结构)
     */
    @GetMapping("/menus")

    public Result<List<Menu>> getAllMenus() {
        List<Menu> menus = rolePermissionService.getAllMenus();
        return Result.success(menus);
    }

    /**
     * 获取当前用户的菜单
     */
    @GetMapping("/getUserMenus")
    @Permission(Permission.LOGIN)
    public Result<List<Menu>> getUserMenus(ProxyRequest q) {
        List<Menu> menus = rolePermissionService.getUserMenus(q.getUserId(),q.getUserType());
        return Result.success(menus);
    }

    // ==================== DTO类 ====================

    @Data
    public static class CreateRoleRequest {
        private String roleCode;
        private String roleName;
        private String description;
        /**
         * @see UserType
         */
        private Integer userType;
        private Integer sortOrder;
    }

    @Data
    public static class UpdateRoleRequest {
        private Long roleId;
        private String roleName;
        private String description;
        /**
         * @see UserType
         */
        private Integer userType;
        private Integer sortOrder;
    }

    @Data
    public static class AssignMenusRequest {
        private Long roleId;
        private List<Long> menuIds;
    }

    @Data
    public static class CreateMenuRequest {
        private Long parentId;
        private String name;
        private String path;
        private String component;
        private String icon;
        private String permissionCode;
        /**
         * 菜单类型: 1-目录, 2-菜单, 3-按钮
         * @see com.youyu.auth.api.model.MenuType
         */
        private Integer type;

        /**
         * @see UserType
         */
        private Integer targetUserType;
        private Integer visible;
        private Integer sortOrder;
        private String redirect;
    }

    @Data
    public static class UpdateMenuRequest {
        private String name;
        private String path;
        private String component;
        private String icon;
        private String permissionCode;
        /**
         * @see UserType
         */
        private Integer targetUserType;
        private Integer visible;
        private Integer sortOrder;
        private String redirect;
    }
}
