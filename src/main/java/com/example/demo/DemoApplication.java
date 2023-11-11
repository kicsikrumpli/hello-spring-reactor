package com.example.demo;

import com.example.demo.repo.Event;
import com.example.demo.repo.EventRepository;
import com.example.demo.repo.Payload;
import com.example.demo.repo.Success;
import com.example.demo.thing.ThingClient;
import com.example.demo.thing.ThingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private ThingClient thingClient;

    @Autowired
    private EventRepository<Payload> repo;

    @Autowired
    private ObjectMapper om;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Mono<ThingResponse> thingResponse = thingClient
                .getThing("first thing", false)
                .doOnNext(thing -> log.info("successfully received a thing: {}", thing))
                .doOnError(err -> log.error("log error without throwing up: {}", err.getMessage()))
                //.onErrorComplete() // put this at the end to skip rest on error
                .doOnNext(resp -> log.info("log only for 2xx response: Blarg? {}!", resp));

        Mono<Payload> success = thingResponse
                .flatMap(resp -> Mono.just(Success.builder()
                        .name(resp.getPayload().getName())
                        .sum(resp.getPayload()
                                .getNumbers()
                                .stream()
                                .collect(Collectors.summingInt(Integer::intValue)))
                        .build())
                );

        Mono<Event<Payload>> repositoryResponse = success.flatMap(succ -> repo.save(succ));

        repositoryResponse
                        .onErrorComplete()
                        .block();

        log.info("---done---");
    }

}
