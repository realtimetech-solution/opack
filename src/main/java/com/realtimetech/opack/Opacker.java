package com.realtimetech.opack;

import com.realtimetech.opack.compile.ClassInfo;
import com.realtimetech.opack.compile.InfoCompiler;
import com.realtimetech.opack.exception.CompileException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.util.FastStack;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

public class Opacker {
    public class Builder {

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

    public synchronized OpackValue executeSerializeStack(Object object) throws SerializeException {
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

            /*
                Optimize algorithm for big array
             */
            if (OpackArray.isAllowArrayType(clazz)) {
                int dimensions = ReflectionUtil.getArrayDimension(clazz);
                if (dimensions == 1) {
                    return new OpackArray(ReflectionUtil.cloneArray(object));
                }

                OpackArray opackArray = new OpackArray(Array.getLength(object));

                this.objectStack.push(object);
                this.valueStack.push(opackArray);
                this.classInfoStack.push(classInfo);

                return opackArray;
            }

            if (OpackValue.isAllowType(clazz)) {
                return object;
            }

            for (Transformer transformer : classInfo.getTransformers()) {
                object = transformer.serialize(this, object);
            }

            Class<?> objectType = object.getClass();
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
                    Object element = Array.get(object, index);
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
                        throw new SerializeException("Cant get " + fieldInfo.getName() + " field data in " + classInfo.getTargetClass().getSimpleName(), exception);
                    }
                }
            }
        }
    }

    public synchronized Object pushSomeThingDe(Class<?> targetClass, Object object) throws SerializeException {
        if (object == null) {
            return null;
        }

        Class<?> type = object.getClass();

        if (type == String.class) {
            return object;
        }
        if (type == Integer.class) {
            return object;
        }
        if (type == Byte.class) {
            return object;
        }

        try {
            if (targetClass == type) {
                return object;
            }

            ClassInfo classInfo = this.infoCompiler.get(targetClass);

            for (Transformer transformer : classInfo.getTransformers()) {
                object = transformer.deserialize(this, object);
            }

            if (object instanceof OpackValue) {
                OpackValue opackValue = (OpackValue) object;

                if (targetClass.isArray()) {
                    if (object instanceof OpackArray) {
                        OpackArray opackArray = (OpackArray) object;
                        int length = opackArray.length();
                        object = Array.newInstance(targetClass.getComponentType(), length);
                    } else {
                        throw new Error("??");
                    }
                } else {
                    if (object instanceof OpackObject) {
                        OpackObject opackObject = (OpackObject) object;
                        object = ReflectionUtil.createInstanceUnsafe(targetClass);
                    } else {
                        throw new Error("??");
                    }
                }
                this.objectStack.push(object);
                this.valueStack.push(opackValue);
                this.classInfoStack.push(classInfo);
            } else {
                throw new Error("??");
            }


            return object;
        } catch (CompileException | InvocationTargetException | IllegalAccessException exception) {
            throw new SerializeException("PUT MESSAGE", exception);
        }

    }

    public synchronized <T> T derialize(Class<T> targetClass, OpackValue opackValue) throws SerializeException {
        Object o = pushSomeThingDe(targetClass, opackValue);

        derialize();

        return (T) o;
    }

    private void derialize() throws SerializeException {
        while (!this.objectStack.isEmpty()) {
            Object o = this.objectStack.pop();
            OpackValue v = this.valueStack.pop();
            ClassInfo i = this.classInfoStack.pop();

            if (v instanceof OpackArray) {
                OpackArray opackArray = (OpackArray) v;
                int length = opackArray.length();

                for (int index = 0; index < length; index++) {
                    Object element = opackArray.get(index);
                    Object deserializedValue = this.pushSomeThingDe(i.getTargetClass().getComponentType(), element);

                    Array.set(o, index, deserializedValue);
                }
            } else if (v instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) v;
                for (ClassInfo.FieldInfo fieldInfo : i.getFields()) {
                    Object element = opackObject.get(fieldInfo.getField().getName());
                    Object serializedValue = this.pushSomeThingDe(fieldInfo.getExplicitType() == null ? fieldInfo.getField().getType() : fieldInfo.getExplicitType(), element);

                    try {
                        fieldInfo.getField().set(o, serializedValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public static void main(String[] args) throws SerializeException, InterruptedException {
        Opacker opacker = new Opacker();

        Example example = new Example();
        OpackValue opackValue = opacker.executeSerializeStack(example);
        OpackObject opackObject = (OpackObject) opackValue;

    }
}
