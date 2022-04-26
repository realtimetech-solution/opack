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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.codec.json.JsonCodec;
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class JacksonPerformanceTest {
    @Test
    public void jackson_bytes() throws SerializeException, EncodeException, DecodeException {
        PerformanceClass performanceClass = new PerformanceClass();

        /*
            Jackson Contexts
         */
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        /*
            Opack Contexts
         */
        Opacker opacker = new Opacker.Builder().create();
        JsonCodec jsonCodec = new JsonCodec.Builder().create();

        int warmLoop = 128;
        int loop = 256;

        PerformanceClass.ExceptionRunnable jacksonRunnable = () -> {
            String value = objectMapper.writeValueAsString(performanceClass);
            PerformanceClass deserialize = objectMapper.readValue(value, PerformanceClass.class);

            deserialize.hashCode();
        };

        PerformanceClass.ExceptionRunnable opackRunnable = () -> {
            OpackValue serialize = opacker.serialize(performanceClass);
            String encode = jsonCodec.encode(serialize);
            OpackValue decode = jsonCodec.decode(encode);
            PerformanceClass deserialize = opacker.deserialize(PerformanceClass.class, decode);

            deserialize.hashCode();
        };

        // Warm up!
        PerformanceClass.measureRunningTime(warmLoop, jacksonRunnable);
        PerformanceClass.measureRunningTime(warmLoop, opackRunnable);

        long jacksonTime = PerformanceClass.measureRunningTime(loop, jacksonRunnable);
        long opackTime = PerformanceClass.measureRunningTime(loop, opackRunnable);

        System.out.println("# " + this.getClass().getSimpleName());
        System.out.println("\tJackson\t: " + jacksonTime + "ms");
        System.out.println("\tOpack  \t: " + opackTime + "ms");

        double delta = Math.abs((double) opackTime - (double) jacksonTime) / Math.max(opackTime, jacksonTime);

        delta *= 100;

        if (delta > 5 && jacksonTime < opackTime) {
            Assertions.fail("Opack performance must similar with jackson");
        }
    }
}