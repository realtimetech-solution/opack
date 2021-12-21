package com.realtimetech.opack.transformer;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;

public interface Transformer {
    /**
     *
     * @param value
     * @return
     */
    public Object serialize(Opacker opacker, Object value) throws SerializeException;

    /**
     *
     * @param value
     * @return
     */
    public Object deserialize(Opacker opacker, Class<?> goalType, Object value) throws DeserializeException;
}
