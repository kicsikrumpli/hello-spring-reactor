package com.example.demo.svc.thing;

import com.example.demo.svc.ThingResponse;

public class ThingException extends Exception{
    public ThingException(Integer status, String body) {
        super(String.format("Thing Failed with status %d, body %s", status, body));
    }
}
