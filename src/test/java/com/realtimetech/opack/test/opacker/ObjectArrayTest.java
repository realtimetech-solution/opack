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
import org.junit.jupiter.api.Test;

import java.util.Random;

public class ObjectArrayTest {
    static final Random RANDOM = new Random();

    public static class ObjectArrayClass {
        private Object nullValue;

        private ObjectTest.SubObjectClass[] subObjectArrayValue;
        private ObjectTest.SubObjectClass[] subObjectArrayWithNullValue;

        public ObjectArrayClass() {
            this.nullValue = null;
            int length = RANDOM.nextInt(5) + 5;

            this.subObjectArrayValue = new ObjectTest.SubObjectClass[length];
            for (int index = 0; index < length; index++) {
                this.subObjectArrayValue[index] = new ObjectTest.SubObjectClass();
            }

            this.subObjectArrayWithNullValue = new ObjectTest.SubObjectClass[length];
            for (int index = 0; index < length; index++) {
                this.subObjectArrayWithNullValue[index] = new ObjectTest.SubObjectClass();
            }

            this.subObjectArrayWithNullValue[RANDOM.nextInt(length)] = null;
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        ObjectArrayClass originalObject = new ObjectArrayClass();

        OpackValue serialized = opacker.serialize(originalObject);
        ObjectArrayClass deserialized = opacker.deserialize(ObjectArrayClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
