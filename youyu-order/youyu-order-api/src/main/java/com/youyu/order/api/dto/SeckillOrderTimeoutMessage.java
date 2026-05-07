package com.youyu.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单超时未支付消息（通用，支持普通订单和秒杀订单）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderTimeoutMessage implements Serializable {

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
     * 订单类型：NORMAL-普通订单，SECKILL-秒杀订单
     */
    private String orderType;

    /**
     * 活动ID（仅秒杀订单有值）
     */
    private Long activityId;
}