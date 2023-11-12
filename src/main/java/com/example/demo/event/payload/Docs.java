package com.example.demo.event.payload;

import com.example.demo.event.Payload;
import com.example.demo.event.payload.docs.Doc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Docs implements Payload {
    private Set<Doc> docs;
}
