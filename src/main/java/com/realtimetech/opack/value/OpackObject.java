package com.realtimetech.opack.value;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OpackObject extends OpackLazyValue<HashMap<String, OpackValue>> {
    @Override
    HashMap<String, OpackValue> createLazyValue() {
        return new HashMap<>();
    }

    public OpackValue get(@NotNull String key) {
        return this.get().get(key);
    }

    public OpackValue put(@NotNull String key, @NotNull OpackValue opackValue) {
        return this.get().put(key, opackValue);
    }
}
