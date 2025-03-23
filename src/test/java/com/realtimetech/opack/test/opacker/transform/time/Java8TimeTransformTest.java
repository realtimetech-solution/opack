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

package com.realtimetech.opack.test.opacker.transform.time;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.transformer.impl.time.annotation.TimeFormat;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.time.*;

public class Java8TimeTransformTest {
    @SuppressWarnings("ALL")
    public static class Java8TimeTransformClass {
        private LocalDate localDate;
        private LocalTime localTime;
        private LocalDateTime localDateTime;

        private OffsetTime offsetTime;
        private OffsetDateTime offsetDateTime;
        private ZonedDateTime zonedDateTime;

        public Java8TimeTransformClass() {
            this.localDate = LocalDate.now();
            this.localTime = LocalTime.now();
            this.localDateTime = LocalDateTime.now();

            this.offsetTime = OffsetTime.now();
            this.offsetDateTime = OffsetDateTime.now();
            this.zonedDateTime = ZonedDateTime.now();
        }
    }

    @SuppressWarnings("ALL")
    public static class Java8TimeTransformWithFormatClass {
        @TimeFormat("yyyy-MM-dd")
        private LocalDate localDate;
        @TimeFormat("HH:mm:ss.SSSSSSSSS")
        private LocalTime localTime;
        @TimeFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
        private LocalDateTime localDateTime;

        @TimeFormat("HH:mm:ss.SSSSSSSSSXXXZ")
        private OffsetTime offsetTime;
        @TimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXXZ")
        private OffsetDateTime offsetDateTime;
        @TimeFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXXZ")
        private ZonedDateTime zonedDateTime;

        public Java8TimeTransformWithFormatClass() {
            this.localDate = LocalDate.now();
            this.localTime = LocalTime.now();
            this.localDateTime = LocalDateTime.now();

            this.offsetTime = OffsetTime.now();
            this.offsetDateTime = OffsetDateTime.now();
            this.zonedDateTime = ZonedDateTime.now();
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = Opacker.Builder.create().build();
        Java8TimeTransformClass originalObject = new Java8TimeTransformClass();

        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        Java8TimeTransformClass deserialized = opacker.deserialize(Java8TimeTransformClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }

    @Test
    public void test_with_format() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = Opacker.Builder.create().build();
        Java8TimeTransformWithFormatClass originalObject = new Java8TimeTransformWithFormatClass();

        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        Java8TimeTransformWithFormatClass deserialized = opacker.deserialize(Java8TimeTransformWithFormatClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
