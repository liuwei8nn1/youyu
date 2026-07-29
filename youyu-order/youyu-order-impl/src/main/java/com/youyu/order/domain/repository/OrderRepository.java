package com.youyu.order.domain.repository;

import java.util.Optional;

import com.youyu.order.domain.aggregate.Order;

public interface OrderRepository {

    void save(Order order);

    void update(Order order);

    Optional<Order> findById(Long orderId);

    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 回滚普通订单库存（直接操作数据库）
     *
     * @param productId 商品ID
     * @param quantity  回滚数量
     */
    void rollbackStock(Long productId, Integer quantity);
}