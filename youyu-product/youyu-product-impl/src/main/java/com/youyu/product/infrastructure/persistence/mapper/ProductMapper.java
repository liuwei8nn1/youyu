package com.youyu.product.infrastructure.persistence.mapper;

import com.youyu.framework.datasource.mybatis.BaseDao;
import com.youyu.product.infrastructure.persistence.entity.ProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseDao<ProductDO> {

    @Update("UPDATE t_product SET stock = stock - #{quantity} " +
            "WHERE id = #{productId} AND stock >= #{quantity}")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 回滚库存（订单超时未支付时调用）
     *
     * @param productId 商品ID
     * @param quantity  回滚数量
     * @return 影响行数
     */
    @Update("UPDATE t_product SET stock = stock + #{quantity} " +
            "WHERE id = #{productId}")
    int rollbackStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}