package com.realtimetech.opack.util;

import org.jetbrains.annotations.NotNull;

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.*;
import java.util.*;

public class ReflectionUtil {
    private static abstract class Allocator {
        abstract <T> T allocate(Class<T> c) throws InvocationTargetException, IllegalAccessException;
    }

    static final Map<Class<?>, Class<?>> PRIMITIVES_WRAPPERS_MAP = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
        {
            put(boolean.class, Boolean.class);
            put(byte.class, Byte.class);
            put(char.class, Character.class);
            put(double.class, Double.class);
            put(float.class, Float.class);
            put(int.class, Integer.class);
            put(long.class, Long.class);
            put(short.class, Short.class);
            put(void.class, Void.class);
        }
    });
    static final Map<Class<?>, Class<?>> WRAPPERS_PRIMITIVES_MAP = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
        {
            for (Class<?> primitiveType : PRIMITIVES_WRAPPERS_MAP.keySet()) {
                Class<?> wrapperType = PRIMITIVES_WRAPPERS_MAP.get(primitiveType);

                put(wrapperType, primitiveType);
            }
        }
    });

    static final Allocator ALLOCATOR;

    static {
        ALLOCATOR = ReflectionUtil.createAvailableAllocator();
        if (ALLOCATOR == null) {
            throw new ExceptionInInitializerError("This virtual machine doesn't support unsafe allocator.");
        }
    }

    static Allocator createAvailableAllocator() {
        // for JVM
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);

            final Object unsafeObject = unsafeField.get(null);
            final Method allocateMethod = unsafeClass.getMethod("allocateInstance", Class.class);
            return new Allocator() {
                @Override
                public <T> T allocate(Class<T> c) throws InvocationTargetException, IllegalAccessException {
                    return (T) allocateMethod.invoke(unsafeObject, c);
                }
            };
        } catch (Exception exception) {
        }

        // for DalvikVM (> 2.3)
        try {
            final Method getterMethod = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
            getterMethod.setAccessible(true);

            final int constructorId = (Integer) getterMethod.invoke(null, Object.class);
            final Method allocateMethod = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, int.class);
            allocateMethod.setAccessible(true);
            return new Allocator() {
                @Override
                public <T> T allocate(Class<T> c) throws InvocationTargetException, IllegalAccessException {
                    return (T) allocateMethod.invoke(null, c, constructorId);
                }
            };
        } catch (Exception exception) {
        }

        // for DalvikVM (< 2.3)
        try {
            final Method allocateMethod = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
            allocateMethod.setAccessible(true);
            return new Allocator() {
                @Override
                public <T> T allocate(Class<T> c) throws InvocationTargetException, IllegalAccessException {
                    return (T) allocateMethod.invoke(null, c, Object.class);
                }
            };
        } catch (Exception exception) {
        }

        return null;
    }

    public static Object cloneArray(Object object) {
        if (!object.getClass().isArray()) {
            throw new IllegalArgumentException(object + " is not array object.");
        }

        int length = Array.getLength(object);
        Object newArray = Array.newInstance(object.getClass().getComponentType(), length);
        System.arraycopy(object, 0, newArray, 0, length);
        return newArray;
    }


    static void addAccessibleFields(List<Field> fieldList, Class<?> clazz) {
        Class<?> superClass = clazz.getSuperclass();

        if (superClass != null && superClass != Object.class) {
            ReflectionUtil.addAccessibleFields(fieldList, clazz.getSuperclass());
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (!fieldList.contains(field) && !Modifier.isStatic(field.getModifiers())) {
                fieldList.add(field);
            }
        }

    }

    public static Field[] getAccessibleFields(Class<?> clazz) {
        List<Field> fields = new LinkedList<>();
        ReflectionUtil.addAccessibleFields(fields, clazz);

        return fields.toArray(new Field[fields.size()]);
    }

    public static int getArrayDimension(Class<?> clazz) {
        if (!clazz.isArray()) {
            throw new IllegalArgumentException(clazz + " is not array class.");
        }

        int count = 0;

        while (true) {
            Class<?> type = clazz.getComponentType();

            if (type == null) {
                break;
            } else {
                count++;
                clazz = type;
            }
        }

        return count;
    }

    public static Class<?> getArrayLastComponentType(Class<?> clazz) {
        if (!clazz.isArray()) {
            throw new IllegalArgumentException(clazz + " is not array class.");
        }

        Class<?> last = clazz;

        while (true) {
            Class<?> type = last.getComponentType();

            if (type == null) {
                break;
            } else {
                last = type;
            }
        }

        return last;
    }

    public static @NotNull boolean isWrapperClass(Class<?> clazz) {
        return WRAPPERS_PRIMITIVES_MAP.containsKey(clazz);
    }

    public static @NotNull Class<?> getPrimitiveClassOfWrapperClass(@NotNull Class<?> wrapperClass) {
        Class<?> primitiveClass = WRAPPERS_PRIMITIVES_MAP.getOrDefault(wrapperClass, null);

        if (primitiveClass == null) {
            throw new IllegalArgumentException(wrapperClass + " is not primitive class.");
        }

        return primitiveClass;
    }

    public static @NotNull boolean isPrimitiveClass(Class<?> clazz) {
        return PRIMITIVES_WRAPPERS_MAP.containsKey(clazz);
    }

    public static @NotNull Class<?> getWrapperClassOfPrimitiveClass(@NotNull Class<?> primitiveClass) {
        Class<?> wrapperClass = PRIMITIVES_WRAPPERS_MAP.getOrDefault(primitiveClass, null);

        if (wrapperClass == null) {
            throw new IllegalArgumentException(primitiveClass + " is not primitive class.");
        }

        return wrapperClass;
    }

    public static boolean checkClassCastable(@NotNull Class<?> fromClass, @NotNull Class<?> toClass) {
        if (fromClass.isPrimitive()) {
            fromClass = ReflectionUtil.getWrapperClassOfPrimitiveClass(fromClass);
        }

        if (toClass.isPrimitive()) {
            toClass = ReflectionUtil.getWrapperClassOfPrimitiveClass(toClass);
        }

        return toClass.isAssignableFrom(fromClass);
    }

    public static <T> @NotNull T createInstanceUnsafe(@NotNull Class<T> clazz) throws InvocationTargetException, IllegalAccessException {
        int modifiers = clazz.getModifiers();

        if (Modifier.isInterface(modifiers)) {
            throw new IllegalArgumentException("Interface can't be instantiated, got " + clazz.getSimpleName() + " interface.");
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new IllegalArgumentException("Abstract class can't be instantiated, got " + clazz.getSimpleName() + " abstract class.");
        }

        return ReflectionUtil.ALLOCATOR.allocate(clazz);
    }

    public static <T> @NotNull T createInstance(@NotNull Class<T> clazz, Object @NotNull ... objects) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int modifiers = clazz.getModifiers();

        if (Modifier.isInterface(modifiers)) {
            throw new IllegalArgumentException("Interface can't be instantiated, got " + clazz.getSimpleName() + " interface.");
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new IllegalArgumentException("Abstract class can't be instantiated, got " + clazz.getSimpleName() + " abstract class.");
        }

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Class<?>[] types = new Class[objects.length];

        for (int index = 0; index < objects.length; index++) {
            types[index] = objects[index].getClass();
        }

        for (Constructor<?> constructor : constructors) {
            if (constructor.getDeclaringClass() == clazz) {
                int parameterCount = constructor.getParameterCount();
                Class<?>[] parameterTypes = constructor.getParameterTypes();

                if (parameterCount == objects.length) {
                    boolean matched = true;

                    for (int index = 0; index < parameterCount; index++) {
                        if (!ReflectionUtil.checkClassCastable(types[index], parameterTypes[index])) {
                            matched = false;
                            break;
                        }
                    }

                    if (matched) {
                        constructor.setAccessible(true);

                        return (T) constructor.newInstance(objects);
                    }
                }
            }
        }

        throw new IllegalArgumentException(clazz.getSimpleName() + " class doesn't have matched constructor.");
    }
}