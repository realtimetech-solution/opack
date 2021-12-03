package com.realtimetech.opack.value;

import java.util.HashMap;

public abstract class OpackValue<T> {
    private T value;

    public OpackValue(T value){
        this.value = value;
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }
}