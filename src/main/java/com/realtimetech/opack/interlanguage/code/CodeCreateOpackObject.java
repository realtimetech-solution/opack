package com.realtimetech.opack.interlanguage.code;

public class CodeCreateOpackObject extends Code {
    public final int count;

    public CodeCreateOpackObject(int count) {
        super(Code.Type.CREATE_OPACK_OBJECT);

        this.count = count;
    }
}