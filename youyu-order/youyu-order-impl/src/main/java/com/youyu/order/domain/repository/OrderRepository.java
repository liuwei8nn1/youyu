package com.youyu.order.domain.repository;

import com.youyu.order.domain.model.OrderAggregate;

import java.util.Optional;

public interface OrderRepository {

    void save(OrderAggregate order);

    void update(OrderAggregate order);

    Optional<OrderAggregate> findById(Long orderId);

    Optional<OrderAggregate> findByOrderNo(String orderNo);

    /**
     * 回滚普通订单库存（直接操作数据库）
     *
     * @param productId 商品ID
     * @param quantity  回滚数量
     */
    void rollbackStock(Long productId, Integer quantity);
}