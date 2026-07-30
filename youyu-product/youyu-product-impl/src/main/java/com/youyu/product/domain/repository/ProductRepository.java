package com.youyu.product.domain.repository;

import java.util.Optional;

import com.youyu.product.domain.model.Product;

public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(Long productId);

    void update(Product product);

    boolean deductStock(Long productId, Integer quantity);

    /**
     * 回滚库存（订单超时未支付时调用）
     *
     * @param productId 商品ID
     * @param quantity  回滚数量
     */
    void rollbackStock(Long productId, Integer quantity);
}