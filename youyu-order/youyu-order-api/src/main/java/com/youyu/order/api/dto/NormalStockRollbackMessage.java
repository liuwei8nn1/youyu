package com.youyu.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 普通订单库存回滚消息DTO
 * <p>
 * 用于订单超时未支付时，通知商品服务回滚数据库库存
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NormalStockRollbackMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer quantity;
}
