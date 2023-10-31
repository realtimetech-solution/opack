/*
 * Copyright (C) 2023 REALTIMETECH All Rights Reserved
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
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AnnotationWithTypeTest {
    static final Random RANDOM = new Random();

    public static class ObjectClass {
        private Object nullValue;

        private String stringValue;
        private int intValue;
        private Integer integerValue;

        public ObjectClass() {
            this.nullValue = null;

            this.stringValue = "object_string_value" + System.currentTimeMillis();
            this.intValue = RANDOM.nextInt();
            this.integerValue = RANDOM.nextInt();
        }
    }

    public static class WithTypeClass {
        @WithType
        private List<Double> listWithTransformer;

        @WithType
        private List<Double>[] listArrayWithTransformer;

        @WithType
        private List<Double> listWithAnnotation;

        @WithType
        private List<ObjectClass> objectListWithTransformer;

        @WithType
        private List<ObjectClass>[] objectListArrayWithTransformer;

        @WithType
        private List<ObjectClass> objectListWithAnnotation;

        public WithTypeClass() {
            this.listWithTransformer = new LinkedList<>();
            this.listArrayWithTransformer = new List[8];
            this.listWithAnnotation = new LinkedList<>();

            this.addItemsRandomly(this.listWithTransformer);
            this.addItemsRandomly(this.listWithAnnotation);

            for (int index = 0; index < this.listArrayWithTransformer.length; index++) {
                if (index % 2 == 0) {
                    this.listArrayWithTransformer[index] = new ArrayList<>();
                } else {
                    this.listArrayWithTransformer[index] = new LinkedList<>();
                }

                this.addItemsRandomly(this.listArrayWithTransformer[index]);
            }


            this.objectListWithTransformer = new LinkedList<>();
            this.objectListArrayWithTransformer = new List[8];
            this.objectListWithAnnotation = new LinkedList<>();

            this.addObjectItemsRandomly(this.objectListWithTransformer);
            this.addObjectItemsRandomly(this.objectListWithAnnotation);

            for (int index = 0; index < this.objectListArrayWithTransformer.length; index++) {
                if (index % 2 == 0) {
                    this.objectListArrayWithTransformer[index] = new ArrayList<>();
                } else {
                    this.objectListArrayWithTransformer[index] = new LinkedList<>();
                }

                this.addObjectItemsRandomly(this.objectListArrayWithTransformer[index]);
            }
        }

        private void addItemsRandomly(List<Double> list) {
            for (int index = 0; index < 32; index++) {
                list.add(Math.random());
            }
        }

        private void addObjectItemsRandomly(List<ObjectClass> list) {
            for (int index = 0; index < 32; index++) {
                list.add(new ObjectClass());
            }
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder()
                .setEnableWrapListElementType(true)
                .setEnableWrapMapElementType(true)
                .create();
        WithTypeClass originalObject = new WithTypeClass();

        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        WithTypeClass deserialized = opacker.deserialize(WithTypeClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
