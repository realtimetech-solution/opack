/*
 * Copyright (C) 2021 REALTIMETECH All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.realtimetech.opack.example;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.value.OpackArray;

import java.util.ArrayList;
import java.util.List;

public class ListOpackTransformer implements Transformer {
    @Override
    public Object serialize(Opacker opacker, Object value) {
        return null;
    }

    @Override
    public Object deserialize(Opacker opacker, Class<?> goalType, Object value) {
        return null;
    }
}