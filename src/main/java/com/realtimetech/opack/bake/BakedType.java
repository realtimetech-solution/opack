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

package com.realtimetech.opack.bake;

import com.realtimetech.opack.provider.DefaultValueProvider;
import com.realtimetech.opack.transformer.Transformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public final class BakedType {
    public static final class Property {
        private final @NotNull Field field;
        private final @NotNull String name;
        private final @NotNull Class<?> type;
        private final boolean withType;

        private final @Nullable Transformer transformer;
        private final @Nullable DefaultValueProvider defaultValueProvider;

        public Property(@NotNull Field field, @Nullable String name, @Nullable Class<?> type, boolean withType, @Nullable Transformer transformer, @Nullable DefaultValueProvider defaultValueProvider) {
            this.field = field;
            this.name = name == null ? this.field.getName() : name;
            this.type = type == null ? this.field.getType() : type;
            this.withType = withType;

            this.transformer = transformer;
            this.defaultValueProvider = defaultValueProvider;
        }

        public @NotNull Field getField() {
            return field;
        }

        public @NotNull String getName() {
            return name;
        }

        public @NotNull Class<?> getType() {
            return type;
        }

        public boolean isWithType() {
            return withType;
        }

        public @Nullable Transformer getTransformer() {
            return transformer;
        }

        public @Nullable DefaultValueProvider getDefaultValueProvider() {
            return defaultValueProvider;
        }

        /**
         * Sets the field of the object to a specified value
         *
         * @param object the object whose field should be modified
         * @param value  the new value for the field of an object being modified
         * @throws IllegalAccessException if the field cannot be accessed or modified
         */
        public void set(@NotNull Object object, @Nullable Object value) throws IllegalAccessException {
            if (!this.field.canAccess(object)) {
                this.field.setAccessible(true);
            }

            this.field.set(object, value);
        }

        /**
         * Returns the field value extracted from the object
         *
         * @param object the object to extract the field value
         * @return the field value
         * @throws IllegalAccessException if the field cannot be accessed or modified
         */
        public @Nullable Object get(@NotNull Object object) throws IllegalAccessException {
            if (!this.field.canAccess(object)) {
                this.field.setAccessible(true);
            }

            return this.field.get(object);
        }
    }

    final @NotNull Class<?> type;
    final @NotNull Transformer @NotNull [] transformers;
    final @NotNull Property @NotNull [] fields;

    public BakedType(@NotNull Class<?> type, @NotNull Transformer @NotNull [] transformers, @NotNull Property @NotNull [] fields) {
        this.type = type;
        this.transformers = transformers;
        this.fields = fields;
    }

    public @NotNull Class<?> getType() {
        return type;
    }

    public @NotNull Transformer @NotNull [] getTransformers() {
        return transformers;
    }

    public @NotNull Property @NotNull [] getFields() {
        return fields;
    }
}