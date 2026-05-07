package com.youyu.seckill.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 秒杀库存回滚消息DTO
 * <p>
 * 用于订单超时未支付时，通知秒杀服务回滚库存和限购数量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillStockRollbackMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 活动ID
     */
    private Long activityId;
}
