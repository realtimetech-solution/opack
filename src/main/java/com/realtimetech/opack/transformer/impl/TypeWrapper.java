/*
 * Copyright (C) 2023 REALTIMETECH All Rights Reserved
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

package com.realtimetech.opack.transformer.impl;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

public class TypeWrapper {
    /**
     * Wrap object into opack object with object type
     *
     * @param context the opacker context
     * @param object  the object to be serialized
     * @return the wrapped object
     * @throws SerializeException if a problem occurs during serializing
     */
    public static @Nullable Object wrapObject(@NotNull Opacker.Context context, @Nullable Object object) throws SerializeException {
        if (object == null) {
            return null;
        }

        if (OpackValue.isAllowType(object.getClass())) {
            return object;
        }

        OpackObject opackObject = new OpackObject();

        if (object.getClass().isArray()) {
            OpackArray opackArray = new OpackArray();
            int length = Array.getLength(object);

            for (int index = 0; index < length; index++) {
                Object element = ReflectionUtil.getArrayItem(object, index);
                Object wrappedObject = TypeWrapper.wrapObject(context, element);

                opackArray.add(wrappedObject);
            }

            opackObject.put("value", opackArray);
        } else {
            Object serializedObject = context.getOpacker().serializeObject(object);

            opackObject.put("value", serializedObject);
        }

        opackObject.put("type", object.getClass().getName());

        return opackObject;
    }

    /**
     * Unwrap opack object into object using a proper object type
     *
     * @param context the opacker context
     * @param object  the object to be unwrapped
     * @return the unwrapped object
     * @throws DeserializeException if a problem occurs during deserializing
     */
    public static @Nullable Object unwrapObject(@NotNull Opacker.Context context, @Nullable Object object) throws DeserializeException {
        if (object == null) {
            return null;
        }

        if (!(object instanceof OpackObject)) {
            return object;
        }

        OpackObject opackObject = (OpackObject) object;

        if (!opackObject.containsKey("type") || !opackObject.containsKey("value")) {
            throw new DeserializeException("Not exists properties in wrapped opack object.");
        }

        Object type = opackObject.get("type");
        Object value = opackObject.get("value");

        if (!(type instanceof String)) {
            throw new DeserializeException("Expected string as a `type` in wrapped object but " + type.getClass().getName() + ".");
        }

        try {
            Class<?> objectType = Class.forName((String) type, true, context.getOpacker().getClassLoader());

            if (objectType.isArray()) {
                Class<?> componentType = objectType.getComponentType();

                if (!(value instanceof OpackArray)) {
                    throw new DeserializeException("Expected opack array as a `value` in wrapped object but " + value.getClass().getName() + ".");
                }

                OpackArray opackArray = (OpackArray) value;
                Object arrayObject = Array.newInstance(componentType, opackArray.length());

                for (int index = 0; index < opackArray.length(); index++) {
                    Object element = opackArray.get(index);

                    if (element != null) {
                        element = TypeWrapper.unwrapObject(context, element);
                    }

                    ReflectionUtil.setArrayItem(arrayObject, index, element);
                }

                return arrayObject;
            } else {
                return context.getOpacker().deserializeObject(objectType, value);
            }
        } catch (ClassNotFoundException classNotFoundException) {
            throw new DeserializeException(classNotFoundException);
        }
    }
}
