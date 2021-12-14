package com.realtimetech.opack.example;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.transformer.Transformer;

import java.nio.charset.StandardCharsets;

public class StringTransformer implements Transformer {
    @Override
    public Object serialize(Opacker opacker, Object value) {
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }

        throw new IllegalStateException("NEED BYTE[]");
    }

    @Override
    public Object deserialize(Opacker opacker, Object value) {
        if (value instanceof String) {
            return ((String) value).getBytes(StandardCharsets.UTF_8);
        }

        throw new IllegalStateException("NEED STRING");
    }
}