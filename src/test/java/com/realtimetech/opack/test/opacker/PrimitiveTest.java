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

package com.realtimetech.opack.test.opacker;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class PrimitiveTest {
    static final Random RANDOM = new Random();

    static class PrimitiveClass {
        private boolean booleanValue;

        private byte byteValue;
        private char charValue;

        private short shortValue;

        private int intValue;
        private float floatValue;

        private double doubleValue;
        private long longValue;

        public PrimitiveClass() {
            this.booleanValue = RANDOM.nextBoolean();

            this.byteValue = (byte) RANDOM.nextInt();
            this.charValue = (char) RANDOM.nextInt();

            this.shortValue = (short) RANDOM.nextInt();

            this.intValue = RANDOM.nextInt();
            this.floatValue = RANDOM.nextFloat();

            this.doubleValue = RANDOM.nextDouble();
            this.longValue = RANDOM.nextLong();
        }

        public boolean getBooleanValue() {
            return booleanValue;
        }

        public byte getByteValue() {
            return byteValue;
        }

        public char getCharValue() {
            return charValue;
        }

        public short getShortValue() {
            return shortValue;
        }

        public int getIntValue() {
            return intValue;
        }

        public float getFloatValue() {
            return floatValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public long getLongValue() {
            return longValue;
        }
    }

    @Test
    public void test() throws InstantiationException, SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        PrimitiveClass originalObject = new PrimitiveClass();

        OpackValue serialized = opacker.serialize(originalObject);
        PrimitiveClass deserialized = opacker.deserialize(PrimitiveClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }

}
