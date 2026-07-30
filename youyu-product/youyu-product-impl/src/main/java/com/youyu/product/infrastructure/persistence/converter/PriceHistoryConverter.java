package com.youyu.product.infrastructure.persistence.converter;

import com.youyu.product.domain.model.PriceHistory;
import com.youyu.product.infrastructure.persistence.entity.PriceHistoryDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PriceHistoryConverter {

    PriceHistoryConverter INSTANCE = Mappers.getMapper(PriceHistoryConverter.class);

    default PriceHistory toDomain(PriceHistoryDO priceHistoryDO) {
        if (priceHistoryDO == null) {
            return null;
        }
        return PriceHistory.restore(
            priceHistoryDO.getId(),
            priceHistoryDO.getProductId(),
            priceHistoryDO.getOldPrice(),
            priceHistoryDO.getNewPrice(),
            priceHistoryDO.getChangeReason(),
            priceHistoryDO.getOperator(),
            priceHistoryDO.getCreatedAt()
        );
    }

    PriceHistoryDO toDO(PriceHistory domain);
}