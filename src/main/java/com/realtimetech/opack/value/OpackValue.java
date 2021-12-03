package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class OpackValue<T> {
    private T value;

    public OpackValue(@NotNull T value){
        this.value = value;
    }

    public void set(@NotNull T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }
}