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

package com.realtimetech.opack.test.performance;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.codec.dense.DenseCodec;
import com.realtimetech.opack.codec.dense.writer.ByteArrayWriter;
import com.realtimetech.opack.test.opacker.array.PrimitiveArrayTest;
import com.realtimetech.opack.test.opacker.array.WrapperArrayTest;
import com.realtimetech.opack.test.opacker.single.ObjectTest;
import com.realtimetech.opack.test.opacker.single.PrimitiveTest;
import com.realtimetech.opack.test.opacker.single.StringTest;
import com.realtimetech.opack.test.opacker.single.WrapperTest;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KryoPerformanceTest {
    @Test
    public void kryo_bytes() throws Exception {
        PerformanceClass performanceClass = new PerformanceClass();

        /*
            Kryo Contexts
         */
        Kryo kryo = new Kryo();
        ByteBufferOutput byteBufferOutput = new ByteBufferOutput(4096 * 256);

        kryo.register(PerformanceClass.class);
        kryo.register(ObjectTest.SubObjectClass.class);

        kryo.register(ObjectTest.ObjectClass.class);
        kryo.register(ObjectTest.ObjectClass[].class);

        kryo.register(WrapperTest.WrapperClass.class);
        kryo.register(WrapperTest.WrapperClass[].class);

        kryo.register(WrapperArrayTest.WrapperArrayClass.class);
        kryo.register(WrapperArrayTest.WrapperArrayClass[].class);

        kryo.register(PrimitiveTest.PrimitiveClass.class);
        kryo.register(PrimitiveTest.PrimitiveClass[].class);

        kryo.register(PrimitiveArrayTest.PrimitiveArrayClass.class);
        kryo.register(PrimitiveArrayTest.PrimitiveArrayClass[].class);

        kryo.register(StringTest.StringClass.class);
        kryo.register(StringTest.StringClass[].class);

        kryo.register(boolean[].class);
        kryo.register(byte[].class);
        kryo.register(char[].class);
        kryo.register(double[].class);
        kryo.register(float[].class);
        kryo.register(int[].class);
        kryo.register(long[].class);
        kryo.register(short[].class);

        kryo.register(Boolean[].class);
        kryo.register(Byte[].class);
        kryo.register(Character[].class);
        kryo.register(Double[].class);
        kryo.register(Float[].class);
        kryo.register(Integer[].class);
        kryo.register(Long[].class);
        kryo.register(Short[].class);

        /*
            Opack Contexts
         */
        Opacker opacker = new Opacker.Builder().create();
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter();
        DenseCodec denseCodec = new DenseCodec.Builder().create();

        int warmloop = 512;
        int loop = 512 * 2;

        PerformanceClass.ExceptionRunnable kryoRunnable = () -> {
            byteBufferOutput.reset();

            kryo.writeObject(byteBufferOutput, performanceClass);
            byte[] encode = byteBufferOutput.toBytes();
            PerformanceClass deserialize = kryo.readObject(new ByteBufferInput(encode), PerformanceClass.class);

            deserialize.hashCode();
        };
        PerformanceClass.ExceptionRunnable opackRunnable = () -> {
            byteArrayWriter.reset();

            OpackValue serialize = opacker.serialize(performanceClass);
            denseCodec.encode(byteArrayWriter, serialize);
            byte[] encode = byteArrayWriter.toByteArray();
            OpackValue decode = denseCodec.decode(encode);
            PerformanceClass deserialize = opacker.deserialize(PerformanceClass.class, decode);

            deserialize.hashCode();
        };

        // Warm up!
        PerformanceClass.measureRunningTime(warmloop, kryoRunnable);
        PerformanceClass.measureRunningTime(warmloop, opackRunnable);

        long kryoTime = PerformanceClass.measureRunningTime(loop, kryoRunnable);
        long opackTime = PerformanceClass.measureRunningTime(loop, opackRunnable);

        System.out.println("# " + this.getClass().getSimpleName());
        System.out.println(" Kryo\t: " + kryoTime + "ms");
        System.out.println(" Opack\t: " + opackTime + "ms");

        if (opackTime > kryoTime) {
            Assertions.fail("Opack must faster then kryo");
        }
    }
}