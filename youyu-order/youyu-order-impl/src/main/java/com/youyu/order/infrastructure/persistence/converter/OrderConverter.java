package com.youyu.order.infrastructure.persistence.converter;

import com.youyu.order.domain.model.OrderAggregate;
import com.youyu.order.infrastructure.persistence.entity.OrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderConverter {

    OrderConverter INSTANCE = Mappers.getMapper(OrderConverter.class);

    default OrderAggregate toAggregate(OrderDO orderDO) {
        if (orderDO == null) {
            return null;
        }
        // 注意：OrderDO 中缺少 orderType, activityId, payExpireTime 字段
        // 这里使用默认值，实际应该从数据库查询这些字段
        return OrderAggregate.restore(
            orderDO.getId(),
            orderDO.getOrderNo(),
            orderDO.getUserId(),
            orderDO.getProductId(),
            orderDO.getQuantity(),
            orderDO.getAmount(),
            OrderAggregate.ORDER_TYPE_NORMAL, // 默认为普通订单
            null, // activityId
            null, // payExpireTime
            orderDO.getStatus(),
            orderDO.getCreatedAt(),
            orderDO.getUpdatedAt()
        );
    }

    OrderDO toDO(OrderAggregate aggregate);
}