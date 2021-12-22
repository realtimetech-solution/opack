/*
 * Copyright (C) 2021 REALTIMETECH All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.realtimetech.opack.example;

import com.realtimetech.opack.annotation.Transform;

import java.util.Random;

@Transform(transformer = DebugPrintTransformer.class)
public class DataObject {
    private static Random RANDOM = new Random();

    private int intValue;
    private Integer integerValue;

    public DataObject() {
        this.intValue = RANDOM.nextInt();
        this.integerValue = RANDOM.nextInt();
    }

    public int getIntValue() {
        return intValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }
}