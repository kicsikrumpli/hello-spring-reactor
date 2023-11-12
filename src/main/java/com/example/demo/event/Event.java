package com.example.demo.event;

import com.example.demo.event.payload.Docs;
import com.example.demo.event.payload.Failure;
import com.example.demo.event.payload.Success;
import com.example.demo.event.payload.Thing;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Event<T extends Payload> {
    private String id;

    private String groupId;

    @JsonTypeId
    @JsonProperty("eventType")
    private EventType eventType;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            property = "eventType",
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(
                    value = Thing.class,
                    name = "THING_EVENT"
            ),
            @JsonSubTypes.Type(
                    value = Docs.class,
                    name = "ATTACH_EVENT"
            ),
            @JsonSubTypes.Type(
                    value = Success.class,
                    name = "SUCCESS_EVENT"
            ),
            @JsonSubTypes.Type(
                    value = Failure.class,
                    name = "FAILURE_EVENT"
            )
    })
    private T payload;
}
