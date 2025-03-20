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
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.value.OpackArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;

public class LocalTimeTransformer implements Transformer {
    /**
     * Serialize specific value to opack value
     *
     * @param opacker      the opacker
     * @param originalType the original type
     * @param object       the object to be serialized
     * @return opack value
     */
    @Override
    public @Nullable Object serialize(@NotNull Opacker opacker, @NotNull Class<?> originalType, @Nullable Object object) {
        if (object instanceof LocalTime) {
            LocalTime localTime = (LocalTime) object;

            return OpackArray.createWithArrayObject(
                    new int[]{
                            localTime.getHour(),
                            localTime.getMinute(),
                            localTime.getSecond(),
                            localTime.getNano()
                    }
            );
        }

        return object;
    }

    /**
     * Deserialize opack value
     *
     * @param opacker  the opacker
     * @param goalType the goal type to deserialize
     * @param object   the object to be deserialized
     * @return deserialized value
     */
    @Override
    public @Nullable Object deserialize(@NotNull Opacker opacker, @NotNull Class<?> goalType, @Nullable Object object) {
        if (object instanceof OpackArray) {
            OpackArray opackArray = (OpackArray) object;

            if (opackArray.length() == 4) {
                return LocalTime.of(
                        opackArray.getAsInt(0),
                        opackArray.getAsInt(1),
                        opackArray.getAsInt(2),
                        opackArray.getAsInt(3)
                );
            }
        }

        return object;
    }
}
