package com.youyu.seckill.interfaces.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillActivityRequest {
    private Long id;
    private Long productId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer stock;
    private Integer limitPerUser;
    private BigDecimal seckillPrice;
}
