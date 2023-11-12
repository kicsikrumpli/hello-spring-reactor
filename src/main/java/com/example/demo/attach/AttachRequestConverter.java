package com.example.demo.attach;

import com.example.demo.event.payload.docs.Doc;
import org.springframework.stereotype.Component;

@Component
public class AttachRequestConverter {
    public AttachRequest convert(Doc doc, String thingName) {
        return AttachRequest.builder()
                .docName(doc.getName())
                .thingName(thingName)
                .build();
    }
}
