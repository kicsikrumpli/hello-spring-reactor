package com.example.demo.event.payload;

import com.example.demo.event.Payload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Success implements Payload {
    private String name;
    private Set<String> attachments;
}
