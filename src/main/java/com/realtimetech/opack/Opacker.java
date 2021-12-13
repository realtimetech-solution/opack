package com.realtimetech.opack;

import com.realtimetech.opack.annotation.ExplicitType;
import com.realtimetech.opack.annotation.Ignore;
import com.realtimetech.opack.annotation.Transform;
import com.realtimetech.opack.compile.ClassInfo;
import com.realtimetech.opack.compile.InfoCompiler;
import com.realtimetech.opack.exception.CompileException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.TransformerFactory;
import com.realtimetech.opack.util.FastStack;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.security.KeyPair;

public class Opacker {
    public class Builder {

    }

    final @NotNull InfoCompiler infoCompiler;

    final @NotNull FastStack<Object> objectStack;
    final @NotNull FastStack<OpackValue> valueStack;
    final @NotNull FastStack<ClassInfo> classInfoStack;

    Opacker() {
        this.infoCompiler = new InfoCompiler(this);

        this.objectStack = new FastStack<>();
        this.valueStack = new FastStack<>();
        this.classInfoStack = new FastStack<>();
    }

    public Object pushSomeThing(Object object) throws SerializeException {
        if (object == null) {
            return null;
        }

        Class<?> type = ReflectionUtil.getClass(object);

        if (type == String.class) {
            return object;
        }
        if (type == Integer.class) {
            return object;
        }

        try {

            ClassInfo classInfo = this.infoCompiler.get(object.getClass());

            if (classInfo.getTransformer() != null)
                object = classInfo.getTransformer().serialize(object);

            OpackValue opackValue = null;

            if (object.getClass().isArray()) {
                opackValue = new OpackArray(Array.getLength(object));
            } else {
                opackValue = new OpackObject();
            }

            this.objectStack.push(object);
            this.valueStack.push(opackValue);
            this.classInfoStack.push(classInfo);

            return opackValue;
        } catch (CompileException exception) {
            throw new SerializeException("PUT MESSAGE", exception);
        }
    }

    public OpackValue serialize(Object object) throws SerializeException {
        OpackValue value = (OpackValue) this.pushSomeThing(object);

        this.serialize();

        return value;
    }

    public void serialize() throws SerializeException {
        while (!this.objectStack.isEmpty()) {
            Object o = this.objectStack.pop();
            OpackValue v = this.valueStack.pop();
            ClassInfo i = this.classInfoStack.pop();

            if (v instanceof OpackArray) {
                OpackArray opackArray = (OpackArray) v;
                int length = Array.getLength(o);

                for (int index = 0; index < length; index++) {
                    Object element = Array.get(o, index);
                    Object serializedValue = this.pushSomeThing(element);

                    opackArray.set(index, serializedValue);
                }
            } else if (v instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) v;
                for (ClassInfo.FieldInfo fieldInfo : i.getFields()) {
                    try {
                        Object element = fieldInfo.getField().get(o);
                        Object serializedValue = this.pushSomeThing(element);

                        opackObject.put(fieldInfo.getField().getName(), serializedValue);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static class ExampleSub {
        public int value1;
    }

    public static class Example {
        public String value1;
        public ExampleSub value2;
        public String value3;
    }

    public static void main(String[] args) throws SerializeException {
        Opacker opacker = new Opacker();

        Example example = new Example();
        example.value1 = "Test";
        example.value2 = new ExampleSub();
        example.value2.value1 = 190;

        OpackValue opackValue = opacker.serialize(example);

        System.out.println(opackValue);
    }
}
