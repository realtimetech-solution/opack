package com.realtimetech.opack.value;

import java.util.HashMap;

public abstract class OpackLazyValue<T> extends OpackValue<T>{
    public OpackLazyValue() {
        super(null);
    }

    abstract T createLazyValue();

    @Override
    public void set(T value) {
        throw new Doe
    }

    @Override
    public T get() {
        T value = super.get();

        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    super.set(value = createLazyValue());
                }
            }
        }

        return value;
    }
}
