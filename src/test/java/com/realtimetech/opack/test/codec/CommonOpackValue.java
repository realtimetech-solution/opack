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

import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

public class CommonOpackValue {
    public static OpackValue create() {
        OpackObject opackObject = new OpackObject();
        {
            OpackArray opackArray = new OpackArray();

            for (int i = 0; i < 10; i++) {
                OpackObject sample = new OpackObject();
                sample.put("string", "hello, " + i + " times!");

                sample.put("int", (int) 10 * i);
                sample.put("integer", (Integer) 10 * i);

                sample.put("double", (Double) 1.990218E8 + i);
                sample.put("float", (float) 1.990218E8 + i);

                opackArray.add(sample);
            }

            opackObject.put("array", opackArray);
        }
        for (int i = 0; i < 10; i++) {
            OpackObject sample = new OpackObject();
            sample.put("null", null);
            sample.put("java.version", System.getProperty("java.version"));

            opackObject.put("object" + i, sample);
        }

        return opackObject;
    }
}
