package com.youyu.seckill.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀订单响应DTO（应用层）
 * <p>
 * 注意：外层已由Result包装，此处只包含业务数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderResponse {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 提示信息（如：排队中、抢购成功等）
     */
    private String tipMessage;

    /**
     * 创建成功响应
     */
    public static SeckillOrderResponse of(String orderId, String tipMessage) {
        return new SeckillOrderResponse(orderId, tipMessage);
    }
}
