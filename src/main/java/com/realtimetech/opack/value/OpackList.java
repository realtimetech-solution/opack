package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class OpackList<E extends OpackValue> extends OpackLazyValue<LinkedList<E>> {
    @Override
    public void set(LinkedList<E> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    LinkedList<E> createLazyValue() {
        return new LinkedList<>();
    }

    public boolean add(@NotNull E value) {
        return this.get().add(value);
    }

    public boolean remove(@NotNull E value) {
        return this.get().remove(value);
    }
}
