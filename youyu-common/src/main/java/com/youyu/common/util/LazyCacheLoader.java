package com.youyu.common.util;

import java.util.function.Supplier;

/**
 * 懒加载缓存工具类
 * 用于标记未初始化的值，支持延迟加载
 */
public class LazyCacheLoader<E> {

    public static final Object uninitialized = new Object();
    //
    protected transient volatile Object value = uninitialized;
    protected final Supplier<E> loader;

    public LazyCacheLoader(Supplier<E> loader) {
        Assert.notNull(loader);
        this.loader = loader;
    }

    public LazyCacheLoader(final boolean initialize, Supplier<E> loader) {
        this(loader);
        if (initialize) {
            this.value = loader.get();
        }
    }

    public E get() {
        Object val = value;
        if (isUninitialized(val)) {
            synchronized (this) {
                if (isUninitialized(val = value)) {
                    value = val = loader.get();
                }
            }
        }
        return (E) val;
    }


    protected boolean isUninitialized(final Object val) {
        return val == uninitialized;
    }



}