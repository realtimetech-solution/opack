package com.realtimetech.opack.transformer.impl;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class WrapListTransformer extends ListTransformer{
    @Override
    protected boolean allowWrapWithType() {
        return true;
    }
}
