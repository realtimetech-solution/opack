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

package com.realtimetech.opack.codec;

import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OpackCodec<I, Writer> {
    /**
     * Encodes the {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue} through a codec
     *
     * @param writer the writer to store an encoded result
     * @param object the object to encode
     * @throws EncodeException if a problem occurs during encoding
     */
    protected abstract void encodeObject(@NotNull Writer writer, @Nullable Object object) throws EncodeException;

    /**
     * Decodes the input through a codec into {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue}
     *
     * @param input the input to decode
     * @return the decoded result
     * @throws DecodeException if a problem occurs during decoding
     */
    protected abstract @Nullable Object decodeObject(@NotNull I input) throws DecodeException;

    /**
     * Encodes the {@link OpackValue OpackValue} through a codec
     *
     * @param writer the writer to store an encoded result
     * @param object the object to encode
     * @throws EncodeException if a problem occurs during encoding
     */
    public final synchronized void encode(@NotNull Writer writer, @NotNull OpackValue object) throws EncodeException {
        this.encodeObject(writer, object);
    }

    /**
     * Decodes the input through a codec into {@link OpackValue OpackValue}
     *
     * @param input the input to decode
     * @return the decoded result
     * @throws DecodeException if a problem occurs during decoding
     */
    public final synchronized @NotNull OpackValue decode(@NotNull I input) throws DecodeException {
        Object decodedObject = this.decodeObject(input);

        if (decodedObject instanceof OpackValue) {
            return (OpackValue) decodedObject;
        }

        throw new DecodeException("Successfully decoded but given input is not an OpackValue.");
    }
}