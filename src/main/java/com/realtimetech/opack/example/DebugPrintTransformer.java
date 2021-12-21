package com.realtimetech.opack.example;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.transformer.Transformer;

public class DebugPrintTransformer implements Transformer {
    @Override
    public Object serialize(Opacker opacker, Object value) {
//        System.out.println("DebugPrintTransformer " + value);
        return value;
    }

    @Override
    public Object deserialize(Opacker opacker, Class<?> goalType, Object value) {
//        System.out.println("DebugPrintTransformer " + value);
        return value;
    }
}