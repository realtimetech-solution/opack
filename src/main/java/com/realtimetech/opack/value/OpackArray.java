package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class OpackArray<E> extends OpackValue<ArrayList<E>> {
    public OpackArray(E @NotNull [] array) {
        this(Arrays.asList(array));
    }

    public OpackArray(@NotNull Collection<E> collection) {
        this(collection.size());

        this.get().addAll(collection);
    }

    public OpackArray(int length) {
        this.set(new ArrayList<>(length));
    }

    public OpackArray() {
    }

    @Override
    ArrayList<E> createLazyValue() {
        return new ArrayList<>();
    }

    public E set(int index, @NotNull E value) {
        return this.get().set(index, value);
    }

    public boolean add(@NotNull E value) {
        return this.get().add(value);
    }

    public boolean remove(@NotNull E value) {
        return this.get().remove(value);
    }

    public E get(int index){
        return this.get().get(index);
    }

    public int length() {
        return this.get().size();
    }
}
