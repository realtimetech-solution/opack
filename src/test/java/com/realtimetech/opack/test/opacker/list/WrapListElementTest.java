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

package com.realtimetech.opack.test.opacker.list;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.test.RandomUtil;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class WrapListElementTest {
    @SuppressWarnings("ALL")
    public static class WrapListClass {
        private LinkedList<Object> wrappedTypeList;

        public WrapListClass() {
            this.wrappedTypeList = new LinkedList<>();
            this.wrappedTypeList.add(null);
            this.wrappedTypeList.add("test 1");
            this.wrappedTypeList.add(new TestElement());

            {
                LinkedList<Object> linkedList = new LinkedList<Object>();

                linkedList.add(new WrapListElementTest.TestElement());

                this.wrappedTypeList.add(linkedList);
            }

            {
                List<Object>[] linkedListArray = new List[4];

                for (int index = 0; index < linkedListArray.length; index++) {
                    if (index % 2 == 0) {
                        linkedListArray[index] = new ArrayList<>();
                    } else {
                        linkedListArray[index] = new LinkedList<>();
                    }

                    linkedListArray[index].add(new WrapListElementTest.TestElement());
                }

                this.wrappedTypeList.add(linkedListArray);
            }
        }
    }

    @SuppressWarnings("ALL")
    public static class TestElement {
        private int intValue;
        private String stringValue;

        public TestElement() {
            this.intValue = RandomUtil.nextInt();
            this.stringValue = RandomUtil.nextInt() + "";
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            TestElement that = (TestElement) object;
            return intValue == that.intValue && Objects.equals(stringValue, that.stringValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(intValue, stringValue);
        }
    }

    @Test
    public void testWithWrapListTransformer() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        this.common(true);
    }

    @Test
    public void testWithNoWrapListTransformer() {
        Assertions.assertThrows(Exception.class, () -> this.common(false));
    }

    private void common(boolean enableWrapListElementType) throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = Opacker.Builder.create()
                .setEnableWrapListElementType(enableWrapListElementType)
                .build();
        WrapListClass originalObject = new WrapListClass();

        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        WrapListClass deserialized = opacker.deserialize(WrapListClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
