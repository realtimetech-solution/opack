package com.realtimetech.opack.util.structure;

import org.jetbrains.annotations.NotNull;

import java.util.EmptyStackException;

public class FastStack<T> {
    private int blockSize;

    private T[] objects;

    private int currentIndex;
    private int currentSize;

    private int scope;

    private int startIndex;

    public FastStack() {
        this(10);
    }

    public FastStack(int blockSize) {
        this.blockSize = blockSize;
        this.currentIndex = -1;
        this.startIndex = 0;
        this.scope = 0;

        this.growArray();
    }

    private void growArray() {
        this.scope++;

        T[] oldObjects = this.objects;

        this.currentSize = this.scope * this.blockSize;
        this.objects = (T[]) new Object[this.currentSize];

        if (oldObjects != null) {
            for (int i = 0; i <= this.currentIndex; i++) {
                this.objects[i] = oldObjects[i];
            }
        }
    }

    public @NotNull T push(@NotNull T object) {
        if (this.currentIndex + 2 >= this.currentSize) {
            growArray();
        }

        this.currentIndex++;
        this.objects[this.currentIndex] = object;

        return object;
    }

    public @NotNull T @NotNull [] getArray() {
        return objects;
    }

    public boolean isEmpty() {
        return this.currentIndex == -1;
    }

    public @NotNull T get(int index) {
        if (this.currentIndex == -1)
            throw new EmptyStackException();

        return this.objects[index];
    }

    public @NotNull void swap(int index1, int index2) {
        T temp = this.objects[index1];
        this.objects[index1] = this.objects[index2];
        this.objects[index2] = temp;
    }

    public @NotNull void reverse(int start, int end) {
        int length = (end - start + 1) / 2;
        for (int index = 0; index < length; index++) {
            T temp = this.objects[start + index];
            this.objects[start + index] = this.objects[end - index];
            this.objects[end - index] = temp;
        }
    }

    public @NotNull T peek() {
        if (this.currentIndex == -1)
            throw new EmptyStackException();

        return this.objects[this.currentIndex];
    }

    public @NotNull T pop() {
        if (this.currentIndex == -1)
            throw new EmptyStackException();

        T object = this.objects[this.currentIndex];

        this.objects[this.currentIndex] = null;
        this.currentIndex--;

        return object;
    }

    public T shift() {
        if (this.currentIndex == -1)
            throw new EmptyStackException();

        return this.objects[this.startIndex++];
    }

    public void reset() {
        this.currentIndex = -1;
        this.startIndex = 0;
    }

    public int getSize() {
        return this.currentIndex + 1;
    }
}