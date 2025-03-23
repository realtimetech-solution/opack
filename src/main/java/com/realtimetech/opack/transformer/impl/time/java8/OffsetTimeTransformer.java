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
import com.realtimetech.opack.transformer.impl.time.annotation.TimeFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class OffsetTimeTransformer implements Transformer {
    /**
     * Serialize specific value to opack value
     *
     * @param context      the opacker context
     * @param originalType the original type
     * @param object       the object to be serialized
     * @return the opack value
     */
    @Override
    public @Nullable Object serialize(@NotNull Opacker.Context context, @NotNull Class<?> originalType, @Nullable Object object) {
        if (object instanceof OffsetTime) {
            OffsetTime offsetTime = (OffsetTime) object;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_TIME;

            if (context.getFieldProperty() != null) {
                TimeFormat timeFormat = context.getFieldProperty().getAnnotation(TimeFormat.class);

                if (timeFormat != null) {
                    dateTimeFormatter = DateTimeFormatter.ofPattern(timeFormat.value());
                }
            }

            return offsetTime.format(dateTimeFormatter);
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
     */
    @Override
    public @Nullable Object deserialize(@NotNull Opacker.Context context, @NotNull Class<?> goalType, @Nullable Object object) {
        if (object instanceof String) {
            String string = (String) object;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_TIME;

            if (context.getFieldProperty() != null) {
                TimeFormat timeFormat = context.getFieldProperty().getAnnotation(TimeFormat.class);

                if (timeFormat != null) {
                    dateTimeFormatter = DateTimeFormatter.ofPattern(timeFormat.value());
                }
            }

            return OffsetTime.parse(string, dateTimeFormatter);
        }

        return object;
    }
}
