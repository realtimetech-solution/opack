package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OpackObject<K, V> extends OpackValue<HashMap<K, V>> {
    public OpackObject(int initialCapacity) {
        this.set(new HashMap<>(initialCapacity));
    }

    public OpackObject() {
    }

    @Override
    HashMap<K, V> createLazyValue() {
        return new HashMap<>();
    }

    public V get(@NotNull K key) {
        return this.get().get(key);
    }

    public V put(@NotNull K key, @NotNull V value) {
        if (key != null)
            OpackValue.assertAllowType(key.getClass());

        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        return this.get().put(key, value);
    }
}
