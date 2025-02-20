package com.example.aiquiz.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QuizGenie API文档")
                        .version("1.0")
                        .description("QuizGenie API文档")
                        .contact(new Contact()
                                .name("QuizGenie")
                                .url("https://github.com/ai-question/QuizGenie")
                                .email("your-email@example.com")));
    }
} 