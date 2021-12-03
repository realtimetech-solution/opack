package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.HashMap;

public class OpackArray<T extends OpackValue> extends OpackValue<T[]> {
    public OpackArray(@NotNull Class<T> type, int length) {
        super((T[]) Array.newInstance(type, length));
    }

    public OpackArray(T @NotNull [] value) {
        super(value);
    }

    @Override
    public void set(T[] value) {
        throw new UnsupportedOperationException();
    }

    public void set(int index, T value) {
        super.get()[index] = value;
    }

    public T get(int index) {
        return super.get()[index];
    }
}
