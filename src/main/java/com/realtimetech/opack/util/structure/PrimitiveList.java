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

import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;

public class PrimitiveList implements List<Object> {
    final Object arrayObject;

    public PrimitiveList(Object arrayObject) {
        if (!arrayObject.getClass().isArray()) {
            throw new IllegalArgumentException(arrayObject + " is not array object.");
        }

        this.arrayObject = arrayObject;
    }

    public Object getArrayObject() {
        return arrayObject;
    }

    @Override
    public int size() {
        return Array.getLength(this.arrayObject);
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.indexOf(o) != -1;
    }

    @Override
    public @NotNull Iterator<Object> iterator() {
        return Arrays.asList(toArray()).iterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        Object[] objects = new Object[this.size()];

        for (int i = 0; i < objects.length; i++) {
            objects[i] = this.get(i);
        }

        return objects;
    }

    @Override
    public <T> T @NotNull [] toArray(T @NotNull [] array) {
        T[] arrayObject = (T[]) Array.newInstance(array.getClass().getComponentType(), this.size());

        for (int i = 0; i < array.length; i++) {
            arrayObject[i] = (T) this.get(i);
        }

        return arrayObject;
    }

    @Override
    public boolean add(Object e) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Object> c) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends Object> c) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public Object get(int index) {
        return ReflectionUtil.getArrayItem(this.arrayObject, index);
    }

    @Override
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public Object remove(int index) {
        throw new UnsupportedOperationException("PrimitiveList can't modify element.");
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).equals(o)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = this.size() - 1; i >= 0; i--) {
            if (this.get(i).equals(o)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (!(object instanceof List)) {
            return false;
        }

        List list = (List) object;

        if (list.size() != this.size()){
            return false;
        }

        for (int i = 0; i < this.size(); i++) {
            Object element = this.get(i);
            Object target = list.get(i);

            if (element == null || target == null) {
                if (element != target) {
                    return false;
                }
            }

            if (!element.equals(target)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < size(); i++) {
            Object element = this.get(i);
            hashCode = 31 * hashCode + (element == null ? 0 : element.hashCode());
        }
        return hashCode;
    }

    @NotNull
    @Override
    public ListIterator<Object> listIterator() {
        return Arrays.asList(toArray()).listIterator();
    }

    @NotNull
    @Override
    public ListIterator<Object> listIterator(int index) {
        return Arrays.asList(toArray()).listIterator(index);
    }

    @NotNull
    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return Arrays.asList(toArray()).subList(fromIndex, toIndex);
    }
}
