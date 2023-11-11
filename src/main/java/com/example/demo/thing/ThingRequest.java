package com.example.demo.thing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThingRequest {
    private String name;
    private List<Integer> numbers;
    private Really oreally;
}
