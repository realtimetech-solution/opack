package com.realtimetech.opack.transformer;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class TransformerFactory {
    @NotNull
    final Opacker opacker;

    @NotNull
    final HashMap<Class<? extends Transformer>, Transformer> transformerMap;

    public TransformerFactory(@NotNull Opacker opacker) {
        this.opacker = opacker;

        this.transformerMap = new HashMap<>();
    }

    public <T extends Transformer> T get(@NotNull Class<T> transformerClass) throws InstantiationException {
        if (!this.transformerMap.containsKey(transformerClass)) {
            synchronized (this.transformerMap) {
                if (!this.transformerMap.containsKey(transformerClass)) {
                    T instance = null;

                    try {
                        // Create instance using Transformer(Opacker) constructor
                        try {
                            if (instance == null) {
                                instance = ReflectionUtil.createInstance(transformerClass, this.opacker);
                            }
                        } catch (IllegalArgumentException exception) {
                            // Ok, let's find no parameter constructor
                        }

                        // Create instance using Transformer() constructor
                        try {
                            if (instance == null) {
                                instance = ReflectionUtil.createInstance(transformerClass);
                            }
                        } catch (IllegalArgumentException exception) {
                            // Ok, let's throw exception
                        }
                    } catch (InvocationTargetException | IllegalAccessException exception) {
                        InstantiationException instantiationException = new InstantiationException(transformerClass.getSimpleName() + " transformer can't instantiation.");
                        instantiationException.initCause(exception);

                        throw instantiationException;
                    }

                    if (instance == null) {
                        throw new InstantiationException(transformerClass.getSimpleName() + " transformer must be implemented constructor(Opacker) or constructor().");
                    }

                    this.transformerMap.put(transformerClass, instance);
                }
            }
        }

        return (T) this.transformerMap.get(transformerClass);
    }
}