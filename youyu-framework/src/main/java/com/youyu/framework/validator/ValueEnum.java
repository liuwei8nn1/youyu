package com.youyu.framework.validator;

import java.io.Serializable;

/**
 * 值枚举接口
 * 用于支持基于值的枚举验证
 * 
 * @param <T> 值类型
 * @param <V> 枚举类型
 */
public interface ValueEnum<T, V extends Serializable> {

    /**
     * 获取枚举的值
     *
     * @return 枚举值
     */
    T getValue();

    /**
     * 根据值获取对应的枚举实例
     *
     * @param value 要查找的值
     * @return 对应的枚举实例，如果找不到则返回 null
     */
    static <T, E extends Enum<E> & ValueEnum<T, ?>> E valueOf(Class<E> enumClass, T value) {
        if (value == null || enumClass == null) {
            return null;
        }
        
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (value.equals(enumConstant.getValue())) {
                return enumConstant;
            }
        }
        return null;
    }

    /**
     * 根据值获取对应的枚举实例（通用方法）
     *
     * @param value 要查找的值
     * @return 对应的枚举实例，如果找不到则返回 null
     */
    @SuppressWarnings("unchecked")
    default <E extends Enum<E> & ValueEnum<T, ?>> E getValueOf(Serializable value) {
        if (value == null) {
            return null;
        }
        
        E[] constants = (E[]) this.getClass().getEnumConstants();
        
        for (E constant : constants) {
            if (value.equals(constant.getValue())) {
                return constant;
            }
        }
        return null;
    }
}
