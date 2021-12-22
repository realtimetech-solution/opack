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

    public void swap(int index1, int index2) {
        T temp = this.objects[index1];
        this.objects[index1] = this.objects[index2];
        this.objects[index2] = temp;
    }

    public void reverse(int start, int end) {
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