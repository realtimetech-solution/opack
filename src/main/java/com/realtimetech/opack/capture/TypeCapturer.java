/*
 * Copyright (C) 2021 REALTIMETECH All Rights Reserved
 *
 * Licensed either under the Apache License, Version 2.0, or (at your option)
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation (subject to the "Classpath" exception),
 * either version 2, or any later version (collectively, the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     http://www.gnu.org/licenses/
 *     http://www.gnu.org/software/classpath/license.html
 *
 * or as provided in the LICENSE file that accompanied this code.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.realtimetech.opack.capture;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.*;
import com.realtimetech.opack.exception.TypeCaptureException;
import com.realtimetech.opack.provider.DefaultValueProvider;
import com.realtimetech.opack.provider.DefaultValueProviderFactory;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.TransformerFactory;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public final class TypeCapturer {
    public static final class PredefinedTransformer {
        private final @NotNull Transformer transformer;
        private final boolean inheritable;

        /**
         * Constructs the PredefinedTransformer
         *
         * @param transformer the transformer to be registered
         * @param inheritable true if the transformer is inheritable
         */
        public PredefinedTransformer(@NotNull Transformer transformer, boolean inheritable) {
            this.transformer = transformer;
            this.inheritable = inheritable;
        }

        public @NotNull Transformer getTransformer() {
            return transformer;
        }

        public boolean isInheritable() {
            return inheritable;
        }
    }

    private final @NotNull Opacker opacker;

    private final @NotNull TransformerFactory transformerFactory;
    private final @NotNull DefaultValueProviderFactory defaultValueProviderFactory;

    private final @NotNull HashMap<@NotNull Class<?>, @NotNull CapturedType> capturedTypeMap;
    private final @NotNull HashMap<@NotNull Class<?>, @NotNull List<@NotNull PredefinedTransformer>> predefinedTransformerMap;


    /**
     * Constructs a new instance of TypeCapturer.
     *
     * @param opacker the Opacker instance used for managing serialization and deserialization processes
     */
    public TypeCapturer(@NotNull Opacker opacker) {
        this.opacker = opacker;

        this.transformerFactory = new TransformerFactory(opacker);
        this.defaultValueProviderFactory = new DefaultValueProviderFactory(opacker);

        this.capturedTypeMap = new HashMap<>();
        this.predefinedTransformerMap = new HashMap<>();
    }

    public @NotNull Opacker getOpacker() {
        return opacker;
    }

    /**
     * Returns predefined transformers for a specific class
     *
     * @param type the class to be the target
     * @return the found predefined transformers
     */
    public @NotNull PredefinedTransformer @NotNull [] getPredefinedTransformers(@NotNull Class<?> type) {
        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(type);

        if (predefinedTransformers == null) {
            return new PredefinedTransformer[0];
        }

        return predefinedTransformers.toArray(new PredefinedTransformer[0]);
    }

    /**
     * Register a predefined transformer for the specific class with a transformer instance
     *
     * @param type        the class to be the target
     * @param transformer the transformer to register
     * @param inheritable the flag indicating transformer is inheritable
     * @return true if the predefined transformer registration is successful, false otherwise
     */
    public synchronized boolean registerPredefinedTransformer(@NotNull Class<?> type, @NotNull Transformer transformer, boolean inheritable) {
        if (!this.predefinedTransformerMap.containsKey(type)) {
            this.predefinedTransformerMap.put(type, new LinkedList<>());
        }

        Class<? extends Transformer> transformerType = transformer.getClass();
        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(type);

        for (PredefinedTransformer predefinedTransformer : predefinedTransformers) {
            if (predefinedTransformer.getTransformer().getClass() == transformerType) {
                return false;
            }
        }

        predefinedTransformers.add(new PredefinedTransformer(transformer, inheritable));

        return true;
    }

    /**
     * Calls {@code registerPredefinedTransformer(type, transformerClass, false);}
     *
     * @param type            the class to be the target
     * @param transformerType the predefined transformer class to register
     * @return true if the predefined transformer registration is successful, false otherwise
     * @throws InstantiationException if a transformer class object cannot be instantiated
     */
    public boolean registerPredefinedTransformer(@NotNull Class<?> type, @NotNull Class<? extends Transformer> transformerType) throws InstantiationException {
        return this.registerPredefinedTransformer(type, transformerType, false);
    }

    /**
     * Register a predefined transformer for the specific class
     *
     * @param type            the class to be the target
     * @param transformerType the predefined transformer to register
     * @param inheritable     the flag indicating transformer is inheritable
     * @return true if the predefined transformer registration is successful, false otherwise
     * @throws InstantiationException if a transformer class object cannot be instantiated
     */
    public synchronized boolean registerPredefinedTransformer(@NotNull Class<?> type, @NotNull Class<? extends Transformer> transformerType, boolean inheritable) throws InstantiationException {
        if (!this.predefinedTransformerMap.containsKey(type)) {
            this.predefinedTransformerMap.put(type, new LinkedList<>());
        }

        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(type);

        for (PredefinedTransformer predefinedTransformer : predefinedTransformers) {
            if (predefinedTransformer.getTransformer().getClass() == transformerType) {
                return false;
            }
        }

        Transformer transformer = this.transformerFactory.get(transformerType);
        predefinedTransformers.add(new PredefinedTransformer(transformer, inheritable));

        return true;
    }

    /**
     * Unregister a predefined transformer for the specific class
     *
     * @param type            the targeted type class
     * @param transformerType the predefined transformer to unregister
     * @return true if the cancellation of predefined transformer registration is successful, false otherwise
     */
    public synchronized boolean unregisterPredefinedTransformer(@NotNull Class<?> type, @NotNull Class<? extends Transformer> transformerType) {
        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(type);

        if (predefinedTransformers == null) {
            return false;
        }

        PredefinedTransformer targetPredefinedTransformer = null;
        for (PredefinedTransformer predefinedTransformer : predefinedTransformers) {
            if (predefinedTransformer.getTransformer().getClass() == transformerType) {
                targetPredefinedTransformer = predefinedTransformer;
                break;
            }
        }

        if (targetPredefinedTransformer == null) {
            return false;
        }

        predefinedTransformers.remove(targetPredefinedTransformer);

        return true;
    }

    /**
     * Add transformers of the element to the transformer list
     *
     * @param transformers     the transformer list for adding
     * @param annotatedElement the element to be targeted
     * @param root             the flag indicating whether the element is not super class (whether the element is the root)
     * @throws TypeCaptureException if a transformer class object cannot be instantiated
     */
    private void addTransformer(@NotNull List<@NotNull Transformer> transformers, @NotNull AnnotatedElement annotatedElement, boolean root) throws TypeCaptureException {
        if (annotatedElement instanceof Class) {
            Class<?> elementType = (Class<?>) annotatedElement;
            Class<?> superType = elementType.getSuperclass();

            if (superType != null && superType != Object.class) {
                this.addTransformer(transformers, superType, false);
            }

            for (Class<?> interfaceClass : elementType.getInterfaces()) {
                this.addTransformer(transformers, interfaceClass, false);
            }
        }

        if (annotatedElement instanceof Class) {
            Class<?> elementType = (Class<?>) annotatedElement;

            if (this.predefinedTransformerMap.containsKey(elementType)) {
                List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(elementType);

                if (predefinedTransformers != null) {
                    for (PredefinedTransformer predefinedTransformer : predefinedTransformers) {
                        if (root || predefinedTransformer.isInheritable() && !transformers.contains(predefinedTransformer.getTransformer())) {
                            transformers.add(predefinedTransformer.getTransformer());
                        }
                    }
                }
            }
        }

        if (annotatedElement.isAnnotationPresent(Transform.class)) {
            Transform transform = annotatedElement.getAnnotation(Transform.class);

            if (root || transform.inheritable()) {
                //noinspection unchecked
                Class<Transformer> transformerType = (Class<Transformer>) transform.transformer();

                try {
                    transformers.add(this.transformerFactory.get(transformerType));
                } catch (InstantiationException e) {
                    throw new TypeCaptureException(e);
                }
            }
        }
    }

    /**
     * Returns transformers registered through {@link Transform Transform} annotation
     *
     * @param annotatedElement the element that annotated {@link Transform Transform}
     * @return the transformers
     * @throws TypeCaptureException if a transformer class object cannot be instantiated
     */
    private @NotNull Transformer @NotNull [] getTransformer(@NotNull AnnotatedElement annotatedElement) throws TypeCaptureException {
        List<Transformer> transformers = new LinkedList<>();

        this.addTransformer(transformers, annotatedElement, true);

        return transformers.toArray(new Transformer[0]);
    }

    /**
     * Returns the explicit type of specific element registered through {@link Type ExplicitType}
     *
     * @param annotatedElement the element that annotated {@link Type ExplicitType}
     * @return the annotated type
     */
    private @Nullable Class<?> getAnnotatedType(@NotNull AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(Type.class)) {
            Type type = annotatedElement.getAnnotation(Type.class);
            return type.value();
        }

        return null;
    }

    /**
     * Returns the serialized type of specific element registered through {@link Name SerializedName}
     *
     * @param annotatedElement the element that annotated {@link Type ExplicitType}
     * @return the annotated type
     */
    private @Nullable String getAnnotatedName(@NotNull AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(Name.class)) {
            Name name = annotatedElement.getAnnotation(Name.class);
            return name.value();
        }

        return null;
    }

    /**
     * Capture the class into {@link CapturedType CapturedType}
     *
     * @param clazz the class to capture
     * @return the captured type
     * @throws TypeCaptureException if a problem occurs during capturing a class into {@link CapturedType CapturedType}
     */
    private @NotNull CapturedType capture(@NotNull Class<?> clazz) throws TypeCaptureException {
        List<CapturedType.FieldProperty> properties = new LinkedList<>();
        Transformer[] transformers = new Transformer[0];

        if (!clazz.isArray() &&
                clazz != String.class &&
                !ReflectionUtil.isPrimitiveType(clazz) &&
                !ReflectionUtil.isWrapperType(clazz)) {

            Field[] fields = ReflectionUtil.getAccessibleFields(clazz);
            for (Field field : fields) {
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }

                Transformer[] fieldTransformers = this.getTransformer(field);
                DefaultValueProvider defaultValueProvider = null;

                Class<?> type = this.getAnnotatedType(field);
                String name = this.getAnnotatedName(field);

                boolean withType = field.isAnnotationPresent(WithType.class);

                if (field.isAnnotationPresent(DefaultValue.class)) {
                    DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
                    //noinspection unchecked
                    Class<DefaultValueProvider> defaultValueProviderType = (Class<DefaultValueProvider>) defaultValue.provider();

                    try {
                        defaultValueProvider = this.defaultValueProviderFactory.get(defaultValueProviderType);
                    } catch (InstantiationException e) {
                        throw new TypeCaptureException(e);
                    }
                }

                properties.add(new CapturedType.FieldProperty(field, name, type, withType, fieldTransformers.length > 0 ? fieldTransformers[0] : null, defaultValueProvider));
            }

            transformers = this.getTransformer(clazz);
        }

        return new CapturedType(clazz, transformers, properties.toArray(new CapturedType.FieldProperty[0]));
    }

    /**
     * Returns CapturedType for target class
     *
     * @param clazz the class to get
     * @return the class info
     * @throws TypeCaptureException if a problem occurs during capturing a class into class info
     */
    public @NotNull CapturedType get(@NotNull Class<?> clazz) throws TypeCaptureException {
        if (!this.capturedTypeMap.containsKey(clazz)) {
            synchronized (this.capturedTypeMap) {
                if (!this.capturedTypeMap.containsKey(clazz)) {
                    CapturedType capturedType = this.capture(clazz);

                    this.capturedTypeMap.put(clazz, capturedType);
                }
            }
        }

        return this.capturedTypeMap.get(clazz);
    }
}