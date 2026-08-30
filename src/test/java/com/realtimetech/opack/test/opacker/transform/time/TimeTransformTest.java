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

import java.util.Calendar;
import java.util.Date;

public class TimeTransformTest {
    @SuppressWarnings("ALL")
    public static class TimeTransformClass {
        private Date date;
        private Calendar calendar;

        public TimeTransformClass() {
            this.date = new Date();

            this.calendar = Calendar.getInstance();
            this.calendar.setTime(new Date());
        }
    }

    @SuppressWarnings("ALL")
    public static class TimeTransformWithFormatClass {
        @TimeFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
        private Date date;
        @TimeFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
        private Calendar calendar;

        public TimeTransformWithFormatClass() {
            this.date = new Date();

            this.calendar = Calendar.getInstance();
            this.calendar.setTime(new Date());
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = Opacker.Builder.create().build();
        TimeTransformClass originalObject = new TimeTransformClass();

        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        TimeTransformClass deserialized = opacker.deserialize(TimeTransformClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }

    @Test
    public void test_with_format() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = Opacker.Builder.create().build();
        TimeTransformWithFormatClass originalObject = new TimeTransformWithFormatClass();

        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        TimeTransformWithFormatClass deserialized = opacker.deserialize(TimeTransformWithFormatClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
