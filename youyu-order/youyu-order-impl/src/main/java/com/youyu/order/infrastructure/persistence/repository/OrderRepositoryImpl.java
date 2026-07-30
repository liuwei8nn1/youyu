package com.youyu.order.infrastructure.persistence.repository;

import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.order.domain.model.Order;
import com.youyu.order.domain.repository.OrderRepository;
import com.youyu.order.infrastructure.persistence.converter.OrderConverter;
import com.youyu.order.infrastructure.persistence.entity.OrderDO;
import com.youyu.order.infrastructure.persistence.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class OrderRepositoryImpl extends BaseRepositoryImpl<OrderDO, OrderMapper, Long> implements OrderRepository {

    @Override
    public void save(Order order) {
        OrderDO orderDO = OrderConverter.INSTANCE.toDO(order);
        if (orderDO.getId() == null) {
            baseDao.insert(orderDO);
            order.setId(orderDO.getId());
            log.info("订单保存成功，orderId: {}", orderDO.getId());
        } else {
            baseDao.updateById(orderDO);
            log.info("订单更新成功，orderId: {}", orderDO.getId());
        }
    }

    @Override
    public void update(Order order) {
        OrderDO orderDO = OrderConverter.INSTANCE.toDO(order);
        baseDao.updateById(orderDO);
        log.info("订单更新成功，orderId: {}", orderDO.getId());
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        OrderDO orderDO = baseDao.selectById(orderId);
        return Optional.ofNullable(OrderConverter.INSTANCE.toAggregate(orderDO));
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        SmartQueryWrapper<OrderDO> wrapper = new SmartQueryWrapper<OrderDO>().eq(OrderDO.ORDER_NO, orderNo);
        OrderDO orderDO = baseDao.selectOne(wrapper);
        return Optional.ofNullable(OrderConverter.INSTANCE.toAggregate(orderDO));
    }

    @Override
    public void rollbackStock(Long productId, Integer quantity) {
        log.info("开始回滚普通订单库存，productId: {}, quantity: {}", productId, quantity);
        int rows = baseDao.rollbackStock(productId, quantity);
        if (rows > 0) {
            log.info("普通订单库存回滚成功，productId: {}, quantity: {}", productId, quantity);
        } else {
            log.warn("普通订单库存回滚失败，productId: {}, quantity: {}", productId, quantity);
            throw new RuntimeException("库存回滚失败");
        }
    }
}
