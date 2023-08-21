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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AnnotationWithTypeTest {
    public static class WithTypeClass {
        @WithType
        private @NotNull List<Double> listWithTransformer;

        @WithType
        private @NotNull List<Double> @NotNull [] listArrayWithTransformer;

        @WithType
        private @NotNull List<Double> listWithAnnotation;

        public WithTypeClass() {
            this.listWithTransformer = new LinkedList<>();
            this.listArrayWithTransformer = new List[8];
            this.listWithAnnotation = new LinkedList<>();

            this.addItemsRandomly(this.listWithTransformer);
            this.addItemsRandomly(this.listWithAnnotation);

            for(int index = 0; index < this.listArrayWithTransformer.length; index++) {
                if(index % 2 == 0) {
                    this.listArrayWithTransformer[index] = new ArrayList<>();
                } else {
                    this.listArrayWithTransformer[index] = new LinkedList<>();
                }

                this.addItemsRandomly(this.listArrayWithTransformer[index]);
            }
        }

        private void addItemsRandomly(@NotNull List<Double> list) {
            for (int index = 0; index < 32; index++) {
                list.add(Math.random());
            }
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        WithTypeClass originalObject = new WithTypeClass();

        OpackValue serialized = opacker.serialize(originalObject);
        WithTypeClass deserialized = opacker.deserialize(WithTypeClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
