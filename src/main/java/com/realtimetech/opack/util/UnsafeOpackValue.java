/*
 * Copyright (C) 2022 REALTIMETECH All Rights Reserved
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

import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class UnsafeOpackValue {
    private static final @NotNull Method OPACK_GETTER_METHOD;

    static {
        Method method;

        try {
            method = Class.forName("com.realtimetech.opack.value.AbstractOpackValue").getDeclaredMethod("get");
        } catch (NoSuchMethodException | ClassNotFoundException exception) {
            throw new ExceptionInInitializerError("No getter method found in OpackValue.");
        }

        OPACK_GETTER_METHOD = method;
        OPACK_GETTER_METHOD.setAccessible(true);

        // Test to avoid runtime exception
        OpackArray opackArray = new OpackArray();
        OpackObject opackObject = new OpackObject();

        try {
            OPACK_GETTER_METHOD.invoke(opackArray);
            OPACK_GETTER_METHOD.invoke(opackObject);
        } catch (Throwable throwable) {
            throw new ExceptionInInitializerError("This virtual machine doesn't support invoke opack value getter method.");
        }
    }

    /**
     * Get the native list of an opack array
     *
     * @param opackArray the opack array
     * @return the native list
     */
    public static List<Object> getList(@NotNull OpackArray opackArray) {
        try {
            //noinspection unchecked
            return (List<Object>) OPACK_GETTER_METHOD.invoke(opackArray);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            // Not reachable
            throw new RuntimeException(exception);
        }
    }

    /**
     * Get the native map of an opack object
     *
     * @param opackObject the opack object
     * @return the native map
     */
    public static Map<Object, Object> getMap(@NotNull OpackObject opackObject) {
        try {
            //noinspection unchecked
            return (Map<Object, Object>) OPACK_GETTER_METHOD.invoke(opackObject);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            // Not reachable
            throw new RuntimeException(exception);
        }
    }
}
