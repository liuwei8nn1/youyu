package com.youyu.product.domain.model;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class PriceHistory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    private Long productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String changeReason;
    private String operator;
    private LocalDateTime createTime;

    public PriceHistory() {
    }

    public static PriceHistory create(Long productId, BigDecimal oldPrice, BigDecimal newPrice,
                                       String changeReason, String operator) {
        PriceHistory history = new PriceHistory();
        history.productId = productId;
        history.oldPrice = oldPrice;
        history.newPrice = newPrice;
        history.changeReason = changeReason;
        history.operator = operator;
        history.createTime = LocalDateTime.now();
        return history;
    }

    public static PriceHistory restore(Long id, Long productId, BigDecimal oldPrice,
                                        BigDecimal newPrice, String changeReason,
                                        String operator, LocalDateTime createTime) {
        PriceHistory history = new PriceHistory();
        history.id = id;
        history.productId = productId;
        history.oldPrice = oldPrice;
        history.newPrice = newPrice;
        history.changeReason = changeReason;
        history.operator = operator;
        history.createTime = createTime;
        return history;
    }

    public void validate() {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("商品ID无效");
        }
        if (oldPrice == null) {
            throw new IllegalArgumentException("原价不能为空");
        }
        if (newPrice == null) {
            throw new IllegalArgumentException("新价格不能为空");
        }
    }
}