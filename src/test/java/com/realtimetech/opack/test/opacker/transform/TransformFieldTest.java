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

package com.realtimetech.opack.test.opacker.transform;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.Transform;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class TransformFieldTest {
    public static class ByteToStringTransformer implements Transformer {
        /**
         * Serialize specific value to opack value.
         *
         * @param opacker      the opacker
         * @param originalType the original type
         * @param value        the value to be serialized
         * @return opack value
         * @throws SerializeException if a problem occurs during serializing
         */
        @Override
        public @Nullable Object serialize(@NotNull Opacker opacker, @NotNull Class<?> originalType, @Nullable Object value) throws SerializeException {
            if (value instanceof byte[]) {
                return new String(((byte[]) value), StandardCharsets.UTF_8);
            }

            return value;
        }

        @Override
        public Object deserialize(@NotNull Opacker opacker, @NotNull Class<?> goalType, Object value) throws DeserializeException {
            if (value instanceof String && goalType == byte[].class) {
                return ((String) value).getBytes(StandardCharsets.UTF_8);
            }

            return value;
        }
    }

    public static class FieldTransformClass {
        @Transform(transformer = ByteToStringTransformer.class)
        private byte[] transformValue;

        public FieldTransformClass() {
            this.transformValue = "Thank you for visiting Realtime Tech Co., Ltd. Realtime Tech Co., Ltd., since its founding in 2000 by Professor, has entered the DBMS field monopolized by foreign global companies. Its high stability and performance are recognized by supplying Kairos, a product, to various fields of application in the military, government and civilian sectors. In particular, we are leading the 4D computing market by developing real-time spatial DBMS and moving object trajectory management solutions. Based on these system SW and 4D computing technologies and experiences, we are making efforts to advance into AI and big data fields and take another leap forward as a company leading digital transformation such as smart factories and smart cities.".getBytes(StandardCharsets.UTF_8);
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        FieldTransformClass originalObject = new FieldTransformClass();

        OpackValue serialized = opacker.serialize(originalObject);
        FieldTransformClass deserialized = opacker.deserialize(FieldTransformClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
