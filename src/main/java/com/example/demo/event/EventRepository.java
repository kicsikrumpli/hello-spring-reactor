package com.example.demo.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@Slf4j
public class EventRepository<T extends Payload> {
    private final String basePath;

    private final ObjectMapper objectMapper;

    public EventRepository(ObjectMapper objectMapper) {
        this.basePath = new ClassPathResource("events").getPath();
        this.objectMapper = objectMapper;
    }

    public Flux<Event<T>> findAll() {
        return Flux
                .fromStream(getContents(basePath))
                .filter(Files::isRegularFile)
                .doOnNext(path -> log.info("*** Found file: {}", path))
                .flatMap(path -> DataBufferUtils.read(
                                path,
                                DefaultDataBufferFactory.sharedInstance,
                                512
                        )
                )
                .map(this::readJsonFromBuffer);
    }

    public Flux<Event<T>> findByPredicate(Predicate<? super Event<T>> predicate) {
        return findAll().
                filter(predicate);
    }

    @SneakyThrows
    private Stream<Path> getContents(String directory) {
        return Files.walk(Paths.get(directory));
    }

    public Mono<Event<T>> save(Event<T> event) {
        String id = UUID.randomUUID().toString();
        Path savePath = Paths.get(basePath, id + ".json");
        var eventToSave = event.toBuilder()
                .id(id)
                .build();

        Flux<DataBuffer> buffer = Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(
                writeJsonToBuffer(eventToSave)
        ));

        Flux<Event<T>> saveResult = DataBufferUtils.write(buffer, savePath, StandardOpenOption.CREATE)
                .doOnSuccess(x -> log.info("+++ Written file: {}", savePath))
                .thenMany(buffer)
                .map(this::readJsonFromBuffer);

        return Mono.from(saveResult);
    }

    @SneakyThrows
    private byte[] writeJsonToBuffer(Event<T> eventToSave) {
        return objectMapper.writeValueAsBytes(eventToSave);
    }

    @SneakyThrows
    private Event<T> readJsonFromBuffer(DataBuffer buff) {
        return objectMapper.readValue(buff.asInputStream(), new TypeReference<Event<T>>() {
        });
    }


}
