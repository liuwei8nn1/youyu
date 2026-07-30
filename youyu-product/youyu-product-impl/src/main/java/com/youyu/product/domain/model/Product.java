package com.youyu.product.domain.model;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    private String productName;
    private String description;
    private BigDecimal price;
    private Long stock;
    private Integer status;
    private Boolean isSeckill;
    private LocalDateTime seckillStartTime;
    private LocalDateTime seckillEndTime;

    public Product() {
    }

    public static Product create(String productName, String description, BigDecimal price, Long stock) {
        Product product = new Product();
        product.productName = productName;
        product.description = description;
        product.price = price;
        product.stock = stock;
        product.status = 1;
        product.isSeckill = false;
        return product;
    }

    public static Product createSeckillProduct(String productName, String description,
                                               BigDecimal price, Long stock,
                                               LocalDateTime startTime, LocalDateTime endTime) {
        Product product = new Product();
        product.productName = productName;
        product.description = description;
        product.price = price;
        product.stock = stock;
        product.status = 1;
        product.isSeckill = true;
        product.seckillStartTime = startTime;
        product.seckillEndTime = endTime;
        return product;
    }

    public static Product restore(Long id, String productName, String description,
                                  BigDecimal price, Long stock, Integer status,
                                  Boolean isSeckill, LocalDateTime seckillStartTime,
                                  LocalDateTime seckillEndTime) {
        Product product = new Product();
        product.id = id;
        product.productName = productName;
        product.description = description;
        product.price = price;
        product.stock = stock;
        product.status = status;
        product.isSeckill = isSeckill != null ? isSeckill : false;
        product.seckillStartTime = seckillStartTime;
        product.seckillEndTime = seckillEndTime;
        return product;
    }

    public void validate() {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("商品价格必须大于0");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("商品库存不能为负数");
        }
    }

    public boolean isStockSufficient(Integer quantity) {
        return stock != null && stock >= quantity;
    }

    public boolean isInSeckillPeriod() {
        if (!Boolean.TRUE.equals(isSeckill)) {
            return false;
        }
        if (seckillStartTime == null || seckillEndTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(seckillStartTime) && !now.isAfter(seckillEndTime);
    }

    public void putOnShelf() {
        if (this.status != null && this.status == 1) {
            throw new IllegalStateException("商品已在上架状态");
        }
        this.status = 1;
    }

    public void takeOffShelf() {
        if (this.status != null && this.status == 0) {
            throw new IllegalStateException("商品已在下架状态");
        }
        this.status = 0;
    }

    public void updateInfo(String productName, String description, BigDecimal price) {
        if (productName != null && !productName.trim().isEmpty()) {
            this.productName = productName;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            this.price = price;
        }
        validate();
    }

    public void markAsDeleted() {
        this.status = -1;
    }

    public boolean isDeleted() {
        return this.status != null && this.status == -1;
    }

    public boolean isOnShelf() {
        return this.status != null && this.status == 1;
    }
}