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

    public TransformerFactory(Opacker opacker) {
        this.opacker = opacker;

        this.transformerMap = new HashMap<>();
    }

    public <T extends Transformer> T get(@NotNull Class<T> transformerClass) throws InstantiationException {
        if (!this.transformerMap.containsKey(transformerClass)) {
            synchronized (this.transformerMap) {
                if (!this.transformerMap.containsKey(transformerClass)) {
                    T instance = null;

                    if (instance == null) {
                        try {
                            instance = ReflectionUtil.createInstance(transformerClass, this.opacker);
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                            // What can we do?
                        } catch (IllegalArgumentException exception) {
                            // Ok, let's find no parameter constructor
                        }
                    }

                    if (instance == null) {
                        try {
                            instance = ReflectionUtil.createInstance(transformerClass);
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                            // What can we do?
                        } catch (IllegalArgumentException exception) {
                            System.out.println("a2 " );
                            // below throw InstantiationException
                        }
                    }

                    if (instance == null) {
                        throw new InstantiationException(transformerClass.getSimpleName() + " transformer can't instantiation.");
                    }

                    this.transformerMap.put(transformerClass, instance);
                }
            }
        }

        return (T) this.transformerMap.get(transformerClass);
    }
}
