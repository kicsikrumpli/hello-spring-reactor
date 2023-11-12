package com.example.demo;

import com.example.demo.attach.AttachClient;
import com.example.demo.attach.AttachRequest;
import com.example.demo.attach.AttachRequestConverter;
import com.example.demo.attach.AttachResponse;
import com.example.demo.event.Event;
import com.example.demo.event.EventRepository;
import com.example.demo.event.EventType;
import com.example.demo.event.Payload;
import com.example.demo.event.payload.Docs;
import com.example.demo.event.payload.Failure;
import com.example.demo.event.payload.Success;
import com.example.demo.event.payload.Thing;
import com.example.demo.event.payload.docs.Doc;
import com.example.demo.thing.ThingClient;
import com.example.demo.thing.ThingRequestConverter;
import com.example.demo.thing.ThingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.demo.event.Predicates.hasEventType;
import static com.example.demo.event.Predicates.hasGroupId;

@SpringBootApplication
@Slf4j
public class DemoApplication implements CommandLineRunner {
    @Autowired
    private EventRepository<Payload> repo;

    @Autowired
    private ThingRequestConverter thingRequestConverter;
    @Autowired
    private ThingClient thingClient;

    @Autowired
    private AttachRequestConverter attachRequestConverter;
    @Autowired
    private AttachClient attachClient;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String groupId = "12345B";
        writeExample(groupId);

        // readExample();

        // get things from repo for group id, discard all but first
        var thingEvent = repo
                .findByPredicate(
                        hasEventType(EventType.THING_EVENT)
                                .and(hasGroupId(groupId))
                )
                .collectList()
                .map(thingResponses -> thingResponses.stream().findFirst().orElseThrow());

        // call event client with all the things, use only last one
        Mono<ThingResponse> thingEventResponse = thingEvent
                .doOnNext(e -> log.info("@@@ Thing Event: {}", e))
                .map(event -> thingRequestConverter.convert((Thing) event.getPayload()))
                .flatMap(thingRequest -> thingClient.doWithThing(thingRequest))
                .doOnNext(resp -> log.info("@@@ Thing Response: {}", resp));

        // get docs to attach from repo for group id
        Flux<Event<Payload>> attachEvents = repo.findByPredicate(
                hasEventType(EventType.ATTACH_EVENT).and(
                        hasGroupId(groupId))
        );

        // convert
        //   - 1 thing response
        //   - many attach __events__
        // into series of attach __requests__
        Flux<AttachRequest> attachRequests = thingEventResponse
                .flatMapMany(thingResponse -> attachEvents
                        .map(attachEvent -> (Docs) attachEvent.getPayload())
                        .flatMapIterable(Docs::getDocs)
                        .map(doc -> attachRequestConverter.convert(doc, thingResponse.getResponse()))
        );

        // call attach client, collect successful calls into a mono of set
        Mono<Set<AttachResponse>> attachResponses = attachRequests
                .doOnNext(attachRequest -> log.info("### Attach REQ: {}", attachRequest))
                .flatMap(attachRequest -> attachClient.doAttach(attachRequest))
                .doOnError(e -> log.warn("Attach failed, skipping", e))
                .onErrorResume(throwable -> Flux.empty())  // skip on failed request
                .doOnNext(attachResponse -> log.info("### Attach RESP: {}", attachResponse))
                .collectList()
                .map(Set::copyOf);

        // aggregate transaction result
        // map success to SUCCESS event
        // map error to FAILURE event
        //var txResult = Mono.zip(thingEventResponse, attachResponses) // can't zip again, has already been closed
        var txResult = thingEventResponse
                .map(thingResponse_attachResponse -> Event.builder()
                        .groupId(groupId)
                        .payload(Success.builder()
                                .name(thingResponse_attachResponse.getResponse())
                                .attachments(Set.of()
                                        /*
                                        thingResponse_attachResponse.getT2()
                                        .stream()
                                        .map(AttachResponse::getData)
                                        .collect(Collectors.toSet())
                                         */
                                )
                                .build())
                        .build())
                .onErrorResume(throwable -> Mono.just(Event.builder()
                        .groupId(groupId)
                        .payload(Failure.builder()
                                .reason(throwable.getMessage())
                                .build())
                        .build()));

        // save and log transaction result
        txResult
                .flatMap(repo::save)
                .doOnSuccess(tx -> log.info("TX result: {}", tx))
                .block();

        log.info("--- done ---");
    }

    private void writeExample(String groupId) throws JsonProcessingException {
        var thing = Event.builder()
                .groupId(groupId)
                .payload(Thing.builder()
                        .name("example name")
                        .prefix("X-")
                        .build())
                .build();

        var docs = Event.builder()
                .groupId(groupId)
                .payload(Docs.builder()
                        .docs(Set.of(
                                Doc.builder().name("doc a").build(),
                                Doc.builder().name("doc b!").build(),
                                Doc.builder().name("doc c").build()
                        ))
                        .build())
                .build();

        var saveThing = repo
                .save(thing)
                .doOnSuccess(x -> log.info("### Saved event: {}", x));

        var saveDocs = repo
                .save(docs)
                .doOnSuccess(x -> log.info("### Saved event: {}", x));

        saveThing
                .then(saveDocs)
                .doOnSuccess(x -> log.info("---done writing---"))
                .block();
    }

    private void readExample() {
        repo
                .findAll()
                .doOnNext(event -> log.info("*** Found Event: {}", event))
                .then(Mono.empty())
                .block();

        log.info("---done reading---");

    }

}
