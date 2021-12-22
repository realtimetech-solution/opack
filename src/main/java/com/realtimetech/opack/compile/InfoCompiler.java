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

    public InfoCompiler(@NotNull Opacker opacker) {
        this.opacker = opacker;

        this.transformerFactory = new TransformerFactory(opacker);

        this.classInfoMap = new HashMap<>();
        this.predefinedTransformerMap = new HashMap<>();
    }

    public PredefinedTransformer[] getPredefinedTransformers(Class<?> classType) {
        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(classType);

        if (predefinedTransformers == null) {
            return new PredefinedTransformer[0];
        }

        return predefinedTransformers.toArray(new PredefinedTransformer[predefinedTransformers.size()]);
    }

    public boolean registerPredefinedTransformer(@NotNull Class<?> classType, @NotNull Class<? extends Transformer> transformer) throws InstantiationException {
        return this.registerPredefinedTransformer(classType, transformer, false);
    }

    public synchronized boolean registerPredefinedTransformer(@NotNull Class<?> classType, @NotNull Class<? extends Transformer> transformerClass, boolean inheritable) throws InstantiationException {
        if (!this.predefinedTransformerMap.containsKey(classType)) {
            this.predefinedTransformerMap.put(classType, new LinkedList<>());
        }

        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(classType);

        for (PredefinedTransformer predefinedTransformer : predefinedTransformers) {
            if (predefinedTransformer.getTransformer().getClass() == transformerClass) {
                return false;
            }
        }

        Transformer transformer = this.transformerFactory.get(transformerClass);
        predefinedTransformers.add(new PredefinedTransformer(transformer, inheritable));

        return true;
    }

    public synchronized boolean unregisterPredefinedTransformer(@NotNull Class<?> classType, @NotNull Class<? extends Transformer> transformerClass) {
        List<PredefinedTransformer> predefinedTransformers = this.predefinedTransformerMap.get(classType);

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

        if(targetPredefinedTransformer == null){
            return false;
        }

        predefinedTransformers.remove(targetPredefinedTransformer);

        return true;
    }

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

    Transformer[] getTransformer(AnnotatedElement annotatedElement) throws CompileException {
        List<Transformer> transformers = new LinkedList<>();
        this.addTransformer(transformers, annotatedElement, true);
        return transformers.toArray(new Transformer[transformers.size()]);
    }

    Class<?> getExplicitType(AnnotatedElement annotatedElement) {
        if (annotatedElement.isAnnotationPresent(ExplicitType.class)) {
            ExplicitType explicit = annotatedElement.getAnnotation(ExplicitType.class);
            return explicit.type();
        }

        return null;
    }

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

        return new ClassInfo(compileClass, transformers, fieldInfos.toArray(new ClassInfo.FieldInfo[fieldInfos.size()]));
    }

    public @NotNull ClassInfo get(@NotNull Class<?> targetClass) throws CompileException {
        if (!this.classInfoMap.containsKey(targetClass)) {
            synchronized (this.classInfoMap) {
                if (!this.classInfoMap.containsKey(targetClass)) {
                    ClassInfo classInfo = this.compile(targetClass);

                    this.classInfoMap.put(targetClass, classInfo);
                }
            }
        }

        return this.classInfoMap.get(targetClass);
    }
}
