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
import com.realtimetech.opack.codec.json.JsonCodec;
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.test.opacker.other.ComplexTest;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonTest {
    @Test
    public void object_to_string_to_object() throws DecodeException, EncodeException {
        String targetData = "{\n" +
                "\t\"unicode\": \"\\u003d is = and \\u0041 is A\",\n" +
                "\t\"users\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"index\": 1,\n" +
                "\t\t\t\"name\": \"user1\",\n" +
                "\t\t\t\"age\": 0.001,\n" +
                "\t\t\t\"gps\": [10.021899, 10.990218]\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"index\": 2,\n" +
                "\t\t\t\"name\": \"user2\",\n" +
                "\t\t\t\"age\": 0.002,\n" +
                "\t\t\t\"gps\": [20.021899, 20.990218]\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"index\": 3,\n" +
                "\t\t\t\"name\": \"user3\",\n" +
                "\t\t\t\"age\": 0.003,\n" +
                "\t\t\t\"gps\": [30.021899, 30.990218]\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";

        JsonCodec jsonCodec = new JsonCodec.Builder().create();

        OpackValue opackValue1 = jsonCodec.decode(targetData);
        Assertions.assertEquals("\u003d is = and \u0041 is A", ((OpackObject) opackValue1).get("unicode"));

        String middle = jsonCodec.encode(opackValue1);
        OpackValue opackValue2 = jsonCodec.decode(middle);

        Assertions.assertEquals(opackValue1, opackValue2);
        Assertions.assertEquals(opackValue1.toString(), opackValue2.toString());
    }

    @Test
    public void empty_object() throws DecodeException, EncodeException {
        String targetData = "{\"object\":{},\"value\":2147483648}";

        JsonCodec jsonCodec = new JsonCodec.Builder().create();

        OpackValue opackValue1 = jsonCodec.decode(targetData);

        String middle = jsonCodec.encode(opackValue1);
        OpackValue opackValue2 = jsonCodec.decode(middle);

        Assertions.assertEquals(opackValue1, opackValue2);
        Assertions.assertEquals(opackValue1.toString(), opackValue2.toString());
    }

    @Test
    public void empty_array() throws DecodeException, EncodeException {
        String targetData = "{\"array\":[],\"value\":2147483648}";

        JsonCodec jsonCodec = new JsonCodec.Builder().create();

        OpackValue opackValue1 = jsonCodec.decode(targetData);

        String middle = jsonCodec.encode(opackValue1);
        OpackValue opackValue2 = jsonCodec.decode(middle);

        Assertions.assertEquals(opackValue1, opackValue2);
        Assertions.assertEquals(opackValue1.toString(), opackValue2.toString());
    }

    @Test
    public void string_to_object_to_string_object() throws DecodeException, EncodeException {
        OpackValue opackValue = CommonOpackValue.create();
        JsonCodec jsonCodec = new JsonCodec.Builder().create();

        String json1 = jsonCodec.encode(opackValue);
        Assertions.assertTrue(json1.contains("\"unicode\":\"\\u0000\\u0001̂ݷ\\u0000\""));

        OpackValue opackValue1 = jsonCodec.decode(json1);

        String json2 = jsonCodec.encode(opackValue1);
        Assertions.assertTrue(json2.contains("\"unicode\":\"\\u0000\\u0001̂ݷ\\u0000\""));

        OpackValue opackValue2 = jsonCodec.decode(json2);

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
        Opacker opacker = Opacker.Builder.create().build();
        JsonCodec jsonCodec = new JsonCodec.Builder()
                .setEnableConvertCharacterToString(false)
                .create();

        ComplexTest.ComplexClass originalObject = new ComplexTest.ComplexClass();
        OpackValue serialized = opacker.serialize(originalObject);
        assert serialized != null;
        String encoded = jsonCodec.encode(serialized);
        OpackValue decoded = jsonCodec.decode(encoded);
        ComplexTest.ComplexClass deserialized = opacker.deserialize(ComplexTest.ComplexClass.class, decoded);

        OpackAssert.assertEquals(originalObject, deserialized);
    }

    @Test
    public void with_long_miss_double_cause_big_decimal() throws DecodeException, EncodeException, OpackAssert.AssertException {
        JsonCodec jsonCodec = new JsonCodec.Builder().create();

        OpackObject originalObject = new OpackObject();
        originalObject.put("long", -5026738480679942478L);

        String encoded = jsonCodec.encode(originalObject);
        OpackValue decodedObject = jsonCodec.decode(encoded);

        OpackAssert.assertEquals(originalObject, decodedObject);
    }

    @Test
    public void with_big_integer_decimal() throws DecodeException, EncodeException, OpackAssert.AssertException {
        JsonCodec jsonCodec = new JsonCodec.Builder().create();

        OpackObject originalObject = new OpackObject();
        originalObject.put("big_integer", new BigInteger("1" + Long.MAX_VALUE));
        originalObject.put("big_decimal", new BigDecimal("1e400"));

        String encoded = jsonCodec.encode(originalObject);
        OpackValue decodedObject = jsonCodec.decode(encoded);

        OpackAssert.assertEquals(originalObject, decodedObject);
    }
}
