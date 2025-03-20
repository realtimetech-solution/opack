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

package com.realtimetech.opack.test.opacker.single;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class WrapperTest {
    static final Random RANDOM = new Random();

    @SuppressWarnings("ALL")
    public static class WrapperClass {
        private Boolean booleanValue;
        private Byte byteValue;

        private Character characterValue;
        private Short shortValue;

        private Integer integerValue;
        private Float floatValue;

        private Double doubleValue;
        private Long longValue;

        private Boolean booleanNullValue;
        private Byte byteNullValue;

        private Character characterNullValue;
        private Short shortNullValue;

        private Integer integerNullValue;
        private Float floatNullValue;
        private Double doubleNullValue;
        private Long longNullValue;

        public WrapperClass() {
            this.booleanValue = RANDOM.nextBoolean();
            this.byteValue = (byte) RANDOM.nextInt();
            this.characterValue = (char) RANDOM.nextInt();
            this.shortValue = (short) RANDOM.nextInt();
            this.integerValue = RANDOM.nextInt();
            this.floatValue = RANDOM.nextFloat();
            this.doubleValue = RANDOM.nextDouble();
            this.longValue = RANDOM.nextLong();

            this.booleanNullValue = null;
            this.byteNullValue = null;
            this.characterNullValue = null;
            this.shortNullValue = null;
            this.integerNullValue = null;
            this.floatNullValue = null;
            this.doubleNullValue = null;
            this.longNullValue = null;
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        WrapperClass originalObject = new WrapperClass();

        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        WrapperClass deserialized = opacker.deserialize(WrapperClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
