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

package com.realtimetech.opack.transformer.impl.map;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.transformer.impl.TypeWrapper;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrapMapTransformer extends MapTransformer {
    /**
     * Serializes the element to {@link OpackValue OpackValue}.
     *
     * @param opacker the opacker
     * @param element the element to be serialized
     * @return serialized value
     * @throws SerializeException if a problem occurs during serializing
     */
    @Override
    protected @Nullable Object serializeObject(@NotNull Opacker opacker, @Nullable Object element) throws SerializeException {
        return TypeWrapper.wrapObject(opacker,element);
    }

    /**
     * Deserializes the {@link OpackValue OpackValue}.
     *
     * @param opacker the opacker
     * @param element the element to be deserialized
     * @return deserialized element
     * @throws ClassNotFoundException if the class cannot be located
     * @throws DeserializeException   if a problem occurs during deserializing
     */
    @Override
    protected @Nullable Object deserializeObject(@NotNull Opacker opacker, @Nullable Object element) throws ClassNotFoundException, DeserializeException {
        return TypeWrapper.unwrapObject(opacker, element);
    }
}
