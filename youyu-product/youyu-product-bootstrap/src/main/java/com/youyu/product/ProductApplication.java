package com.youyu.product;

import com.youyu.framework.BaseApplication;
import com.youyu.auth.api.client.AuthFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients( clients = { AuthFeignClient.class })
@MapperScan("com.youyu.product.infrastructure.persistence.mapper")
public class ProductApplication extends BaseApplication {
    public static void main(String[] args) {
        startup(ProductApplication.class, args);
    }
}
