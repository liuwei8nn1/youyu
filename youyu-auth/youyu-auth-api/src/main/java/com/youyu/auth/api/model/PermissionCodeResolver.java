package com.youyu.auth.api.model;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

/**
 * 权限码解析工具类(基于 request.getParameter)
 * <p>
 * 根据 @Permission 和 @Menu 注解动态构建最终权限码
 * <p>
 * 匹配规则:
 * 1. Menu 有 value: 直接使用该 value 作为权限码
 * 2. Menu 没有 value: 使用 @Permission 的 value + suffix 拼接
 * 3. args 匹配(从 HttpServletRequest 获取参数):
 *    - { "id", "*" } - request.getParameter("id") 有任意非空值
 *    - { "id", "" } - request.getParameter("id") 为空或 null
 *    - { "type", "1" } - request.getParameter("type") 等于 "1"
 * <p>
 * 注意: 权限相关参数应放在 URL query string 或 form data 中,不要放在 @RequestBody 的 JSON 里
 */
public class PermissionCodeResolver {

    /**
     * 解析方法上的权限码
     *
     * @param permission @Permission 注解
     * @return 权限码(多个时返回匹配的第一个)
     */
    public static String resolve(Permission permission) {
        // 1. 获取基础权限码
        String baseCode = permission.value();

        // 2. 如果没有配置 menus，直接返回基础权限码
        Menu[] menus = permission.menus();
        if (menus == null || menus.length == 0) {
            return baseCode;
        }

        // 3. 获取 HttpServletRequest
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            // 无法获取 request，返回基础权限码
            return baseCode;
        }

        // 4. 遍历所有 Menu，找到匹配的权限码
        for (Menu menu : menus) {
            String permissionCode = matchAndResolve(menu, baseCode, request);
            if (permissionCode != null && !permissionCode.isEmpty()) {
                return permissionCode;
            }
        }

        // 5. 如果没有匹配的 Menu，返回基础权限码
        return baseCode;
    }

    /**
     * 匹配并解析单个 Menu 的权限码
     *
     * @param menu     Menu 注解
     * @param baseCode 基础权限码
     * @param request  HTTP 请求
     * @return 匹配到的权限码，不匹配返回 null
     */
    private static String matchAndResolve(Menu menu, String baseCode, HttpServletRequest request) {
        // 1. 检查 args 是否匹配
        if (!matchArgs(menu.args(), request)) {
            return null;
        }

        // 2. 如果 Menu 有 value，直接使用
        if (menu.value() != null && !menu.value().isEmpty()) {
            return menu.value();
        }

        // 3. 如果 Menu 没有 value，使用 baseCode + suffix
        String suffix = menu.suffix();
        if (suffix == null || Menu.DEFAULT_SUFFIX.equals(suffix)) {
            // 没有配置 suffix，直接使用 baseCode
            return baseCode;
        }

        // 拼接权限码
        return baseCode + "-" + suffix;
    }

    /**
     * 检查 args 是否匹配当前请求参数
     *
     * @param argsConfig args 配置，如 { "id", "*" }
     * @param request    HTTP 请求
     * @return 是否匹配
     */
    private static boolean matchArgs(String[] argsConfig, HttpServletRequest request) {
        // 没有配置 args，表示无条件匹配
        if (argsConfig == null || argsConfig.length == 0) {
            return true;
        }

        // args 配置必须是成对的: { paramName, expectedValue }
        for (int i = 0; i < argsConfig.length; i += 2) {
            if (i + 1 >= argsConfig.length) {
                break;
            }

            String paramName = argsConfig[i];
            String expectedValue = argsConfig[i + 1];

            // 从 request 获取参数值
            String actualValue = request.getParameter(paramName);

            // 匹配规则
            if (!matchValue(actualValue, expectedValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 匹配参数值
     *
     * @param actualValue   实际参数值
     * @param expectedValue 期望值 (* 表示任意非空, "" 表示空, 其他表示精确匹配)
     * @return 是否匹配
     */
    private static boolean matchValue(String actualValue, String expectedValue) {
        // "*" 表示任意非空值
        if ("*".equals(expectedValue)) {
            return actualValue != null && !actualValue.trim().isEmpty();
        }

        // "" 表示空值
        if ("".equals(expectedValue)) {
            return actualValue == null || actualValue.trim().isEmpty();
        }

        // 精确匹配
        if (actualValue == null) {
            return false;
        }

        return expectedValue.equals(actualValue);
    }

    /**
     * 获取当前 HTTP 请求
     *
     * @return HttpServletRequest，如果不在请求上下文中则返回 null
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
