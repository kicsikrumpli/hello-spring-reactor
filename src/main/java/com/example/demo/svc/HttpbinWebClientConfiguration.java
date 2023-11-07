package com.example.demo.svc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpbinWebClientConfiguration {

    @Bean("httpbinclient")
    public WebClient buildHttpWebClient() {
        return WebClient.builder()
                .baseUrl("http://httpbin.org")
                .build();
    }
}
