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
import com.realtimetech.opack.annotation.Type;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.util.*;

public class MapTest {
    public static class MapClass {
        private HashMap<String, String> hashMapValue;

        @Type(HashMap.class)
        private Map<String, String> mapValue;

        public MapClass() {
            this.hashMapValue = new HashMap<>();
            this.hashMapValue.put("hash_map_key_1", "hash_map_value_1");
            this.hashMapValue.put("hash_map_key_2", "hash_map_value_2");
            this.hashMapValue.put("hash_map_key_3", "hash_map_value_3");
            this.hashMapValue.put("hash_map_key_4", "hash_map_value_4");
            this.hashMapValue.put("hash_map_key_5", "hash_map_value_5");

            this.mapValue = new HashMap<>();
            this.mapValue.put("map_key_1", "map_value_1");
            this.mapValue.put("map_key_2", "map_value_2");
            this.mapValue.put("map_key_3", "map_value_3");
            this.mapValue.put("map_key_4", "map_value_4");
            this.mapValue.put("map_key_5", "map_value_5");
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        MapClass originalObject = new MapClass();

        OpackValue serialized = opacker.serialize(originalObject);
        MapClass deserialized = opacker.deserialize(MapClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
