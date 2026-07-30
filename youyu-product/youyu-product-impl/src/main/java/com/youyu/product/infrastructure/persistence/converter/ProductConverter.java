package com.youyu.product.infrastructure.persistence.converter;

import java.math.BigDecimal;

import com.youyu.product.domain.model.Product;
import com.youyu.product.infrastructure.persistence.entity.ProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProductConverter {

    ProductConverter INSTANCE = Mappers.getMapper(ProductConverter.class);

    default Product toProduct(ProductDO productDO) {
        if (productDO == null) {
            return null;
        }
        return Product.restore(
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
    ProductDO toDO(Product product);
}