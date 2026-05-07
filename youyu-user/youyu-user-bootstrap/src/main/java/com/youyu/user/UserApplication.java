package com.youyu.user;

import com.youyu.auth.api.client.UserIdentityCreateApi;
import com.youyu.framework.BaseApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.youyu.user.impl.infrastructure.persistence.mapper")
@EnableFeignClients(clients = { UserIdentityCreateApi.class })
public class UserApplication extends BaseApplication {

    public static void main(String[] args) {
        startup(UserApplication.class, args);
    }
}
