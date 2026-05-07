package com.youyu.order.api.client;


import com.youyu.common.model.Result;
import com.youyu.order.api.dto.SeckillOrderCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单服务 Feign 客户端
 * 用于其他微服务调用 order-service 提供的订单创建接口
 *  * <p>
 *  * 工作模式:
 *  * - 如果配置了 service.url.order-service: 直接使用该 URL(K8s 环境,负载均衡由 K8s 处理)
 *  * - 如果未配置 service.url.order-service: 使用 name 从注册中心查找(传统微服务架构,客户端负载均衡)
 */
@FeignClient(name = "order-service", path = "/api/order", url = "${service.url.order-service:}")
public interface OrderFeignClient {

    /**
     * 创建秒杀订单
     *
     * @param request 订单创建请求
     * @return 订单ID
     */
    @PostMapping("/seckill/create")
    Result<Long> createSeckillOrder(@RequestBody SeckillOrderCreateRequest request);
}
