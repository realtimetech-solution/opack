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
import com.realtimetech.opack.annotation.ExplicitType;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListTest {
    public static class ListClass {
        private LinkedList<String> linkedListValue;

        @ExplicitType(type = ArrayList.class)
        private List<String> arrayListValue;

        public ListClass() {
            this.linkedListValue = new LinkedList<>();
            this.linkedListValue.add("linked_list_value_1");
            this.linkedListValue.add("linked_list_value_2");
            this.linkedListValue.add("linked_list_value_3");
            this.linkedListValue.add("linked_list_value_4");
            this.linkedListValue.add("linked_list_value_5");

            this.arrayListValue = new ArrayList<>();
            this.arrayListValue.add("array_list_value_1");
            this.arrayListValue.add("array_list_value_2");
            this.arrayListValue.add("array_list_value_3");
            this.arrayListValue.add("array_list_value_4");
            this.arrayListValue.add("array_list_value_5");
        }
    }

    @Test
    public void test() throws InstantiationException, SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        ListClass originalObject = new ListClass();

        OpackValue serialized = opacker.serialize(originalObject);
        ListClass deserialized = opacker.deserialize(ListClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
