package com.youyu.product.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.LogicDeleteBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_category")
public class CategoryDO extends LogicDeleteBaseDO {

    private String categoryName;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private Integer status;

    // ==================== 字段常量定义 ====================
    public static final String CATEGORY_NAME = "category_name";
    public static final String PARENT_ID = "parent_id";
    public static final String LEVEL = "level";
    public static final String SORT_ORDER = "sort_order";
    public static final String STATUS = "status";
}
