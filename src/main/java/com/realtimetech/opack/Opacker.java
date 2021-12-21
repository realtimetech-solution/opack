package com.realtimetech.opack;

import com.realtimetech.opack.annotation.ExplicitType;
import com.realtimetech.opack.compile.ClassInfo;
import com.realtimetech.opack.compile.InfoCompiler;
import com.realtimetech.opack.exception.*;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.impl.NoWrapListTransformer;
import com.realtimetech.opack.transformer.impl.WrapListTransformer;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.util.OpackArrayConverter;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Opacker {
    public static class Builder {
        int valueStackInitialSize;
        int contextStackInitialSize;

        boolean allowListTransformWithTypeWrap;

        public Builder() {
            this.valueStackInitialSize = 512;
            this.contextStackInitialSize = 128;
            this.allowListTransformWithTypeWrap = false;
        }

        public Builder setValueStackInitialSize(int valueStackInitialSize) {
            this.valueStackInitialSize = valueStackInitialSize;
            return this;
        }

        public Builder setContextStackInitialSize(int contextStackInitialSize) {
            this.contextStackInitialSize = contextStackInitialSize;
            return this;
        }

        public Builder setAllowListTransformWithTypeWrap(boolean allowListTransformWithTypeWrap) {
            this.allowListTransformWithTypeWrap = allowListTransformWithTypeWrap;
            return this;
        }

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

    @NotNull State state;

    Opacker(Builder builder) throws InstantiationException {
        this.infoCompiler = new InfoCompiler(this);

        this.objectStack = new FastStack<>(builder.contextStackInitialSize);
        this.classInfoStack = new FastStack<>(builder.contextStackInitialSize);
        this.valueStack = new FastStack<>(builder.valueStackInitialSize);

        this.state = State.NONE;

        if (builder.allowListTransformWithTypeWrap) {
            this.infoCompiler.registerPredefinedTransformer(List.class, WrapListTransformer.class, true);
        } else {
            this.infoCompiler.registerPredefinedTransformer(List.class, NoWrapListTransformer.class, true);
        }
    }

    public synchronized OpackValue serialize(Object object) throws SerializeException {
        if (this.state == State.DESERIALIZE)
            throw new SerializeException("Opacker is deserializing");

        OpackValue value = (OpackValue) this.prepareObjectSerialize(object.getClass(), object);

        if (this.state == State.NONE) {
            try {
                this.state = State.SERIALIZE;
                this.executeSerializeStack();
            } finally {
                this.state = State.NONE;
            }
        }

        return value;
    }

    Object prepareObjectSerialize(Class<?> baseClass, Object object) throws SerializeException {
        if (baseClass == null || object == null) {
            return null;
        }

        try {
            Class<?> firstObjectType = object.getClass();
            ClassInfo classInfo = this.infoCompiler.get(baseClass);

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
                if (OpackValue.class.isAssignableFrom(firstObjectType)) {
                    object = ((OpackValue) object).clone();
                }

                return object;
            }
            /*
                Optimize algorithm for big array
             */
            if (OpackArray.isAllowArrayType(objectType)) {
                int dimensions = ReflectionUtil.getArrayDimension(objectType);
                if (dimensions == 1) {
                    return OpackArray.createWithArrayObject(ReflectionUtil.cloneArray(object));
                }
            }


            OpackValue opackValue = null;

            if (objectType.isArray()) {
                opackValue = new OpackArray(Array.getLength(object));
            } else {
                opackValue = new OpackObject();
            }

            this.objectStack.push(object);
            this.valueStack.push(opackValue);
            this.classInfoStack.push(classInfo);

            return opackValue;
        } catch (CompileException exception) {
            throw new SerializeException("Can't compile " + baseClass.getName() + " class information", exception);
        }
    }

    void executeSerializeStack() throws SerializeException {
        while (!this.objectStack.isEmpty()) {
            Object object = this.objectStack.pop();
            OpackValue opackValue = this.valueStack.pop();
            ClassInfo classInfo = this.classInfoStack.pop();

            if (opackValue instanceof OpackArray) {
                OpackArray opackArray = (OpackArray) opackValue;
                int length = Array.getLength(object);

                for (int index = 0; index < length; index++) {
                    Object element = ReflectionUtil.getArrayItem(object, index);
                    Object serializedValue = this.prepareObjectSerialize(element.getClass(), element);

                    opackArray.add(serializedValue);
                }
            } else if (opackValue instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) opackValue;
                for (ClassInfo.FieldInfo fieldInfo : classInfo.getFields()) {
                    try {
                        Object element = fieldInfo.getField().get(object);
                        Class<?> fieldClass = fieldInfo.getTypeClass();

                        if (fieldInfo.getTransformer() != null) {
                            element = fieldInfo.getTransformer().serialize(this, element);
                            fieldClass = element.getClass();
                        }

                        Object serializedValue = this.prepareObjectSerialize(fieldClass, element);
                        opackObject.put(fieldInfo.getName(), serializedValue);
                    } catch (IllegalAccessException exception) {
                        throw new SerializeException("Can't get " + fieldInfo.getName() + " field data in " + classInfo.getTargetClass().getSimpleName(), exception);
                    }
                }
            }
        }
    }

    public synchronized <T> T deserialize(Class<T> targetClass, OpackValue opackValue) throws DeserializeException {
        if (this.state == State.SERIALIZE)
            throw new DeserializeException("Opacker is serializing");

        T value = (T) this.prepareObjectDeserialize(targetClass, opackValue);

        if (this.state == State.NONE) {
            try {
                this.state = State.DESERIALIZE;
                this.executeDeserializeStack();
            } finally {
                this.state = State.NONE;
            }
        }

        return value;
    }

    public synchronized Object prepareObjectDeserialize(Class<?> goalClass, Object object) throws DeserializeException {
        if (goalClass == null || object == null) {
            return null;
        }

        try {
            ClassInfo classInfo = this.infoCompiler.get(goalClass);

            for (Transformer transformer : classInfo.getTransformers()) {
                object = transformer.deserialize(this, goalClass, object);
            }

            /*
                Early stopping
             */
            if (OpackValue.isAllowType(goalClass)) {
                /*
                    If directly pass opack value, deep clone
                 */
                if (object instanceof OpackValue) {
                    object = ((OpackValue) object).clone();
                }
                return object;
            }

            /*
                Optimize algorithm for big array
             */
            if (OpackArray.isAllowArrayType(goalClass)) {
                int dimensions = ReflectionUtil.getArrayDimension(goalClass);

                if (dimensions == 1 && object instanceof OpackArray) {
                    OpackArray opackArray = (OpackArray) object;
                    Class<?> componentType = goalClass.getComponentType();

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

                if (goalClass.isArray()) {
                    if (object instanceof OpackArray) {
                        OpackArray opackArray = (OpackArray) object;

                        targetObject = Array.newInstance(goalClass.getComponentType(), opackArray.length());
                    } else {
                        throw new DeserializeException("Target class is array. but, object is not OpackArray");
                    }
                } else {
                    if (object instanceof OpackObject) {
                        OpackObject opackObject = (OpackObject) object;

                        try {
                            targetObject = ReflectionUtil.createInstanceUnsafe(goalClass);
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
            } else if (object.getClass() == goalClass) {
                return object;
            } else {
                throw new DeserializeException("Found object, stack corruption");
            }
        } catch (CompileException exception) {
            throw new DeserializeException("Can't compile " + goalClass.getName() + " class information", exception);
        }
    }

    void executeDeserializeStack() throws DeserializeException {
        while (!this.objectStack.isEmpty()) {
            Object object = this.objectStack.pop();
            OpackValue opackValue = this.valueStack.pop();
            ClassInfo classInfo = this.classInfoStack.pop();

            if (opackValue instanceof OpackArray) {
                OpackArray opackArray = (OpackArray) opackValue;
                Class<?> componentType = object.getClass().getComponentType();
                int length = opackArray.length();

                for (int index = 0; index < length; index++) {
                    Object element = opackArray.get(index);
                    Object deserializedValue = this.prepareObjectDeserialize(componentType, element);

                    ReflectionUtil.setArrayItem(object, index, ReflectionUtil.cast(componentType, deserializedValue));
                }
            } else if (opackValue instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) opackValue;
                for (ClassInfo.FieldInfo fieldInfo : classInfo.getFields()) {
                    try {
                        Object element = opackObject.get(fieldInfo.getField().getName());
                        Class<?> fieldClass = fieldInfo.getTypeClass();
                        Class<?> actualFieldClass = fieldInfo.getField().getType();

                        if (fieldInfo.getTransformer() != null) {
                            element = fieldInfo.getTransformer().deserialize(this, actualFieldClass, element);
                        }

                        Object deserializedValue = this.prepareObjectDeserialize(fieldClass, element);

                        fieldInfo.getField().set(object, ReflectionUtil.cast(actualFieldClass, deserializedValue));
                    } catch (IllegalAccessException exception) {
                        throw new DeserializeException("Can't set " + fieldInfo.getName() + " field in " + classInfo.getTargetClass().getSimpleName(), exception);
                    } catch (IllegalArgumentException exception) {
                        //Exception in thread "main" java.lang.IllegalArgumentException: Can not set static int field com.realtimetech.opack.util.ReflectionUtil.a to java.lang.Long
                        //	at java.base/jdk.internal.reflect.UnsafeFieldAccessorImpl.throwSetIllegalArgumentException(UnsafeFieldAccessorImpl.java:167)
                        //	at java.base/jdk.internal.reflect.UnsafeFieldAccessorImpl.throwSetIllegalArgumentException(UnsafeFieldAccessorImpl.java:171)
                        //	at java.base/jdk.internal.reflect.UnsafeStaticIntegerFieldAccessorImpl.set(UnsafeStaticIntegerFieldAccessorImpl.java:96)
                        throw new DeserializeException(exception);
                    }
                }
            }
        }
    }

    static class Test {
        @ExplicitType(type = ArrayList.class)
        private List<TinyTest> list;

        public Test() {
            this.list = new ArrayList<>();
        }

        public List<TinyTest> getList() {
            return list;
        }
    }
    static class TinyTest {
        private int a;

        public TinyTest(int a) {
            this.a = a;
        }

        public int getA() {
            return a;
        }
    }
}
