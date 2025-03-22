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

package com.realtimetech.opack.transformer.impl.list;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.impl.DataStructureTransformer;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ListTransformer extends DataStructureTransformer {
    /**
     * Serialize specific value to opack value
     *
     * @param context      the opacker context
     * @param originalType the original type
     * @param object       the object to be serialized
     * @return the opack value
     * @throws SerializeException if a problem occurs during serializing
     */
    @Override
    public @Nullable Object serialize(@NotNull Opacker.Context context, @NotNull Class<?> originalType, @Nullable Object object) throws SerializeException {
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            OpackArray opackArray = new OpackArray(list.size());

            for (Object element : list) {
                opackArray.add(this.serializeObject(context, element));
            }

            return opackArray;
        }

        return object;
    }

    /**
     * Deserialize opack value
     *
     * @param context  the opacker context
     * @param goalType the goal type to deserialize
     * @param object   the object to be deserialized
     * @return the deserialized value
     * @throws DeserializeException if a problem occurs during deserializing
     */
    @Override
    public @Nullable Object deserialize(@NotNull Opacker.Context context, @NotNull Class<?> goalType, @Nullable Object object) throws DeserializeException {
        if (object instanceof OpackArray) {
            OpackArray opackArray = (OpackArray) object;

            if (List.class.isAssignableFrom(goalType)) {
                try {
                    //noinspection unchecked
                    List<Object> list = (List<Object>) ReflectionUtil.createInstance(goalType);
                    Class<?> genericType = null;

                    if (context.getCurrentFieldProperty() != null) {
                        Class<?>[] genericTypes = context.getCurrentFieldProperty().getGenericTypes();
                        genericType = genericTypes.length > 0 ? genericTypes[0] : null;
                    }

                    for (int index = 0; index < opackArray.length(); index++) {
                        Object element = opackArray.get(index);

                        list.add(this.deserializeObject(context, genericType, element));
                    }

                    return list;
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                    throw new DeserializeException(exception);
                }
            }
        }

        return object;
    }

    /**
     * Serializes the element to {@link OpackValue OpackValue}
     *
     * @param context the opacker context
     * @param element the element to be serialized
     * @return the serialized value
     * @throws SerializeException if a problem occurs during serializing
     */
    @Override
    protected @Nullable Object serializeObject(@NotNull Opacker.Context context, @Nullable Object element) throws SerializeException {
        if (element != null && !OpackValue.isAllowType(element.getClass())) {
            return context.getOpacker().serializeObject(element);
        }

        return element;
    }

    /**
     * Deserializes the {@link OpackValue OpackValue}
     *
     * @param context     the opacker
     * @param genericType the generic type of data structure to deserialize an object
     * @param element     the element to be deserialized
     * @return the deserialized element
     * @throws DeserializeException if a problem occurs during deserializing
     */
    @Override
    protected @Nullable Object deserializeObject(@NotNull Opacker.Context context, @Nullable Class<?> genericType, @Nullable Object element) throws DeserializeException {
        if (genericType != null && element != null) {
            return context.getOpacker().deserializeObject(genericType, element);
        }

        return element;
    }
}