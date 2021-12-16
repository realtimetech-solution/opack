package com.realtimetech.opack.util;

import com.realtimetech.opack.util.structure.PinnedList;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackValue;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class OpackArrayConverter {
    static final Method OPACK_ARRAY_GETTER_METHOD;

    static {
        Method method = null;
        try {
            method = OpackValue.class.getDeclaredMethod("get");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError("Not found getter method in OpackArray");
        }
        OPACK_ARRAY_GETTER_METHOD = method;
        OPACK_ARRAY_GETTER_METHOD.setAccessible(true);
    }

    public static Object convertToArray(Class<?> componentType, OpackArray opackArray) throws InvocationTargetException, IllegalAccessException {
        if (!OpackValue.isAllowType(componentType)) {
            throw new IllegalArgumentException(componentType + " type is not allowed");
        }

        List<?> list = (List<?>) OPACK_ARRAY_GETTER_METHOD.invoke(opackArray);

        if (list instanceof PinnedList) {
            /*
                Optimize code for pinned list
             */
            Object object = ((PinnedList<?>) list).getArrayObject();
            Class<?> arrayType = object.getClass();

            if (arrayType.getComponentType() != componentType) {
                throw new IllegalArgumentException("Array component type is " + arrayType + " but got " + componentType + " type");
            }

            int length = opackArray.length();
            Object dest = Array.newInstance(componentType, length);

            System.arraycopy(object, 0, dest, 0, opackArray.length());

            return dest;
        }

        Object array = Array.newInstance(componentType, opackArray.length());
        for (int i = 0; i < opackArray.length(); i++) {
            Object object = opackArray.get(i);

            if (object instanceof OpackValue) {
                object = ((OpackValue) object).clone();
            }

            ReflectionUtil.setArrayItem(array, i, object);
        }

        return array;
    }
}
