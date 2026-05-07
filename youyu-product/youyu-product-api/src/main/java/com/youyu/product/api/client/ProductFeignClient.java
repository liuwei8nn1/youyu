package com.youyu.product.api.client;

import com.youyu.common.model.Result;
import com.youyu.product.api.dto.ProductDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务 Feign 客户端
 * <p>
 * 用于其他微服务调用 product-service 提供的商品查询接口
 * <p>
 * 工作模式:
 * - 如果配置了 service.url.product-service: 直接使用该 URL(K8s 环境,负载均衡由 K8s 处理)
 * - 如果未配置 service.url.product-service: 使用 name 从注册中心查找(传统微服务架构,客户端负载均衡)
 */
@FeignClient(name = "product-service", path = "/api/product", url = "${service.url.product-service:}")
public interface ProductFeignClient {

    /**
     * 根据商品ID查询商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @GetMapping("/detail/{productId}")
    Result<ProductDetailDTO> getProductDetail(@PathVariable("productId") Long productId);
}
