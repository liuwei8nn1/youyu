package com.youyu.seckill.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.youyu.framework.datasource.mybatis.BaseRepositoryImpl;
import com.youyu.framework.datasource.mybatis.SmartQueryWrapper;
import com.youyu.seckill.domain.model.SeckillActivity;
import com.youyu.seckill.domain.repository.SeckillActivityRepository;
import com.youyu.seckill.infrastructure.persistence.converter.SeckillActivityConverter;
import com.youyu.seckill.infrastructure.persistence.entity.SeckillActivityDO;
import com.youyu.seckill.infrastructure.persistence.mapper.SeckillActivityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀活动仓储实现（基础设施层）
 */
@Repository
public class SeckillActivityRepositoryImpl extends BaseRepositoryImpl<SeckillActivityDO, SeckillActivityMapper, Long> implements SeckillActivityRepository {

    @Override
    public SeckillActivity findById(Long id) {
        SeckillActivityDO activityDO = baseDao.selectById(id);
        return activityDO != null ? SeckillActivityConverter.INSTANCE.toDomain(activityDO) : null;
    }

    @Override
    public SeckillActivity findByProductId(Long productId) {
        SmartQueryWrapper<SeckillActivityDO> wrapper = new SmartQueryWrapper<SeckillActivityDO>()
                .eq(SeckillActivityDO.PRODUCT_ID, productId);
        SeckillActivityDO activityDO = baseDao.selectOne(wrapper);
        return activityDO != null ? SeckillActivityConverter.INSTANCE.toDomain(activityDO) : null;
    }

    @Override
    public List<SeckillActivity> listAll() {
        List<SeckillActivityDO> activityDOList = baseDao.selectList(new QueryWrapper<>());
        return activityDOList.stream()
                .map(SeckillActivityConverter.INSTANCE::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(SeckillActivity activity) {
        SeckillActivityDO activityDO = SeckillActivityConverter.INSTANCE.toDO(activity);
        baseDao.insert(activityDO);
        activity.setId(activityDO.getId());
    }

    @Override
    public void update(SeckillActivity activity) {
        SeckillActivityDO activityDO = SeckillActivityConverter.INSTANCE.toDO(activity);
        baseDao.updateById(activityDO);
    }

    @Override
    public void delete(Long id) {
        baseDao.deleteById(id);
    }
}
