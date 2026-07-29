package com.youyu.product.domain.repository;

import com.youyu.product.domain.aggregate.StockFlow;

import java.util.List;

public interface StockFlowRepository {

    void save(StockFlow stockFlow);

    List<StockFlow> findByProductId(Long productId, Integer limit);
}