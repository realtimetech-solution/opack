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
        JsonCodec jsonCodec = new JsonCodec.Builder().create();

        int loop = 128;
        PerformanceClass.ExceptionRunnable kryoRunnable = () -> {
            JsonElement serialize = gson.toJsonTree(performanceClass);
            String encode = serialize.toString();
            JsonElement decode = gson.fromJson(encode, JsonElement.class);
            PerformanceClass deserialize = gson.fromJson(decode, PerformanceClass.class);
        };
        PerformanceClass.ExceptionRunnable opackRunnable = () -> {
            OpackValue serialize = opacker.serialize(performanceClass);
            String encode = jsonCodec.encode(serialize);
            OpackValue decode = jsonCodec.decode(encode);
            PerformanceClass deserialize = opacker.deserialize(PerformanceClass.class, decode);
        };

        long gsonTime = PerformanceClass.measureRunningTime(loop, kryoRunnable);
        long opackTime = PerformanceClass.measureRunningTime(loop, opackRunnable);

        System.out.println("# " + this.getClass().getSimpleName());
        System.out.println(" Gson\t: " + gsonTime + "ms");
        System.out.println(" Opack\t: " + opackTime + "ms");

        if (opackTime > gsonTime) {
            Assertions.fail("Opack must faster then gson");
        }
    }
}
