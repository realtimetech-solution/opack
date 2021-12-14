package com.realtimetech.opack.value;

import com.realtimetech.opack.util.PinnedList;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;

public class OpackArray<E> extends OpackValue<List<E>> {
    public static boolean isAllowArrayType(Class<?> clazz) {
        if (clazz.isArray()) {
            Class<?> componentType = ReflectionUtil.getArrayLastComponentType(clazz);

            if(OpackValue.isAllowType(componentType)) {
                return true;
            }
        }

        return false;
    }

    public OpackArray(@NotNull Object arrayObject) {
        if (!arrayObject.getClass().isArray()) {
            throw new IllegalArgumentException(arrayObject + " is not array object.");
        }

        if (!OpackArray.isAllowArrayType(arrayObject.getClass())) {
            throw new IllegalArgumentException(arrayObject + " array element is not allowed type, allow only primitive type or String or OpackValues or null");
        }

        this.set(new PinnedList(arrayObject));
    }

    public OpackArray(E @NotNull [] array) {
        this.set(Arrays.asList(array));
    }

    public OpackArray() {
    }

    public OpackArray(@NotNull Collection<E> collection) {
        this(collection.size());

        List<E> list = this.get();

        for (E element : collection) {
            if (element != null)
                OpackValue.assertAllowType(element.getClass());

            list.add(element);
        }
    }

    public OpackArray(int initialCapacity) {
        this.set(new ArrayList<>(initialCapacity));
    }

    @Override
    ArrayList<E> createLazyValue() {
        return new ArrayList<>();
    }

    void unpinList() {
        List<E> list = this.get();

        if (list instanceof PinnedList) {
            this.set((List<E>) Arrays.asList(list.toArray()));
        }
    }

    public E set(int index, @NotNull E value) {
        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        this.unpinList();

        return this.get().set(index, value);
    }

    public boolean add(@NotNull E value) {
        if (value != null)
            OpackValue.assertAllowType(value.getClass());

        this.unpinList();

        return this.get().add(value);
    }

    public boolean remove(@NotNull E value) {
        return this.get().remove(value);
    }

    public E get(int index) {
        return this.get().get(index);
    }

    public int length() {
        return this.get().size();
    }
}
