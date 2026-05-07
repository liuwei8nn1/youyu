package com.youyu.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单创建响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 订单号
     */
    private String orderNo;
}
