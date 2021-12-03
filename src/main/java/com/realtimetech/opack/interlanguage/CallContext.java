package com.realtimetech.opack.interlanguage;

import com.realtimetech.opack.interlanguage.code.Code;

import java.lang.reflect.Field;

public class CallContext {
    final PrebuiltCodes prebuiltCodes;

    final Object object;
    int index;

    public CallContext(PrebuiltCodes codes, Object object) {
        this.prebuiltCodes = codes;
        this.object = object;
    }

    public Code take() {
        if (prebuiltCodes.codes.length > index) {
            return prebuiltCodes.codes[index++];
        }

        return null;
    }

    public Object getField(Field field) throws IllegalAccessException {
        return field.get(object);
    }
}