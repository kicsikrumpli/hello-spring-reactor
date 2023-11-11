package com.example.demo;

import com.example.demo.event.Event;
import com.example.demo.event.EventRepository;
import com.example.demo.event.Payload;
import com.example.demo.event.payload.Thing;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Slf4j
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private EventRepository<Payload> repo;

    @Autowired
    private ObjectMapper om;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        var exampleEvent = Event.builder()
                .groupId("123")
                .payload(Thing.builder()
                        .name("example name")
                        .prefix("X-")
                        .build())
                .build();

        log.info("exampleEvent: {}", om.writeValueAsString(exampleEvent));

        repo
                .save(exampleEvent)
                .doOnSuccess(x -> log.info("### Saved event: {}", x))
                .block();

        log.info("---done writing---");

        repo
                .findAll()
                .doOnNext(event -> log.info("*** Found Event: {}", event))
                .then(Mono.empty())
                .block();

        log.info("---done reading---");
    }

}
