package com.example.demo.event;

import java.util.function.Predicate;

public class Predicates {
    public static Predicate<Event<Payload>> hasGroupId(String groupId) {
        return event -> groupId.equals(event.getGroupId());
    }

    public static Predicate<Event<Payload>> hasEventType(EventType eventType) {
        return event -> eventType.equals(event.getEventType());
    }
}
