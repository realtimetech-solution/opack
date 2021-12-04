package com.realtimetech.opack.interlanguage;

import com.realtimetech.opack.interlanguage.code.Code;

public class Compiler {
    public static PrebuiltCodes compile(Class<?> compileTarget) {
        return new PrebuiltCodes(compileTarget, new Code[]{

        });
    }
}
