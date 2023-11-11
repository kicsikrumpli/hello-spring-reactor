package com.example.demo.attach;

import reactor.core.publisher.Mono;

public class AttachClient {
    private final String url;

    public AttachClient() {
        this.url = "http://httpbin.org/";
    }

    public Mono<AttachResponse> attach(AttachRequest request) {
        return null;
    }
}
