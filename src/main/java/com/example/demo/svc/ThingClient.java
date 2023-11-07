package com.example.demo.svc;

import com.example.demo.svc.thing.Really;
import com.example.demo.svc.thing.ThingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ThingClient {
    private final WebClient httpbin;

    public ThingClient(@Qualifier("httpbinclient") WebClient httpbin) {
        this.httpbin = httpbin;
    }

    public Mono<ThingResponse> getThing(boolean emulateFailure) {
        ThingRequest body = ThingRequest.builder()
                .name("thing")
                .numbers(List.of(1, 2, 3, 4))
                .oreally(Really.YAH_REALLY)
                .build();

        String path;
        if (emulateFailure) {
            path = "/status/500";
        } else {
            path = "/anything";
        }
        return httpbin
                .method(HttpMethod.POST)
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ThingException(
                                clientResponse.statusCode().value(),
                                "bla"
                        ))
                )
                .bodyToMono(ThingResponse.class);
    }
}
