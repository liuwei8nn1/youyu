package com.youyu.seckill.domain.repository;

import com.youyu.seckill.domain.model.SeckillActivityAggregate;

import java.util.List;

/**
 * 秒杀活动仓储接口（领域层）
 */
public interface SeckillActivityRepository {

    /**
     * 根据ID查询活动
     */
    SeckillActivityAggregate findById(Long id);

    /**
     * 根据商品ID查询活动
     */
    SeckillActivityAggregate findByProductId(Long productId);

    /**
     * 查询所有活动
     */
    List<SeckillActivityAggregate> listAll();

    /**
     * 保存活动
     */
    void save(SeckillActivityAggregate activity);

    /**
     * 更新活动
     */
    void update(SeckillActivityAggregate activity);

    /**
     * 删除活动
     */
    void delete(Long id);
}
