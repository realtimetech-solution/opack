package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OpackObject<K extends OpackValue, V extends OpackValue> extends OpackLazyValue<HashMap<K, V>> {
    @Override
    public void set(HashMap<K, V> value) {
        throw new UnsupportedOperationException();
    }

    @Override
    HashMap<K, V> createLazyValue() {
        return new HashMap<>();
    }

    public V get(@NotNull K key) {
        return this.get().get(key);
    }

    public V put(@NotNull K key, @NotNull V opackValue) {
        return this.get().put(key, opackValue);
    }
}
