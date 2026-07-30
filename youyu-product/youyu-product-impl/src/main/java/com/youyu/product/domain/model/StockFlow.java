package com.youyu.product.domain.model;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
public class StockFlow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    private Long productId;
    private Long beforeStock;
    private Integer changeQuantity;
    private String flowType;
    private String orderNo;
    private String remark;
    private String operator;
    private LocalDateTime createTime;

    public StockFlow() {
    }

    public static StockFlow create(Long productId, Long beforeStock, Integer changeQuantity,
                                    String flowType, String orderNo, String remark, String operator) {
        StockFlow flow = new StockFlow();
        flow.productId = productId;
        flow.beforeStock = beforeStock;
        flow.changeQuantity = changeQuantity;
        flow.flowType = flowType;
        flow.orderNo = orderNo;
        flow.remark = remark;
        flow.operator = operator;
        flow.createTime = LocalDateTime.now();
        return flow;
    }

    public static StockFlow restore(Long id, Long productId, Long beforeStock,
                                     Integer changeQuantity, String flowType, String orderNo,
                                     String remark, String operator, LocalDateTime createTime) {
        StockFlow flow = new StockFlow();
        flow.id = id;
        flow.productId = productId;
        flow.beforeStock = beforeStock;
        flow.changeQuantity = changeQuantity;
        flow.flowType = flowType;
        flow.orderNo = orderNo;
        flow.remark = remark;
        flow.operator = operator;
        flow.createTime = createTime;
        return flow;
    }

    public void validate() {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("商品ID无效");
        }
        if (changeQuantity == null || changeQuantity == 0) {
            throw new IllegalArgumentException("库存变化量不能为0");
        }
    }
}