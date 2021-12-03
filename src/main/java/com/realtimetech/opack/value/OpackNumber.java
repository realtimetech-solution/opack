package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

public class OpackNumber extends OpackValue<Number> {
    public OpackNumber(@NotNull Number value) {
        super(value);
    }
}
