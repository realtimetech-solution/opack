package com.realtimetech.opack.interlanguage.code;

public class CodePushConst extends Code {
    public final Object value;

    public CodePushConst(Object value) {
        super(Type.PUSH_CONST);

        this.value = value;
    }
}