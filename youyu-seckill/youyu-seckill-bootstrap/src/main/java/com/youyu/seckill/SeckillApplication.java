package com.youyu.seckill;

import com.youyu.framework.BaseApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SeckillApplication extends BaseApplication {
    public static void main(String[] args) {
        startup(SeckillApplication.class, args);
    }
}
