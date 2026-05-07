package com.youyu.order.infrastructure.external.adapter;

import com.youyu.common.model.Result;
import com.youyu.order.domain.repository.ProductRepository;
import com.youyu.product.api.client.ProductFeignClient;
import com.youyu.product.api.dto.ProductDetailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商品仓储实现(基础设施层 - Adapter)
 * <p>
 * 职责:
 * 1. 实现领域层定义的 ProductRepository 接口
 * 2. 封装对 ProductFeignClient 的调用
 * 3. 异常处理和日志记录
 * <p>
 * DDD 设计说明:
 * - 这是基础设施层的适配器(Adapter)
 * - 将外部服务(Feign)适配为领域层需要的接口
 * - 符合六边形架构(Hexagonal Architecture)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductFeignClient productFeignClient;

    @Override
    public Optional<ProductDetailDTO> findById(Long productId) {
        log.info("查询商品详情，productId: {}", productId);
        
        try {
            // 1. 调用 Feign Client
            Result<ProductDetailDTO> result = productFeignClient.getProductDetail(productId);
            
            // 2. 检查结果
            if (result == null || !result.isSuccess() || result.getData() == null) {
                log.warn("未找到商品信息，productId: {}", productId);
                return Optional.empty();
            }
            
            ProductDetailDTO product = result.getData();
            
            log.info("查询商品详情成功，productId: {}, productName: {}, price: {}", 
                productId, product.getProductName(), product.getPrice());
            
            return Optional.of(product);
            
        } catch (Exception e) {
            log.error("查询商品详情失败，productId: {}", productId, e);
            return Optional.empty();
        }
    }
}
