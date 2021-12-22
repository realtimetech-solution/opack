/*
 * Copyright (C) 2021 REALTIMETECH All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.realtimetech.opack.compile;

import com.realtimetech.opack.transformer.Transformer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class ClassInfo {
    public static class FieldInfo {
        final Field field;
        final String name;
        final Class<?> typeClass;

        final Transformer transformer;
        final Class<?> explicitType;

        public FieldInfo(@NotNull Field field, Transformer transformer, Class<?> explicitType) {
            this.field = field;
            this.name = this.field.getName();
            this.typeClass = explicitType == null ? this.field.getType() : explicitType;

            this.transformer = transformer;
            this.explicitType = explicitType;
        }

        public String getName() {
            return name;
        }

        public Class<?> getTypeClass() {
            return typeClass;
        }

        public Field getField() {
            return field;
        }

        public Transformer getTransformer() {
            return transformer;
        }

        public Class<?> getExplicitType() {
            return explicitType;
        }

        public void set(Object object, Object value) throws IllegalAccessException {
            if (!this.field.canAccess(object)) {
                this.field.setAccessible(true);
            }
            this.field.set(object, value);
        }

        public Object get(Object object) throws IllegalAccessException {
            if (!this.field.canAccess(object)) {
                this.field.setAccessible(true);
            }
            return this.field.get(object);
        }
    }

    final Class<?> targetClass;
    final Transformer[] transformers;
    final FieldInfo[] fields;

    public ClassInfo(Class<?> targetClass, Transformer[] transformers, FieldInfo[] fields) {
        this.targetClass = targetClass;
        this.transformers = transformers;
        this.fields = fields;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Transformer[] getTransformers() {
        return transformers;
    }

    public FieldInfo[] getFields() {
        return fields;
    }
}