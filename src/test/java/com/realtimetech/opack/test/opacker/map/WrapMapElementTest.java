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

package com.realtimetech.opack.test.opacker.map;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.test.opacker.list.WrapListElementTest;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;

public class WrapMapElementTest {
    static final Random RANDOM = new Random();

    public static class WrapMapClass {
        private HashMap<Object, Object> wrappedTypeMap;

        public WrapMapClass() {
            this.wrappedTypeMap = new HashMap<>();

            this.wrappedTypeMap.put("null_value", null);
            this.wrappedTypeMap.put(null, "null_key");

            this.wrappedTypeMap.put("object_value", new WrapListElementTest.TestElement());
            this.wrappedTypeMap.put(new WrapListElementTest.TestElement(), "object_key");
        }
    }

    @Test
    public void testWithWrapMapTransformer() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        this.common(true);
    }

    @Test
    public void testWithNoWrapMapTransformer() {
        Assertions.assertThrows(OpackAssert.AssertException.class, () -> {
            this.common(false);
        });
    }

    private void common(boolean enableWrapMapElementType) throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().setEnableWrapMapElementType(enableWrapMapElementType).create();
        WrapMapClass originalObject = new WrapMapClass();

        OpackValue serialized = opacker.serialize(originalObject);
        WrapMapClass deserialized = opacker.deserialize(WrapMapClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
