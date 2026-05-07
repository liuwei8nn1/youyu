package com.youyu.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * AI 服务启动类
 *
 * @author YouYu Team
 * @since 2026/05/07
 */
@SpringBootApplication
public class AiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
        System.out.println("========================================");
        System.out.println("   YouYu AI Service Started Successfully!");
        System.out.println("========================================");
    }
}
