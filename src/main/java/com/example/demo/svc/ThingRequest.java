package com.example.demo.svc;

import com.example.demo.svc.thing.Really;
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
