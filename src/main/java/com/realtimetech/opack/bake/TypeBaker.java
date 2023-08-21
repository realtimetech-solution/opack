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

package com.realtimetech.opack.bake;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.*;
import com.realtimetech.opack.exception.BakeException;
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

public final class TypeBaker {
    static final class PredefinedTransformer {
        private final @NotNull Transformer transformer;
        private final boolean inheritable;

        /**
         * Constructs the PredefinedTransformer.
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

    private final @NotNull HashMap<@NotNull Class<?>, @NotNull BakedType> backedTypeMap;
    private final @NotNull HashMap<@NotNull Class<?>, @NotNull List<@NotNull PredefinedTransformer>> predefinedTransformerMap;

    /**
     * Constructs an TypeBaker with the opacker.
     *
     * @param opacker the opacker
     */
    public TypeBaker(@NotNull Opacker opacker) {
        this.opacker = opacker;

        this.transformerFactory = new TransformerFactory(opacker);

        this.backedTypeMap = new HashMap<>();
        this.predefinedTransformerMap = new HashMap<>();
    }

    /**
     * Returns predefined transformers for a specific class.
     *
     * @param type the class to be the target
     * @return found predefined transformers
     */
    public @NotNull PredefinedTransformer @NotNull [] getPredefinedTransformers(@NotNull Class<?> type) {
        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(type);

        if (predefinedTransformers == null) {
            return new PredefinedTransformer[0];
        }

        return predefinedTransformers.toArray(new PredefinedTransformer[0]);
    }

    /**
     * Calls {@code registerPredefinedTransformer(type, transformerClass, false);}
     *
     * @param type            the class to be the target
     * @param transformerType the predefined transformer class to register
     * @return true if the predefined transformer registration is successful
     * @throws InstantiationException if transformer class object cannot be instantiated
     */
    public boolean registerPredefinedTransformer(@NotNull Class<?> type, @NotNull Class<? extends Transformer> transformerType) throws InstantiationException {
        return this.registerPredefinedTransformer(type, transformerType, false);
    }

    /**
     * Register a predefined transformer for the specific class.
     *
     * @param type            the class to be the target
     * @param transformerType the predefined transformer to register
     * @param inheritable     whether transformer is inheritable
     * @return true if the predefined transformer registration is successful
     * @throws InstantiationException if transformer class object cannot be instantiated
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
     * Unregister a predefined transformer for the specific class.
     *
     * @param type            the targeted type class
     * @param transformerType the predefined transformer to unregister
     * @return true if the cancellation of predefined transformer registration is successful
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
     * Add transformers of the element to the transformer list.
     *
     * @param transformers     the transformer list for add
     * @param annotatedElement the element to be targeted
     * @param root             whether the element is not super class (whether the element is the root)
     * @throws BakeException if transformer class object cannot be instantiated
     */
    private void addTransformer(@NotNull List<@NotNull Transformer> transformers, @NotNull AnnotatedElement annotatedElement, boolean root) throws BakeException {
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
                Class<Transformer> transformerType = (Class<Transformer>) transform.transformer();

                try {
                    transformers.add(this.transformerFactory.get(transformerType));
                } catch (InstantiationException e) {
                    throw new BakeException(e);
                }
            }
        }
    }

    /**
     * Returns transformers registered through {@link Transform Transform} annotation.
     *
     * @param annotatedElement the element that annotated {@link Transform Transform}
     * @return transformers
     * @throws BakeException if transformer class object cannot be instantiated
     */
    private @NotNull Transformer @NotNull [] getTransformer(@NotNull AnnotatedElement annotatedElement) throws BakeException {
        List<Transformer> transformers = new LinkedList<>();

        this.addTransformer(transformers, annotatedElement, true);

        return transformers.toArray(new Transformer[0]);
    }

    /**
     * Returns the explicit type of specific element registered through {@link Type ExplicitType}.
     *
     * @param annotatedElement the element that annotated {@link Type ExplicitType}
     * @return returns annotated type
     */
    private @Nullable Class<?> getAnnotatedType(@NotNull AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(Type.class)) {
            Type type = annotatedElement.getAnnotation(Type.class);
            return type.value();
        }

        return null;
    }

    /**
     * Returns the serialized type of specific element registered through {@link Name SerializedName}.
     *
     * @param annotatedElement the element that annotated {@link Type ExplicitType}
     * @return returns annotated type
     */
    private @Nullable String getAnnotatedName(@NotNull AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(Name.class)) {
            Name name = annotatedElement.getAnnotation(Name.class);
            return name.value();
        }

        return null;
    }

    /**
     * Bake the class into {@link BakedType BakedType}.
     *
     * @param bakeType the type to bake
     * @return baked type info
     * @throws BakeException if a problem occurs during baking a class into {@link BakedType BakedType}
     */
    private @NotNull BakedType bake(@NotNull Class<?> bakeType) throws BakeException {
        List<BakedType.Property> properties = new LinkedList<>();
        Transformer[] transformers = new Transformer[0];

        if (!bakeType.isArray() &&
                bakeType != String.class &&
                !ReflectionUtil.isPrimitiveType(bakeType) &&
                !ReflectionUtil.isWrapperType(bakeType)) {

            Field[] fields = ReflectionUtil.getAccessibleFields(bakeType);
            for (Field field : fields) {
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }

                Transformer[] fieldTransformers = this.getTransformer(field);
                Class<?> type = this.getAnnotatedType(field);
                String name = this.getAnnotatedName(field);
                boolean withType = field.isAnnotationPresent(WithType.class);

                properties.add(new BakedType.Property(field, name, type, withType, fieldTransformers.length > 0 ? fieldTransformers[0] : null));
            }

            transformers = this.getTransformer(bakeType);
        }

        return new BakedType(bakeType, transformers, properties.toArray(new BakedType.Property[0]));
    }

    /**
     * Returns BakedType for target class.
     *
     * @param bakeType the class to be baked
     * @return class info
     * @throws BakeException if a problem occurs during baking a class into class info
     */
    public @NotNull BakedType get(@NotNull Class<?> bakeType) throws BakeException {
        if (!this.backedTypeMap.containsKey(bakeType)) {
            synchronized (this.backedTypeMap) {
                if (!this.backedTypeMap.containsKey(bakeType)) {
                    BakedType bakedType = this.bake(bakeType);

                    this.backedTypeMap.put(bakeType, bakedType);
                }
            }
        }

        return this.backedTypeMap.get(bakeType);
    }
}