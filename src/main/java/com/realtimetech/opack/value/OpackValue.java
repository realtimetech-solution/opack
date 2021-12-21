package com.realtimetech.opack.value;

import com.realtimetech.opack.util.ReflectionUtil;

import java.util.Objects;

public abstract class OpackValue<T> {
    public static void assertAllowType(Class<?> typeClass) {
        if (!OpackValue.isAllowType(typeClass)) {
            throw new IllegalArgumentException(typeClass.getName() + " is not allowed type, allow only primitive type or String or OpackValues or null");
        }
    }

    public static boolean isAllowType(Class<?> typeClass) {
        return ReflectionUtil.isWrapperClass(typeClass) ||
                ReflectionUtil.isPrimitiveClass(typeClass) ||
                (typeClass == String.class) ||
                (OpackValue.class.isAssignableFrom(typeClass));
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

//    public final String test(){
//        return super.toString();
//    }
//
    @Override
    public final String toString() {
        return this.getClass().getSimpleName() + "(" + this.toString(get()) + ")";
    }
}