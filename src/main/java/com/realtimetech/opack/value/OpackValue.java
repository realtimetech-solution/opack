package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpackValue<?> that = (OpackValue<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + value + "}";
    }
}