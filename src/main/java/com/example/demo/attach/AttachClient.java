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

    public Mono<AttachResponse> doAttach(AttachRequest request) {
        String requestBody = String.join(":", request.getThingName(), request.getDocName());

        return webClient
                .method(HttpMethod.POST)
                .uri(request.getShouldFail() ? "/status/418" : "/delay/2")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AttachResponse.class);
    }
}
