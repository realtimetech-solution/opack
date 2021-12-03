package com.realtimetech.opack.interlanguage.code;

import java.lang.reflect.Field;

public class CodePushField extends Code {
    public final Field field;

    public CodePushField(Field field) {
        super(Type.PUSH_FIELD);

        this.field = field;
    }
}