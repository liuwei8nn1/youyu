package com.youyu.product.infrastructure.persistence.converter;

import com.youyu.product.domain.model.ProductAggregate;
import com.youyu.product.infrastructure.persistence.entity.ProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

@Mapper
public interface ProductConverter {

    ProductConverter INSTANCE = Mappers.getMapper(ProductConverter.class);

    default ProductAggregate toAggregate(ProductDO productDO) {
        if (productDO == null) {
            return null;
        }
        return ProductAggregate.restore(
            productDO.getId(),
            productDO.getProductName(),
            productDO.getDescription(),
            productDO.getPrice() != null ? productDO.getPrice() : BigDecimal.ZERO,
            productDO.getStock() != null ? productDO.getStock() : 0L,
            productDO.getStatus() != null ? productDO.getStatus() : 1,
            productDO.getIsSeckill() != null ? productDO.getIsSeckill() : false,
            productDO.getSeckillStartTime(),
            productDO.getSeckillEndTime()
        );
    }

    @Mapping(target = "deletedAt", ignore = true)
    ProductDO toDO(ProductAggregate aggregate);
}