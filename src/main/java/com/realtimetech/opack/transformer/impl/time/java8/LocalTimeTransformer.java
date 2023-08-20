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

package com.realtimetech.opack.transformer.impl.time.java8;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.value.OpackArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;

public class LocalTimeTransformer implements Transformer {
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
        if (value instanceof LocalTime) {
            LocalTime localTime = (LocalTime) value;

            return OpackArray.createWithArrayObject(
                    new int[]{
                            localTime.getHour(),
                            localTime.getMinute(),
                            localTime.getSecond(),
                            localTime.getNano()
                    }
            );
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
        if (value instanceof OpackArray) {
            OpackArray<Integer> opackArray = (OpackArray<Integer>) value;

            if (opackArray.length() == 4) {
                return LocalTime.of(
                        opackArray.get(0),
                        opackArray.get(1),
                        opackArray.get(2),
                        opackArray.get(3)
                );
            }
        }

        return value;
    }
}
