package com.example.demo.svc.thing;

public enum Really {
    YAH_REALLY(true), NO(false);

    private final boolean value;

    Really(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
