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

import com.realtimetech.opack.util.structure.PrimitiveList;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackValue;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class OpackArrayConverter {
    static final Method OPACK_ARRAY_GETTER_METHOD;

    static {
        Method method;
        try {
            method = Class.forName("com.realtimetech.opack.value.AbstractOpackValue").getDeclaredMethod("get");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No getter method found in OpackArray");
        }
        OPACK_ARRAY_GETTER_METHOD = method;
        OPACK_ARRAY_GETTER_METHOD.setAccessible(true);
    }

    /**
     * Returns the underlying list of opack array.
     *
     * @param opackArray the opack array to be targeted
     * @return the underlying list
     * @throws InvocationTargetException if exception occurs during invoke opack array getter method
     * @throws IllegalAccessException if the getter method object in opack array is enforcing Java language access control and cannot access that method
     */
    public static List<?> getOpackArrayList(OpackArray<?> opackArray) throws InvocationTargetException, IllegalAccessException {
        return (List<?>) OPACK_ARRAY_GETTER_METHOD.invoke(opackArray);
    }

    /**
     * Convert the opack array to array.
     *
     * @param componentType the component type of array
     * @param opackArray the opack array to convert
     * @return the converted array
     * @throws InvocationTargetException if exception occurs during invoke opack array getter method
     * @throws IllegalAccessException if the getter method object in opack array is enforcing Java language access control and cannot access that method.
     * @throws IllegalArgumentException if component type is now allowed or invalid
     */
    public static Object convertToArray(Class<?> componentType, OpackArray<?> opackArray) throws InvocationTargetException, IllegalAccessException {
        if (!OpackValue.isAllowClass(componentType)) {
            throw new IllegalArgumentException(componentType + " type is not allowed");
        }

        List<?> list = (List<?>) OPACK_ARRAY_GETTER_METHOD.invoke(opackArray);

        if (list instanceof PrimitiveList) {
            /*
                Optimize code for pinned list
             */
            Object object = ((PrimitiveList) list).getArrayObject();
            Class<?> arrayType = object.getClass();

            if(!arrayType.isArray()){
                throw new IllegalArgumentException("PrimitiveList array object is not array type");
            }

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

            ReflectionUtil.setArrayItem(array, i, ReflectionUtil.cast(componentType, object));
        }

        return array;
    }
}
