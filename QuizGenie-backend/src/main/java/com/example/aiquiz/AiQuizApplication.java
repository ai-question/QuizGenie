package com.example.aiquiz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.aiquiz.mapper")
public class AiQuizApplication {
    public static void main(String[] args) {
        // 设置配置文件
        String profile = System.getProperty("spring.profiles.active");
        if (profile != null) {
            System.setProperty("spring.profiles.active", profile);
        }
        SpringApplication.run(AiQuizApplication.class, args);
    }
} 