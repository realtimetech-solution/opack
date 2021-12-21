package com.realtimetech.opack.transformer.impl;

public class NoWrapListTransformer extends ListTransformer{
    @Override
    protected boolean allowWrapWithType() {
        return false;
    }
}
