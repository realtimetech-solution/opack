package com.realtimetech.opack.value;

import com.realtimetech.opack.util.ReflectionUtil;

import java.util.Objects;

public abstract class OpackValue<T> {
    public static void assertAllowType(Class<?> clazz) {
        if (!OpackValue.isAllowType(clazz)) {
            throw new IllegalArgumentException(clazz.getName() + " is not allowed type, allow only primitive type or String or OpackValues or null");
        }
    }

    public static boolean isAllowType(Class<?> clazz) {
        return ReflectionUtil.isWrapperClass(clazz) ||
                ReflectionUtil.isPrimitiveClass(clazz) ||
                (clazz == String.class) ||
                (OpackValue.class.isAssignableFrom(clazz));
    }

    private T value;

    abstract T createLazyValue();

    T get() {
        if (this.value == null) {
            synchronized (this) {
                if (this.value == null) {
                    this.value = createLazyValue();
                }
            }
        }

        return this.value;
    }

    void set(T value) {
        synchronized (this) {
            this.value = value;
        }
    }

    public abstract OpackValue clone();

    abstract String toString(T value);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpackValue<?> that = (OpackValue<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + "(" + this.toString(get()) + ")";
    }
}