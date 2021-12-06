package com.realtimetech.opack.transformer;

import com.realtimetech.opack.value.OpackValue;

public interface TransformerInterface<T> {
    public Object serialize(T value);

    public Object deserialize(OpackValue value);
}
