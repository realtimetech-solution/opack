package com.realtimetech.opack.interlanguage.code;

public class CodeCreateOpackArray extends Code {
    public final int count;
    public CodeCreateOpackArray(int count) {
        super(Type.CREATE_OPACK_ARRAY);

        this.count = count;
    }
}