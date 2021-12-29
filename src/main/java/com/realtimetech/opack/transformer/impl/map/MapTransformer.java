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

package com.realtimetech.opack.transformer.impl.map;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class MapTransformer implements Transformer {
    final boolean wrapWithType;

    /**
     * Constructs a MapTransformer
     */
    public MapTransformer() {
        this.wrapWithType = this.allowWrapWithType();
    }

    /**
     * Returns whether the types of each element are also serialized.
     *
     * @return whether the types of each element are also serialized.
     */
    protected abstract boolean allowWrapWithType();

    /**
     * Serializes the map to {@link OpackObject OpackObject}.
     *
     * @param opacker the opacker
     * @param value   the value to serialize
     * @return the serialized value
     * @throws SerializeException if a problem occurs during serializing
     */
    @Override
    public Object serialize(Opacker opacker, Object value) throws SerializeException {
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            OpackObject<Object, Object> opackObject = new OpackObject<>(map.size());

            for (Object object : map.keySet()) {
                Object keyObject = object;
                Object valueObject = map.get(object);

                keyObject = serializeObject(opacker, keyObject);
                valueObject = serializeObject(opacker, valueObject);

                opackObject.put(keyObject, valueObject);
            }

            return opackObject;
        }

        return value;
    }

    Object serializeObject(Opacker opacker, Object object) throws SerializeException {
        if (object != null && !OpackValue.isAllowType(object.getClass())) {
            OpackValue opackValue = opacker.serialize(object);

            if (this.wrapWithType) {
                OpackObject<Object, Object> opackObject = new OpackObject<>();
                opackObject.put("type", object.getClass().getName());
                opackObject.put("value", opackValue);

                opackValue = opackObject;
            }

            object = opackValue;
        }
        return object;
    }

    /**
     * Deserializes the {@link OpackObject OpackObject} to {@link Map map}.
     *
     * @param opacker  the opacker
     * @param goalType the class of map to be deserialized
     * @param value    the opack value to deserialize
     * @return deserialized value
     * @throws DeserializeException if a problem occurs during deserializing
     */
    @Override
    public Object deserialize(Opacker opacker, Class<?> goalType, Object value) throws DeserializeException {
        if (value instanceof OpackObject) {
            OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) value;
            if (Map.class.isAssignableFrom(goalType)) {
                try {
                    Map<Object, Object> map = (Map<Object, Object>) ReflectionUtil.createInstance(goalType);

                    for (Map.Entry<Object, Object> element : opackObject.entrySet()) {
                        Object keyObject = element.getKey();
                        Object valueObject = element.getValue();

                        if (this.wrapWithType) {
                            keyObject = deserializeObject(opacker, keyObject);
                            valueObject = deserializeObject(opacker, valueObject);
                        }

                        map.put(keyObject, valueObject);
                    }

                    return map;
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
                    throw new DeserializeException(exception);
                }
            }
        }

        return value;
    }

    Object deserializeObject(Opacker opacker, Object object) throws ClassNotFoundException, DeserializeException {
        if (object instanceof OpackObject) {
            OpackObject<Object, Object> wrapperObject = (OpackObject<Object, Object>) object;

            if (wrapperObject.containsKey("type") && wrapperObject.containsKey("value")) {
                String type = (String) wrapperObject.get("type");
                Object value = wrapperObject.get("value");

                Class<?> objectType = Class.forName(type);

                if (value instanceof OpackValue) {
                    object = opacker.deserialize(objectType, (OpackValue) value);
                }
            }
        }

        return object;
    }
}
