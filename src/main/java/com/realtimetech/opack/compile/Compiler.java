package com.realtimetech.opack.compile;

import com.realtimetech.opack.compile.code.Code;

public class Compiler {
    public static PrebuiltCodes compile(Class<?> compileTarget) {
        return new PrebuiltCodes(compileTarget, new Code[]{

        });
    }
}
