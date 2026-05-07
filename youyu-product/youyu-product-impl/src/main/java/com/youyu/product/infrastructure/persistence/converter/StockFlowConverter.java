package com.youyu.product.infrastructure.persistence.converter;

import com.youyu.product.domain.model.StockFlow;
import com.youyu.product.infrastructure.persistence.entity.StockFlowDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StockFlowConverter {

    StockFlowConverter INSTANCE = Mappers.getMapper(StockFlowConverter.class);

    default StockFlow toDomain(StockFlowDO stockFlowDO) {
        if (stockFlowDO == null) {
            return null;
        }
        return StockFlow.restore(
            stockFlowDO.getId(),
            stockFlowDO.getProductId(),
            stockFlowDO.getBeforeStock(),
            stockFlowDO.getChangeQuantity(),
            stockFlowDO.getFlowType(),
            stockFlowDO.getOrderNo(),
            stockFlowDO.getRemark(),
            stockFlowDO.getOperator(),
            stockFlowDO.getCreatedAt()
        );
    }

    StockFlowDO toDO(StockFlow domain);
}