package com.youyu.auth.api.model;

import java.lang.annotation.Annotation;

import com.youyu.framework.context.UserType;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.assertEquals;

/**
 * PermissionCodeResolver 测试类(基于 request.getParameter)
 */
public class PermissionCodeResolverTest {

    /**
     * 测试: Menu 有 value,直接使用
     */
    @Test
    public void testMenuWithValue() {
        // 准备 Mock Request
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("id", "123");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            Permission permission = createPermission(
                "config:manage",
                new Menu[]{
                    createMenu("编辑", new String[]{"id", "*"}, "admin.Config.edit", null),
                    createMenu("新增", new String[]{"id", ""}, "admin.Config.add", null)
                }
            );

            String code = PermissionCodeResolver.resolve(permission);

            assertEquals("admin.Config.edit", code);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     * 测试: Menu 没有 value,使用 baseCode + suffix
     */
    @Test
    public void testMenuWithSuffix() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("type", "1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            Permission permission = createPermission(
                "log:view",
                new Menu[]{
                    createMenu("总台日志", new String[]{"type", "1"}, null, ""),
                    createMenu("商户日志", new String[]{"type", "2"}, null, "1")
                }
            );

            String code = PermissionCodeResolver.resolve(permission);
            assertEquals("log:view", code);

            // 测试 type=2
            request.setParameter("type", "2");
            String code2 = PermissionCodeResolver.resolve(permission);
            assertEquals("log:view-1", code2);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     * 测试: 多条件匹配
     */
    @Test
    public void testMultipleConditions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("status", "pending");
        request.setParameter("action", "approve");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            Permission permission = createPermission(
                "order:manage",
                new Menu[]{
                    createMenu("审核订单", new String[]{"status", "pending", "action", "approve"}, "order:audit", null)
                }
            );

            String code = PermissionCodeResolver.resolve(permission);
            assertEquals("order:audit", code);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    /**
     * 测试: 无匹配时返回基础权限码
     */
    @Test
    public void testNoMatch() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        // id 参数为空
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            Permission permission = createPermission(
                "config:manage",
                new Menu[]{
                    createMenu(null, new String[]{"id", "*"}, "config:edit", null),
                    createMenu(null, new String[]{"id", ""}, "config:add", null)
                }
            );

            String code = PermissionCodeResolver.resolve(permission);
            assertEquals("config:manage", code);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    // 辅助方法
    private Permission createPermission(String value, Menu[] menus) {
        return new Permission() {
            @Override
            public String value() { return value; }
            @Override
            public UserType userType() {
                return UserType.UNKNOWN;
            }
            @Override
            public Menu[] menus() { return menus; }
            @Override
            public boolean export() { return false; }
            @Override
            public Class<? extends Annotation> annotationType() {
                return Permission.class; 
            }
        };
    }

    private Menu createMenu(String name, String[] args, String value, String suffix) {
        return new Menu() {
            @Override
            public String name() { return name != null ? name : ""; }
            @Override
            public String[] args() { return args; }
            @Override
            public String suffix() { return suffix != null ? suffix : Menu.DEFAULT_SUFFIX; }
            @Override
            public String value() { return value != null ? value : ""; }
            @Override
            public Class<? extends Annotation> annotationType() {
                return Menu.class; 
            }
        };
    }
}
