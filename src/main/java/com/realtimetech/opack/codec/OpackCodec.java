package com.realtimetech.opack.codec;

import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;

public abstract class OpackCodec<D> {
    protected abstract D doEncode(OpackValue opackValue);

    protected abstract OpackValue doDecode(D data);

    public D encode(OpackValue opackValue) throws EncodeException {
        try {
            return this.doEncode(opackValue);
        } catch (Exception exception) {
            throw new EncodeException(exception);
        }
    }

    public OpackValue decode(D data) throws DecodeException {
        try {
            return this.doDecode(data);
        } catch (Exception exception) {
            throw new DecodeException(exception);
        }
    }
}
