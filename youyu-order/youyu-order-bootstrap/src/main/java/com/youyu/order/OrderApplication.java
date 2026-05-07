package com.youyu.order;

import com.youyu.product.api.client.ProductFeignClient;
import com.youyu.user.api.client.UserFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类
 * <p>
 * 注意:
 * - 使用 clients 属性显式指定需要的 Feign Client,避免扫描不必要的接口
 * - 如果需要添加新的 Feign Client,在这里显式声明
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.youyu.order.infrastructure.persistence.mapper")
@EnableFeignClients(clients = {
    UserFeignClient.class,      // 用户服务 Feign Client
    ProductFeignClient.class    // 商品服务 Feign Client
})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
