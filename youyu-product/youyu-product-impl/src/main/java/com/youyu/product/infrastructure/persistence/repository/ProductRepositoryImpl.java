package com.youyu.product.infrastructure.persistence.repository;

import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.product.domain.model.ProductAggregate;
import com.youyu.product.domain.repository.ProductRepository;
import com.youyu.product.infrastructure.persistence.converter.ProductConverter;
import com.youyu.product.infrastructure.persistence.entity.ProductDO;
import com.youyu.product.infrastructure.persistence.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
public class ProductRepositoryImpl extends BaseRepositoryImpl<ProductDO, ProductMapper, Long> implements ProductRepository {

    @Override
    public void save(ProductAggregate product) {
        ProductDO productDO = ProductConverter.INSTANCE.toDO(product);
        if (productDO.getId() == null) {
            baseDao.insert(productDO);
            product.setId(productDO.getId());
            log.info("商品保存成功，productId: {}", productDO.getId());
        } else {
            baseDao.updateById(productDO);
            log.info("商品更新成功，productId: {}", productDO.getId());
        }
    }

    @Override
    public Optional<ProductAggregate> findById(Long productId) {
        ProductDO productDO = baseDao.selectById(productId);
        return Optional.ofNullable(ProductConverter.INSTANCE.toAggregate(productDO));
    }

    @Override
    public void update(ProductAggregate product) {
        ProductDO productDO = ProductConverter.INSTANCE.toDO(product);
        baseDao.updateById(productDO);
        log.info("商品更新成功，productId: {}", productDO.getId());
    }

    @Override
    public boolean deductStock(Long productId, Integer quantity) {
        int rows = baseDao.deductStock(productId, quantity);
        return rows > 0;
    }

    @Override
    public void rollbackStock(Long productId, Integer quantity) {
        int rows = baseDao.rollbackStock(productId, quantity);
        if (rows == 0) {
            log.warn("库存回滚失败，商品可能不存在，productId: {}, quantity: {}", productId, quantity);
        } else {
            log.info("库存回滚成功，productId: {}, quantity: {}", productId, quantity);
        }
    }
}
