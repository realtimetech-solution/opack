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

package com.realtimetech.opack;

import com.realtimetech.opack.bake.BakedType;
import com.realtimetech.opack.bake.TypeBaker;
import com.realtimetech.opack.exception.BakeException;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.impl.TypeWrapper;
import com.realtimetech.opack.transformer.impl.file.FileTransformer;
import com.realtimetech.opack.transformer.impl.list.ListTransformer;
import com.realtimetech.opack.transformer.impl.list.WrapListTransformer;
import com.realtimetech.opack.transformer.impl.map.MapTransformer;
import com.realtimetech.opack.transformer.impl.map.WrapMapTransformer;
import com.realtimetech.opack.transformer.impl.path.PathTransformer;
import com.realtimetech.opack.transformer.impl.reflection.ClassTransformer;
import com.realtimetech.opack.transformer.impl.time.CalendarTransformer;
import com.realtimetech.opack.transformer.impl.time.DateTransformer;
import com.realtimetech.opack.transformer.impl.time.java8.LocalDateTimeTransformer;
import com.realtimetech.opack.transformer.impl.time.java8.LocalDateTransformer;
import com.realtimetech.opack.transformer.impl.time.java8.LocalTimeTransformer;
import com.realtimetech.opack.util.OpackArrayConverter;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class Opacker {
    public static class Builder {
        private int valueStackInitialSize;
        private int contextStackInitialSize;

        private boolean enableWrapListElementType;
        private boolean enableWrapMapElementType;
        private boolean enableConvertEnumToOrdinal;
        private boolean enableConvertRecursiveDependencyToNull;

        private @NotNull ClassLoader classLoader;

        public Builder() {
            this.valueStackInitialSize = 512;
            this.contextStackInitialSize = 128;

            this.enableWrapListElementType = false;
            this.enableWrapMapElementType = false;
            this.enableConvertEnumToOrdinal = false;
            this.enableConvertRecursiveDependencyToNull = false;

            this.classLoader = this.getClass().getClassLoader();
        }

        public @NotNull Builder setValueStackInitialSize(int valueStackInitialSize) {
            this.valueStackInitialSize = valueStackInitialSize;
            return this;
        }

        public @NotNull Builder setContextStackInitialSize(int contextStackInitialSize) {
            this.contextStackInitialSize = contextStackInitialSize;
            return this;
        }

        public @NotNull Builder setEnableWrapListElementType(boolean enableWrapListElementType) {
            this.enableWrapListElementType = enableWrapListElementType;
            return this;
        }

        public @NotNull Builder setEnableWrapMapElementType(boolean enableWrapMapElementType) {
            this.enableWrapMapElementType = enableWrapMapElementType;
            return this;
        }

        public @NotNull Builder setEnableConvertEnumToOrdinal(boolean enableConvertEnumToOrdinal) {
            this.enableConvertEnumToOrdinal = enableConvertEnumToOrdinal;
            return this;
        }

        public @NotNull Builder setEnableConvertRecursiveDependencyToNull(boolean enableConvertRecursiveDependencyToNull) {
            this.enableConvertRecursiveDependencyToNull = enableConvertRecursiveDependencyToNull;
            return this;
        }

        public @NotNull Builder setClassLoader(@NotNull ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Create the {@link Opacker Opacker} through this builder
         *
         * @return created opacker
         */
        public @NotNull Opacker create() {
            return new Opacker(this);
        }
    }

    public enum State {
        NONE, SERIALIZE, DESERIALIZE
    }

    private final @NotNull ClassLoader classLoader;
    private final @NotNull TypeBaker typeBaker;

    private final @NotNull FastStack<@NotNull Object> objectStack;
    private final @NotNull FastStack<@NotNull BakedType> typeStack;
    private final @NotNull FastStack<@NotNull OpackValue> valueStack;
    private final @NotNull HashSet<@NotNull Object> overlapSet;

    private final boolean enableConvertEnumToOrdinal;
    private final boolean enableConvertRecursiveDependencyToNull;

    @NotNull State state;

    /**
     * Constructs the Opacker with the builder of Opacker.
     *
     * @param builder the builder of Opacker
     * @throws IllegalStateException if the predefined transformer cannot be instanced
     */
    private Opacker(@NotNull Builder builder) {
        this.classLoader = builder.classLoader;
        this.typeBaker = new TypeBaker(this);

        this.objectStack = new FastStack<>(builder.contextStackInitialSize);
        this.typeStack = new FastStack<>(builder.contextStackInitialSize);
        this.valueStack = new FastStack<>(builder.valueStackInitialSize);
        this.overlapSet = new HashSet<>();
        this.state = State.NONE;

        try {
            if (builder.enableWrapListElementType) {
                this.typeBaker.registerPredefinedTransformer(List.class, WrapListTransformer.class, true);
            } else {
                this.typeBaker.registerPredefinedTransformer(List.class, ListTransformer.class, true);
            }

            if (builder.enableWrapMapElementType) {
                this.typeBaker.registerPredefinedTransformer(Map.class, WrapMapTransformer.class, true);
            } else {
                this.typeBaker.registerPredefinedTransformer(Map.class, MapTransformer.class, true);
            }

            this.typeBaker.registerPredefinedTransformer(File.class, FileTransformer.class, true);
            this.typeBaker.registerPredefinedTransformer(Path.class, PathTransformer.class, true);

            this.typeBaker.registerPredefinedTransformer(Date.class, DateTransformer.class, true);
            this.typeBaker.registerPredefinedTransformer(Calendar.class, CalendarTransformer.class, true);

            this.typeBaker.registerPredefinedTransformer(LocalDate.class, LocalDateTransformer.class, true);
            this.typeBaker.registerPredefinedTransformer(LocalTime.class, LocalTimeTransformer.class, true);
            this.typeBaker.registerPredefinedTransformer(LocalDateTime.class, LocalDateTimeTransformer.class, true);

            this.typeBaker.registerPredefinedTransformer(Class.class, ClassTransformer.class, true);
        } catch (InstantiationException exception) {
            throw new IllegalStateException(exception);
        }

        this.enableConvertEnumToOrdinal = builder.enableConvertEnumToOrdinal;
        this.enableConvertRecursiveDependencyToNull = builder.enableConvertRecursiveDependencyToNull;
    }

    public @NotNull ClassLoader getClassLoader() {
        return classLoader;
    }

    public @NotNull TypeBaker getTypeBaker() {
        return this.typeBaker;
    }


    /**
     * Serializes the object to {@link OpackValue OpackValue}
     *
     * @param object the object to be serialized
     * @return the serialized opack value
     * @throws SerializeException if a problem occurs during serializing, if this opacker is deserializing
     */
    public synchronized @Nullable OpackValue serialize(@NotNull Object object) throws SerializeException {
        return (OpackValue) this.serializeObject(object);
    }

    /**
     * Serializes the object to {@link OpackValue OpackValue}
     *
     * @param object the object to be serialized
     * @return the serialized object
     * @throws SerializeException if a problem occurs during serializing, if this opacker is deserializing
     */
    public synchronized @Nullable Object serializeObject(@NotNull Object object) throws SerializeException {
        if (this.state == State.DESERIALIZE)
            throw new SerializeException("Opacker is deserializing.");

        int separatorStack = this.objectStack.getSize();
        Object serializedObject = this.prepareObjectSerialize(object.getClass(), object);

        State lastState = this.state;
        try {
            this.state = State.SERIALIZE;
            this.executeSerializeStack(separatorStack);
        } finally {
            this.state = lastState;

            if (this.state == State.NONE) {
                this.overlapSet.clear();
            }
        }

        return serializedObject;
    }

    /**
     * Store information needed for serialization in stacks
     *
     * @param baseType the class of object to be serialized
     * @param object   the object to be serialized
     * @return prepared opack value
     * @throws SerializeException if a problem occurs during serializing, if the baseType cannot be baked into {@link BakedType BakedType}
     */
    private @Nullable Object prepareObjectSerialize(@NotNull Class<?> baseType, @NotNull Object object) throws SerializeException {
        try {
            BakedType bakedType = this.typeBaker.get(baseType);

            for (Transformer transformer : bakedType.getTransformers()) {
                object = transformer.serialize(this, baseType, object);

                if (object == null) {
                    return null;
                }
            }

            Class<?> objectType = object.getClass();

            /*
                Early stopping
             */
            if (OpackValue.isAllowType(objectType)) {
                /*
                    If directly pass opack value, deep clone
                 */
                if (object instanceof OpackValue) {
                    object = ((OpackValue) object).clone();
                }

                return object;
            }

            /*
                Enum converting
             */
            if (objectType.isEnum()) {
                if (this.enableConvertEnumToOrdinal) {
                    Object[] enums = objectType.getEnumConstants();

                    for (int i = 0; i < enums.length; i++) {
                        if (enums[i] == object) {
                            return i;
                        }
                    }

                    return -1;
                } else {
                    return object.toString();
                }
            }

            /*
                Optimize algorithm for a big array
             */
            if (OpackArray.isAllowArray(objectType)) {
                int dimensions = ReflectionUtil.getArrayDimension(objectType);
                if (dimensions == 1) {
                    return OpackArray.createWithArrayObject(ReflectionUtil.cloneArray(object));
                }
            }


            OpackValue opackValue;

            if (objectType.isArray()) {
                opackValue = new OpackArray<>(Array.getLength(object));
            } else {
                opackValue = new OpackObject<>();
            }

            if (this.overlapSet.contains(object)) {
                if (!this.enableConvertRecursiveDependencyToNull) {
                    throw new SerializeException("Recursive dependencies are not serializable.");
                }

                return null;
            }

            this.overlapSet.add(object);
            this.objectStack.push(object);
            this.valueStack.push(opackValue);
            this.typeStack.push(bakedType);

            return opackValue;
        } catch (BakeException exception) {
            throw new SerializeException("Can't bake " + baseType.getName() + " class information.", exception);
        }
    }

    /**
     * Serialize the elements of each opack value in the stack
     *
     * @throws SerializeException if a problem occurs during serializing, if the field in the class of instance to be serialized is not accessible
     */
    private void executeSerializeStack(int endOfStack) throws SerializeException {
        while (this.objectStack.getSize() > endOfStack) {
            Object object = this.objectStack.pop();
            OpackValue opackValue = this.valueStack.pop();
            BakedType bakedType = this.typeStack.pop();

            if (opackValue instanceof OpackArray) {
                OpackArray<Object> opackArray = (OpackArray<Object>) opackValue;
                int length = Array.getLength(object);

                for (int index = 0; index < length; index++) {
                    Object element = ReflectionUtil.getArrayItem(object, index);

                    if (element != null) {
                        opackArray.add(this.prepareObjectSerialize(element.getClass(), element));
                    } else {
                        opackArray.add(null);
                    }
                }
            } else if (opackValue instanceof OpackObject) {
                OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) opackValue;

                for (BakedType.Property property : bakedType.getFields()) {
                    try {
                        Object element = property.get(object);
                        Class<?> fieldType = property.getType();

                        if (property.isWithType()) {
                            element = TypeWrapper.wrapObject(this, element);
                        }

                        if (property.getTransformer() != null) {
                            element = property.getTransformer().serialize(this, fieldType, element);
                        }

                        if (element != null) {
                            opackObject.put(property.getName(), this.prepareObjectSerialize(fieldType, element));
                        } else {
                            opackObject.put(property.getName(), null);
                        }

                    } catch (IllegalAccessException exception) {
                        throw new SerializeException("Can't get " + property.getName() + " field data in " + bakedType.getType().getSimpleName() + ".", exception);
                    }
                }
            }
        }
    }


    /**
     * Deserializes the object to object of the target class
     *
     * @param type       the target class
     * @param opackValue the opack value to be deserialized
     * @return the deserialized object
     * @throws DeserializeException if a problem occurs during deserializing, if this opacker is serializing
     */
    public synchronized <T> @Nullable T deserialize(@NotNull Class<T> type, @NotNull OpackValue opackValue) throws DeserializeException {
        return this.deserializeObject(type, opackValue);
    }

    /**
     * Deserializes the object to object of the target class
     *
     * @param type   the target class
     * @param object the object to be deserialized
     * @return the deserialized object
     * @throws DeserializeException if a problem occurs during deserializing, if this opacker is serializing
     */
    public synchronized <T> @Nullable T deserializeObject(@NotNull Class<T> type, @NotNull Object object) throws DeserializeException {
        if (this.state == State.SERIALIZE)
            throw new DeserializeException("Opacker is serializing.");

        int separatorStack = this.objectStack.getSize();

        Object deserializedObject = this.prepareObjectDeserialize(type, object, false, null);

        if (deserializedObject == null) {
            return null;
        }

        T value = type.cast(deserializedObject);

        State lastState = this.state;
        try {
            this.state = State.DESERIALIZE;
            this.executeDeserializeStack(separatorStack);
        } finally {
            this.state = lastState;

            if (this.state == State.NONE) {
                this.overlapSet.clear();
            }
        }

        return value;
    }

    /**
     * Store information needed for deserialization in stacks
     *
     * @param goalType the class of object to be deserialized
     * @param object   the object to be deserialized
     * @return prepared object
     * @throws DeserializeException if a problem occurs during deserializing
     */
    private synchronized @Nullable Object prepareObjectDeserialize(@NotNull Class<?> goalType, @NotNull Object object, boolean withType, @Nullable Transformer fieldTransformer) throws DeserializeException {
        try {
            BakedType bakedType = this.typeBaker.get(goalType);

            Transformer[] transformers = bakedType.getTransformers();

            for (int index = transformers.length - 1; index >= 0; index--) {
                object = transformers[index].deserialize(this, goalType, object);

                if (object == null) {
                    return null;
                }
            }

            if (fieldTransformer != null) {
                object = fieldTransformer.deserialize(this, goalType, object);

                if (object == null) {
                    return null;
                }
            }

            if (withType) {
                object = TypeWrapper.unwrapObject(this, object);

                if (object == null) {
                    return null;
                }
            }

            /*
                Early stopping
             */
            if (OpackValue.isAllowType(goalType)) {
                /*
                    If directly pass opack value, deep clone
                 */
                if (object instanceof OpackValue) {
                    object = ((OpackValue) object).clone();
                }
                return object;
            }

            /*
                Enum converting
             */
            if (goalType.isEnum()) {
                if (this.enableConvertEnumToOrdinal) {
                    return goalType.getEnumConstants()[(int) ReflectionUtil.cast(Integer.class, object)];
                } else {
                    return Enum.valueOf((Class<? extends Enum>) goalType, object.toString());
                }
            }

            /*
                Optimize algorithm for big array
             */
            if (OpackArray.isAllowArray(goalType)) {
                int dimensions = ReflectionUtil.getArrayDimension(goalType);

                if (dimensions == 1 && object instanceof OpackArray) {
                    OpackArray<?> opackArray = (OpackArray<?>) object;
                    Class<?> componentType = goalType.getComponentType();

                    try {
                        return OpackArrayConverter.convertToArray(componentType, opackArray);
                    } catch (InvocationTargetException | IllegalAccessException exception) {
                        throw new DeserializeException("Can't convert OpackArray to native array.", exception);
                    }
                }
            }


            if (object instanceof OpackValue) {
                OpackValue opackValue = (OpackValue) object;
                Object targetObject;

                if (goalType.isArray()) {
                    if (object instanceof OpackArray) {
                        OpackArray<?> opackArray = (OpackArray<?>) object;

                        targetObject = Array.newInstance(goalType.getComponentType(), opackArray.length());
                    } else {
                        throw new DeserializeException("Target class is array. but, object is not OpackArray.");
                    }
                } else {
                    if (object instanceof OpackObject) {
                        OpackObject<?, ?> opackObject = (OpackObject<?, ?>) object;

                        try {
                            targetObject = ReflectionUtil.createInstanceUnsafe(goalType);
                        } catch (InvocationTargetException | IllegalAccessException |
                                 InstantiationException exception) {
                            throw new DeserializeException("Can't create instance using unsafe method.", exception);
                        }
                    } else {
                        throw new DeserializeException("Target class is object. but, object is not OpackObject.");
                    }
                }

                this.objectStack.push(targetObject);
                this.valueStack.push(opackValue);
                this.typeStack.push(bakedType);

                return targetObject;
            } else if (goalType.isAssignableFrom(object.getClass())) {
                return object;
            } else {
                throw new DeserializeException("Found object, stack corruption.");
            }
        } catch (BakeException exception) {
            throw new DeserializeException("Can't bake " + goalType.getName() + " class information.", exception);
        }
    }

    /**
     * Deserialize the elements of each opack value in the stack
     *
     * @throws DeserializeException if a problem occurs during deserializing, if the field in the class of instance to be deserialized is not accessible
     */
    private void executeDeserializeStack(int endOfStack) throws DeserializeException {
        while (this.objectStack.getSize() > endOfStack) {
            Object object = this.objectStack.pop();
            OpackValue opackValue = this.valueStack.pop();
            BakedType bakedType = this.typeStack.pop();

            if (opackValue instanceof OpackArray) {
                OpackArray<Object> opackArray = (OpackArray<Object>) opackValue;
                Class<?> componentType = object.getClass().getComponentType();
                int length = opackArray.length();

                for (int index = 0; index < length; index++) {
                    Object element = opackArray.get(index);

                    if (element != null) {
                        Object deserializedValue = this.prepareObjectDeserialize(componentType, element, false, null);

                        if (deserializedValue != null) {
                            ReflectionUtil.setArrayItem(object, index, ReflectionUtil.cast(componentType, deserializedValue));
                        } else {
                            ReflectionUtil.setArrayItem(object, index, null);
                        }
                    } else {
                        ReflectionUtil.setArrayItem(object, index, null);
                    }
                }
            } else if (opackValue instanceof OpackObject) {
                OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) opackValue;
                for (BakedType.Property property : bakedType.getFields()) {
                    try {
                        Object element = opackObject.get(property.getName());
                        Class<?> fieldType = property.getType();
                        Class<?> actualFieldType = property.getField().getType();

                        Object propertyValue = null;

                        if (element != null) {
                            Object deserializedValue = this.prepareObjectDeserialize(fieldType, element, property.isWithType(), property.getTransformer());

                            if (deserializedValue != null) {
                                propertyValue = ReflectionUtil.cast(actualFieldType, deserializedValue);
                            }
                        }

                        property.set(object, propertyValue);
                    } catch (IllegalAccessException | IllegalArgumentException exception) {
                        throw new DeserializeException("Can't set " + property.getName() + " field in " + bakedType.getType().getSimpleName() + ".", exception);
                    }
                }
            }
        }
    }
}