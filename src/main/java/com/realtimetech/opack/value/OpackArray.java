package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;

public class OpackArray<E extends OpackValue> extends OpackLazyValue<LinkedList<E>> {
    public OpackArray() {
        super();
    }

    public OpackArray(@NotNull Collection<E> collection) {
        this.get().addAll(collection);
    }

    public OpackArray(E @NotNull [] collection) {
        for (E element : collection) {
            this.add(element);
        }
    }

    @Override
    public void set(LinkedList<E> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    LinkedList<E> createLazyValue() {
        return new LinkedList<>();
    }

    public E set(int index, @NotNull E value) {
        return this.get().set(index,value);
    }

    public boolean add(@NotNull E value) {
        return this.get().add(value);
    }

    public boolean remove(@NotNull E value) {
        return this.get().remove(value);
    }
}
