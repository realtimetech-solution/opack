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

package com.realtimetech.opack.test.opacker.transform;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.Transform;
import com.realtimetech.opack.annotation.Type;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.transformer.impl.TypeWrapTransformer;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class TypeWrapTransformTest {
    public static class TypeWrapTransformClass {
        @Transform(transformer = TypeWrapTransformer.class)
        private @NotNull List<Double> listWithTransformer;

        @Type(LinkedList.class)
        private @NotNull List<Double> listWithAnnotation;

        public TypeWrapTransformClass() {
            this.listWithTransformer = new LinkedList<>();
            this.listWithAnnotation = new LinkedList<>();
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
        TypeWrapTransformClass originalObject = new TypeWrapTransformClass();

        OpackValue serialized = opacker.serialize(originalObject);
        TypeWrapTransformClass deserialized = opacker.deserialize(TypeWrapTransformClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
