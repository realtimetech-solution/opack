package com.realtimetech.opack.codec;

import com.realtimetech.opack.value.OpackValue;

public interface OpackCodec<E> {
    public E encode(OpackValue opackValue);

    public OpackValue decode(E data);
}
