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

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public final class OpackObject<K, V> extends OpackValue<HashMap<K, V>> {
    public OpackObject(int initialCapacity) {
        this.set(new HashMap<>(initialCapacity));
    }

    public OpackObject() {
    }

    @Override
    HashMap<K, V> createLazyValue() {
        return new HashMap<>();
    }

    public int size() {
        return this.get().size();
    }

    public V get(@NotNull K key) {
        return this.get().get(key);
    }

    public V put(@NotNull K key, @NotNull V value) {
        if (key != null)
            OpackValue.assertAllowType(key.getClass());

        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        return this.get().put(key, value);
    }

    public boolean containsKey(Object object) {
        return this.get().containsKey(object);
    }

    public boolean containsValue(Object object) {
        return this.get().containsValue(object);
    }

    public Set<K> keySet(){
        return this.get().keySet();
    }

    public Collection<V> values(){
        return this.get().values();
    }

    @Override
    String toString(HashMap<K, V> value) {
        return value.toString();
    }

    @Override
    public OpackValue clone() {
        OpackObject<K, V> opackObject = new OpackObject<K, V>(this.size());

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
}
