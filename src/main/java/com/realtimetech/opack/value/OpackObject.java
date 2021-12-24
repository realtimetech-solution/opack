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

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public final class OpackObject<K, V> extends AbstractOpackValue<HashMap<K, V>> {
    /**
     * Constructs an empty opack object with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity
     */
    public OpackObject(int initialCapacity) {
        this.set(new HashMap<>(initialCapacity));
    }

    /**
     * Constructs an empty opack object without underlying map.
     */
    public OpackObject() {
    }

    /**
     * Create and return the underlying map of this opack object.
     * This method will be called if {@link AbstractOpackValue#get() get()} method is called, when this opack object does not have an underlying map.
     *
     * @return underlying map
     */
    @Override
    HashMap<K, V> createLazyValue() {
        return new HashMap<>();
    }

    /**
     * Returns the number of items in this opack object.
     *
     * @return the number of items
     */
    public int size() {
        return this.get().size();
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     *
     * @param key the key
     * @return mapped value
     */
    public V get(K key) {
        return this.get().get(key);
    }

    /**
     * Put a pair of key and value into this opack object.
     * If the opack object previously contained a mapping for the key, the old value is replaced.
     *
     * @param key   the key
     * @param value the value to put
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public V put(K key, V value) {
        if (key != null)
            OpackValue.assertAllowType(key.getClass());

        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        return this.get().put(key, value);
    }

    /**
     * Removes the mapping for the specified key from this opack object.
     *
     * @param key the key
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public V remove(K key) {
        if (key != null)
            OpackValue.assertAllowType(key.getClass());

        return this.get().remove(key);
    }

    /**
     * Returns true if this opack object contains a mapping for the specified key.
     *
     * @param object the key
     * @return true if this opack object contains a mapping for the specified key
     */
    public boolean containsKey(Object object) {
        return this.get().containsKey(object);
    }

    /**
     * Returns true if this opack object maps one or more keys to the specified value.
     *
     * @param object the value
     * @return true if this opack object maps one or more keys to the specified value
     */
    public boolean containsValue(Object object) {
        return this.get().containsValue(object);
    }

    /**
     * Returns a {@link Set Set} view of the keys contained in this opack object.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<K> keySet() {
        return this.get().keySet();
    }

    /**
     * Returns a {@link Collection Collection} view of the values contained in this opack object.
     *
     * @return a view of the values contained in this map
     */
    public Collection<V> values() {
        return this.get().values();
    }

    /**
     * Returns a string representation of the {@link HashMap HashMap} that is the underlying of the opack object.
     *
     * @param value the underlying object of the opack object
     * @return a string representation of the HashMap
     */
    @Override
    String toString(HashMap<K, V> value) {
        return value.toString();
    }

    /**
     * Returns a deep copy of this opack object instance.
     *
     * @return a deep copy of this opack object instance
     */
    @Override
    public OpackObject<K, V> clone() {
        OpackObject<K, V> opackObject = new OpackObject<>(this.size());

        for (K key : this.get().keySet()) {
            V value = this.get(key);

            if (key instanceof OpackValue) {
                key = (K) ((OpackValue) key).clone();
            }

            if (value instanceof OpackValue) {
                value = (V) ((OpackValue) value).clone();
            }

            opackObject.put(key, value);
        }

        return opackObject;
    }

    /**
     * Returns true if a specific object is the same as this opack object.
     *
     * @param object the reference object with which to compare
     * @return true if a specific object is the same as this opack object
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        OpackObject<?, ?> opackObject = (OpackObject<?, ?>) object;

        return opackObject.get().equals(this.get());
    }

    /**
     * Returns the hash code of this opack object.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return this.get().hashCode();
    }
}
