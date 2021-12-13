package com.realtimetech.opack;

import com.realtimetech.opack.annotation.ExplicitType;
import com.realtimetech.opack.annotation.Ignore;
import com.realtimetech.opack.annotation.Transform;
import com.realtimetech.opack.compile.ClassInfo;
import com.realtimetech.opack.compile.InfoCompiler;
import com.realtimetech.opack.exception.CompileException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.TransformerFactory;
import com.realtimetech.opack.value.OpackValue;

import java.security.KeyPair;

public class Opacker {
    public class Builder {

    }

    final TransformerFactory transformerFactory;

    Opacker() {
        this.transformerFactory = new TransformerFactory(this);
    }

    public TransformerFactory getTransformerFactory() {
        return transformerFactory;
    }
}
