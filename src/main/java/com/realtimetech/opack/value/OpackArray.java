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

package com.realtimetech.opack.value;

import com.realtimetech.opack.util.structure.PrimitiveList;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class OpackArray<E> extends OpackValue<List<E>> {
    public static boolean isAllowArrayType(Class<?> typeClass) {
        if (typeClass.isArray()) {
            Class<?> componentType = ReflectionUtil.getArrayLastComponentType(typeClass);

            return ReflectionUtil.isPrimitiveClass(componentType);
        }

        return false;
    }

    public static OpackArray<?> createWithArrayObject(@NotNull Object arrayObject){
        return new OpackArray<>(arrayObject);
    }

    OpackArray(@NotNull Object arrayObject) {
        if (!arrayObject.getClass().isArray()) {
            throw new IllegalArgumentException(arrayObject + " is not array object");
        }

        if (ReflectionUtil.getArrayDimension(arrayObject.getClass()) != 1) {
            throw new IllegalArgumentException(arrayObject + " must have 1 dimension");
        }

        if (!OpackArray.isAllowArrayType(arrayObject.getClass())) {
            throw new IllegalArgumentException(arrayObject + " array element is not allowed type, allow only primitive type or String or OpackValues or null");
        }

        this.set((List<E>) new PrimitiveList(arrayObject));
    }

    public OpackArray(E @NotNull [] array) {
        this.set(Arrays.asList(array));
    }

    public OpackArray(@NotNull Collection<E> collection) {
        this(collection.size());

        List<E> list = this.get();

        for (E element : collection) {
            if (element != null)
                OpackValue.assertAllowType(element.getClass());

            list.add(element);
        }
    }

    public OpackArray(int initialCapacity) {
        this.set(new ArrayList<>(initialCapacity));
    }

    public OpackArray() {
    }

    @Override
    ArrayList<E> createLazyValue() {
        return new ArrayList<>();
    }

    void unpinList() {
        List<E> list = this.get();

        if (list instanceof PrimitiveList) {
            this.set((List<E>) Arrays.asList(list.toArray()));
        }
    }

    public E set(int index, E value) {
        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        this.unpinList();

        return this.get().set(index, value);
    }

    public boolean add(E value) {
        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        this.unpinList();

        return this.get().add(value);
    }

    public boolean remove(@NotNull E value) {
        return this.get().remove(value);
    }

    public E get(int index) {
        return this.get().get(index);
    }

    public int length() {
        return this.get().size();
    }

    @Override
    String toString(List<E> value) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('[');

        boolean first = true;
        for (E element : value) {
            if (first){
                first = false;
            }else{
                stringBuilder.append(',');
            }

            stringBuilder.append(element == null ? "null" : element.toString());
        }

        stringBuilder.append(']');

        return stringBuilder.toString();
    }

    @Override
    public OpackArray<E> clone() {
        OpackArray<E> opackArray = new OpackArray<>(this.length());

        for (int index = 0; index < this.length(); index++) {
            E object = this.get(index);

            if (object instanceof OpackValue) {
                object = (E) ((OpackValue<?>) object).clone();
            }

            opackArray.add(object);
        }

        return opackArray;
    }
}
