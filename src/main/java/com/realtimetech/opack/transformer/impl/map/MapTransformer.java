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

package com.realtimetech.opack.transformer.impl.map;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.impl.DataStructureTransformer;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class MapTransformer extends DataStructureTransformer {
    /**
     * Serialize specific value to opack value.
     *
     * @param opacker      the opacker
     * @param originalType the original type
     * @param object       the object to be serialized
     * @return opack value
     * @throws SerializeException if a problem occurs during serializing
     */
    @Override
    public @Nullable Object serialize(@NotNull Opacker opacker, @NotNull Class<?> originalType, @Nullable Object object) throws SerializeException {
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            OpackObject<Object, Object> opackObject = new OpackObject<>(map.size());

            for (Object element : map.keySet()) {
                Object keyObject = element;
                Object valueObject = map.get(element);

                keyObject = serializeObject(opacker, keyObject);
                valueObject = serializeObject(opacker, valueObject);

                opackObject.put(keyObject, valueObject);
            }

            return opackObject;
        }

        return object;
    }

    /**
     * Deserialize opack value.
     *
     * @param opacker  the opacker
     * @param goalType the goal type to deserialize
     * @param object   the object to be deserialized
     * @return deserialized value
     * @throws DeserializeException if a problem occurs during deserializing
     */
    @Override
    public @Nullable Object deserialize(@NotNull Opacker opacker, @NotNull Class<?> goalType, @Nullable Object object) throws DeserializeException {
        if (object instanceof OpackObject) {
            OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) object;
            if (Map.class.isAssignableFrom(goalType)) {
                try {
                    Map<Object, Object> map = (Map<Object, Object>) ReflectionUtil.createInstance(goalType);

                    for (Map.Entry<Object, Object> element : opackObject.entrySet()) {
                        Object keyObject = element.getKey();
                        Object valueObject = element.getValue();

                        keyObject = this.deserializeObject(opacker, keyObject);
                        valueObject = this.deserializeObject(opacker, valueObject);

                        map.put(keyObject, valueObject);
                    }

                    return map;
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                         ClassNotFoundException exception) {
                    throw new DeserializeException(exception);
                }
            }
        }

        return object;
    }

    /**
     * Serializes the element to {@link OpackValue OpackValue}.
     *
     * @param opacker the opacker
     * @param element the element to be serialized
     * @return serialized value
     * @throws SerializeException if a problem occurs during serializing
     */
    @Override
    protected @Nullable Object serializeObject(@NotNull Opacker opacker, @Nullable Object element) throws SerializeException {
        if (element != null && !OpackValue.isAllowType(element.getClass())) {
            return opacker.serialize(element);
        }

        return element;
    }

    /**
     * Deserializes the {@link OpackValue OpackValue}.
     *
     * @param opacker the opacker
     * @param element the element to be deserialized
     * @return deserialized element
     * @throws ClassNotFoundException if the class cannot be located
     * @throws DeserializeException   if a problem occurs during deserializing
     */
    @Override
    protected @Nullable Object deserializeObject(@NotNull Opacker opacker, @Nullable Object element) throws ClassNotFoundException, DeserializeException {
        return element;
    }
}