package com.example.demo.thing;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
public class ThingResponse {

    @JsonProperty("data")
    private String response;

    @JsonAnySetter
    private Map<String, Object> stuff;
}
