package com.example.demo.thing;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ThingClient {

    private final WebClient webClient;

    public ThingClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ThingResponse> doWithThing(ThingRequest thingRequest) {
        return webClient
                .method(HttpMethod.POST)
                .uri(thingRequest.getShouldFail() ? "/status/500" : "/delay/1")
                .bodyValue(thingRequest.getRequestContent())
                .retrieve()
                .bodyToMono(ThingResponse.class);
    }
}
