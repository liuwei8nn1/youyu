package com.youyu.auth;

import com.youyu.framework.BaseApplication;
import com.youyu.user.api.client.UserFeignClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.youyu.auth.infrastructure.persistence.mapper")
@EnableFeignClients( clients = { UserFeignClient.class })
@EnableAspectJAutoProxy
public class AuthApplication extends BaseApplication {
    public static void main(String[] args) {
        startup(AuthApplication.class, args);
    }
}
