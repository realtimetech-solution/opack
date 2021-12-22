/*
 * Copyright (C) 2021 REALTIMETECH All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.realtimetech.opack.transformer.impl;

public class NoWrapListTransformer extends ListTransformer{
    @Override
    protected boolean allowWrapWithType() {
        return false;
    }
}
