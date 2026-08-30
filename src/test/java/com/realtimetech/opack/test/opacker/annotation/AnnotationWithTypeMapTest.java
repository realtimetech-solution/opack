/*
 * Copyright (C) 2026 REALTIMETECH All Rights Reserved
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

package com.realtimetech.opack.test.opacker.annotation;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.WithType;
import com.realtimetech.opack.codec.json.JsonCodec;
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class AnnotationWithTypeMapTest {
    @SuppressWarnings("ALL")
    public static class WithTypeMapClass {
        @WithType
        private Map<String, Object> hashMap;

        @WithType
        private Map<String, Object> linkedHashMap;

        @WithType
        private Map<String, Object> treeMap;

        public WithTypeMapClass() {
            this.hashMap = new HashMap<>();
            this.hashMap.put("string", "hash-map");
            this.hashMap.put("number", 1L);

            this.linkedHashMap = new LinkedHashMap<>();
            this.linkedHashMap.put("string", "linked-hash-map");
            this.linkedHashMap.put("number", 2L);

            this.treeMap = new TreeMap<>();
            this.treeMap.put("string", "tree-map");
            this.treeMap.put("number", 3L);
        }
    }

    @Test
    public void testOpackRoundTrip() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        for (boolean enableWrapMapElementType : new boolean[]{false, true}) {
            Opacker opacker = Opacker.Builder.create()
                    .setEnableWrapMapElementType(enableWrapMapElementType)
                    .build();
            WithTypeMapClass originalObject = new WithTypeMapClass();

            OpackValue serialized = opacker.serialize(originalObject);
            assert serialized != null;
            WithTypeMapClass deserialized = opacker.deserialize(WithTypeMapClass.class, serialized);

            OpackAssert.assertEquals(originalObject, deserialized);
        }
    }

    @Test
    public void testJsonRoundTrip() throws SerializeException, DeserializeException, EncodeException, DecodeException, OpackAssert.AssertException {
        JsonCodec jsonCodec = JsonCodec.Builder.create().build();

        for (boolean enableWrapMapElementType : new boolean[]{false, true}) {
            Opacker opacker = Opacker.Builder.create()
                    .setEnableWrapMapElementType(enableWrapMapElementType)
                    .build();
            WithTypeMapClass originalObject = new WithTypeMapClass();

            OpackValue serialized = opacker.serialize(originalObject);
            assert serialized != null;
            String encoded = jsonCodec.encode(serialized);
            OpackValue decoded = jsonCodec.decode(encoded);
            WithTypeMapClass deserialized = opacker.deserialize(WithTypeMapClass.class, decoded);

            OpackAssert.assertEquals(originalObject, deserialized);
        }
    }
}