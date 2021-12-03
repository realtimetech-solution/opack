package com.realtimetech.opack.interlanguage.code;

public class CodeModifyOpackArrayWithIndex extends Code {
    public final int index;

    public CodeModifyOpackArrayWithIndex(int index) {
        super(Type.MODIFY_OPACK_ARRAY_WITH_INDEX);

        this.index = index;
    }
}