package com.youyu.gateway;

import com.youyu.framework.BaseApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication extends BaseApplication {
    public static void main(String[] args) {
        startup(GatewayApplication.class, args);
    }
}
