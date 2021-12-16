package com.realtimetech.opack.transformer;

import com.realtimetech.opack.Opacker;

public interface Transformer {
    /**
     *
     * @param value
     * @return
     */
    public Object serialize(Opacker opacker, Object value);

    /**
     *
     * @param value
     * @return
     */
    public Object deserialize(Opacker opacker, Object value);
}
