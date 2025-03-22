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
import java.util.Map;
import java.util.TreeMap;

public class WrapMapElementTest {
    @SuppressWarnings("ALL")
    public static class WrapMapClass {
        private HashMap<Object, Object> wrappedTypeMap;

        public WrapMapClass() {
            this.wrappedTypeMap = new HashMap<>();

            this.wrappedTypeMap.put("null_value", null);
            this.wrappedTypeMap.put(null, "null_key");

            this.wrappedTypeMap.put("object_value", new WrapListElementTest.TestElement());
            this.wrappedTypeMap.put(new WrapListElementTest.TestElement(), "object_key");

            {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();

                hashMap.put("inner_object_value", new WrapListElementTest.TestElement());

                this.wrappedTypeMap.put("inner_map", hashMap);
            }

            {
                Map<String, Object>[] hashMapArray = new Map[4];

                for (int index = 0; index < hashMapArray.length; index++) {
                    if (index % 2 == 0) {
                        hashMapArray[index] = new HashMap<>();
                    } else {
                        hashMapArray[index] = new TreeMap<>();
                    }

                    hashMapArray[index].put("inner_object_value", new WrapListElementTest.TestElement());
                }

                this.wrappedTypeMap.put("inner_map_array", hashMapArray);
            }
        }
    }

    @Test
    public void testWithWrapMapTransformer() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        this.common(true);
    }

    @Test
    public void testWithNoWrapMapTransformer() {
        Assertions.assertThrows(Exception.class, () -> this.common(false));
    }

    private void common(boolean enableWrapMapElementType) throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = Opacker.Builder.create()
                .setEnableWrapMapElementType(enableWrapMapElementType)
                .build();
        WrapMapClass originalObject = new WrapMapClass();

        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        WrapMapClass deserialized = opacker.deserialize(WrapMapClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
