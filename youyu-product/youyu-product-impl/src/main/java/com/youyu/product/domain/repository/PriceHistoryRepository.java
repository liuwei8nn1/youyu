package com.youyu.product.domain.repository;

import com.youyu.product.domain.model.PriceHistory;

import java.util.List;

public interface PriceHistoryRepository {

    void save(PriceHistory priceHistory);

    List<PriceHistory> findByProductId(Long productId, Integer limit);
}