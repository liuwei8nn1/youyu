package com.youyu.product.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.LogicDeleteBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 价格历史数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_price_history")
public class PriceHistoryDO extends LogicDeleteBaseDO {

    private Long productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String changeReason;
    private String operator;

    // ==================== 字段常量定义 ====================
    public static final String PRODUCT_ID = "product_id";
    public static final String OLD_PRICE = "old_price";
    public static final String NEW_PRICE = "new_price";
    public static final String CHANGE_REASON = "change_reason";
    public static final String OPERATOR = "operator";
}
