package com.youyu.product.domain.repository;

import com.youyu.product.domain.model.ProductAggregate;

import java.util.Optional;

public interface ProductRepository {

    void save(ProductAggregate product);

    Optional<ProductAggregate> findById(Long productId);

    void update(ProductAggregate product);

    boolean deductStock(Long productId, Integer quantity);

    /**
     * 回滚库存（订单超时未支付时调用）
     *
     * @param productId 商品ID
     * @param quantity  回滚数量
     */
    void rollbackStock(Long productId, Integer quantity);
}