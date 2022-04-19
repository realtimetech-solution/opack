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

package com.realtimetech.opack.test.opacker.array;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class WrapperArrayTest {
    static final Random RANDOM = new Random();

    public static class WrapperArrayClass {
        private Boolean[] booleanArrayValue;
        private Byte[] byteArrayValue;

        private Character[] charArrayValue;
        private Short[] shortArrayValue;

        private Integer[] intArrayValue;
        private Float[] floatArrayValue;

        private Double[] doubleArrayValue;
        private Long[] longArrayValue;

        public WrapperArrayClass() {
            int length = RANDOM.nextInt(1024) + 512;

            this.booleanArrayValue = new Boolean[length];
            for(int index = 0; index < length; index++){
                this.booleanArrayValue[index] = RANDOM.nextBoolean();
            }
            this.booleanArrayValue[length / 2] = null;

            this.byteArrayValue = new Byte[length];
            for(int index = 0; index < length; index++){
                this.byteArrayValue[index] = (byte) RANDOM.nextInt();
            }
            this.byteArrayValue[length / 2] = null;

            this.charArrayValue = new Character[length];
            for(int index = 0; index < length; index++){
                this.charArrayValue[index] = (char) RANDOM.nextInt();
            }
            this.charArrayValue[length / 2] = null;

            this.shortArrayValue = new Short[length];
            for(int index = 0; index < length; index++){
                this.shortArrayValue[index] = (short) RANDOM.nextInt();
            }
            this.shortArrayValue[length / 2] = null;

            this.intArrayValue = new Integer[length];
            for(int index = 0; index < length; index++){
                this.intArrayValue[index] = RANDOM.nextInt();
            }
            this.intArrayValue[length / 2] = null;

            this.floatArrayValue = new Float[length];
            for(int index = 0; index < length; index++){
                this.floatArrayValue[index] = RANDOM.nextFloat();
            }
            this.floatArrayValue[length / 2] = null;

            this.doubleArrayValue = new Double[length];
            for(int index = 0; index < length; index++){
                this.doubleArrayValue[index] = RANDOM.nextDouble();
            }
            this.doubleArrayValue[length / 2] = null;

            this.longArrayValue = new Long[length];
            for(int index = 0; index < length; index++){
                this.longArrayValue[index] = RANDOM.nextLong();
            }
            this.longArrayValue[length / 2] = null;
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        WrapperArrayClass originalObject = new WrapperArrayClass();

        OpackValue serialized = opacker.serialize(originalObject);
        WrapperArrayClass deserialized = opacker.deserialize(WrapperArrayClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
