/*
 * Copyright (C) 2025 REALTIMETECH All Rights Reserved
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

package com.realtimetech.opack.benchmark;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.codec.json.JsonCodec;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.performance.PerformanceClass;
import com.realtimetech.opack.value.OpackValue;

public class JsonParsePerfTest {
    public static void main(String[] args) throws EncodeException, SerializeException {
        PerformanceClass performanceClass = new PerformanceClass();

        /*
            Opack Contexts
         */
        Opacker opacker = Opacker.Builder.create().build();
        JsonCodec jsonCodec = JsonCodec.Builder.create().build();

        OpackValue serialize = opacker.serialize(performanceClass);
        assert serialize != null;
        String targetJson = jsonCodec.encode(serialize);


        PerformanceClass.ExceptionRunnable opackRunnable = () -> {
            OpackValue decode = jsonCodec.decode(targetJson);
        };

        // Warm up!
        PerformanceClass.measureRunningTime(1024, opackRunnable);

        for (int i = 0; i < 8; i++) {

            long opackTime = PerformanceClass.measureRunningTime(1024 * 2, opackRunnable);

            System.out.println((i + 1) + ". " + opackTime + "ms");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
