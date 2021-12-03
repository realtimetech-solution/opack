package com.realtimetech.opack.interlanguage;

import com.realtimetech.opack.interlanguage.code.Code;

public class PrebuiltCodes {
    final Class<?> targetClass;
    final Code[] codes;

    public PrebuiltCodes(Class<?> targetClass, Code[] codes) {
        this.targetClass = targetClass;
        this.codes = codes;
    }
}