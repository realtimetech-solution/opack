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

package com.realtimetech.opack.transformer.impl.time;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.capture.CapturedType;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.impl.time.annotation.TimeFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarTransformer implements Transformer {
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
        if (object instanceof Calendar) {
            Calendar calendar = (Calendar) object;
            CapturedType.FieldProperty fieldProperty = context.getFieldProperty();

            if (fieldProperty != null) {
                TimeFormat timeFormat = fieldProperty.getField().getAnnotation(TimeFormat.class);

                if (timeFormat != null) {
                    return new SimpleDateFormat(timeFormat.value()).format(calendar.getTime());
                }
            }

            return calendar.getTime().getTime();
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
    public @Nullable Object deserialize(@NotNull Opacker.Context context, @NotNull Class<?> goalType, @Nullable Object object) throws DeserializeException {
        if (object instanceof String) {
            Calendar calendar = Calendar.getInstance();
            CapturedType.FieldProperty fieldProperty = context.getFieldProperty();

            if (fieldProperty != null) {
                TimeFormat timeFormat = fieldProperty.getField().getAnnotation(TimeFormat.class);

                if (timeFormat != null) {
                    try {
                        calendar.setTime(new SimpleDateFormat(timeFormat.value()).parse((String) object));
                    } catch (ParseException parseException) {
                        throw new DeserializeException(parseException);
                    }

                    return calendar;
                }
            }
        } else if (object instanceof Long) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date((Long) object));

            return calendar;
        }

        return object;
    }
}
