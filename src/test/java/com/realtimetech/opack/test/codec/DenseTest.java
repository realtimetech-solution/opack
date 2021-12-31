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

package com.realtimetech.opack.test.codec;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.codec.dense.DenseCodec;
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.test.opacker.ComplexTest;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DenseTest {
    @Test
    public void bytes_to_object_to_bytes_object() throws DecodeException, EncodeException {
        OpackValue opackValue = CommonOpackValue.create();
        DenseCodec denseCodec = new DenseCodec.Builder().create();

        byte[] bytes1 = denseCodec.encode(opackValue);
        OpackValue opackValue1 = denseCodec.decode(bytes1);
        byte[] bytes2 = denseCodec.encode(opackValue1);
        OpackValue opackValue2 = denseCodec.decode(bytes2);

        Assertions.assertEquals(opackValue1, opackValue2);

        ((OpackObject) opackValue2).put("check", "A");
        Assertions.assertNotEquals(opackValue1, opackValue2);

        ((OpackObject) opackValue2).remove("check");
        Assertions.assertEquals(opackValue1, opackValue2);

        ((OpackObject) opackValue2).put("object0", "A");
        Assertions.assertNotEquals(opackValue1, opackValue2);
    }

    @Test
    public void with_object() throws DecodeException, EncodeException, SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        DenseCodec denseCodec = new DenseCodec.Builder().create();

        ComplexTest.ComplexClass originalObject = new ComplexTest.ComplexClass();
        OpackValue serialized = opacker.serialize(originalObject);
        byte[] encoded = denseCodec.encode(serialized);
        OpackValue decoded = denseCodec.decode(encoded);
        ComplexTest.ComplexClass deserialized = opacker.deserialize(ComplexTest.ComplexClass.class, decoded);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
