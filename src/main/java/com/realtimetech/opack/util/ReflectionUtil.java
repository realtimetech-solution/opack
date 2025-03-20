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

package com.realtimetech.opack.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.*;
import java.util.LinkedList;
import java.util.List;

public class ReflectionUtil {
    private static abstract class Allocator {
        /**
         * Creates new instance for specific class
         *
         * @param typeClass the class to create
         * @return the created instance
         * @throws InvocationTargetException if exception occurs in invoked underlying method
         * @throws IllegalAccessException    if this Method object is enforcing Java language access control and the underlying method is inaccessible
         */
        abstract <T> @NotNull T allocate(@NotNull Class<T> typeClass) throws InvocationTargetException, IllegalAccessException;
    }

    private static final @NotNull Allocator ALLOCATOR;

    static {
        Allocator allocator = ReflectionUtil.createAvailableAllocator();

        if (allocator == null) {
            throw new ExceptionInInitializerError("This virtual machine doesn't support unsafe allocator.");
        }

        ALLOCATOR = allocator;
    }

    /**
     * Creates a new unsafe allocator for JVM, DalvikVM
     *
     * @return the created unsafe allocator
     */
    private static @Nullable Allocator createAvailableAllocator() {
        // for JVM
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);

            final Object unsafeObject = unsafeField.get(null);
            final Method allocateMethod = unsafeClass.getMethod("allocateInstance", Class.class);
            return new Allocator() {
                @Override
                public <T> @NotNull T allocate(@NotNull Class<T> typeClass) throws InvocationTargetException, IllegalAccessException {
                    return typeClass.cast(allocateMethod.invoke(unsafeObject, typeClass));
                }
            };
        } catch (Exception exception) {
            // Ignore exception
        }

        // for DalvikVM (> 2.3)
        try {
            //noinspection JavaReflectionMemberAccess
            final Method getterMethod = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
            getterMethod.setAccessible(true);

            final int constructorId = (Integer) getterMethod.invoke(null, Object.class);
            //noinspection JavaReflectionMemberAccess
            final Method allocateMethod = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, int.class);
            allocateMethod.setAccessible(true);
            return new Allocator() {
                @Override
                public <T> @NotNull T allocate(@NotNull Class<T> typeClass) throws InvocationTargetException, IllegalAccessException {
                    return typeClass.cast(allocateMethod.invoke(null, typeClass, constructorId));
                }
            };
        } catch (Exception exception) {
            // Ignore exception
        }

        // for DalvikVM (< 2.3)
        try {
            //noinspection JavaReflectionMemberAccess
            final Method allocateMethod = ObjectInputStream.class.getDeclaredMethod("newInstance", Class.class, Class.class);
            allocateMethod.setAccessible(true);
            return new Allocator() {
                @Override
                public <T> @NotNull T allocate(@NotNull Class<T> typeClass) throws InvocationTargetException, IllegalAccessException {
                    return typeClass.cast(allocateMethod.invoke(null, typeClass, Object.class));
                }
            };
        } catch (Exception exception) {
            // Ignore exception
        }

        return null;
    }

    /**
     * Casts an object to the specific class
     *
     * @param type   the class to cast
     * @param object the object to be cast
     * @return the object after casting
     */
    public static @NotNull Object cast(@NotNull Class<?> type, @NotNull Object object) {
        Class<?> objectType = object.getClass();

        if (Number.class.isAssignableFrom(objectType)) {
            Number number = (Number) object;

            if (type == byte.class || type == Byte.class) {
                return number.byteValue();
            } else if (type == char.class || type == Character.class) {
                return (char) number.intValue();
            } else if (type == short.class || type == Short.class) {
                return number.shortValue();
            } else if (type == int.class || type == Integer.class) {
                return number.intValue();
            } else if (type == float.class || type == Float.class) {
                return number.floatValue();
            } else if (type == double.class || type == Double.class) {
                return number.doubleValue();
            } else if (type == long.class || type == Long.class) {
                return number.longValue();
            }
        } else if (objectType == Boolean.class) {
            if (type == boolean.class || type == Boolean.class) {
                return object;
            }
        } else if (objectType == Character.class) {
            if (type == char.class || type == Character.class) {
                return object;
            }
        }

        return type.cast(object);
    }

    /**
     * Returns the value of the indexed component in the specified array object
     *
     * @param array the array object
     * @param index the index
     * @return the value of the indexed component in the specified array
     */
    public static @Nullable Object getArrayItem(@NotNull Object array, int index) {
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

    /**
     * Sets the value of the specified index in the given array object
     *
     * @param array the target array object whose indexed component will be set
     * @param index the index of the array element to set
     * @param value the value to be set at the specified index; can be null for
     *              arrays of reference types
     * @throws IllegalArgumentException       if the provided object is not an array
     * @throws ArrayIndexOutOfBoundsException if the index is out of bounds for the
     *                                        given array
     * @throws ClassCastException             if the value's type is incompatible with the
     *                                        component type of the array
     * @throws NullPointerException           if the array passed is null
     */
    public static void setArrayItem(@NotNull Object array, int index, @Nullable Object value) {
        Class<?> arrayType = array.getClass();

        if (value != null) {
            if (int[].class == arrayType) {
                ((int[]) array)[index] = (int) value;
            } else if (float[].class == arrayType) {
                ((float[]) array)[index] = (float) value;
            } else if (boolean[].class == arrayType) {
                ((boolean[]) array)[index] = (boolean) value;
            } else if (char[].class == arrayType) {
                ((char[]) array)[index] = (char) value;
            } else if (double[].class == arrayType) {
                ((double[]) array)[index] = (double) value;
            } else if (long[].class == arrayType) {
                ((long[]) array)[index] = (long) value;
            } else if (short[].class == arrayType) {
                ((short[]) array)[index] = (short) value;
            } else if (byte[].class == arrayType) {
                ((byte[]) array)[index] = (byte) value;
            } else {
                ((Object[]) array)[index] = value;
            }
        } else {
            ((Object[]) array)[index] = null;
        }
    }

    /**
     * Clones the array object
     *
     * @param array the object to clone
     * @return the cloned array object
     * @throws IllegalArgumentException if the object is not an array object
     */
    public static @NotNull Object cloneArray(@NotNull Object array) {
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException(array + " is not array object.");
        }

        int length = Array.getLength(array);
        Object newArray = Array.newInstance(array.getClass().getComponentType(), length);
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(array, 0, newArray, 0, length);

        return newArray;
    }

    /**
     * Add accessible fields of the target Class to the field List
     *
     * @param fieldList the field list to be added
     * @param type      the target class
     */
    static void addAccessibleFields(@NotNull List<Field> fieldList, @NotNull Class<?> type) {
        Class<?> superClass = type.getSuperclass();

        if (superClass != null && superClass != Object.class) {
            ReflectionUtil.addAccessibleFields(fieldList, type.getSuperclass());
        }

        for (Field field : type.getDeclaredFields()) {
            if (!fieldList.contains(field) && !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                fieldList.add(field);
            }
        }
    }

    /**
     * Returns accessible fields of the target class
     *
     * @param type the target class
     * @return the accessible fields
     */
    public static @NotNull Field @NotNull [] getAccessibleFields(@NotNull Class<?> type) {
        List<Field> fields = new LinkedList<>();
        ReflectionUtil.addAccessibleFields(fields, type);

        return fields.toArray(new Field[0]);
    }

    /**
     * Returns the dimension of the array through the class object of the array object
     *
     * @param arrayType the class of the target array object
     * @return the dimension
     * @throws IllegalArgumentException if the class is not the class of the array object
     */
    public static int getArrayDimension(@NotNull Class<?> arrayType) {
        if (!arrayType.isArray()) {
            throw new IllegalArgumentException(arrayType + " is not array class.");
        }

        int count = 0;

        while (true) {
            Class<?> componentClass = arrayType.getComponentType();

            if (componentClass == null) {
                break;
            } else {
                count++;
                arrayType = componentClass;
            }
        }

        return count;
    }

    /**
     * Returns the underlying component type of the array through the class object of the array object
     *
     * @param arrayType the class of the array object
     * @return the component type
     */
    public static @NotNull Class<?> getArrayLastComponentType(@NotNull Class<?> arrayType) {
        if (!arrayType.isArray()) {
            throw new IllegalArgumentException(arrayType + " is not array class.");
        }

        Class<?> lastClass = arrayType;

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

    /**
     * @param type the target class
     * @return whether the target class is a wrapper class
     */
    public static boolean isWrapperType(@NotNull Class<?> type) {
        return type == Boolean.class ||

                type == Byte.class ||
                type == Character.class ||

                type == Short.class ||
                type == Integer.class ||

                type == Float.class ||
                type == Double.class ||

                type == Long.class;
    }

    /**
     * Returns the primitive class corresponding to the wrapper class
     *
     * @param type the wrapper class
     * @return the primitive class
     * @throws IllegalArgumentException if target class is not wrapper class
     */
    public static @NotNull Class<?> convertWrapperClassToPrimitiveClass(@NotNull Class<?> type) {
        if (type == Boolean.class) {
            return boolean.class;
        } else if (type == Byte.class) {
            return byte.class;
        } else if (type == Character.class) {
            return char.class;
        } else if (type == Short.class) {
            return short.class;
        } else if (type == Integer.class) {
            return int.class;
        } else if (type == Float.class) {
            return float.class;
        } else if (type == Double.class) {
            return double.class;
        } else if (type == Long.class) {
            return long.class;
        }

        throw new IllegalArgumentException(type + " is not wrapper class.");
    }

    /**
     * @param type the target class
     * @return whether the target class is a primitive class
     */
    public static boolean isPrimitiveType(@NotNull Class<?> type) {
        return type == boolean.class ||

                type == byte.class ||
                type == char.class ||

                type == short.class ||
                type == int.class ||

                type == float.class ||
                type == double.class ||

                type == long.class;
    }

    /**
     * Returns the wrapper class corresponding to the primitive class
     *
     * @param type the primitive class
     * @return the wrapper class
     * @throws IllegalArgumentException if target class is not primitive class
     */
    public static @NotNull Class<?> convertPrimitiveTypeToWrapperType(@NotNull Class<?> type) {
        if (type == boolean.class) {
            return Boolean.class;
        } else if (type == byte.class) {
            return Byte.class;
        } else if (type == char.class) {
            return Character.class;
        } else if (type == short.class) {
            return Short.class;
        } else if (type == int.class) {
            return Integer.class;
        } else if (type == float.class) {
            return Float.class;
        } else if (type == double.class) {
            return Double.class;
        } else if (type == long.class) {
            return Long.class;
        }

        throw new IllegalArgumentException(type + " is not primitive class.");
    }

    /**
     * Returns whether casting from the source class to the destination class is possible
     *
     * @param fromType the source class
     * @param toType   the destination class
     * @return true if the source type can be safely cast to the target type, otherwise false
     */
    public static boolean checkCastable(@NotNull Class<?> fromType, @NotNull Class<?> toType) {
        if (fromType.isPrimitive()) {
            fromType = ReflectionUtil.convertPrimitiveTypeToWrapperType(fromType);
        }

        if (toType.isPrimitive()) {
            toType = ReflectionUtil.convertPrimitiveTypeToWrapperType(toType);
        }

        return toType.isAssignableFrom(fromType);
    }

    /**
     * Creates a new instance of the class with unsafe allocator
     *
     * @param <T>          the type of the object to be instantiated
     * @param instanceType the class to create instance
     * @return the created instance
     * @throws InvocationTargetException if exception occurs in invoked underlying method
     * @throws IllegalAccessException    if the method object is enforcing Java language access control and the underlying method is inaccessible
     * @throws InstantiationException    if the class object represents an abstract class, an interface
     */
    public static <T> @NotNull T createInstanceUnsafe(@NotNull Class<T> instanceType) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        int modifiers = instanceType.getModifiers();

        if (Modifier.isInterface(modifiers)) {
            throw new InstantiationException("Interface can't be instantiated, got " + instanceType.getSimpleName() + " interface.");
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new InstantiationException("Abstract class can't be instantiated, got " + instanceType.getSimpleName() + " abstract class.");
        }

        return ReflectionUtil.ALLOCATOR.allocate(instanceType);
    }

    /**
     * Creates a new instance of the class through constructor
     *
     * @param <T>          the type of the object to be instantiated
     * @param instanceType the class to create instance
     * @param objects      the argument of the constructor
     * @return the created instance
     * @throws IllegalArgumentException  if the class doesn't have a matched constructor
     * @throws InvocationTargetException if exception occurs in invoked underlying method
     * @throws IllegalAccessException    if the method object is enforcing Java language access control and the underlying method is inaccessible
     * @throws InstantiationException    if the class object represents an abstract class, an interface
     */
    public static <T> @NotNull T createInstance(@NotNull Class<T> instanceType, Object @NotNull ... objects) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int modifiers = instanceType.getModifiers();

        if (Modifier.isInterface(modifiers)) {
            throw new InstantiationException("Interface can't be instantiated, got " + instanceType.getSimpleName() + " interface.");
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new InstantiationException("Abstract class can't be instantiated, got " + instanceType.getSimpleName() + " abstract class.");
        }

        Constructor<?>[] constructors = instanceType.getDeclaredConstructors();
        Class<?>[] classes = new Class[objects.length];

        for (int index = 0; index < objects.length; index++) {
            classes[index] = objects[index].getClass();
        }

        for (Constructor<?> constructor : constructors) {
            if (constructor.getDeclaringClass() == instanceType) {
                int parameterCount = constructor.getParameterCount();
                Class<?>[] parameterTypes = constructor.getParameterTypes();

                if (parameterCount == objects.length) {
                    boolean matched = true;

                    for (int index = 0; index < parameterCount; index++) {
                        if (!ReflectionUtil.checkCastable(classes[index], parameterTypes[index])) {
                            matched = false;
                            break;
                        }
                    }

                    if (matched) {
                        constructor.setAccessible(true);

                        return instanceType.cast(constructor.newInstance(objects));
                    }
                }
            }
        }

        throw new IllegalArgumentException(instanceType.getSimpleName() + " class doesn't have matched constructor.");
    }
}