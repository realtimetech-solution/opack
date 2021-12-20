package com.realtimetech.opack.codec.dense;

import com.realtimetech.opack.codec.OpackCodec;
import com.realtimetech.opack.value.OpackValue;

public class DenseCodec extends OpackCodec<byte[]> {
    public DenseCodec(OpackValue opackValue) {

    }

    public DenseCodec(byte[] bytes) {

    }

    @Override
    protected byte[] doEncode(OpackValue opackValue) {
        return new byte[0];
    }

    @Override
    protected OpackValue doDecode(byte[] data) {
        return null;
    }
}
