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

package com.realtimetech.opack.transformer.impl.list;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

public class WrapListTransformer extends ListTransformer {
    @Override
    protected Object serializeObject(Opacker opacker, Object element) throws SerializeException {
        if (element != null && !OpackValue.isAllowType(element.getClass())) {
            OpackValue opackValue = opacker.serialize(element);
            OpackObject<Object, Object> opackObject = new OpackObject<>();

            opackObject.put("type", element.getClass().getName());
            opackObject.put("value", opackValue);

            return opackObject;
        }

        return element;
    }

    @Override
    protected Object deserializeObject(Opacker opacker, Object element) throws ClassNotFoundException, DeserializeException {
        if (element != null && element instanceof OpackObject) {
            OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) element;

            if (opackObject.containsKey("type") && opackObject.containsKey("value")) {
                String type = (String) opackObject.get("type");
                Object value = opackObject.get("value");

                Class<?> objectClass = Class.forName(type);

                if (value instanceof OpackValue) {
                    return opacker.deserialize(objectClass, (OpackValue) value);
                }
            }
        }

        return element;
    }
}
