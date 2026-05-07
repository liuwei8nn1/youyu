package com.youyu.framework.web.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus Page 分页转换工具类
 * 
 * <p>用于在 DO（数据对象）和 Domain（领域模型）之间进行分页结果的转换，
 * 避免创建新的 Page 对象，减少内存占用和 GC 压力。</p>
 *
 * @author youyu
 * @since 2026-05-07
 */
public class PageUtil {

    /**
     * 将 Page<Source> 转换为 Page<Target>
     * 
     * <p>通过原始类型操作避免泛型检查，直接复用原 Page 对象，不创建新实例。</p>
     *
     * @param sourcePage 源分页对象
     * @param converter  转换函数，将 Source 类型转换为 Target 类型
     * @param <Source>   源类型（如 DO）
     * @param <Target>   目标类型（如 Domain）
     * @return 转换后的分页对象
     */
    @SuppressWarnings("unchecked")
    public static <Source, Target> Page<Target> convert(Page<Source> sourcePage, Function<Source, Target> converter) {
        if (sourcePage == null) {
            return null;
        }

        // 转换记录列表
        List<Target> targetList = sourcePage.getRecords().stream()
                .map(converter)
                .collect(Collectors.toList());

        // 通过原始类型操作避免泛型检查，不创建新Page对象
        Page rawPage = sourcePage;
        rawPage.setRecords(targetList);
        
        return (Page<Target>) rawPage;
    }

    /**
     * 将 Page<Source> 转换为 Page<Target>（支持自定义转换逻辑）
     *
     * @param sourcePage 源分页对象
     * @param converter  转换函数，接收整个 records 列表进行批量转换
     * @param <Source>   源类型（如 DO）
     * @param <Target>   目标类型（如 Domain）
     * @return 转换后的分页对象
     */
    @SuppressWarnings("unchecked")
    public static <Source, Target> Page<Target> convertBatch(Page<Source> sourcePage, 
                                                              Function<List<Source>, List<Target>> converter) {
        if (sourcePage == null) {
            return null;
        }

        // 批量转换记录列表
        List<Target> targetList = converter.apply(sourcePage.getRecords());

        // 通过原始类型操作避免泛型检查，不创建新Page对象
        Page rawPage = sourcePage;
        rawPage.setRecords(targetList);
        
        return (Page<Target>) rawPage;
    }
}
