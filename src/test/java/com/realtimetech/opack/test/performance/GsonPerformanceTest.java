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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.codec.json.JsonCodec;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GsonPerformanceTest {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void gson_json() {
        PerformanceClass performanceClass = new PerformanceClass();

        /*
            Gson Contexts
         */
        Gson gson = new GsonBuilder().create();

        /*
            Opack Contexts
         */
        Opacker opacker = new Opacker.Builder().create();
        JsonCodec jsonCodec = new JsonCodec.Builder()
                .setEnableConvertCharacterToString(false)
                .create();

        int warmLoop = 64;
        int loop = 128;

        PerformanceClass.ExceptionRunnable gsonTreeRunnable = () -> {
            JsonElement serialize = gson.toJsonTree(performanceClass);
            String encode = serialize.toString();
            JsonElement decode = gson.fromJson(encode, JsonElement.class);
            PerformanceClass deserialize = gson.fromJson(decode, PerformanceClass.class);

            assert deserialize != null;

            deserialize.hashCode();
        };
        PerformanceClass.ExceptionRunnable gsonDirectRunnable = () -> {
            String encode = gson.toJson(performanceClass);
            PerformanceClass decode = gson.fromJson(encode, PerformanceClass.class);

            assert decode != null;

            decode.hashCode();
        };
        PerformanceClass.ExceptionRunnable opackRunnable = () -> {
            OpackValue serialize = opacker.serialize(performanceClass);
            assert serialize != null;
            String encode = jsonCodec.encode(serialize);
            OpackValue decode = jsonCodec.decode(encode);
            PerformanceClass deserialize = opacker.deserialize(PerformanceClass.class, decode);

            assert deserialize != null;

            deserialize.hashCode();
        };

        // Warm up!
        PerformanceClass.measureRunningTime(warmLoop, gsonTreeRunnable);
        PerformanceClass.measureRunningTime(warmLoop, gsonDirectRunnable);
        PerformanceClass.measureRunningTime(warmLoop * 2, opackRunnable);

        long gsonTreeTime = PerformanceClass.measureRunningTime(loop, gsonTreeRunnable);
        long gsonDirectTime = PerformanceClass.measureRunningTime(loop, gsonDirectRunnable);
        long opackTime = PerformanceClass.measureRunningTime(loop, opackRunnable);

        System.out.println("# " + this.getClass().getSimpleName());
        System.out.println("\tGson(T)\t: " + gsonTreeTime + "ms");
        System.out.println("\tGson(D)\t: " + gsonDirectTime + "ms");
        System.out.println("\tOpack  \t: " + opackTime + "ms");

        if (opackTime > gsonTreeTime) {
            Assertions.fail("Opack must faster then gson");
        }
        if (opackTime > gsonDirectTime) {
            Assertions.fail("Opack must faster then gson");
        }
    }
}
