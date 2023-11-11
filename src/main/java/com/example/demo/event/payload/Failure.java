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
public class Failure implements Payload {
    private String reason;
}
