package com.youyu.product.infrastructure.persistence.repository;

import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.product.domain.aggregate.PriceHistory;
import com.youyu.product.domain.repository.PriceHistoryRepository;
import com.youyu.product.infrastructure.persistence.converter.PriceHistoryConverter;
import com.youyu.product.infrastructure.persistence.entity.PriceHistoryDO;
import com.youyu.product.infrastructure.persistence.mapper.PriceHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class PriceHistoryRepositoryImpl extends BaseRepositoryImpl<PriceHistoryDO, PriceHistoryMapper, Long> implements PriceHistoryRepository {

    @Override
    public void save(PriceHistory priceHistory) {
        PriceHistoryDO priceHistoryDO = PriceHistoryConverter.INSTANCE.toDO(priceHistory);
        baseDao.insert(priceHistoryDO);
        priceHistory.setId(priceHistoryDO.getId());
        log.info("价格历史保存成功，id: {}", priceHistoryDO.getId());
    }

    @Override
    public List<PriceHistory> findByProductId(Long productId, Integer limit) {
        SmartQueryWrapper<PriceHistoryDO> wrapper = new SmartQueryWrapper<PriceHistoryDO>()
                .eq(PriceHistoryDO.PRODUCT_ID, productId)
                .orderByDesc(PriceHistoryDO.CREATED_AT)
                .last("LIMIT " + limit);
        List<PriceHistoryDO> doList = baseDao.selectList(wrapper);
        List<PriceHistory> result = new ArrayList<>();
        for (PriceHistoryDO priceHistoryDO : doList) {
            result.add(PriceHistoryConverter.INSTANCE.toDomain(priceHistoryDO));
        }
        return result;
    }
}
