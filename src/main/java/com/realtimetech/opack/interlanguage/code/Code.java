package com.realtimetech.opack.interlanguage.code;


public abstract class Code {
    public enum Type {
        CREATE_OPACK_OBJECT,
        CREATE_OPACK_ARRAY,

        CREATE_OPACK_NONE,
        CREATE_OPACK_BOOL,
        CREATE_OPACK_NUMBER,
        CREATE_OPACK_STRING,

        MODIFY_OPACK_OBJECT,
        MODIFY_OPACK_OBJECT_WITH_STRING,
        MODIFY_OPACK_ARRAY,
        MODIFY_OPACK_ARRAY_WITH_INDEX,

        PUSH_CONST,
        PUSH_FIELD,
        CALL
    }

    public final Type type;

    public Code(Type type) {
        this.type = type;
    }
}