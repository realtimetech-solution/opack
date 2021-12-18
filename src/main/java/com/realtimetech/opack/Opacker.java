package com.realtimetech.opack;

import com.realtimetech.opack.codec.json.JsonCodec;
import com.realtimetech.opack.compile.ClassInfo;
import com.realtimetech.opack.compile.InfoCompiler;
import com.realtimetech.opack.example.Example;
import com.realtimetech.opack.exception.*;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.util.OpackArrayConverter;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;

public class Opacker {
    public static class Builder {

    }

    public enum State {
        NONE, SERIALIZE, DESERIALIZE
    }

    final @NotNull InfoCompiler infoCompiler;

    final @NotNull FastStack<Object> objectStack;
    final @NotNull FastStack<OpackValue> valueStack;
    final @NotNull FastStack<ClassInfo> classInfoStack;

    @NotNull State state;

    Opacker() {
        this.infoCompiler = new InfoCompiler(this);

        this.objectStack = new FastStack<>();
        this.valueStack = new FastStack<>();
        this.classInfoStack = new FastStack<>();

        this.state = State.NONE;
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

    Object prepareObjectSerialize(Class<?> clazz, Object object) throws SerializeException {
        if (clazz == null || object == null) {
            return null;
        }

        try {
            ClassInfo classInfo = this.infoCompiler.get(clazz);

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
                if (object instanceof OpackValue) {
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
            throw new SerializeException("Can't compile " + clazz.getName() + " class information", exception);
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
                        Class<?> type = fieldInfo.getType();

                        if (fieldInfo.getTransformer() != null) {
                            element = fieldInfo.getTransformer().serialize(this, element);
                            type = element.getClass();
                        }

                        Object serializedValue = this.prepareObjectSerialize(type, element);

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

    public synchronized Object prepareObjectDeserialize(Class<?> clazz, Object object) throws DeserializeException {
        if (clazz == null || object == null) {
            return null;
        }

        try {
            ClassInfo classInfo = this.infoCompiler.get(clazz);

            for (Transformer transformer : classInfo.getTransformers()) {
                object = transformer.deserialize(this, object);
            }

            /*
                Early stopping
             */
            if (OpackValue.isAllowType(clazz)) {
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
            if (OpackArray.isAllowArrayType(clazz)) {
                int dimensions = ReflectionUtil.getArrayDimension(clazz);

                if (dimensions == 1 && object instanceof OpackArray) {
                    OpackArray opackArray = (OpackArray) object;
                    Class<?> componentType = clazz.getComponentType();

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

                if (clazz.isArray()) {
                    if (object instanceof OpackArray) {
                        OpackArray opackArray = (OpackArray) object;

                        targetObject = Array.newInstance(clazz.getComponentType(), opackArray.length());
                    } else {
                        throw new DeserializeException("Target class is array. but, object is not OpackArray");
                    }
                } else {
                    if (object instanceof OpackObject) {
                        OpackObject opackObject = (OpackObject) object;

                        try {
                            targetObject = ReflectionUtil.createInstanceUnsafe(clazz);
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
            } else if (object.getClass() == clazz) {
                return object;
            } else {
                throw new DeserializeException("Found object, stack corruption");
            }
        } catch (CompileException exception) {
            throw new DeserializeException("Can't compile " + clazz.getName() + " class information", exception);
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
                        Class<?> type = fieldInfo.getType();
                        Class<?> fieldType = fieldInfo.getField().getType();

                        if (fieldInfo.getTransformer() != null) {
                            element = fieldInfo.getTransformer().deserialize(this, element);
                        }

                        Object deserializedValue = this.prepareObjectDeserialize(type, element);

                        fieldInfo.getField().set(object, ReflectionUtil.cast(fieldType, deserializedValue));
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


    public static void main(String[] args) throws SerializeException, DeserializeException, IllegalAccessException, InterruptedException, IOException, EncodeException, DecodeException {
//                Thread.sleep(1024 * 12);

        Opacker opacker = new Opacker();
        JsonCodec jsonCodec = new JsonCodec.Builder().create();
        Example originalExample = new Example();
        long exampleSize = opacker.serialize(originalExample).toString().length();

        String jsonString = jsonCodec.encode(opacker.serialize(originalExample));
        long size = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            OpackValue serializedExample = opacker.serialize(originalExample);
            size += exampleSize;
            OpackValue decodedValue = jsonCodec.decode(jsonString);
            Example deserializedExample = opacker.deserialize(Example.class, serializedExample);
        }
        long end = System.currentTimeMillis();
        float speed = (float)size / (float)(end - start);
        System.out.println(((speed * 1000) / 1024 / 1024) + "mb/s");
//        Example originalExample = new Example();
//        OpackValue serializedExample = opacker.serialize(originalExample);
//        String jsonString = jsonCodec.encode(serializedExample);
//        OpackValue decodedValue = jsonCodec.decode(jsonString);
//        Example deserializedExample = opacker.deserialize(Example.class, decodedValue);
//
//        String bool = originalExample.validationObject(deserializedExample);
//        if (bool != null)
//            System.out.println("Wrong " + bool);

    }
}
