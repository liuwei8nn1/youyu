package com.youyu.order.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.order.infrastructure.persistence.entity.OrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrderMapper extends BaseDao<OrderDO> {

    /**
     * 回滚商品库存（数据库乐观锁）
     *
     * @param productId 商品ID
     * @param quantity  回滚数量
     * @return 影响行数
     */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{productId}")
    int rollbackStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}