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

import java.util.Map;

abstract class AbstractOpackValue<T> implements OpackValue {
    private volatile T value;

    /**
     * Create and return the underlying list of this opack value.
     * This method will be called if {@link AbstractOpackValue#get() get()} method is called, when this opack value does not have an underlying object.
     *
     * @return underlying object
     */
    abstract T createLazyValue();

    /**
     * Returns the underlying object of this opack value.
     *
     * @return underlying object
     */
    T get() {
        if (this.value == null) {
            synchronized (this) {
                if (this.value == null) {
                    this.value = createLazyValue();
                }
            }
        }

        return this.value;
    }

    /**
     * Sets the underlying object of this opack value.
     *
     * @param value the underlying object to set
     */
    void set(T value) {
        synchronized (this) {
            this.value = value;
        }
    }

    /**
     * Returns a string representation of the underlying object of opack value.
     *
     * @param value the underlying object of opack value
     * @return a string representation of the underlying object of opack value
     */
    abstract String toString(T value);

    /**
     * Returns true if a specific object is the same as this opack value.
     *
     * @param object the reference object with which to compare
     * @return true if a specific object is the same as this opack value
     */
    public abstract boolean equals(Object object);

    /**
     * Returns the hash code of this opack value.
     *
     * @return hash code
     */
    public abstract int hashCode();

    /**
     * Clone this opack value.
     *
     * @return cloned opack value
     */
    @Override
    public abstract OpackValue clone();

    /**
     * Returns a string representation of this opack value.
     *
     * @return a string representation of this opack value
     */
    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + "(" + this.toString(get()) + ")";
    }
}