package com.realtimetech.opack.interlanguage.code;

public class CodeModifyOpackObjectWithString extends Code {
    public final String string;

    public CodeModifyOpackObjectWithString(String string) {
        super(Type.MODIFY_OPACK_OBJECT_WITH_STRING);

        this.string = string;
    }
}