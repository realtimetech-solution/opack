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

import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class OpackObject extends AbstractOpackValue<LinkedHashMap<Object, Object>> {
    /**
     * Constructs an OpackObject with the specified initial capacity
     *
     * @param initialCapacity the initial capacity
     */
    public OpackObject(int initialCapacity) {
        this.set(new LinkedHashMap<>(initialCapacity));
    }

    /**
     * Constructs an empty OpackObject without underlying map
     */
    public OpackObject() {
    }

    /**
     * Create and return the underlying map of this opack object
     * This method will be called if {@link AbstractOpackValue#get() get()} method is called, when this opack object does not have an underlying map
     *
     * @return underlying map
     */
    @Override
    protected @NotNull LinkedHashMap<Object, Object> createLazyValue() {
        return new LinkedHashMap<>();
    }

    /**
     * Returns the number of items in this opack object
     *
     * @return the number of items
     */
    public int size() {
        return this.get().size();
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public Object get(Object key) {
        return this.get().get(key);
    }

    /**
     * Returns the value to which the specified key is mapped as an opack value,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public OpackValue getAsOpackValue(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof OpackValue) {
            return (OpackValue) object;
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to opack value.");
    }

    /**
     * Returns the value to which the specified key is mapped as an opack array,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public OpackArray getAsOpackArray(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof OpackArray) {
            return (OpackArray) object;
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to opack array.");
    }

    /**
     * Returns the value to which the specified key is mapped as an opack object,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public OpackObject getAsOpackObject(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof OpackObject) {
            return (OpackObject) object;
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to opack object.");
    }

    /**
     * Returns the value to which the specified key is mapped as a char,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public Character getAsChar(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return (char) ((Number) object).byteValue();
        } else if (object instanceof Character) {
            return (char) object;
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to char.");
    }

    /**
     * Returns the value to which the specified key is mapped as a string,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public String getAsString(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        }

        return object.toString();
    }

    /**
     * Returns the value to which the specified key is mapped as a byte,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public Byte getAsByte(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).byteValue();
        } else if (object instanceof String) {
            return Byte.parseByte((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to byte.");
    }

    /**
     * Returns the value to which the specified key is mapped as a short,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public Short getAsShort(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).shortValue();
        } else if (object instanceof String) {
            return Short.parseShort((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to short.");
    }

    /**
     * Returns the value to which the specified key is mapped as an int,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public Integer getAsInt(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).intValue();
        } else if (object instanceof String) {
            return Integer.parseInt((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to int.");
    }

    /**
     * Returns the value to which the specified key is mapped as a float,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public Float getAsFloat(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).floatValue();
        } else if (object instanceof String) {
            return Float.parseFloat((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to float.");
    }

    /**
     * Returns the value to which the specified key is mapped as a double,
     * or null if this map contains no mapping for the key
     *
     * @param key the key
     * @return the mapped value
     */
    public Double getAsDouble(Object key) {
        Object object = this.get().get(key);

        if (object == null) {
            return null;
        } else if (object instanceof Number) {
            return ((Number) object).doubleValue();
        } else if (object instanceof String) {
            return Double.parseDouble((String) object);
        }

        throw new ClassCastException("Cannot cast " + object.getClass().getName() + " to double.");
    }

    /**
     * Put a pair of key and value into this opack object
     * If the opack object previously contained a mapping for the key, the old value is replaced
     *
     * @param key   the key
     * @param value the value to put
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public Object put(Object key, Object value) {
        if (key != null) {
            OpackValue.assertAllowType(key.getClass());
        }

        if (value != null) {
            OpackValue.assertAllowType(value.getClass());
        }

        return this.get().put(key, value);
    }

    /**
     * Removes the mapping for the specified key from this opack object
     *
     * @param key the key
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public Object remove(Object key) {
        if (key != null) {
            OpackValue.assertAllowType(key.getClass());
        }

        return this.get().remove(key);
    }

    /**
     * Returns true if this opack object contains a mapping for the specified key
     *
     * @param object the key
     * @return true if this opack object contains a mapping for the specified key
     */
    public boolean containsKey(Object object) {
        return this.get().containsKey(object);
    }

    /**
     * Returns true if this opack object maps one or more keys to the specified value
     *
     * @param object the value
     * @return true if this opack object maps one or more keys to the specified value
     */
    public boolean containsValue(Object object) {
        return this.get().containsValue(object);
    }

    /**
     * Returns a {@link Set Set} view of the keys contained in this opack object
     *
     * @return a set view of the keys contained in this map
     */
    public Set<Object> keySet() {
        return this.get().keySet();
    }

    /**
     * Returns a {@link Set Set} view of the key and value pair in this opack object
     *
     * @return a set view of the key and value pair in this map
     */
    public Set<Map.Entry<Object, Object>> entrySet() {
        return this.get().entrySet();
    }

    /**
     * Returns a {@link Collection Collection} view of the values contained in this opack object
     *
     * @return a view of the values contained in this map
     */
    public Collection<Object> values() {
        return this.get().values();
    }

    /**
     * Returns a string representation of the {@link HashMap HashMap} that is the underlying of the opack object
     *
     * @param value the underlying object of the opack object
     * @return a string representation of the HashMap
     */
    @Override
    protected String toString(LinkedHashMap<Object, Object> value) {
        return value.toString();
    }

    /**
     * Returns a deep copy of this opack object instance
     *
     * @return a deep copy of this opack object instance
     */
    @Override
    public OpackObject clone() {
        OpackObject opackObject = new OpackObject(this.size());

        for (Object key : this.get().keySet()) {
            Object value = this.get(key);

            if (key instanceof OpackValue) {
                key = ((OpackValue) key).clone();
            }

            if (value instanceof OpackValue) {
                value = ((OpackValue) value).clone();
            }

            opackObject.put(key, value);
        }

        return opackObject;
    }

    /**
     * Returns true if a specific object is the same as this opack object
     *
     * @param object the reference object with which to compare
     * @return true if a specific object is the same as this opack object
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        OpackObject opackObject = (OpackObject) object;

        return opackObject.get().equals(this.get());
    }

    /**
     * Returns the hash code of this opack object
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return this.get().hashCode();
    }
}