package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class OpackObject<K, V> extends OpackValue<HashMap<K, V>> {
    public OpackObject(int length) {
        this.set(new HashMap<>(length));
    }

    public OpackObject() {}

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
