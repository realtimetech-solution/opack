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

import com.realtimetech.opack.util.structure.NativeList;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class OpackArray<E> extends AbstractOpackValue<List<E>> {
    /**
     * Returns whether component type for specific class of array object is primitive type.
     *
     * @param arrayType the class of array object
     * @return true if component type for the class of array object is primitive class
     */
    public static boolean isAllowArray(Class<?> arrayType) {
        if (arrayType.isArray()) {
            Class<?> componentType = ReflectionUtil.getArrayLastComponentType(arrayType);

            return ReflectionUtil.isPrimitiveType(componentType) || ReflectionUtil.isWrapperType(componentType);
        }

        return false;
    }

    /**
     * Create the opack array through array object of which component type is the primitive type.
     *
     * @param arrayObject the array object for create
     * @return created opack array
     * @throws IllegalArgumentException if the component type for array object is not primitive type; if the array object is not 1 dimension
     */
    public static OpackArray<?> createWithArrayObject(@NotNull Object arrayObject) {
        return new OpackArray<>(arrayObject);
    }

    /**
     * Constructs an opack array with the specified array object of which component type is the primitive type.
     *
     * @param arrayObject the array object for create
     * @throws IllegalArgumentException if the component type for array object is not primitive type; if the array object is not 1 dimension
     */
    OpackArray(@NotNull Object arrayObject) {
        if (!arrayObject.getClass().isArray()) {
            throw new IllegalArgumentException(arrayObject + " is not array object");
        }

        if (ReflectionUtil.getArrayDimension(arrayObject.getClass()) != 1) {
            throw new IllegalArgumentException(arrayObject + " must have 1 dimension");
        }

        if (!OpackArray.isAllowArray(arrayObject.getClass())) {
            throw new IllegalArgumentException(arrayObject + " array element is not allowed type, allow only primitive type or String or OpackValues or null");
        }

        this.set((List<E>) new NativeList(arrayObject));
    }

    /**
     * Constructs an opack array with the specified array.
     *
     * @param array the array for create
     */
    public OpackArray(E @NotNull [] array) {
        this.set(Arrays.asList(array));
    }

    /**
     * Constructs an opack array with the specified collection object.
     *
     * @param collection the collection for create
     * @throws IllegalArgumentException if type of the element in collection is not allowed in opack value
     */
    public OpackArray(@NotNull Collection<E> collection) {
        this(collection.size());

        List<E> list = this.get();

        for (E element : collection) {
            if (element != null)
                OpackValue.assertAllowType(element.getClass());

            list.add(element);
        }
    }

    /**
     * Constructs an empty opack array with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public OpackArray(int initialCapacity) {
        this.set(new ArrayList<>(initialCapacity));
    }

    /**
     * Constructs an empty opack array without underlying list.
     */
    public OpackArray() {
    }

    /**
     * Create and return the underlying list of this opack array.
     * This method will be called if {@link AbstractOpackValue#get() get()} method is called, when this opack array does not have an underlying list.
     *
     * @return underlying list
     */
    @Override
    ArrayList<E> createLazyValue() {
        return new ArrayList<>();
    }

    /**
     * If the underlying list is {@link NativeList NativeList}, it is converted to List.
     */
    void unpinList() {
        List<E> list = this.get();

        if (list instanceof NativeList) {
            this.set((List<E>) Arrays.asList(list.toArray()));
        }
    }

    /**
     * Replaces the value at the specified position in this opack array with the specified value.
     *
     * @param index index of the value to replace
     * @param value value to be stored at the specified position
     * @return the value previously at the specified position
     * @throws IllegalArgumentException if type of the value is not allowed in opack value
     */
    public E set(int index, E value) {
        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        this.unpinList();

        return this.get().set(index, value);
    }

    /**
     * Appends the specified value to the end of this opack array.
     *
     * @param value the value to be appended to this list
     * @return true if this opack array changed as a result of the call
     * @throws IllegalArgumentException if type of the value is not allowed in opack value
     */
    public boolean add(E value) {
        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        this.unpinList();

        return this.get().add(value);
    }

    /**
     * Removes the first occurrence of the specified value from this opack array, if it is present.
     *
     * @param value the value to be removed from this opack array, if present
     * @return true if this opack array contained the specified value
     */
    public boolean remove(@NotNull E value) {
        return this.get().remove(value);
    }

    /**
     * Returns the value at the specified position in this opack array.
     *
     * @param index index of the value to replace
     * @return the value previously at the specified position
     */
    public E get(int index) {
        return this.get().get(index);
    }

    /**
     * Returns the number of elements in this opack array.
     *
     * @return the number of elements in this opack array
     */
    public int length() {
        return this.get().size();
    }

    /**
     * Returns a string representation of the {@link List List} that is the underlying of the opack array.
     *
     * @param value the underlying object of the opack array
     * @return a string representation of the List
     */
    @Override
    String toString(List<E> value) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('[');

        boolean first = true;
        for (E element : value) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(',');
            }

            stringBuilder.append(element == null ? "null" : element.toString());
        }

        stringBuilder.append(']');

        return stringBuilder.toString();
    }

    /**
     * Returns a deep copy of this opack array instance.
     *
     * @return a deep copy of this opack array instance
     */
    @Override
    public OpackArray<E> clone() {
        OpackArray<E> opackArray = new OpackArray<>(this.length());

        for (int index = 0; index < this.length(); index++) {
            E object = this.get(index);

            if (object instanceof OpackValue) {
                object = (E) ((OpackValue) object).clone();
            }

            opackArray.add(object);
        }

        return opackArray;
    }

    /**
     * Returns true if a specific object is the same as this opack array.
     *
     * @param object the reference object with which to compare
     * @return true if a specific object is the same as this opack array
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        OpackArray<?> opackArray = (OpackArray<?>) object;

        return opackArray.get().equals(this.get());
    }

    /**
     * Returns the hash code of this opack array.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return this.get().hashCode();
    }
}
