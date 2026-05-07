package com.youyu.product.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youyu.framework.datasource.mybatis.LogicDeleteBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存流水数据对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_flow")
public class StockFlowDO extends LogicDeleteBaseDO {

    private Long productId;
    private Long beforeStock;
    private Integer changeQuantity;
    private String flowType;
    private String orderNo;
    private String remark;
    private String operator;

    // ==================== 字段常量定义 ====================
    public static final String PRODUCT_ID = "product_id";
    public static final String BEFORE_STOCK = "before_stock";
    public static final String CHANGE_QUANTITY = "change_quantity";
    public static final String FLOW_TYPE = "flow_type";
    public static final String ORDER_NO = "order_no";
    public static final String REMARK = "remark";
    public static final String OPERATOR = "operator";
}
