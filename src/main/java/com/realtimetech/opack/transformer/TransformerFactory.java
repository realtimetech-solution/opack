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

package com.realtimetech.opack.transformer;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class TransformerFactory {
    @NotNull
    private final Opacker opacker;

    @NotNull
    private final HashMap<Class<? extends Transformer>, Transformer> transformerMap;

    /**
     * Constructs a TransformerFactory with the opacker.
     *
     * @param opacker the opacker
     */
    public TransformerFactory(@NotNull Opacker opacker) {
        this.opacker = opacker;

        this.transformerMap = new HashMap<>();
    }

    /**
     * Returns transformer instance.
     *
     * @param transformerType the transformer class
     * @return transformer instance
     * @throws InstantiationException if transformer class object cannot be instantiated; if the constructor is not in the transformer class
     */
    public <T extends Transformer> T get(@NotNull Class<T> transformerType) throws InstantiationException {
        if (!this.transformerMap.containsKey(transformerType)) {
            synchronized (this.transformerMap) {
                if (!this.transformerMap.containsKey(transformerType)) {
                    T instance = null;

                    try {
                        // Create instance using Transformer(Opacker) constructor
                        try {
                            instance = ReflectionUtil.createInstance(transformerType, this.opacker);
                        } catch (IllegalArgumentException exception) {
                            // Ok, let's find no parameter constructor
                        }

                        // Create instance using Transformer() constructor
                        try {
                            if (instance == null) {
                                instance = ReflectionUtil.createInstance(transformerType);
                            }
                        } catch (IllegalArgumentException exception) {
                            // Ok, let's throw exception
                        }
                    } catch (InvocationTargetException | IllegalAccessException exception) {
                        InstantiationException instantiationException = new InstantiationException(transformerType.getSimpleName() + " transformer can't instantiation.");
                        instantiationException.initCause(exception);

                        throw instantiationException;
                    }

                    if (instance == null) {
                        throw new InstantiationException(transformerType.getSimpleName() + " transformer must be implemented constructor(Opacker) or constructor().");
                    }

                    this.transformerMap.put(transformerType, instance);
                }
            }
        }

        return transformerType.cast(this.transformerMap.get(transformerType));
    }
}