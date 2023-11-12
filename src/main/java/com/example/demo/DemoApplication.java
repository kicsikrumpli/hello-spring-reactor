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

    /**
     * Reactive Example.
     *
     * - uses reactive repo: read - write json files for each document
     * - reactive clients:
     *      - thing client: make request with one thing
     *      - attach client: make one request per doc
     * - source of things, docs: existing events in repo
     * - processing flow:
     *      - make thing request
     *          - from thing event payload
     *          - if fails: skip attach requests
     *          - if succeeds: continue
     *      - make attach request
     *          - from successful thing response and
     *          - from payload of attach event: docs
     *          - many requests, one per doc
     *          - failing attach requests are skipped, ignore error
     *      - combine thing response, attach response
     *          - in case of failure write fail event into repo
     *          - in case of success write success event into repo
     * Usage:
     * - To make a thing request fail: use "!" as prefix (see writeExample())
     * - To make a doc attach fail: suffix doc name with "!" (see writeExample())
     * - define groupId
     */
    @Override
    public void run(String... args) throws Exception {
        String groupId = "12345B";

        // writeExample(groupId);

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
                .doOnNext(resp -> log.info("@@@ Thing Response: {}", resp))
                .cache();  // use the same values in two separate subscriptions

        // get docs to attach from repo for group id
        Flux<Event<Payload>> attachEvents = repo.findByPredicate(
                hasEventType(EventType.ATTACH_EVENT).and(
                        hasGroupId(groupId))
        );

        // convert (1 thing_response, n attach_event) -> n attach_request
        Flux<AttachRequest> attachRequests = thingEventResponse
                .flatMapMany(thingResponse -> attachEvents
                        .map(attachEvent -> (Docs) attachEvent.getPayload())
                        .flatMapIterable(Docs::getDocs)
                        .map(doc -> attachRequestConverter.convert(doc, thingResponse.getResponse()))
                );

        // call attach client, collect successful calls into a mono of set
        Mono<Set<AttachResponse>> attachResponses = attachRequests
                .doOnNext(attachRequest -> log.info("### Attach REQ: {}", attachRequest))
                .flatMap(attachRequest -> attachClient
                        .doAttach(attachRequest)
                        .doOnError(e -> log.warn("Attach failed, skipping: {}", e.getMessage()))
                        .onErrorResume(throwable -> Mono.empty())  // skip on failed request
                )
                .doOnNext(attachResponse -> log.info("### Attach RESP: {}", attachResponse))
                .collectList()
                .map(Set::copyOf);

        // aggregate transaction result
        var txResult = Mono.zip(thingEventResponse, attachResponses) // needs .cached(), 2nd subscription
                .map(thingResponse_attachResponse -> Event.builder()
                        .groupId(groupId)
                        .payload(Success.builder()
                                .name(thingResponse_attachResponse.getT1().getResponse())
                                .attachments(thingResponse_attachResponse.getT2()
                                        .stream()
                                        .map(AttachResponse::getData)
                                        .collect(Collectors.toSet())
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
