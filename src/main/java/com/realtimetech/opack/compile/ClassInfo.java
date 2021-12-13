package com.realtimetech.opack.compile;

import com.realtimetech.opack.transformer.Transformer;

import java.lang.reflect.Field;

public class ClassInfo {
    public static class FieldInfo {
        final Field field;
        final Transformer transformer;
        final Class<?> explicitType;

        public FieldInfo(Field field, Transformer transformer, Class<?> explicitType) {
            this.field = field;
            this.transformer = transformer;
            this.explicitType = explicitType;
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
    }

    final Class<?> targetClass;
    final Transformer transformer;
    final FieldInfo[] fields;

    public ClassInfo(Class<?> targetClass, Transformer transformer, FieldInfo[] fields) {
        this.targetClass = targetClass;
        this.transformer = transformer;
        this.fields = fields;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public FieldInfo[] getFields() {
        return fields;
    }
}