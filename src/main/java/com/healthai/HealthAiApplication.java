package com.healthai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.healthai.mapper")      // 扫描 Mapper 接口
@EnableAsync            // 启用 @Async 异步
public class HealthAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HealthAiApplication.class, args);
    }
}
