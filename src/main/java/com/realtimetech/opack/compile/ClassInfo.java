package com.realtimetech.opack.compile;

import com.realtimetech.opack.transformer.Transformer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class ClassInfo {
    public static class FieldInfo {
        final Field field;
        final String name;
        final Class<?> type;

        final Transformer transformer;
        final Class<?> explicitType;

        public FieldInfo(@NotNull Field field, Transformer transformer, Class<?> explicitType) {
            this.field = field;
            this.name = this.field.getName();
            this.type = explicitType == null ? this.field.getType() : explicitType;

            this.transformer = transformer;
            this.explicitType = explicitType;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
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