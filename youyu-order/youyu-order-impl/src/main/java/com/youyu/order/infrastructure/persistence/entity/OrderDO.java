package com.youyu.order.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.LogicDeleteBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class OrderDO extends LogicDeleteBaseDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal amount;
    private Integer status;

    // ==================== 字段常量定义 ====================
    public static final String ORDER_NO = "order_no";
    public static final String USER_ID = "user_id";
    public static final String PRODUCT_ID = "product_id";
    public static final String QUANTITY = "quantity";
    public static final String AMOUNT = "amount";
    public static final String STATUS = "status";
}
