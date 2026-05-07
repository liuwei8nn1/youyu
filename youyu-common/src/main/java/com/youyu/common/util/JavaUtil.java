package com.youyu.common.util;

import java.lang.invoke.MethodHandles;

/**
 * Java 工具类
 * 提供 Java 反射和 MethodHandles 相关的工具方法
 */
public final class JavaUtil {

    /**
     * MethodHandles.Lookup 实例，用于创建 VarHandle
     * 提供对私有字段的访问能力
     */
    public static final MethodHandles.Lookup IMPL_LOOKUP = MethodHandles.lookup();

    private JavaUtil() {
        // 防止实例化
    }

    /**
     * 获取指定类的 MethodHandles.Lookup
     *
     * @param clazz 目标类
     * @return MethodHandles.Lookup 实例
     */
    public static MethodHandles.Lookup getLookup(Class<?> clazz) {
        try {
            return MethodHandles.privateLookupIn(clazz, IMPL_LOOKUP);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get private lookup for class: " + clazz.getName(), e);
        }
    }
}
