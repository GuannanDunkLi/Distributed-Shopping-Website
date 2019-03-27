package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient // 能够让注册中心发现，扫描到服务
@MapperScan("com.ecommerce.item.mapper")
public class EItemService {
    public static void main(String[] args) {
        SpringApplication.run(EItemService.class, args);
    }
}