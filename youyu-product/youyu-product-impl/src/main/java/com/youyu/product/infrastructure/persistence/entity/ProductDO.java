package com.youyu.product.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.LogicDeleteBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
public class ProductDO extends LogicDeleteBaseDO {

    private String productName;
    private String description;
    private BigDecimal price;
    private Long stock;
    private Integer status;
    private Boolean isSeckill;
    private LocalDateTime seckillStartTime;
    private LocalDateTime seckillEndTime;

    // ==================== 字段常量定义 ====================
    public static final String PRODUCT_NAME = "product_name";
    public static final String DESCRIPTION = "description";
    public static final String PRICE = "price";
    public static final String STOCK = "stock";
    public static final String STATUS = "status";
    public static final String IS_SECKILL = "is_seckill";
    public static final String SECKILL_START_TIME = "seckill_start_time";
    public static final String SECKILL_END_TIME = "seckill_end_time";
}
