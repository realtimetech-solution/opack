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

package com.realtimetech.opack.transformer.impl;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TypeWrapTransformer implements Transformer {
    /**
     * Serialize specific value to opack value.
     *
     * @param opacker      the opacker
     * @param originalType the original type
     * @param value        the value to be serialized
     * @return opack value
     * @throws SerializeException if a problem occurs during serializing
     */
    @Override
    public @Nullable Object serialize(@NotNull Opacker opacker, @NotNull Class<?> originalType, @Nullable Object value) throws SerializeException {
        if (value != null) {
            OpackValue opackValue = opacker.serialize(value);
            OpackObject<Object, Object> opackObject = new OpackObject<>();

            opackObject.put("type", value.getClass().getName());
            opackObject.put("value", opackValue);

            return opackObject;
        }

        return value;
    }

    /**
     * Deserialize opack value.
     *
     * @param opacker  the opacker
     * @param goalType the goal type to deserialize
     * @param value    the opack value to be deserialized
     * @return deserialized value
     * @throws DeserializeException if a problem occurs during deserializing
     */
    @Override
    public @Nullable Object deserialize(@NotNull Opacker opacker, @NotNull Class<?> goalType, @Nullable Object value) throws DeserializeException {
        if (value instanceof OpackObject) {
            OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) value;

            if (opackObject.containsKey("type") && opackObject.containsKey("value")) {
                String type = (String) opackObject.get("type");
                Object opackValue = opackObject.get("value");

                try {
                    Class<?> objectClass = Class.forName(type);

                    if (opackValue instanceof OpackValue) {
                        return opacker.deserialize(objectClass, (OpackValue) value);
                    }
                } catch (ClassNotFoundException classNotFoundException) {
                    throw new DeserializeException(classNotFoundException);
                }
            }
        }

        return value;
    }
}
