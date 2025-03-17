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

import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.structure.NativeList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class OpackArray extends AbstractOpackValue<List<Object>> {
    /**
     * Returns whether component type for specific class of array object is primitive type
     *
     * @param arrayType the class of array object
     * @return true if component type for the class of array object is primitive class
     */
    public static boolean isAllowArray(@NotNull Class<?> arrayType) {
        if (arrayType.isArray()) {
            Class<?> componentType = ReflectionUtil.getArrayLastComponentType(arrayType);

            return ReflectionUtil.isPrimitiveType(componentType) || ReflectionUtil.isWrapperType(componentType);
        }

        return false;
    }

    /**
     * Create the opack array through array object of which component type is the primitive type
     *
     * @param arrayObject the array object for create
     * @return created opack array
     * @throws IllegalArgumentException if the component type for array object is not primitive type, if the array object is not 1 dimension
     */
    public static OpackArray createWithArrayObject(@NotNull Object arrayObject) {
        return new OpackArray(arrayObject);
    }

    private boolean nativeArray;

    /**
     * Constructs an opack array with the specified array object of which component type is the primitive type
     *
     * @param arrayObject the array object for create
     * @throws IllegalArgumentException if the component type for array object is not primitive type, if the array object is not 1 dimension
     */
    private OpackArray(@NotNull Object arrayObject) {
        if (!arrayObject.getClass().isArray()) {
            throw new IllegalArgumentException(arrayObject + " is not array object.");
        }

        if (ReflectionUtil.getArrayDimension(arrayObject.getClass()) != 1) {
            throw new IllegalArgumentException(arrayObject + " must have 1 dimension.");
        }

        if (!OpackArray.isAllowArray(arrayObject.getClass())) {
            throw new IllegalArgumentException(arrayObject + " array element is not allowed type, allow only primitive type or String or OpackValues or null.");
        }

        this.set(new NativeList(arrayObject));
        this.nativeArray = true;
    }

    /**
     * Constructs an opack array with the specified array
     *
     * @param array the array for create
     */
    public OpackArray(Object @NotNull [] array) {
        this.set(Arrays.asList(array));
        this.nativeArray = false;
    }

    /**
     * Constructs an opack array with the specified collection object
     *
     * @param collection the collection for create
     * @throws IllegalArgumentException if type of the element in collection is not allowed in opack value
     */
    public OpackArray(@NotNull Collection<Object> collection) {
        this(collection.size());

        List<Object> list = this.get();

        for (Object element : collection) {
            if (element != null)
                OpackValue.assertAllowType(element.getClass());

            list.add(element);
        }
    }

    /**
     * Constructs an empty opack array with the specified initial capacity
     *
     * @param initialCapacity the initial capacity
     */
    public OpackArray(int initialCapacity) {
        this.set(new ArrayList<>(initialCapacity));

        this.nativeArray = false;
    }

    /**
     * Constructs an empty opack array without underlying list
     */
    public OpackArray() {
        this.nativeArray = false;
    }

    /**
     * Create and return the underlying list of this opack array
     * This method will be called if {@link AbstractOpackValue#get() get()} method is called, when this opack array does not have an underlying list
     *
     * @return underlying list
     */
    @Override
    protected @NotNull ArrayList<Object> createLazyValue() {
        return new ArrayList<>();
    }

    private void convertNativeArrayToList() {
        if (this.nativeArray) {
            List<Object> list = this.get();

            if (list.getClass() == NativeList.class) {
                list = (List<Object>) Arrays.asList(list.toArray());
                this.set(list);
            }

            this.nativeArray = false;
        }
    }

    /**
     * Replaces the value at the specified position in this opack array with the specified value
     *
     * @param index index of the value to replace
     * @param value value to be stored at the specified position
     * @return the value previously at the specified position
     * @throws IllegalArgumentException if type of the value is not allowed in opack value
     */
    public Object set(int index, Object value) {
        if (value != null) {
            OpackValue.assertAllowType(value.getClass());
        }

        this.convertNativeArrayToList();

        return this.get().set(index, value);
    }

    /**
     * Appends the specified value to the end of this opack array
     *
     * @param value the value to be appended to this list
     * @return true if this opack array changed as a result of the call
     * @throws IllegalArgumentException if type of the value is not allowed in opack value
     */
    public boolean add(Object value) {
        if (value != null) {
            OpackValue.assertAllowType(value.getClass());
        }

        this.convertNativeArrayToList();

        return this.get().add(value);
    }

    /**
     * Removes the first occurrence of the specified value from this opack array, if it is present
     *
     * @param value the value to be removed from this opack array, if present
     * @return true if this opack array contained the specified value
     */
    public boolean remove(@NotNull Object value) {
        return this.get().remove(value);
    }

    /**
     * Returns the value at the specified position in this opack array
     *
     * @param index index of the value to replace
     * @return the value
     */
    public Object get(int index) {
        return this.get().get(index);
    }

    /**
     * Returns the value at the specified position in this opack array as an opack value
     *
     * @param index index of the value to replace
     * @return the value
     */
    public OpackValue getAsOpackValue(int index) {
        Object object = this.get().get(index);

        if (object instanceof OpackValue) {
            return (OpackValue) object;
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to opack value.");
    }

    /**
     * Returns the value at the specified position in this opack array as an opack array
     *
     * @param index index of the value to replace
     * @return the value
     */
    public OpackArray getAsOpackArray(int index) {
        Object object = this.get().get(index);

        if (object instanceof OpackArray) {
            return (OpackArray) object;
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to opack array.");
    }

    /**
     * Returns the value at the specified position in this opack array as an opack object
     *
     * @param index index of the value to replace
     * @return the value
     */
    public OpackObject getAsOpackObject(int index) {
        Object object = this.get().get(index);

        if (object instanceof OpackObject) {
            return (OpackObject) object;
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to opack object.");
    }

    /**
     * Returns the value at the specified position in this opack array as a char
     *
     * @param index index of the value to replace
     * @return the value
     */
    public char getAsChar(int index) {
        Object object = this.get().get(index);

        if (object instanceof Number) {
            return (char) ((Number) object).byteValue();
        } else if (object instanceof Character) {
            return (char) object;
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to char.");
    }

    /**
     * Returns the value at the specified position in this opack array as a string
     *
     * @param index index of the value to replace
     * @return the value
     */
    public String getAsString(int index) {
        return this.get().get(index).toString();
    }

    /**
     * Returns the value at the specified position in this opack array as a byte
     *
     * @param index index of the value to replace
     * @return the value
     */
    public byte getAsByte(int index) {
        Object object = this.get().get(index);

        if (object instanceof Number) {
            return ((Number) object).byteValue();
        } else if (object instanceof String) {
            return Byte.parseByte((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to byte.");
    }

    /**
     * Returns the value at the specified position in this opack array as a short
     *
     * @param index index of the value to replace
     * @return the value
     */
    public short getAsShort(int index) {
        Object object = this.get().get(index);

        if (object instanceof Number) {
            return ((Number) object).shortValue();
        } else if (object instanceof String) {
            return Short.parseShort((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to short.");
    }

    /**
     * Returns the value at the specified position in this opack array as an int
     *
     * @param index index of the value to replace
     * @return the value
     */
    public int getAsInt(int index) {
        Object object = this.get().get(index);

        if (object instanceof Number) {
            return ((Number) object).intValue();
        } else if (object instanceof String) {
            return Integer.parseInt((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to int.");
    }

    /**
     * Returns the value at the specified position in this opack array as a float
     *
     * @param index index of the value to replace
     * @return the value
     */
    public float getAsFloat(int index) {
        Object object = this.get().get(index);

        if (object instanceof Number) {
            return ((Number) object).floatValue();
        } else if (object instanceof String) {
            return Float.parseFloat((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to float.");
    }

    /**
     * Returns the value at the specified position in this opack array as a double
     *
     * @param index index of the value to replace
     * @return the value
     */
    public double getAsDouble(int index) {
        Object object = this.get().get(index);

        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else if (object instanceof String) {
            return Double.parseDouble((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to double.");
    }

    /**
     * Returns the number of elements in this opack array
     *
     * @return the number of elements in this opack array
     */
    public int length() {
        return this.get().size();
    }

    /**
     * Returns a string representation of the {@link List List} that is the underlying of the opack array
     *
     * @param value the underlying object of the opack array
     * @return a string representation of the List
     */
    @Override
    protected String toString(List<Object> value) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('[');

        boolean first = true;
        for (Object element : value) {
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
     * Returns a deep copy of this opack array instance
     *
     * @return a deep copy of this opack array instance
     */
    @Override
    public OpackArray clone() {
        OpackArray opackArray = new OpackArray(this.length());

        for (int index = 0; index < this.length(); index++) {
            Object object = this.get(index);

            if (object instanceof OpackValue) {
                object = ((OpackValue) object).clone();
            }

            opackArray.add(object);
        }

        return opackArray;
    }

    /**
     * Returns true if a specific object is the same as this opack array
     *
     * @param object the reference object with which to compare
     * @return true if a specific object is the same as this opack array
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        OpackArray opackArray = (OpackArray) object;

        return opackArray.get().equals(this.get());
    }

    /**
     * Returns the hash code of this opack array
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return this.get().hashCode();
    }
}