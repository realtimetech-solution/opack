/*
 * Copyright (C) 2021 REALTIMETECH All Rights Reserved
 *
 * Licensed either under the Apache License, Version 2.0, or (at your option)
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation (subject to the "Classpath" exception),
 * either version 2, or any later version (collectively, the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     http://www.gnu.org/licenses/
 *     http://www.gnu.org/software/classpath/license.html
 *
 * or as provided in the LICENSE file that accompanied this code.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.realtimetech.opack.util.structure;

import org.jetbrains.annotations.NotNull;

import java.util.EmptyStackException;

public class FastStack<T> {
    private final int blockSize;

    private T[] objects;

    private int currentIndex;
    private int currentSize;

    private int scope;

    private int startIndex;

    /**
     * Calls {@code new StringWriter(1024)}
     */
    public FastStack() {
        this(10);
    }

    /**
     * Constructs a FastStack with block size.
     *
     * @param blockSize the block size
     */
    public FastStack(int blockSize) {
        this.blockSize = blockSize;
        this.currentIndex = -1;
        this.startIndex = 0;
        this.scope = 0;

        this.growArray();
    }

    /**
     * Increase the stack size by the block size.
     */
    private void growArray() {
        this.scope++;

        T[] oldObjects = this.objects;

        this.currentSize = this.scope * this.blockSize;
        this.objects = (T[]) new Object[this.currentSize];

        if (oldObjects != null) {
            System.arraycopy(oldObjects, 0, objects, 0, currentIndex + 1);
        }
    }

    /**
     * @param object the element to push
     * @return pushed element
     */
    public T push(T object) {
        if (this.currentIndex + 2 >= this.currentSize) {
            growArray();
        }

        this.currentIndex++;
        this.objects[this.currentIndex] = object;

        return object;
    }

    /**
     * @return element array
     */
    public @NotNull T @NotNull [] getArray() {
        return objects;
    }

    /**
     * @return true if this stack is empty
     */
    public boolean isEmpty() {
        return this.currentIndex == -1;
    }

    /**
     * Returns the element at the specified position in this Stack.
     *
     * @param index the index of the element to return
     * @return the found element
     */
    public @NotNull T get(int index) {
        if (this.currentIndex == -1)
            throw new EmptyStackException();

        return this.objects[index];
    }

    /**
     * Swap position of A element and B element.
     *
     * @param index1 the index of the element to swap
     * @param index2 the index of the element to swap
     */
    public void swap(int index1, int index2) {
        T temp = this.objects[index1];
        this.objects[index1] = this.objects[index2];
        this.objects[index2] = temp;
    }

    /**
     * Reverse the position of elements in a specific range.
     *
     * @param start the start index of the range
     * @param end   the end index of the range
     */
    public void reverse(int start, int end) {
        int length = (end - start + 1) / 2;
        for (int index = 0; index < length; index++) {
            T temp = this.objects[start + index];
            this.objects[start + index] = this.objects[end - index];
            this.objects[end - index] = temp;
        }
    }

    /**
     * Returns the element at the top of this stack without removing it from the stack.
     *
     * @return the element at the top of this stack
     */
    public @NotNull T peek() {
        if (this.currentIndex == -1)
            throw new EmptyStackException();

        return this.objects[this.currentIndex];
    }

    /**
     * Returns the element at the top of this stack and removes it.
     *
     * @return The object at the top of this stack
     */
    public T pop() {
        if (this.currentIndex == -1)
            throw new EmptyStackException();

        T object = this.objects[this.currentIndex];

        this.objects[this.currentIndex] = null;
        this.currentIndex--;

        return object;
    }

    /**
     * Shifts this stack to right.
     *
     * @return the element at the bottom before shifting
     */
    public T shift() {
        if (this.currentIndex == -1)
            throw new EmptyStackException();

        return this.objects[this.startIndex++];
    }

    /**
     * Reset this stack.
     */
    public void reset() {
        this.currentIndex = -1;
        this.startIndex = 0;
    }

    /**
     * Returns the number of elements in this stack.
     *
     * @return the number of elements
     */
    public int getSize() {
        return this.currentIndex + 1;
    }
}