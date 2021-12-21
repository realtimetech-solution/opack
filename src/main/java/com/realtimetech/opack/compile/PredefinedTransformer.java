package com.realtimetech.opack.compile;

import com.realtimetech.opack.transformer.Transformer;
import org.jetbrains.annotations.NotNull;

public class PredefinedTransformer {
    final Transformer transformer;
    final boolean inheritable;

    public PredefinedTransformer(@NotNull Transformer transformer, boolean inheritable) {
        this.transformer = transformer;
        this.inheritable = inheritable;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public boolean isInheritable() {
        return inheritable;
    }
}