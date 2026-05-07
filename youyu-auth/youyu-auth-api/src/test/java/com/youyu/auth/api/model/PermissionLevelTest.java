package com.youyu.auth.api.model;

import com.youyu.framework.context.UserType;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * PermissionLevel 权限判断测试
 */
public class PermissionLevelTest {

    /**
     * 测试 NONE 权限 - 所有人都可以访问
     */
    @Test
    public void testNonePermission() {
        assertTrue("NONE 应该允许所有用户类型", PermissionLevel.NONE.isAllowed(null));
        assertTrue("NONE 应该允许 CUSTOMER", PermissionLevel.NONE.isAllowed(UserType.CUSTOMER));
        assertTrue("NONE 应该允许 ENTERPRISE", PermissionLevel.NONE.isAllowed(UserType.ENTERPRISE));
        assertTrue("NONE 应该允许 PLATFORM", PermissionLevel.NONE.isAllowed(UserType.PLATFORM));
    }

    /**
     * 测试 LOGIN 权限 - 所有登录用户都可以访问
     */
    @Test
    public void testLoginPermission() {
        assertFalse("LOGIN 不应该允许未登录用户", PermissionLevel.LOGIN.isAllowed(null));
        assertTrue("LOGIN 应该允许 CUSTOMER", PermissionLevel.LOGIN.isAllowed(UserType.CUSTOMER));
        assertTrue("LOGIN 应该允许 ENTERPRISE", PermissionLevel.LOGIN.isAllowed(UserType.ENTERPRISE));
        assertTrue("LOGIN 应该允许 PLATFORM", PermissionLevel.LOGIN.isAllowed(UserType.PLATFORM));
    }

    /**
     * 测试 CUSTOMER 权限 - 仅外部顾客可以访问
     */
    @Test
    public void testCustomerPermission() {
        assertFalse("CUSTOMER 不应该允许未登录用户", PermissionLevel.CUSTOMER.isAllowed(null));
        assertTrue("CUSTOMER 应该允许 CUSTOMER", PermissionLevel.CUSTOMER.isAllowed(UserType.CUSTOMER));
        assertFalse("CUSTOMER 不应该允许 ENTERPRISE", PermissionLevel.CUSTOMER.isAllowed(UserType.ENTERPRISE));
        assertFalse("CUSTOMER 不应该允许 PLATFORM", PermissionLevel.CUSTOMER.isAllowed(UserType.PLATFORM));
    }

    /**
     * 测试 EMP 权限 - 企业员工和平台管理员都可以访问
     */
    @Test
    public void testEmpPermission() {
        assertFalse("EMP 不应该允许未登录用户", PermissionLevel.EMP.isAllowed(null));
        assertFalse("EMP 不应该允许 CUSTOMER", PermissionLevel.EMP.isAllowed(UserType.CUSTOMER));
        assertTrue("EMP 应该允许 ENTERPRISE", PermissionLevel.EMP.isAllowed(UserType.ENTERPRISE));
        assertTrue("EMP 应该允许 PLATFORM", PermissionLevel.EMP.isAllowed(UserType.PLATFORM));
    }

    /**
     * 测试 ENTERPRISE 权限 - 仅企业员工可以访问
     */
    @Test
    public void testEnterprisePermission() {
        assertFalse("ENTERPRISE 不应该允许未登录用户", PermissionLevel.ENTERPRISE.isAllowed(null));
        assertFalse("ENTERPRISE 不应该允许 CUSTOMER", PermissionLevel.ENTERPRISE.isAllowed(UserType.CUSTOMER));
        assertTrue("ENTERPRISE 应该允许 ENTERPRISE", PermissionLevel.ENTERPRISE.isAllowed(UserType.ENTERPRISE));
        assertFalse("ENTERPRISE 不应该允许 PLATFORM", PermissionLevel.ENTERPRISE.isAllowed(UserType.PLATFORM));
    }

    /**
     * 测试 PLATFORM 权限 - 仅平台管理员可以访问
     */
    @Test
    public void testPlatformPermission() {
        assertFalse("PLATFORM 不应该允许未登录用户", PermissionLevel.PLATFORM.isAllowed(null));
        assertFalse("PLATFORM 不应该允许 CUSTOMER", PermissionLevel.PLATFORM.isAllowed(UserType.CUSTOMER));
        assertFalse("PLATFORM 不应该允许 ENTERPRISE", PermissionLevel.PLATFORM.isAllowed(UserType.ENTERPRISE));
        assertTrue("PLATFORM 应该允许 PLATFORM", PermissionLevel.PLATFORM.isAllowed(UserType.PLATFORM));
    }

    /**
     * 测试静态方法 hasPermission - 满足任一权限级别即可
     */
    @Test
    public void testHasPermissionWithSet() {
        // 场景1: 要求 EMP 或 PLATFORM 权限，ENTERPRISE 用户应该可以通过
        Set<PermissionLevel> empOrPlatform = Set.of(PermissionLevel.EMP, PermissionLevel.PLATFORM);
        assertTrue("ENTERPRISE 应该有 EMP 或 PLATFORM 权限",
                PermissionLevel.hasPermission(empOrPlatform, com.youyu.framework.context.UserType.ENTERPRISE));

        // 场景2: 要求 EMP 或 PLATFORM 权限，CUSTOMER 用户应该不能通过
        assertFalse("CUSTOMER 不应该有 EMP 或 PLATFORM 权限",
                PermissionLevel.hasPermission(empOrPlatform, com.youyu.framework.context.UserType.CUSTOMER));

        // 场景3: 要求 CUSTOMER 权限，CUSTOMER 用户可以通过
        Set<PermissionLevel> customerOnly = Set.of(PermissionLevel.CUSTOMER);
        assertTrue("CUSTOMER 应该有 CUSTOMER 权限",
                PermissionLevel.hasPermission(customerOnly, com.youyu.framework.context.UserType.CUSTOMER));

        // 场景4: 未登录用户只能访问 NONE
        Set<PermissionLevel> loginRequired = Set.of(PermissionLevel.LOGIN);
        assertFalse("未登录用户不应该有 LOGIN 权限",
                PermissionLevel.hasPermission(loginRequired, null));

        Set<PermissionLevel> noneRequired = Set.of(PermissionLevel.NONE);
        assertTrue("未登录用户应该有 NONE 权限",
                PermissionLevel.hasPermission(noneRequired, null));
    }

    /**
     * 测试 fromCode 方法
     */
    @Test
    public void testFromCode() {
        assertEquals("应该解析 NONE", PermissionLevel.NONE, PermissionLevel.fromCode("none"));
        assertEquals("应该解析 LOGIN", PermissionLevel.LOGIN, PermissionLevel.fromCode("login"));
        assertEquals("应该解析 CUSTOMER", PermissionLevel.CUSTOMER, PermissionLevel.fromCode("customer"));
        assertEquals("应该解析 EMP", PermissionLevel.EMP, PermissionLevel.fromCode("emp"));
        assertEquals("应该解析 ENTERPRISE", PermissionLevel.ENTERPRISE, PermissionLevel.fromCode("enterprise"));
        assertEquals("应该解析 PLATFORM", PermissionLevel.PLATFORM, PermissionLevel.fromCode("platform"));

        // 大小写不敏感
        assertEquals("应该忽略大小写", PermissionLevel.PLATFORM, PermissionLevel.fromCode("PLATFORM"));
        assertEquals("应该忽略大小写", PermissionLevel.EMP, PermissionLevel.fromCode("Emp"));

        // 无效值返回 NONE
        assertEquals("无效值应该返回 NONE", PermissionLevel.NONE, PermissionLevel.fromCode(null));
        assertEquals("无效值应该返回 NONE", PermissionLevel.NONE, PermissionLevel.fromCode(""));
        assertEquals("无效值应该返回 NONE", PermissionLevel.NONE, PermissionLevel.fromCode("invalid"));
    }

    /**
     * 测试权限级别数值
     */
    @Test
    public void testPermissionLevels() {
        assertEquals("NONE level 应该是 0", 0, PermissionLevel.NONE.getLevel());
        assertEquals("LOGIN level 应该是 1", 1, PermissionLevel.LOGIN.getLevel());
        assertEquals("CUSTOMER level 应该是 2", 2, PermissionLevel.CUSTOMER.getLevel());
        assertEquals("EMP level 应该是 3", 3, PermissionLevel.EMP.getLevel());
        assertEquals("ENTERPRISE level 应该是 4", 4, PermissionLevel.ENTERPRISE.getLevel());
        assertEquals("PLATFORM level 应该是 5", 5, PermissionLevel.PLATFORM.getLevel());
    }
}
