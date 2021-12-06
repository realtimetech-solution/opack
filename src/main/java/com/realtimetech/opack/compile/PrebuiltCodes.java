package com.realtimetech.opack.compile;

import com.realtimetech.opack.compile.code.Code;

public class PrebuiltClass {
    public class FieldInfo {

    }

    final Class<?> targetClass;
    final FieldInfo[] fields;

    public PrebuiltClass(Class<?> targetClass, Code[] codes) {
        this.targetClass = targetClass;
        this.codes = codes;
    }
}