package com.youyu.seckill.interfaces.converter;

import com.youyu.seckill.domain.model.SeckillActivity;
import com.youyu.seckill.interfaces.vo.SeckillActivityVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SeckillActivityConverter {

    SeckillActivityConverter INSTANCE = Mappers.getMapper(SeckillActivityConverter.class);

    SeckillActivityVO toVO(SeckillActivity activity);
}
