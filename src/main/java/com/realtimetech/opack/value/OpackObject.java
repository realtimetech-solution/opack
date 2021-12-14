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

    public int size() {
        return this.get().size();
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

    @Override
    public OpackValue clone() {
        OpackObject<K, V> opackObject = new OpackObject<K, V>(this.size());

        for (K key : this.get().keySet()) {
            V value = this.get(key);

            if (key instanceof OpackValue) {
                key = (K) ((OpackValue) key).clone();
            }

            if (value instanceof OpackValue) {
                value = (V) ((OpackValue) value).clone();
            }

            opackObject.put(key, value);
        }

        return opackObject;
    }
}
