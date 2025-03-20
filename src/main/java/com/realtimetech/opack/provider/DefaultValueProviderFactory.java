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

package com.realtimetech.opack.provider;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class DefaultValueProviderFactory {
    private final @NotNull Opacker opacker;

    private final @NotNull HashMap<@NotNull Class<? extends DefaultValueProvider>, @NotNull DefaultValueProvider> defaultValueProviderMap;

    /**
     * Constructs a TransformerFactory with the opacker
     *
     * @param opacker the opacker
     */
    public DefaultValueProviderFactory(@NotNull Opacker opacker) {
        this.opacker = opacker;

        this.defaultValueProviderMap = new HashMap<>();
    }

    /**
     * Returns default value provider instance
     *
     * @param <P>                      the type parameter extending {@link DefaultValueProvider}.
     * @param defaultValueProviderType the default value provider class
     * @return the default value provider instance
     * @throws InstantiationException if the default value provider class object cannot be instantiated; if the default value provider is not in the default value provider class
     */
    @SuppressWarnings("ConstantValue")
    public <P extends DefaultValueProvider> @NotNull P get(@NotNull Class<P> defaultValueProviderType) throws InstantiationException {
        if (!this.defaultValueProviderMap.containsKey(defaultValueProviderType)) {
            synchronized (this.defaultValueProviderMap) {
                if (!this.defaultValueProviderMap.containsKey(defaultValueProviderType)) {
                    P instance = null;

                    try {
                        // Create an instance using DefaultValueProvider(Opacker) constructor
                        try {
                            instance = ReflectionUtil.createInstance(defaultValueProviderType, this.opacker);
                        } catch (IllegalArgumentException exception) {
                            // Ok, let's find no parameter constructor
                        }

                        // Create an instance using DefaultValueProvider() constructor
                        if (instance == null) {
                            instance = ReflectionUtil.createInstance(defaultValueProviderType);
                        }
                    } catch (InvocationTargetException | IllegalAccessException exception) {
                        InstantiationException instantiationException = new InstantiationException(defaultValueProviderType.getSimpleName() + " default value provider can't instantiation.");
                        instantiationException.initCause(exception);

                        throw instantiationException;
                    }

                    if (instance == null) {
                        throw new InstantiationException(defaultValueProviderType.getSimpleName() + " default value provider must be implemented constructor(Opacker) or constructor().");
                    }

                    this.defaultValueProviderMap.put(defaultValueProviderType, instance);
                }
            }
        }

        return defaultValueProviderType.cast(this.defaultValueProviderMap.get(defaultValueProviderType));
    }
}