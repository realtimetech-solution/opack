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

            put(short.class, Short.class);

            put(int.class, Integer.class);
            put(float.class, Float.class);

            put(double.class, Double.class);
            put(long.class, Long.class);
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

    public static Object cast(Class<?> typeClass, Object object) {
        if (ReflectionUtil.isPrimitiveClass(typeClass)) {
            typeClass = ReflectionUtil.getWrapperClassOfPrimitiveClass(typeClass);
        }
        if (ReflectionUtil.isWrapperClass(typeClass)) {
            Class<?> objectType = object.getClass();

            if (objectType == Long.class) {
                Long value = (Long) object;

                if (typeClass == Integer.class) {
                    return value.intValue();
                } else if (typeClass == Short.class) {
                    return value.shortValue();
                } else if (typeClass == Character.class) {
                    return (char) value.intValue();
                } else if (typeClass == Byte.class) {
                    return value.byteValue();
                }
            } else if (objectType == Integer.class) {
                Integer value = (Integer) object;

                if (typeClass == Short.class) {
                    return value.shortValue();
                } else if (typeClass == Character.class) {
                    return (char) value.intValue();
                } else if (typeClass == Byte.class) {
                    return value.byteValue();
                }
            } else if (objectType == Short.class) {
                Short value = (Short) object;

                if (typeClass == Character.class) {
                    return (char) value.intValue();
                } else if (typeClass == Byte.class) {
                    return value.byteValue();
                }
            } else if (objectType == Character.class) {
                Character value = (Character) object;

                if (typeClass == Byte.class) {
                    return (byte) value.charValue();
                }
            } else if (objectType == Double.class) {
                Double value = (Double) object;

                if (typeClass == Float.class) {
                    return value.floatValue();
                }
            }

            return object;
        }

        return typeClass.cast(object);
    }

    public static Object getArrayItem(Object array, int index) {
        Class<?> c = array.getClass();

        if (int[].class == c) {
            return ((int[]) array)[index];
        } else if (float[].class == c) {
            return ((float[]) array)[index];
        } else if (boolean[].class == c) {
            return ((boolean[]) array)[index];
        } else if (char[].class == c) {
            return ((char[]) array)[index];
        } else if (double[].class == c) {
            return ((double[]) array)[index];
        } else if (long[].class == c) {
            return ((long[]) array)[index];
        } else if (short[].class == c) {
            return ((short[]) array)[index];
        } else if (byte[].class == c) {
            return ((byte[]) array)[index];
        }

        return ((Object[]) array)[index];
    }

    public static void setArrayItem(Object array, int index, Object value) {
        Class<?> c = array.getClass();

        if (int[].class == c) {
            ((int[]) array)[index] = (int) value;
        } else if (float[].class == c) {
            ((float[]) array)[index] = (float) value;
        } else if (boolean[].class == c) {
            ((boolean[]) array)[index] = (boolean) value;
        } else if (char[].class == c) {
            ((char[]) array)[index] = (char) value;
        } else if (double[].class == c) {
            ((double[]) array)[index] = (double) value;
        } else if (long[].class == c) {
            ((long[]) array)[index] = (long) value;
        } else if (short[].class == c) {
            ((short[]) array)[index] = (short) value;
        } else if (byte[].class == c) {
            ((byte[]) array)[index] = (byte) value;
        } else {
            ((Object[]) array)[index] = value;
        }
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

    static void addAccessibleFields(List<Field> fieldList, Class<?> targetClass) {
        Class<?> superClass = targetClass.getSuperclass();

        if (superClass != null && superClass != Object.class) {
            ReflectionUtil.addAccessibleFields(fieldList, targetClass.getSuperclass());
        }

        for (Field field : targetClass.getDeclaredFields()) {
            if (!fieldList.contains(field) && !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                fieldList.add(field);
            }
        }
    }

    public static Field[] getAccessibleFields(Class<?> targetClass) {
        List<Field> fields = new LinkedList<>();
        ReflectionUtil.addAccessibleFields(fields, targetClass);

        return fields.toArray(new Field[fields.size()]);
    }

    public static int getArrayDimension(Class<?> typeClass) {
        if (!typeClass.isArray()) {
            throw new IllegalArgumentException(typeClass + " is not array class.");
        }

        int count = 0;

        while (true) {
            Class<?> componentClass = typeClass.getComponentType();

            if (componentClass == null) {
                break;
            } else {
                count++;
                typeClass = componentClass;
            }
        }

        return count;
    }

    public static Class<?> getArrayLastComponentType(Class<?> typeClass) {
        if (!typeClass.isArray()) {
            throw new IllegalArgumentException(typeClass + " is not array class.");
        }

        Class<?> lastClass = typeClass;

        while (true) {
            Class<?> componentClass = lastClass.getComponentType();

            if (componentClass == null) {
                break;
            } else {
                lastClass = componentClass;
            }
        }

        return lastClass;
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

    public static <T> @NotNull T createInstanceUnsafe(@NotNull Class<T> instanceClass) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        int modifiers = instanceClass.getModifiers();

        if (Modifier.isInterface(modifiers)) {
            throw new InstantiationException("Interface can't be instantiated, got " + instanceClass.getSimpleName() + " interface.");
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new InstantiationException("Abstract class can't be instantiated, got " + instanceClass.getSimpleName() + " abstract class.");
        }

        return ReflectionUtil.ALLOCATOR.allocate(instanceClass);
    }

    public static <T> @NotNull T createInstance(@NotNull Class<T> instanceClass, Object @NotNull ... objects) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int modifiers = instanceClass.getModifiers();

        if (Modifier.isInterface(modifiers)) {
            throw new InstantiationException("Interface can't be instantiated, got " + instanceClass.getSimpleName() + " interface.");
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new InstantiationException("Abstract class can't be instantiated, got " + instanceClass.getSimpleName() + " abstract class.");
        }

        Constructor<?>[] constructors = instanceClass.getDeclaredConstructors();
        Class<?>[] classes = new Class[objects.length];

        for (int index = 0; index < objects.length; index++) {
            classes[index] = objects[index].getClass();
        }

        for (Constructor<?> constructor : constructors) {
            if (constructor.getDeclaringClass() == instanceClass) {
                int parameterCount = constructor.getParameterCount();
                Class<?>[] parameterTypes = constructor.getParameterTypes();

                if (parameterCount == objects.length) {
                    boolean matched = true;

                    for (int index = 0; index < parameterCount; index++) {
                        if (!ReflectionUtil.checkClassCastable(classes[index], parameterTypes[index])) {
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

        throw new IllegalArgumentException(instanceClass.getSimpleName() + " class doesn't have matched constructor.");
    }
}