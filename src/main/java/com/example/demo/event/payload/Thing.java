package com.example.demo.event.payload;

import com.example.demo.event.Payload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Thing implements Payload {
    private String name;
    private String prefix;
}
