package com.example.demo;

import com.example.demo.svc.ThingClient;
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
	private ThingClient thingClient;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Mono<Void> done = thingClient
				.getThing(false)
				.doOnNext(thing -> log.info("successfully received a thing: {}", thing))
				.doOnError(err -> log.error("log error without throwing up: {}", err.getMessage()))
				//.onErrorComplete() // put this at the end to skip rest on error
				.doOnNext(resp -> log.info("log only for 2xx response: Blarg? {}!", resp))
				.then(Mono.just("blargh"))
				.thenEmpty(Mono.empty());

		done
				.onErrorComplete()
				.doOnSuccess(unused -> log.info("===done.doOnSuccess==="))
				.block();

		log.info("---done---");
	}

}
