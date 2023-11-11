package com.example.demo.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component
@Slf4j
public class EventRepository<T extends Payload> {
    @Autowired
    private ObjectMapper om;

    public Mono<Event<Payload>> save(T payload) {
        Mono<Event<Payload>> event = Mono.just(Event.builder()
                .timestamp(System.currentTimeMillis())
                .payload(payload)
                .build());


        return event;
    }
}
