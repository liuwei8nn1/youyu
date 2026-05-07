package com.youyu.order.domain.repository;

import com.youyu.product.api.dto.ProductDetailDTO;
import java.util.Optional;

/**
 * 商品仓储接口(领域层)
 * <p>
 * 职责:
 * 1. 抽象商品相关的外部查询
 * 2. 领域层不关心具体实现(HTTP/RPC/MQ)
 * 3. 返回 DTO 或领域对象
 * <p>
 * DDD 设计说明:
 * - 这是领域层的端口(Port),定义契约
 * - 具体实现在基础设施层(Adapter)
 * - 符合依赖倒置原则(DIP)
 * <p>
 * 注意:
 * - 这里返回 ProductDetailDTO 是因为商品信息来自外部服务
 * - 如果商品属于本领域,应该返回 ProductAggregate
 */
public interface ProductRepository {
    
    /**
     * 根据商品ID查询商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    Optional<ProductDetailDTO> findById(Long productId);
}
