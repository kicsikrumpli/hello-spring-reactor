package com.example.demo.attach;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AttachClient {

    private final WebClient webClient;

    public AttachClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<AttachResponse> attach(AttachRequest request) {
        return webClient
                .method(HttpMethod.POST)
                .uri("/delay/2")
                .bodyValue(String.join(":", request.getDocName(), request.getDocName()))
                .retrieve()
                .bodyToMono(AttachResponse.class);
    }
}
