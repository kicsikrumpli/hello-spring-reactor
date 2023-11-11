package com.example.demo.thing;

public class ThingException extends Exception{
    public ThingException(Integer status, String body) {
        super(String.format("Thing Failed with status %d, body %s", status, body));
    }
}
