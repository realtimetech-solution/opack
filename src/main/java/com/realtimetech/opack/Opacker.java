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

import com.realtimetech.opack.compile.ClassInfo;
import com.realtimetech.opack.compile.InfoCompiler;
import com.realtimetech.opack.exception.CompileException;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.impl.list.NoWrapListTransformer;
import com.realtimetech.opack.transformer.impl.list.WrapListTransformer;
import com.realtimetech.opack.transformer.impl.map.NoWrapMapTransformer;
import com.realtimetech.opack.transformer.impl.map.WrapMapTransformer;
import com.realtimetech.opack.util.OpackArrayConverter;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class Opacker {
    public static class Builder {
        int valueStackInitialSize;
        int contextStackInitialSize;

        boolean enableWrapListElementType;
        boolean enableWrapMapElementType;

        boolean convertEnumToOrdinal;

        public Builder() {
            this.valueStackInitialSize = 512;
            this.contextStackInitialSize = 128;

            this.enableWrapListElementType = false;
            this.enableWrapMapElementType = false;

            this.convertEnumToOrdinal = false;
        }

        public Builder setValueStackInitialSize(int valueStackInitialSize) {
            this.valueStackInitialSize = valueStackInitialSize;
            return this;
        }

        public Builder setContextStackInitialSize(int contextStackInitialSize) {
            this.contextStackInitialSize = contextStackInitialSize;
            return this;
        }

        public Builder setEnableWrapListElementType(boolean enableWrapListElementType) {
            this.enableWrapListElementType = enableWrapListElementType;
            return this;
        }

        public Builder setEnableWrapMapElementType(boolean enableWrapMapElementType) {
            this.enableWrapMapElementType = enableWrapMapElementType;
            return this;
        }

        public Builder setConvertEnumToOrdinal(boolean convertEnumToOrdinal) {
            this.convertEnumToOrdinal = convertEnumToOrdinal;
            return this;
        }

        /**
         * Create the Opacker through this builder.
         *
         * @return created opacker
         * @throws InstantiationException if the predefined transformer cannot be instanced
         */
        public Opacker create() throws InstantiationException {
            return new Opacker(this);
        }
    }

    public enum State {
        NONE, SERIALIZE, DESERIALIZE
    }

    final @NotNull InfoCompiler infoCompiler;

    final @NotNull FastStack<Object> objectStack;
    final @NotNull FastStack<ClassInfo> classInfoStack;
    final @NotNull FastStack<OpackValue> valueStack;

    final @NotNull boolean convertEnumToOrdinal;

    @NotNull State state;

    /**
     * Constructs the Opacker with the builder of Opacker.
     *
     * @param builder the builder of Opacker
     * @throws InstantiationException if the predefined transformer cannot be instanced
     */
    Opacker(Builder builder) throws InstantiationException {
        this.infoCompiler = new InfoCompiler(this);

        this.objectStack = new FastStack<>(builder.contextStackInitialSize);
        this.classInfoStack = new FastStack<>(builder.contextStackInitialSize);
        this.valueStack = new FastStack<>(builder.valueStackInitialSize);

        this.state = State.NONE;

        if (builder.enableWrapListElementType) {
            this.infoCompiler.registerPredefinedTransformer(List.class, WrapListTransformer.class, true);
        } else {
            this.infoCompiler.registerPredefinedTransformer(List.class, NoWrapListTransformer.class, true);
        }

        if (builder.enableWrapMapElementType) {
            this.infoCompiler.registerPredefinedTransformer(Map.class, WrapMapTransformer.class, true);
        } else {
            this.infoCompiler.registerPredefinedTransformer(Map.class, NoWrapMapTransformer.class, true);
        }

        this.convertEnumToOrdinal = builder.convertEnumToOrdinal;
    }

    /**
     * Serializes the object to opack value.
     *
     * @param object the object to be serialized
     * @return opack value
     * @throws SerializeException if a problem occurs during serializing; if this opacker is deserializing
     */
    public synchronized OpackValue serialize(Object object) throws SerializeException {
        if (this.state == State.DESERIALIZE)
            throw new SerializeException("Opacker is deserializing");

        int separatorStack = this.objectStack.getSize();
        OpackValue value = (OpackValue) this.prepareObjectSerialize(object.getClass(), object.getClass(), object);

        State lastState = this.state;
        try {
            this.state = State.SERIALIZE;
            this.executeSerializeStack(separatorStack);
        } finally {
            this.state = lastState;
        }

        return value;
    }

    /**
     * Store information needed for serialization in stacks.
     *
     * @param baseType     the class of object to be serialized
     * @param originalType the class of original object
     * @param object        the object to be serialized
     * @return prepared opack value
     * @throws SerializeException if a problem occurs during serializing; if the baseType cannot be compiled into {@link ClassInfo ClassInfo}
     */
    Object prepareObjectSerialize(Class<?> baseType, Class<?> originalType, Object object) throws SerializeException {
        if (baseType == null || originalType == null || object == null) {
            return null;
        }

        try {
            ClassInfo classInfo = this.infoCompiler.get(baseType);

            for (Transformer transformer : classInfo.getTransformers()) {
                object = transformer.serialize(this, object);
            }

            Class<?> objectType = object.getClass();

            /*
                Early stopping
             */
            if (OpackValue.isAllowType(objectType)) {
                /*
                    If directly pass opack value, deep clone
                 */
                if (OpackValue.class.isAssignableFrom(originalType)) {
                    if (object instanceof OpackValue) {
                        object = ((OpackValue) object).clone();
                    }
                }

                return object;
            }

            /*
                Enum converting
             */
            if (objectType.isEnum()) {
                if (this.convertEnumToOrdinal) {
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
                Optimize algorithm for big array
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

            this.objectStack.push(object);
            this.valueStack.push(opackValue);
            this.classInfoStack.push(classInfo);

            return opackValue;
        } catch (CompileException exception) {
            throw new SerializeException("Can't compile " + baseType.getName() + " class information", exception);
        }
    }

    /**
     * Serialize the elements of each opack value in the stack. (OpackObject: fields, OpackArray element : array elements)
     *
     * @throws SerializeException if a problem occurs during serializing; if the field in the class of instance to be serialized is not accessible
     */
    void executeSerializeStack(int endOfStack) throws SerializeException {
        while (this.objectStack.getSize() > endOfStack) {
            Object object = this.objectStack.pop();
            OpackValue opackValue = this.valueStack.pop();
            ClassInfo classInfo = this.classInfoStack.pop();

            if (opackValue instanceof OpackArray) {
                OpackArray<Object> opackArray = (OpackArray<Object>) opackValue;
                int length = Array.getLength(object);

                for (int index = 0; index < length; index++) {
                    Object element = ReflectionUtil.getArrayItem(object, index);
                    Class<?> elementType = element == null ? null : element.getClass();

                    Object serializedValue = this.prepareObjectSerialize(elementType, elementType, element);

                    opackArray.add(serializedValue);
                }
            } else if (opackValue instanceof OpackObject) {
                OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) opackValue;
                for (ClassInfo.FieldInfo fieldInfo : classInfo.getFields()) {
                    try {
                        Object element = fieldInfo.get(object);
                        Class<?> fieldType = fieldInfo.getType();
                        Class<?> originalType = element == null ? null : element.getClass();

                        if (fieldInfo.getTransformer() != null) {
                            element = fieldInfo.getTransformer().serialize(this, element);
                            fieldType = element.getClass();
                        }

                        Object serializedValue = this.prepareObjectSerialize(fieldType, originalType, element);
                        opackObject.put(fieldInfo.getName(), serializedValue);
                    } catch (IllegalAccessException exception) {
                        throw new SerializeException("Can't get " + fieldInfo.getName() + " field data in " + classInfo.getTargetClass().getSimpleName(), exception);
                    }
                }
            }
        }
    }

    /**
     * Deserializes the opack value to object of the target class.
     *
     * @param type the target class
     * @param opackValue  the opack value to be deserialized
     * @return deserialized object
     * @throws DeserializeException if a problem occurs during deserializing; if this opacker is serializing
     */
    public synchronized <T> T deserialize(Class<T> type, OpackValue opackValue) throws DeserializeException {
        if (this.state == State.SERIALIZE)
            throw new DeserializeException("Opacker is serializing");

        int separatorStack = this.objectStack.getSize();
        T value = type.cast(this.prepareObjectDeserialize(type, opackValue));

        State lastState = this.state;
        try {
            this.state = State.DESERIALIZE;
            this.executeDeserializeStack(separatorStack);
        } finally {
            this.state = lastState;
        }

        return value;
    }

    /**
     * Store information needed for deserialization in stacks.
     *
     * @param goalType the class of object to be deserialized
     * @param object    the object to be deserialized
     * @return prepared object
     * @throws DeserializeException if a problem occurs during deserializing
     */
    public synchronized Object prepareObjectDeserialize(Class<?> goalType, Object object) throws DeserializeException {
        if (goalType == null || object == null) {
            return null;
        }

        try {
            ClassInfo classInfo = this.infoCompiler.get(goalType);

            for (Transformer transformer : classInfo.getTransformers()) {
                object = transformer.deserialize(this, goalType, object);
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
                if (this.convertEnumToOrdinal) {
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
                        throw new DeserializeException("Can't convert OpackArray to native array", exception);
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
                        throw new DeserializeException("Target class is array. but, object is not OpackArray");
                    }
                } else {
                    if (object instanceof OpackObject) {
                        OpackObject<?, ?> opackObject = (OpackObject<?, ?>) object;

                        try {
                            targetObject = ReflectionUtil.createInstanceUnsafe(goalType);
                        } catch (InvocationTargetException | IllegalAccessException | InstantiationException exception) {
                            throw new DeserializeException("Can't create instance using unsafe method", exception);
                        }
                    } else {
                        throw new DeserializeException("Target class is object. but, object is not OpackObject");
                    }
                }

                this.objectStack.push(targetObject);
                this.valueStack.push(opackValue);
                this.classInfoStack.push(classInfo);

                return targetObject;
            } else if (object.getClass() == goalType) {
                return object;
            } else {
                throw new DeserializeException("Found object, stack corruption");
            }
        } catch (CompileException exception) {
            throw new DeserializeException("Can't compile " + goalType.getName() + " class information", exception);
        }
    }

    /**
     * Deserialize the elements of each opack value in the stack. (OpackObject: fields, OpackArray element : array elements)
     *
     * @throws DeserializeException if a problem occurs during deserializing; if the field in the class of instance to be deserialized is not accessible
     */
    void executeDeserializeStack(int endOfStack) throws DeserializeException {
        while (this.objectStack.getSize() > endOfStack) {
            Object object = this.objectStack.pop();
            OpackValue opackValue = this.valueStack.pop();
            ClassInfo classInfo = this.classInfoStack.pop();

            if (opackValue instanceof OpackArray) {
                OpackArray<Object> opackArray = (OpackArray<Object>) opackValue;
                Class<?> componentType = object.getClass().getComponentType();
                int length = opackArray.length();

                for (int index = 0; index < length; index++) {
                    Object element = opackArray.get(index);
                    Object deserializedValue = this.prepareObjectDeserialize(componentType, element);

                    ReflectionUtil.setArrayItem(object, index, deserializedValue == null ? null : ReflectionUtil.cast(componentType, deserializedValue));
                }
            } else if (opackValue instanceof OpackObject) {
                OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) opackValue;
                for (ClassInfo.FieldInfo fieldInfo : classInfo.getFields()) {
                    try {
                        Object element = opackObject.get(fieldInfo.getField().getName());
                        Class<?> fieldType = fieldInfo.getType();
                        Class<?> actualFieldType = fieldInfo.getField().getType();

                        if (fieldInfo.getTransformer() != null) {
                            element = fieldInfo.getTransformer().deserialize(this, fieldType, element);
                        }

                        Object deserializedValue = this.prepareObjectDeserialize(fieldType, element);

                        fieldInfo.set(object, deserializedValue == null ? null : ReflectionUtil.cast(actualFieldType, deserializedValue));
                    } catch (IllegalAccessException | IllegalArgumentException exception) {
                        throw new DeserializeException("Can't set " + fieldInfo.getName() + " field in " + classInfo.getTargetClass().getSimpleName(), exception);
                    }
                }
            }
        }
    }
}
