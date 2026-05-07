package com.youyu.framework.datasource.mybatis;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 数据对象基类（Data Object）- 物理删除
 * <p>
 * 符合 DDD 基础设施层规范，抽取公共字段
 * <p>
 * 使用说明：
 * - 此基类不支持逻辑删除，deleteById() 会执行物理删除
 * - 适用于有唯一约束且不允许重用的表（如用户、角色等）
 * - 如需逻辑删除，请继承 LogicDeleteBaseDO
 *
 * @author LiuWei
 * @since 2026/4/23
 */
@Getter
@Setter
public abstract class BaseDO implements Bean<Long> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    protected LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updatedAt;

    /**
     * 创建人
     */
    protected Long createdBy;

    /**
     * 更新人
     */
    protected Long updatedBy;

    public void initTime(LocalDateTime now){
        this.createdAt = now;
        this.updatedAt = now;
    }

    // ==================== 字段常量定义 ====================
    public static final String ID = "id";
    public static final String CREATED_AT = "created_at";
    public static final String UPDATED_AT = "updated_at";
}
