package com.youyu.product.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品详情DTO
 * <p>
 * 用于微服务间通过 HTTP/Feign 传递商品信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品库存
     */
    private Long stock;

    /**
     * 商品状态: 0-下架, 1-上架, -1-已删除
     */
    private Integer status;

    /**
     * 是否为秒杀商品
     */
    private Boolean isSeckill;
}
