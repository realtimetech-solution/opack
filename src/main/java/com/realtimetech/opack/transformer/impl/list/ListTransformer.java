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
import com.realtimetech.opack.transformer.impl.DataStructureTransformer;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackValue;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ListTransformer extends DataStructureTransformer {
    /**
     * Serializes the list to {@link OpackArray OpackArray}.
     *
     * @param opacker the opacker
     * @param value   the value to serialize
     * @return serialized value
     * @throws SerializeException if a problem occurs during serializing
     */
    @Override
    public Object serialize(Opacker opacker, Object value) throws SerializeException {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            OpackArray<Object> opackArray = new OpackArray<>(list.size());

            for (Object object : list) {
                opackArray.add(this.serializeObject(opacker, object));
            }

            return opackArray;
        }

        return value;
    }

    /**
     * Deserializes the {@link OpackArray OpackArray} to {@link List list}.
     *
     * @param opacker  the opacker
     * @param goalType the class of list to be deserialized
     * @param value    the opack value to deserialize
     * @return deserialized value
     * @throws DeserializeException if a problem occurs during deserializing
     */
    @Override
    public Object deserialize(Opacker opacker, Class<?> goalType, Object value) throws DeserializeException {
        if (value instanceof OpackArray) {
            OpackArray<Object> opackArray = (OpackArray<Object>) value;
            if (List.class.isAssignableFrom(goalType)) {
                try {
                    List<Object> list = (List<Object>) ReflectionUtil.createInstance(goalType);

                    for (int index = 0; index < opackArray.length(); index++) {
                        Object element = opackArray.get(index);

                        list.add(this.deserializeObject(opacker, element));
                    }

                    return list;
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
                    throw new DeserializeException(exception);
                }
            }
        }

        return value;
    }

    @Override
    protected Object serializeObject(Opacker opacker, Object element) throws SerializeException {
        if (element != null && !OpackValue.isAllowType(element.getClass())) {
            return opacker.serialize(element);
        }

        return element;
    }

    @Override
    protected Object deserializeObject(Opacker opacker, Object element) throws ClassNotFoundException, DeserializeException {
        return element;
    }
}