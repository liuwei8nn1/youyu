package com.youyu.framework.datasource.mybatis;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;

/**
 * 支持逻辑删除的数据对象基类
 * <p>
 * 使用 deletedAt 时间戳字段实现逻辑删除：
 * - NULL: 未删除（有效数据）
 * - 非NULL: 已删除（值为删除时的秒级时间戳）
 * <p>
 * 优势：
 * 1. 支持唯一索引字段删除后重用（通过联合唯一索引 field + deleted_at）
 * 2. 保留删除时间信息，便于审计追溯
 * 3. MyBatis-Plus 自动处理查询过滤和删除转换
 * <p>
 * 数据库索引建议：
 * - 对于有唯一约束的字段，创建联合唯一索引：UNIQUE(field, deleted_at)
 * - 示例：ALTER TABLE sys_role ADD UNIQUE INDEX uk_role_code_deleted (role_code, deleted_at);
 *
 * @author LiuWei
 * @since 2026/4/23
 */
@Getter
@Setter
public abstract class LogicDeleteBaseDO extends BaseDO {

    /**
     * 逻辑删除时间戳（秒级）
     * <ul>
     *   <li>NULL: 未删除（有效数据）</li>
     *   <li>非NULL: 已删除（值为删除时的秒级时间戳）</li>
     * </ul>
     * <p>
     * 注意：
     * 1. 此字段不使用自动填充，由 MyBatis-Plus 在删除时自动设置
     * 2. 数据库字段类型建议使用 BIGINT NULL DEFAULT NULL
     * 3. 对于有唯一约束的字段，需创建联合唯一索引 (field, deleted_at)
     */
    @TableLogic(value = "NULL", delval = "unix_timestamp()")
    @TableField
    private Long deletedAt;

    // ==================== 字段常量定义 ====================
    public static final String DELETED_AT = "deleted_at";
}
