package com.example.demo.repo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event<T extends Payload> {
    private long timestamp;

    @JsonTypeId
    @JsonProperty("eventType")
    private String eventType;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "eventType"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(
                    value = Success.class
            ),
            @JsonSubTypes.Type(
                    value = Failure.class
            )
    })
    private T payload;
}
