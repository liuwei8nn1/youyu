package com.youyu.order.interfaces.converter;

import com.youyu.order.domain.model.Order;
import com.youyu.order.interfaces.vo.OrderVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderConverter {

    OrderConverter INSTANCE = Mappers.getMapper(OrderConverter.class);

    @Mapping(target = "receiverName", source = "shippingAddress.receiverName")
    @Mapping(target = "receiverPhone", source = "shippingAddress.receiverPhone")
    @Mapping(target = "province", source = "shippingAddress.province")
    @Mapping(target = "city", source = "shippingAddress.city")
    @Mapping(target = "district", source = "shippingAddress.district")
    @Mapping(target = "detailAddress", source = "shippingAddress.detailAddress")
    @Mapping(target = "zipCode", source = "shippingAddress.zipCode")
    OrderVO toVO(Order order);
}
