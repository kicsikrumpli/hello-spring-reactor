package com.example.demo.attach;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class AttachResponse {
    @JsonProperty("data")
    private String data;

    @JsonAnySetter
    private Map<String, Object> stuff;
}
