package com.example.demo.attach;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachRequest {
    private String docName;
    private String thingName;
    private Boolean shouldFail;
}
