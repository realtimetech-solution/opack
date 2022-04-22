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

import com.realtimetech.opack.util.structure.NativeList;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackValue;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class OpackArrayConverter {
    /**
     * Convert the opack array to array.
     *
     * @param componentType the component type of array
     * @param opackArray    the opack array to convert
     * @return the converted array
     * @throws InvocationTargetException if exception occurs during invoke opack array getter method
     * @throws IllegalAccessException    if the getter method object in opack array is enforcing Java language access control and cannot access that method.
     * @throws IllegalArgumentException  if component type is now allowed or invalid
     */
    public static Object convertToArray(Class<?> componentType, OpackArray<?> opackArray) throws InvocationTargetException, IllegalAccessException {
        if (!OpackValue.isAllowType(componentType)) {
            throw new IllegalArgumentException(componentType + " type is not allowed");
        }

        List<?> list = UnsafeOpackValue.getList(opackArray);

        if (list instanceof NativeList) {
            /*
                Optimize code for pinned list
             */
            Object object = ((NativeList) list).getArrayObject();
            Class<?> arrayType = object.getClass();

            if (!arrayType.isArray()) {
                throw new IllegalArgumentException("NativeList array object is not array type");
            }

            if (arrayType.getComponentType() != componentType) {
                throw new IllegalArgumentException("Array component type is " + arrayType + " but got " + componentType + " type");
            }

            int length = opackArray.length();
            Object dest = Array.newInstance(componentType, length);

            System.arraycopy(object, 0, dest, 0, opackArray.length());

            return dest;
        }

        if (ReflectionUtil.isPrimitiveType(componentType)) {
            /*
                Optimize code for primitive array
             */

            if (componentType == boolean.class) {
                boolean[] array = new boolean[list.size()];
                int index = 0;

                for (Object object : list) {
                    array[index] = (boolean) ReflectionUtil.cast(componentType, object);

                    index++;
                }

                return array;
            } else if (componentType == byte.class) {
                byte[] array = new byte[list.size()];
                int index = 0;

                for (Object object : list) {
                    array[index] = ((Number) object).byteValue();

                    index++;
                }

                return array;
            } else if (componentType == char.class) {
                char[] array = new char[list.size()];
                int index = 0;

                for (Object object : list) {
                    array[index] = (char) ReflectionUtil.cast(componentType, object);

                    index++;
                }

                return array;
            } else if (componentType == short.class) {
                short[] array = new short[list.size()];
                int index = 0;

                for (Object object : list) {
                    array[index] = ((Number) object).shortValue();

                    index++;
                }

                return array;
            } else if (componentType == int.class) {
                int[] array = new int[list.size()];
                int index = 0;

                for (Object object : list) {
                    array[index] = ((Number) object).intValue();

                    index++;
                }

                return array;
            } else if (componentType == float.class) {
                float[] array = new float[list.size()];
                int index = 0;

                for (Object object : list) {
                    array[index] = ((Number) object).floatValue();

                    index++;
                }

                return array;
            } else if (componentType == long.class) {
                long[] array = new long[list.size()];
                int index = 0;

                for (Object object : list) {
                    array[index] = ((Number) object).longValue();

                    index++;
                }

                return array;
            } else if (componentType == double.class) {
                double[] array = new double[list.size()];
                int index = 0;

                for (Object object : list) {
                    array[index] = ((Number) object).doubleValue();

                    index++;
                }

                return array;
            }
        }

        Object array = Array.newInstance(componentType, opackArray.length());
        Object[] wrapperArray = (Object[]) array;
        int index = 0;

        for (Object object : list) {
            if (object != null) {
                if (object instanceof OpackValue) {
                    object = ((OpackValue) object).clone();
                }

                wrapperArray[index] = ReflectionUtil.cast(componentType, object);
            }

            index++;
        }

        return array;
    }
}