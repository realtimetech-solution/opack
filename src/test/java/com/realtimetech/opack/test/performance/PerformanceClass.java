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

import com.realtimetech.opack.annotation.SerializedName;
import com.realtimetech.opack.test.opacker.*;

import java.util.Random;

public class PerformanceClass {
    @FunctionalInterface
    public interface ExceptionRunnable {
        public abstract void run() throws Exception;
    }

    public static long measureRunningTime(int loop, ExceptionRunnable runnable) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            try {
                runnable.run();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }
        long end = System.currentTimeMillis();

        return end - start;
    }

    static final Random RANDOM = new Random();

    private WrapperTest.WrapperClass wrapperClassValue;
    private WrapperTest.WrapperClass[] wrapperClassArrayValue;

    private WrapperArrayTest.WrapperArrayClass wrapperArrayClassValue;
    private WrapperArrayTest.WrapperArrayClass[] wrapperArrayClassArrayValue;

    private StringTest.StringClass stringClassValue;
    private StringTest.StringClass[] stringClassArrayValue;

    private PrimitiveTest.PrimitiveClass primitiveClassValue;
    private PrimitiveTest.PrimitiveClass[] primitiveClassArrayValue;

    private PrimitiveArrayTest.PrimitiveArrayClass primitiveArrayClassValue;
    private PrimitiveArrayTest.PrimitiveArrayClass[] primitiveArrayClassArrayValue;

    private ObjectTest.ObjectClass objectClassValue;
    private ObjectTest.ObjectClass[] objectClassArrayValue;

    public PerformanceClass() {
        int length = RANDOM.nextInt(5) + 5;

        this.wrapperClassValue = new WrapperTest.WrapperClass();
        this.wrapperClassArrayValue = new WrapperTest.WrapperClass[length];
        for (int index = 0; index < length; index++) {
            this.wrapperClassArrayValue[index] = new WrapperTest.WrapperClass();
        }

        this.wrapperArrayClassValue = new WrapperArrayTest.WrapperArrayClass();
        this.wrapperArrayClassArrayValue = new WrapperArrayTest.WrapperArrayClass[length];
        for (int index = 0; index < length; index++) {
            this.wrapperArrayClassArrayValue[index] = new WrapperArrayTest.WrapperArrayClass();
        }

        this.stringClassValue = new StringTest.StringClass();
        this.stringClassArrayValue = new StringTest.StringClass[length];
        for (int index = 0; index < length; index++) {
            this.stringClassArrayValue[index] = new StringTest.StringClass();
        }

        this.primitiveClassValue = new PrimitiveTest.PrimitiveClass();
        this.primitiveClassArrayValue = new PrimitiveTest.PrimitiveClass[length];
        for (int index = 0; index < length; index++) {
            this.primitiveClassArrayValue[index] = new PrimitiveTest.PrimitiveClass();
        }

        this.primitiveArrayClassValue = new PrimitiveArrayTest.PrimitiveArrayClass();
        this.primitiveArrayClassArrayValue = new PrimitiveArrayTest.PrimitiveArrayClass[length];
        for (int index = 0; index < length; index++) {
            this.primitiveArrayClassArrayValue[index] = new PrimitiveArrayTest.PrimitiveArrayClass();
        }

        this.objectClassValue = new ObjectTest.ObjectClass();
        this.objectClassArrayValue = new ObjectTest.ObjectClass[length];
        for (int index = 0; index < length; index++) {
            this.objectClassArrayValue[index] = new ObjectTest.ObjectClass();
        }
    }
}