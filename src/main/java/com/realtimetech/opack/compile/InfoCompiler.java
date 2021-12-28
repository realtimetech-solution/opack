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

package com.realtimetech.opack.compile;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.ExplicitType;
import com.realtimetech.opack.annotation.Ignore;
import com.realtimetech.opack.annotation.Transform;
import com.realtimetech.opack.exception.CompileException;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.transformer.TransformerFactory;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InfoCompiler {
    final @NotNull Opacker opacker;

    final @NotNull TransformerFactory transformerFactory;

    final @NotNull HashMap<Class<?>, ClassInfo> classInfoMap;
    final @NotNull HashMap<Class<?>, List<PredefinedTransformer>> predefinedTransformerMap;

    /**
     * Constructs an InfoCompiler with the opacker.
     *
     * @param opacker the opacker
     */
    public InfoCompiler(@NotNull Opacker opacker) {
        this.opacker = opacker;

        this.transformerFactory = new TransformerFactory(opacker);

        this.classInfoMap = new HashMap<>();
        this.predefinedTransformerMap = new HashMap<>();
    }

    /**
     * Returns predefined transformers targeting a specific class.
     *
     * @param typeClass the class to be the target
     * @return found predefined transformers
     */
    public PredefinedTransformer[] getPredefinedTransformers(Class<?> typeClass) {
        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(typeClass);

        if (predefinedTransformers == null) {
            return new PredefinedTransformer[0];
        }

        return predefinedTransformers.toArray(new PredefinedTransformer[0]);
    }

    /**
     * Calls {@code registerPredefinedTransformer(typeClass, transformerClass, false);}
     *
     * @param typeClass        the class to be the target
     * @param transformerClass the predefined transformer class to register
     * @return whether the predefined transformer registration is successful
     * @throws InstantiationException if transformer class object cannot be instantiated
     */
    public boolean registerPredefinedTransformer(@NotNull Class<?> typeClass, @NotNull Class<? extends Transformer> transformerClass) throws InstantiationException {
        return this.registerPredefinedTransformer(typeClass, transformerClass, false);
    }

    /**
     * Register a predefined transformer targeting the specific class.
     *
     * @param typeClass        the class to be the target
     * @param transformerClass the predefined transformer to register
     * @param inheritable      whether transformer is inheritable
     * @return whether the predefined transformer registration is successful
     * @throws InstantiationException if transformer class object cannot be instantiated
     */
    public synchronized boolean registerPredefinedTransformer(@NotNull Class<?> typeClass, @NotNull Class<? extends Transformer> transformerClass, boolean inheritable) throws InstantiationException {
        if (!this.predefinedTransformerMap.containsKey(typeClass)) {
            this.predefinedTransformerMap.put(typeClass, new LinkedList<>());
        }

        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(typeClass);

        for (PredefinedTransformer predefinedTransformer : predefinedTransformers) {
            if (predefinedTransformer.getTransformer().getClass() == transformerClass) {
                return false;
            }
        }

        Transformer transformer = this.transformerFactory.get(transformerClass);
        predefinedTransformers.add(new PredefinedTransformer(transformer, inheritable));

        return true;
    }

    /**
     * Unregister a predefined transformer targeting the specific class.
     *
     * @param typeClass        the targeted type class
     * @param transformerClass the predefined transformer to unregister
     * @return whether the cancellation of predefined transformer registration is successful
     */
    public synchronized boolean unregisterPredefinedTransformer(@NotNull Class<?> typeClass, @NotNull Class<? extends Transformer> transformerClass) {
        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(typeClass);

        if (predefinedTransformers == null) {
            return false;
        }

        PredefinedTransformer targetPredefinedTransformer = null;
        for (PredefinedTransformer predefinedTransformer : predefinedTransformers) {
            if (predefinedTransformer.getTransformer().getClass() == transformerClass) {
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
     * Add transformers for the element to the transformer list.
     *
     * @param transformers     the transformer list to add
     * @param annotatedElement the element to be targeted
     * @param root             whether the element is not super class (whether the element is the root)
     * @throws CompileException if transformer class object cannot be instantiated
     */
    void addTransformer(List<Transformer> transformers, AnnotatedElement annotatedElement, boolean root) throws CompileException {
        if (annotatedElement instanceof Class) {
            Class<?> elementClass = (Class<?>) annotatedElement;
            Class<?> superClass = elementClass.getSuperclass();

            if (superClass != null && superClass != Object.class) {
                this.addTransformer(transformers, superClass, false);
            }

            for (Class<?> interfaceClass : elementClass.getInterfaces()) {
                this.addTransformer(transformers, interfaceClass, false);
            }
        }

        if (annotatedElement instanceof Class) {
            Class<?> elementClass = (Class<?>) annotatedElement;

            if (this.predefinedTransformerMap.containsKey(elementClass)) {
                List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(elementClass);

                if (predefinedTransformers != null) {
                    for (PredefinedTransformer predefinedTransformer : predefinedTransformers) {
                        if (root || predefinedTransformer.isInheritable()) {
                            transformers.add(predefinedTransformer.getTransformer());
                        }
                    }
                }
            }
        }

        if (annotatedElement.isAnnotationPresent(Transform.class)) {
            Transform transform = annotatedElement.getAnnotation(Transform.class);

            if (root || transform.inheritable()) {
                Class<Transformer> transformerInterfaceClass = (Class<Transformer>) transform.transformer();

                try {
                    transformers.add(this.transformerFactory.get(transformerInterfaceClass));
                } catch (InstantiationException e) {
                    throw new CompileException(e);
                }
            }
        }
    }

    /**
     * Returns transformers registered through {@link Transform Transform} annotation.
     *
     * @param annotatedElement the element that annotated {@link Transform Transform}
     * @return transformers
     * @throws CompileException if transformer class object cannot be instantiated
     */
    Transformer[] getTransformer(AnnotatedElement annotatedElement) throws CompileException {
        List<Transformer> transformers = new LinkedList<>();
        this.addTransformer(transformers, annotatedElement, true);
        return transformers.toArray(new Transformer[0]);
    }

    /**
     * Returns the explicit type of specific element registered through {@link ExplicitType ExplicitType}.
     *
     * @param annotatedElement the element that annotated {@link ExplicitType ExplicitType}
     * @return explicit type
     */
    Class<?> getExplicitType(AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(ExplicitType.class)) {
            ExplicitType explicit = annotatedElement.getAnnotation(ExplicitType.class);
            return explicit.type();
        }

        return null;
    }

    /**
     * Compile the class into {@link ClassInfo ClassInfo}.
     *
     * @param compileClass the class to compile
     * @return compiled class info
     * @throws CompileException if a problem occurs during compiling a class into {@link ClassInfo ClassInfo}
     */
    @NotNull ClassInfo compile(@NotNull Class<?> compileClass) throws CompileException {
        List<ClassInfo.FieldInfo> fieldInfos = new LinkedList<>();
        Transformer[] transformers = new Transformer[0];

        if (!compileClass.isArray() &&
                compileClass != String.class &&
                !ReflectionUtil.isPrimitiveClass(compileClass) &&
                !ReflectionUtil.isWrapperClass(compileClass)) {

            Field[] fields = ReflectionUtil.getAccessibleFields(compileClass);
            for (Field field : fields) {
                if (field.isAnnotationPresent(Ignore.class)) {
                    continue;
                }

                Transformer[] fieldTransformers = this.getTransformer(field);
                Class<?> explicitType = this.getExplicitType(field);

                fieldInfos.add(new ClassInfo.FieldInfo(field, fieldTransformers.length > 0 ? fieldTransformers[0] : null, explicitType));
            }

            transformers = this.getTransformer(compileClass);
        }

        return new ClassInfo(compileClass, transformers, fieldInfos.toArray(new ClassInfo.FieldInfo[0]));
    }

    /**
     * Returns ClassInfo for target class.
     *
     * @param compileClass the class to be targeted
     * @return class info
     * @throws CompileException if a problem occurs during compiling a class into class info
     */
    public @NotNull ClassInfo get(@NotNull Class<?> compileClass) throws CompileException {
        if (!this.classInfoMap.containsKey(compileClass)) {
            synchronized (this.classInfoMap) {
                if (!this.classInfoMap.containsKey(compileClass)) {
                    ClassInfo classInfo = this.compile(compileClass);

                    this.classInfoMap.put(compileClass, classInfo);
                }
            }
        }

        return this.classInfoMap.get(compileClass);
    }
}
