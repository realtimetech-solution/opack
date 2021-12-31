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

public class NativeList implements List<Object> {
    final Object arrayObject;

    /**
     * Constructs a NativeList with array object.
     *
     * @param arrayObject the array object for create
     * @throws IllegalArgumentException if this object does not represent an array class
     */
    public NativeList(Object arrayObject) {
        if (!arrayObject.getClass().isArray()) {
            throw new IllegalArgumentException(arrayObject + " is not array object.");
        }

        this.arrayObject = arrayObject;
    }

    /**
     * @return the underlying array object of this list.
     */
    public Object getArrayObject() {
        return arrayObject;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements
     */
    @Override
    public int size() {
        return Array.getLength(this.arrayObject);
    }

    /**
     * @return true if this list is empty
     */
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Returns true if this list contains the specified element.
     *
     * @param o element whose presence in this list is to be tested
     * @return true if this list contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        return this.indexOf(o) != -1;
    }

    /**
     * @return an iterator over the elements in this list in proper sequence.
     */
    @Override
    public @NotNull Iterator<Object> iterator() {
        return Arrays.asList(toArray()).iterator();
    }

    /**
     * Returns an array containing all the elements in this list in proper sequence.
     *
     * @return all elements of this list
     */
    @Override
    public Object @NotNull [] toArray() {
        Object[] objects = new Object[this.size()];

        for (int i = 0; i < objects.length; i++) {
            objects[i] = this.get(i);
        }

        return objects;
    }

    /**
     * Returns an array containing all the elements in this list in proper sequence (from first to last element).
     * The runtime type of the returned array is that of the specified array.
     *
     * @param array the array to get type
     * @return all elements of this list
     */
    @Override
    public <T> T @NotNull [] toArray(T @NotNull [] array) {
        T[] arrayObject = (T[]) Array.newInstance(array.getClass().getComponentType(), this.size());

        for (int i = 0; i < array.length; i++) {
            arrayObject[i] = (T) this.get(i);
        }

        return arrayObject;
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public boolean add(Object e) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * Returns true if this list contains all the elements of the specified collection.
     *
     * @param c collection to be checked for containment in this list
     * @return true if this list contains all the elements of the specified collection
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }

        return true;
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends Object> c) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public boolean addAll(int index, @NotNull Collection<? extends Object> c) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public Object get(int index) {
        return ReflectionUtil.getArrayItem(this.arrayObject, index);
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public void add(int index, Object element) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * This method is not supported in this list.
     *
     * @throws UnsupportedOperationException if this method is called
     */
    @Override
    public Object remove(int index) {
        throw new UnsupportedOperationException("NativeList can't modify element.");
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index i such that Objects.equals(o, get(i)), or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in this list, or -1 if this list does not contain the element
     */
    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).equals(o)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index i such that Objects.equals(o, get(i)), or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in this list, or -1 if this list does not contain the element
     */
    @Override
    public int lastIndexOf(Object o) {
        for (int i = this.size() - 1; i >= 0; i--) {
            if (this.get(i).equals(o)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns true if a specific object is the same as this list.
     *
     * @param object the reference object with which to compare
     * @return true if a specific object is the same as this list
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        if (!(object instanceof List)) {
            return false;
        }

        List list = (List) object;

        if (list.size() != this.size()) {
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

    /**
     * Returns the hash code of this list.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < size(); i++) {
            Object element = this.get(i);
            hashCode = 31 * hashCode + (element == null ? 0 : element.hashCode());
        }
        return hashCode;
    }

    /**
     * @return a list iterator over the elements in this list (in proper sequence)
     */
    @NotNull
    @Override
    public ListIterator<Object> listIterator() {
        return Arrays.asList(toArray()).listIterator();
    }

    /**
     * Returns a list iterator over the elements in this list (in proper sequence), starting at the specified position in the list.
     * The specified index indicates the first element that would be returned by an initial call to {@link ListIterator#next() next}.
     * An initial call to {@link ListIterator#previous previous}previous would return the element with the specified index minus one.
     *
     * @param index index of the first element to be returned from the list iterator (by a call to {@link ListIterator#next next})
     * @return a list iterator over the elements in this list (in proper sequence), starting at the specified position in the list
     */
    @NotNull
    @Override
    public ListIterator<Object> listIterator(int index) {
        return Arrays.asList(toArray()).listIterator(index);
    }

    /**
     * Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, exclusive. (If fromIndex and toIndex are equal, the returned list is empty.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex   high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value {@code fromIndex < 0 || toIndex > size || fromIndex > toIndex}
     */
    @NotNull
    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return Arrays.asList(toArray()).subList(fromIndex, toIndex);
    }
}