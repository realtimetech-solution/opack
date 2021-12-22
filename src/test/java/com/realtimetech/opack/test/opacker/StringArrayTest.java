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

public class StringArrayTest {
    public static class StringArrayClass {
        private String[] stringArrayValue;

        public StringArrayClass() {
            this.stringArrayValue = new String[]{
                    "Hello, World!",
                    "RealtimeTech",
                    "Object Packager",
                    "Opack"
            };
        }
    }

    @Test
    public void test() throws InstantiationException, SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        StringArrayClass originalObject = new StringArrayClass();

        OpackValue serialized = opacker.serialize(originalObject);
        StringArrayClass deserialized = opacker.deserialize(StringArrayClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }

}
