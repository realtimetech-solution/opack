/*
 * Copyright (C) 2021 REALTIMETECH All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.realtimetech.opack.transformer.impl;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class ListTransformer implements Transformer {
    final boolean wrapWithType;

    public ListTransformer() {
        this.wrapWithType = this.allowWrapWithType();
    }

    protected abstract boolean allowWrapWithType();

    @Override
    public Object serialize(Opacker opacker, Object value) throws SerializeException {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            OpackArray opackArray = new OpackArray(list.size());

            for (Object object : list) {
                if (object == null || OpackValue.isAllowType(object.getClass())) {
                    opackArray.add(object);
                } else {
                    OpackValue opackValue = opacker.serialize(object);

                    if (this.wrapWithType) {
                        OpackObject opackObject = new OpackObject();
                        opackObject.put("type", object.getClass().getName());
                        opackObject.put("value", opackValue);

                        opackValue = opackObject;
                    }

                    opackArray.add(opackValue);
                }
            }

            return opackArray;
        }

        return value;
    }

    @Override
    public Object deserialize(Opacker opacker, Class<?> goalType, Object value) throws DeserializeException {
        if (value instanceof OpackArray) {
            OpackArray opackArray = (OpackArray) value;
            if (List.class.isAssignableFrom(goalType)) {
                try {
                    List<Object> list = (List<Object>) ReflectionUtil.createInstance(goalType);

                    for (int index = 0; index < opackArray.length(); index++) {
                        Object element = opackArray.get(index);

                        if (this.wrapWithType) {
                            if (element instanceof OpackObject) {
                                OpackObject opackObject = (OpackObject) element;

                                if (opackObject.containsKey("type") && opackObject.containsKey("value")) {
                                    String type = (String) opackObject.get("type");
                                    Object object = opackObject.get("value");

                                    Class<?> objectType = Class.forName(type);

                                    if (object instanceof OpackValue) {
                                        list.add(opacker.deserialize(objectType, (OpackValue) object));
                                        continue;
                                    }
                                }
                            }
                        }

                        list.add(element);
                    }

                    return list;
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
                    throw new DeserializeException(exception);
                }
            }
        }

        return value;
    }
}
