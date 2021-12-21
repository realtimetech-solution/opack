package com.realtimetech.opack.value;

import com.realtimetech.opack.util.structure.PrimitiveList;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class OpackArray<E> extends OpackValue<List<E>> {
    public static boolean isAllowArrayType(Class<?> typeClass) {
        if (typeClass.isArray()) {
            Class<?> componentType = ReflectionUtil.getArrayLastComponentType(typeClass);

            if (ReflectionUtil.isPrimitiveClass(componentType)) {
                return true;
            }
        }

        return false;
    }

    public static OpackArray createWithArrayObject(@NotNull Object arrayObject){
        return new OpackArray(arrayObject);
    }

    OpackArray(@NotNull Object arrayObject) {
        if (!arrayObject.getClass().isArray()) {
            throw new IllegalArgumentException(arrayObject + " is not array object");
        }

        if (ReflectionUtil.getArrayDimension(arrayObject.getClass()) != 1) {
            throw new IllegalArgumentException(arrayObject + " must have 1 dimension");
        }

        if (!OpackArray.isAllowArrayType(arrayObject.getClass())) {
            throw new IllegalArgumentException(arrayObject + " array element is not allowed type, allow only primitive type or String or OpackValues or null");
        }

        this.set(new PrimitiveList(arrayObject));
    }

    public OpackArray(E @NotNull [] array) {
        this.set(Arrays.asList(array));
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

    public OpackArray() {
    }

    @Override
    ArrayList<E> createLazyValue() {
        return new ArrayList<>();
    }

    void unpinList() {
        List<E> list = this.get();

        if (list instanceof PrimitiveList) {
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

    @Override
    String toString(List<E> value) {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append('[');

        boolean first = true;
        for (E element : value) {
            if (first){
                first = false;
            }else{
                stringBuffer.append(',');
            }

            stringBuffer.append(element == null ? "null" : element.toString());
        }

        stringBuffer.append(']');

        return stringBuffer.toString();
    }

    @Override
    public OpackValue clone() {
        OpackArray<E> opackArray = new OpackArray<E>(this.length());

        for (int index = 0; index < this.length(); index++) {
            E object = this.get(index);

            if (object instanceof OpackValue) {
                object = (E) ((OpackValue) object).clone();
            }

            opackArray.add(object);
        }

        return opackArray;
    }
}
