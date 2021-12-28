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
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class EnumTest {
    static final Random RANDOM = new Random();

    public static enum EnumData {
        TYPE_A, TYPE_B, TYPE_1, TYPE_2
    }

    public static class EnumClass {
        private EnumData enumData;

        public EnumClass() {
            this.enumData = EnumData.values()[RANDOM.nextInt(EnumData.values().length)];
        }
    }

    @Test
    public void test() throws InstantiationException, SerializeException, DeserializeException, OpackAssert.AssertException {
        {
            Opacker opacker = new Opacker.Builder().create();
            EnumTest.EnumClass originalObject = new EnumTest.EnumClass();

            OpackValue serialized = opacker.serialize(originalObject);
            Assertions.assertEquals(((OpackObject) serialized).get("enumData"), originalObject.enumData.toString());
            EnumTest.EnumClass deserialized = opacker.deserialize(EnumTest.EnumClass.class, serialized);

            OpackAssert.assertEquals(originalObject, deserialized);
        }
        {
            Opacker opacker = new Opacker.Builder().setConvertEnumToOrdinal(true).create();
            EnumTest.EnumClass originalObject = new EnumTest.EnumClass();

            OpackValue serialized = opacker.serialize(originalObject);
            Assertions.assertEquals(((OpackObject) serialized).get("enumData"), originalObject.enumData.ordinal());
            EnumTest.EnumClass deserialized = opacker.deserialize(EnumTest.EnumClass.class, serialized);

            OpackAssert.assertEquals(originalObject, deserialized);
        }
    }
}
