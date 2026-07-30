package com.youyu.product.infrastructure.persistence.repository;

import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.product.domain.model.StockFlow;
import com.youyu.product.domain.repository.StockFlowRepository;
import com.youyu.product.infrastructure.persistence.converter.StockFlowConverter;
import com.youyu.product.infrastructure.persistence.entity.StockFlowDO;
import com.youyu.product.infrastructure.persistence.mapper.StockFlowMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class StockFlowRepositoryImpl extends BaseRepositoryImpl<StockFlowDO, StockFlowMapper, Long> implements StockFlowRepository {

    @Override
    public void save(StockFlow stockFlow) {
        StockFlowDO stockFlowDO = StockFlowConverter.INSTANCE.toDO(stockFlow);
        baseDao.insert(stockFlowDO);
        stockFlow.setId(stockFlowDO.getId());
        log.info("库存流水保存成功，id: {}", stockFlowDO.getId());
    }

    @Override
    public List<StockFlow> findByProductId(Long productId, Integer limit) {
        SmartQueryWrapper<StockFlowDO> wrapper = new SmartQueryWrapper<StockFlowDO>()
                .eq(StockFlowDO.PRODUCT_ID, productId)
                .orderByDesc(StockFlowDO.CREATED_AT)
                .last("LIMIT " + limit);
        List<StockFlowDO> doList = baseDao.selectList(wrapper);
        List<StockFlow> result = new ArrayList<>();
        for (StockFlowDO stockFlowDO : doList) {
            result.add(StockFlowConverter.INSTANCE.toDomain(stockFlowDO));
        }
        return result;
    }
}
