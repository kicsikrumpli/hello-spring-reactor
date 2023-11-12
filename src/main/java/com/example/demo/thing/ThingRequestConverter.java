package com.example.demo.thing;

import com.example.demo.event.payload.Thing;
import org.springframework.stereotype.Component;

@Component
public class ThingRequestConverter {
    public ThingRequest convert(Thing thing) {
        return ThingRequest.builder()
                .requestContent(String.join("", thing.getPrefix(), thing.getName()))
                .shouldFail("!".equals(thing.getPrefix()))
                .build();
    }
}
