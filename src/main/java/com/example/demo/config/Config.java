package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Config {

    @Bean
    public WebClient configure() {
        return WebClient.builder()
                .baseUrl("https://httpbin.org/")
                .build();
    }
}
