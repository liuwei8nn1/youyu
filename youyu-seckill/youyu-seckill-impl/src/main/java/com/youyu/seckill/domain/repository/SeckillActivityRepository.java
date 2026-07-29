package com.youyu.seckill.domain.repository;

import java.util.List;

import com.youyu.seckill.domain.aggregate.SeckillActivity;

/**
 * 秒杀活动仓储接口（领域层）
 */
public interface SeckillActivityRepository {

    /**
     * 根据ID查询活动
     */
    SeckillActivity findById(Long id);

    /**
     * 根据商品ID查询活动
     */
    SeckillActivity findByProductId(Long productId);

    /**
     * 查询所有活动
     */
    List<SeckillActivity> listAll();

    /**
     * 保存活动
     */
    void save(SeckillActivity activity);

    /**
     * 更新活动
     */
    void update(SeckillActivity activity);

    /**
     * 删除活动
     */
    void delete(Long id);
}
