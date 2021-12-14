package com.realtimetech.opack.util.structure;

import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;

public class PinnedList<E> implements List<E> {
    final Object arrayObject;

    public PinnedList(Object arrayObject) {
        if (!arrayObject.getClass().isArray()) {
            throw new IllegalArgumentException(arrayObject + " is not array object.");
        }

        this.arrayObject = arrayObject;
    }

    public Object getArrayObject() {
        return arrayObject;
    }

    @Override
    public int size() {
        return Array.getLength(this.arrayObject);
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.indexOf(o) != -1;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return (Iterator<E>) Arrays.asList(toArray()).iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        Object[] objects = new Object[this.size()];

        for (int i = 0; i < objects.length; i++) {
            objects[i] = this.get(i);
        }

        return objects;
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = (T) this.get(i);
        }

        return array;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for(Object o : c){
            if(!this.contains(o)){
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        throw new IllegalArgumentException("aa");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public E get(int index) {
        return (E) ReflectionUtil.getArrayItem(this.arrayObject, index);
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("PinnedList can't modify element.");
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).equals(o)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = this.size() - 1; i >= 0; i--) {
            if (this.get(i).equals(o)) {
                return i;
            }
        }

        return -1;
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return (ListIterator<E>) Arrays.asList(toArray()).listIterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return (ListIterator<E>) Arrays.asList(toArray()).listIterator(index);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return (List<E>) Arrays.asList(toArray()).subList(fromIndex, toIndex);
    }
}
