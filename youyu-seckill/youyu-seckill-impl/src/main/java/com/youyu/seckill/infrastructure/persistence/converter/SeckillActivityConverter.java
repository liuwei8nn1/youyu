package com.youyu.seckill.infrastructure.persistence.converter;

import com.youyu.seckill.domain.aggregate.SeckillActivity;
import com.youyu.seckill.infrastructure.persistence.entity.SeckillActivityDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 秒杀活动转换器（基础设施层）
 */
@Mapper
public interface SeckillActivityConverter {

    SeckillActivityConverter INSTANCE = Mappers.getMapper(SeckillActivityConverter.class);

    /**
     * DO 转领域对象
     */
    SeckillActivity toDomain(SeckillActivityDO seckillActivityDO);

    /**
     * 领域对象转 DO
     */
    SeckillActivityDO toDO(SeckillActivity activity);
}
