package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.ecommerce.user.mapper")
public class EUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(EUserApplication.class, args);
    }
}
