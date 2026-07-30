package com.youyu.order.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private BigDecimal amount;
    private String orderType;
    private Long activityId;

    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String zipCode;

    private LocalDateTime payExpireTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
