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

import com.realtimetech.opack.capture.CapturedType;
import com.realtimetech.opack.capture.TypeCapturer;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.exception.TypeCaptureException;
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
import com.realtimetech.opack.transformer.impl.uuid.UUIDTransformer;
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
    public static final class Builder {
        /**
         * Creates a new instance of the builder class
         *
         * @return the created builder
         */
        public static @NotNull Builder create() {
            return new Builder();
        }

        private int valueStackInitialSize;
        private int contextStackInitialSize;

        private boolean enableWrapListElementType;
        private boolean enableWrapMapElementType;
        private boolean enableConvertEnumToOrdinal;
        private boolean enableConvertRecursiveDependencyToNull;

        private @NotNull ClassLoader classLoader;

        Builder() {
            this.valueStackInitialSize = 512;
            this.contextStackInitialSize = 128;

            this.enableWrapListElementType = false;
            this.enableWrapMapElementType = false;
            this.enableConvertEnumToOrdinal = false;
            this.enableConvertRecursiveDependencyToNull = false;

            this.classLoader = this.getClass().getClassLoader();
        }

        /**
         * Sets the initial size of the value stack
         *
         * @param valueStackInitialSize the initial size for the value stack
         * @return the current builder instance for method chaining
         */
        public @NotNull Builder setValueStackInitialSize(int valueStackInitialSize) {
            this.valueStackInitialSize = valueStackInitialSize;
            return this;
        }

        /**
         * Sets the initial size of the context stack
         *
         * @param contextStackInitialSize the initial size to set for the context stack
         * @return the current builder instance for method chaining
         */
        public @NotNull Builder setContextStackInitialSize(int contextStackInitialSize) {
            this.contextStackInitialSize = contextStackInitialSize;
            return this;
        }

        /**
         * Sets whether wrapping of list element types is enabled or not
         *
         * @param enableWrapListElementType the flag indicating if list element types should be wrapped
         * @return the current builder instance for method chaining
         */
        public @NotNull Builder setEnableWrapListElementType(boolean enableWrapListElementType) {
            this.enableWrapListElementType = enableWrapListElementType;
            return this;
        }

        /**
         * Sets whether wrapping of map element types is enabled or not
         *
         * @param enableWrapMapElementType the flag indicating if map element types should be wrapped
         * @return the current builder instance for method chaining
         */
        public @NotNull Builder setEnableWrapMapElementType(boolean enableWrapMapElementType) {
            this.enableWrapMapElementType = enableWrapMapElementType;
            return this;
        }

        /**
         * Sets whether enums should be converted to their ordinal value instead of string representation
         * during serialization and deserialization
         *
         * @param enableConvertEnumToOrdinal the flag indicating whether to enable conversion of enums to ordinals
         * @return the current builder instance for method chaining
         */
        public @NotNull Builder setEnableConvertEnumToOrdinal(boolean enableConvertEnumToOrdinal) {
            this.enableConvertEnumToOrdinal = enableConvertEnumToOrdinal;
            return this;
        }

        /**
         * Sets whether recursive dependencies should be converted to null during serialization
         *
         * @param enableConvertRecursiveDependencyToNull the flag indicating whether recursive dependencies
         *                                               should be converted to null
         * @return the current builder instance for method chaining
         */
        public @NotNull Builder setEnableConvertRecursiveDependencyToNull(boolean enableConvertRecursiveDependencyToNull) {
            this.enableConvertRecursiveDependencyToNull = enableConvertRecursiveDependencyToNull;
            return this;
        }

        /**
         * Sets the class loader to be used by the builder
         *
         * @param classLoader the {@link ClassLoader} to be set
         * @return the current builder instance for method chaining
         */
        public @NotNull Builder setClassLoader(@NotNull ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Build the {@link Opacker Opacker}
         *
         * @return the created opacker
         */
        public @NotNull Opacker build() {
            return new Opacker(this);
        }
    }

    public static final class Context {
        private final @NotNull Opacker opacker;

        private @Nullable CapturedType superType;

        private @Nullable CapturedType.FieldProperty currentFieldProperty;

        public Context(@NotNull Opacker opacker) {
            this.opacker = opacker;

            this.superType = null;
            this.currentFieldProperty = null;
        }

        public @NotNull Opacker getOpacker() {
            return this.opacker;
        }

        public @Nullable CapturedType getSuperType() {
            return this.superType;
        }

        public void setSuperType(@Nullable CapturedType superType) {
            this.superType = superType;
        }

        public @Nullable CapturedType.FieldProperty getCurrentFieldProperty() {
            return this.currentFieldProperty;
        }

        public void setCurrentFieldProperty(@Nullable CapturedType.FieldProperty currentFieldProperty) {
            this.currentFieldProperty = currentFieldProperty;
        }

        void clear() {
            this.superType = null;
            this.currentFieldProperty = null;
        }
    }

    public enum State {
        NONE, SERIALIZE, DESERIALIZE
    }

    private final @NotNull ClassLoader classLoader;
    private final @NotNull TypeCapturer typeCapturer;

    private final @NotNull FastStack<@NotNull Object> objectStack;
    private final @NotNull FastStack<@NotNull CapturedType> typeStack;
    private final @NotNull FastStack<@NotNull OpackValue> valueStack;
    private final @NotNull HashSet<@NotNull Object> overlapSet;

    private final boolean enableConvertEnumToOrdinal;
    private final boolean enableConvertRecursiveDependencyToNull;

    @NotNull State state;

    @NotNull Context context;

    /**
     * Constructs the Opacker with the builder
     *
     * @param builder the builder of Opacker
     * @throws IllegalStateException if the predefined transformer cannot be instanced
     */
    Opacker(@NotNull Builder builder) {
        this.classLoader = builder.classLoader;
        this.typeCapturer = new TypeCapturer(this);

        this.objectStack = new FastStack<>(builder.contextStackInitialSize);
        this.typeStack = new FastStack<>(builder.contextStackInitialSize);
        this.valueStack = new FastStack<>(builder.valueStackInitialSize);
        this.overlapSet = new HashSet<>();
        this.state = State.NONE;
        this.context = new Context(this);

        try {
            if (builder.enableWrapListElementType) {
                this.typeCapturer.registerPredefinedTransformer(List.class, WrapListTransformer.class, true);
            } else {
                this.typeCapturer.registerPredefinedTransformer(List.class, ListTransformer.class, true);
            }

            if (builder.enableWrapMapElementType) {
                this.typeCapturer.registerPredefinedTransformer(Map.class, WrapMapTransformer.class, true);
            } else {
                this.typeCapturer.registerPredefinedTransformer(Map.class, MapTransformer.class, true);
            }

            this.typeCapturer.registerPredefinedTransformer(File.class, FileTransformer.class, true);
            this.typeCapturer.registerPredefinedTransformer(Path.class, PathTransformer.class, true);

            this.typeCapturer.registerPredefinedTransformer(Date.class, DateTransformer.class, true);
            this.typeCapturer.registerPredefinedTransformer(Calendar.class, CalendarTransformer.class, true);

            this.typeCapturer.registerPredefinedTransformer(LocalDate.class, LocalDateTransformer.class, true);
            this.typeCapturer.registerPredefinedTransformer(LocalTime.class, LocalTimeTransformer.class, true);
            this.typeCapturer.registerPredefinedTransformer(LocalDateTime.class, LocalDateTimeTransformer.class, true);

            this.typeCapturer.registerPredefinedTransformer(UUID.class, UUIDTransformer.class, true);

            this.typeCapturer.registerPredefinedTransformer(Class.class, ClassTransformer.class, true);
        } catch (InstantiationException exception) {
            throw new IllegalStateException(exception);
        }

        this.enableConvertEnumToOrdinal = builder.enableConvertEnumToOrdinal;
        this.enableConvertRecursiveDependencyToNull = builder.enableConvertRecursiveDependencyToNull;
    }


    /**
     * Retrieves the {@link ClassLoader ClassLoader} associated with the Opacker instance
     *
     * @return the {@link ClassLoader ClassLoader} used by this Opacker instance
     */
    public @NotNull ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Retrieves the {@link TypeCapturer TypeCapturer} instance associated with this Opacker
     *
     * @return the {@link TypeCapturer TypeCapturer} used by this Opacker instance
     */
    public @NotNull TypeCapturer getTypeCapturer() {
        return this.typeCapturer;
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
     * Serializes the object to {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue}
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
                this.context.clear();
            }
        }

        return serializedObject;
    }

    /**
     * Store information needed for serialization in stacks
     *
     * @param baseType the class of object to be serialized
     * @param object   the object to be serialized
     * @return the prepared opack value
     * @throws SerializeException if a problem occurs during serializing, if the baseType cannot be captured into {@link CapturedType CapturedType}
     */
    private @Nullable Object prepareObjectSerialize(@NotNull Class<?> baseType, @NotNull Object object) throws SerializeException {
        try {
            CapturedType capturedType = this.typeCapturer.get(baseType);

            for (Transformer transformer : capturedType.getTransformers()) {
                object = transformer.serialize(this.context, baseType, object);

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
                opackValue = new OpackArray(Array.getLength(object));
            } else {
                opackValue = new OpackObject();
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
            this.typeStack.push(capturedType);

            return opackValue;
        } catch (TypeCaptureException exception) {
            throw new SerializeException("Can't capture " + baseType.getName() + " class information.", exception);
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
            CapturedType capturedType = this.typeStack.pop();

            this.context.setSuperType(capturedType);

            if (opackValue instanceof OpackArray) {
                OpackArray opackArray = (OpackArray) opackValue;
                int length = Array.getLength(object);

                this.context.setCurrentFieldProperty(null);

                for (int index = 0; index < length; index++) {
                    Object element = ReflectionUtil.getArrayItem(object, index);

                    if (element != null) {
                        opackArray.add(this.prepareObjectSerialize(element.getClass(), element));
                    } else {
                        opackArray.add(null);
                    }
                }
            } else if (opackValue instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) opackValue;

                for (CapturedType.FieldProperty fieldProperty : capturedType.getFields()) {
                    try {
                        Object element = fieldProperty.get(object);
                        Class<?> fieldType = fieldProperty.getType();

                        this.context.setCurrentFieldProperty(fieldProperty);

                        if (fieldProperty.isWithType()) {
                            element = TypeWrapper.wrapObject(this.context, element);
                        }

                        if (fieldProperty.getTransformer() != null) {
                            element = fieldProperty.getTransformer().serialize(this.context, fieldType, element);
                        }

                        if (element != null) {
                            opackObject.put(fieldProperty.getName(), this.prepareObjectSerialize(fieldType, element));
                        } else {
                            opackObject.put(fieldProperty.getName(), null);
                        }

                    } catch (IllegalAccessException exception) {
                        throw new SerializeException("Can't get " + fieldProperty.getName() + " field data in " + capturedType.getType().getSimpleName() + ".", exception);
                    }
                }
            }
        }
    }


    /**
     * Deserializes the object to object of the target class
     *
     * @param <T>        the type of the object to be deserialized
     * @param type       the target class
     * @param opackValue the opack value to be deserialized
     * @return the deserialized object
     * @throws DeserializeException if a problem occurs during deserializing, if this opacker is serializing
     */
    public synchronized <T> @Nullable T deserialize(@NotNull Class<T> type, @NotNull OpackValue opackValue) throws DeserializeException {
        return this.deserializeObject(type, opackValue);
    }

    /**
     * Deserializes the {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue} to object of the target class
     *
     * @param <T>    the type of the object to be deserialized
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
                this.context.clear();
            }
        }

        return value;
    }

    /**
     * Store information needed for deserialization in stacks
     *
     * @param goalType the class of object to be deserialized
     * @param object   the object to be deserialized
     * @return the prepared object
     * @throws DeserializeException if a problem occurs during deserializing
     */
    private synchronized @Nullable Object prepareObjectDeserialize(@NotNull Class<?> goalType, @NotNull Object object, boolean withType, @Nullable Transformer fieldTransformer) throws DeserializeException {
        try {
            CapturedType capturedType = this.typeCapturer.get(goalType);

            Transformer[] transformers = capturedType.getTransformers();

            for (int index = transformers.length - 1; index >= 0; index--) {
                object = transformers[index].deserialize(this.context, goalType, object);

                if (object == null) {
                    return null;
                }
            }

            if (fieldTransformer != null) {
                object = fieldTransformer.deserialize(this.context, goalType, object);

                if (object == null) {
                    return null;
                }
            }

            if (withType) {
                object = TypeWrapper.unwrapObject(this.context, object);

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
                    //noinspection unchecked,rawtypes
                    return Enum.valueOf((Class<? extends Enum>) goalType, object.toString());
                }
            }

            /*
                Optimize algorithm for a big array
             */
            if (OpackArray.isAllowArray(goalType)) {
                int dimensions = ReflectionUtil.getArrayDimension(goalType);

                if (dimensions == 1 && object instanceof OpackArray) {
                    OpackArray opackArray = (OpackArray) object;
                    Class<?> componentType = goalType.getComponentType();

                    return OpackArrayConverter.convertToArray(componentType, opackArray);
                }
            }


            if (object instanceof OpackValue) {
                OpackValue opackValue = (OpackValue) object;
                Object targetObject;

                if (goalType.isArray()) {
                    if (object instanceof OpackArray) {
                        OpackArray opackArray = (OpackArray) object;

                        targetObject = Array.newInstance(goalType.getComponentType(), opackArray.length());
                    } else {
                        throw new DeserializeException("Target class is array. but, object is not OpackArray.");
                    }
                } else {
                    if (object instanceof OpackObject) {
                        OpackObject opackObject = (OpackObject) object;

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
                this.typeStack.push(capturedType);

                return targetObject;
            } else if (goalType.isAssignableFrom(object.getClass())) {
                return object;
            } else {
                throw new DeserializeException("Failed to convert object(" + object + ") to the specified goal type: " + goalType.getName() + ". This could be due to a missing Transformer.");
            }
        } catch (TypeCaptureException exception) {
            throw new DeserializeException("Can't capture " + goalType.getName() + " class information.", exception);
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
            CapturedType capturedType = this.typeStack.pop();

            this.context.setSuperType(capturedType);

            if (opackValue instanceof OpackArray) {
                OpackArray opackArray = (OpackArray) opackValue;
                Class<?> componentType = object.getClass().getComponentType();
                int length = opackArray.length();

                this.context.setCurrentFieldProperty(null);

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
                OpackObject opackObject = (OpackObject) opackValue;
                for (CapturedType.FieldProperty fieldProperty : capturedType.getFields()) {
                    String propertyName = fieldProperty.getName();

                    try {
                        Object element;
                        Class<?> fieldType = fieldProperty.getType();
                        Class<?> actualFieldType = fieldProperty.getField().getType();

                        this.context.setCurrentFieldProperty(fieldProperty);

                        if (opackObject.containsKey(propertyName)) {
                            element = opackObject.get(propertyName);
                        } else if (fieldProperty.getDefaultValueProvider() != null) {
                            element = fieldProperty.getDefaultValueProvider().provide(this.context, object, fieldProperty);
                        } else {
                            throw new DeserializeException("Missing " + fieldProperty.getName() + " fieldProperty value of for " + capturedType.getType().getSimpleName() + " in given opack value.");
                        }

                        Object propertyValue = null;

                        if (element != null) {
                            Object deserializedValue = this.prepareObjectDeserialize(fieldType, element, fieldProperty.isWithType(), fieldProperty.getTransformer());

                            if (deserializedValue != null) {
                                propertyValue = ReflectionUtil.cast(actualFieldType, deserializedValue);
                            }
                        }

                        fieldProperty.set(object, propertyValue);
                    } catch (IllegalAccessException | IllegalArgumentException exception) {
                        throw new DeserializeException("Can't set " + fieldProperty.getName() + " field in " + capturedType.getType().getSimpleName() + ".", exception);
                    }
                }
            }
        }
    }
}