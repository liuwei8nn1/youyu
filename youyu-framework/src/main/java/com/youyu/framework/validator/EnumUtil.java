package com.youyu.framework.validator;

/**
 * 枚举工具类
 * 提供枚举相关的辅助方法
 */
public final class EnumUtil {

    private EnumUtil() {
        // 防止实例化
    }

    /**
     * 根据名称获取枚举实例
     *
     * @param enumClass 枚举类
     * @param name      枚举名称
     * @return 对应的枚举实例，如果找不到则返回 null
     */
    public static <E extends Enum<E>> E of(Class<E> enumClass, String name) {
        if (enumClass == null || name == null) {
            return null;
        }
        
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 根据名称获取枚举实例（忽略大小写）
     *
     * @param enumClass 枚举类
     * @param name      枚举名称
     * @return 对应的枚举实例，如果找不到则返回 null
     */
    public static <E extends Enum<E>> E ofIgnoreCase(Class<E> enumClass, String name) {
        if (enumClass == null || name == null) {
            return null;
        }
        
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(name)) {
                return enumConstant;
            }
        }
        return null;
    }

    /**
     * 安全地获取枚举实例，如果找不到则返回默认值
     *
     * @param enumClass    枚举类
     * @param name         枚举名称
     * @param defaultValue 默认值
     * @return 对应的枚举实例，如果找不到则返回默认值
     */
    public static <E extends Enum<E>> E ofOrDefault(Class<E> enumClass, String name, E defaultValue) {
        E result = of(enumClass, name);
        return result != null ? result : defaultValue;
    }

    /**
     * 判断给定的名称是否是有效的枚举常量
     *
     * @param enumClass 枚举类
     * @param name      枚举名称
     * @return true 如果是有效的枚举常量
     */
    public static <E extends Enum<E>> boolean isValidName(Class<E> enumClass, String name) {
        return of(enumClass, name) != null;
    }
}
