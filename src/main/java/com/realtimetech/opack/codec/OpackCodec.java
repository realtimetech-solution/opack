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

import java.io.IOException;

public abstract class OpackCodec<I, O> {
    /**
     * Writes a code that encodes opack value by overriding this
     *
     * @param output the output to store an encoded result
     * @param input  the input object to encode
     * @throws IOException if I/O error occurs
     */
    protected abstract void encodeObject(@NotNull O output, @Nullable Object input) throws IOException;

    /**
     * Writes a code that decodes the value encoded by overriding this
     *
     * @param input the input to decode
     * @return the decoded result
     * @throws IOException if I/O error occurs
     */
    protected abstract @Nullable Object decodeObject(@NotNull I input) throws IOException;

    /**
     * Encodes the opack value through a specific codec
     *
     * @param output the output to encode
     * @param object the object to encode
     * @throws EncodeException if a problem occurs during encoding, if the type of data to be encoded is not allowed in specific codec
     */
    public final synchronized void fromObject(@NotNull O output, @NotNull Object object) throws EncodeException {
        try {
            this.encodeObject(output, object);
        } catch (Exception exception) {
            throw new EncodeException(exception);
        }
    }

    /**
     * Decodes the value encoded through a specific codec
     *
     * @param input the input to decode
     * @return decoded value
     * @throws DecodeException if a problem occurs during decoding, if the type of data to be decoded is not allowed in specific codec
     */
    public final synchronized Object toObject(@NotNull I input) throws DecodeException {
        try {
            return this.decodeObject(input);
        } catch (Exception exception) {
            throw new DecodeException(exception);
        }
    }

    /**
     * Encodes the opack value through a specific codec
     *
     * @param output the output to encode
     * @param object the object to encode
     * @throws EncodeException if a problem occurs during encoding, if the type of data to be encoded is not allowed in specific codec
     */
    public final synchronized void fromOpackValue(@NotNull O output, @NotNull OpackValue object) throws EncodeException {
        try {
            this.encodeObject(output, object);
        } catch (Exception exception) {
            throw new EncodeException(exception);
        }
    }

    /**
     * Decodes the value encoded through a specific codec
     *
     * @param input the input to decode
     * @return decoded value
     * @throws DecodeException if a problem occurs during decoding, if the type of data to be decoded is not allowed in specific codec
     */
    public final synchronized OpackValue toOpackValue(@NotNull I input) throws DecodeException {
        try {
            Object decodedObject = this.decodeObject(input);

            if (decodedObject instanceof OpackValue) {
                return (OpackValue) decodedObject;
            }

            throw new IllegalArgumentException("Successfully decoded but given input is not an OpackValue.");
        } catch (Exception exception) {
            throw new DecodeException(exception);
        }
    }


    /**
     * Encodes the opack value through a specific codec
     *
     * @param output     the output to encode
     * @param opackValue the opack value to encode
     * @throws EncodeException if a problem occurs during encoding, if the type of data to be encoded is not allowed in specific codec
     */
    @Deprecated(forRemoval=true)
    public final synchronized void encode(@NotNull O output, @NotNull OpackValue opackValue) throws EncodeException {
        try {
            this.encodeObject(output, opackValue);
        } catch (Exception exception) {
            throw new EncodeException(exception);
        }
    }

    /**
     * Decodes the value encoded through a specific codec
     *
     * @param input the input to decode
     * @return decoded value
     * @throws DecodeException if a problem occurs during decoding, if the type of data to be decoded is not allowed in specific codec
     */
    @Deprecated(forRemoval=true)
    public final synchronized OpackValue decode(@NotNull I input) throws DecodeException {
        try {
            Object decodedObject = this.decodeObject(input);

            if (decodedObject instanceof OpackValue) {
                return (OpackValue) decodedObject;
            }

            throw new IllegalArgumentException("Successfully decoded but given input is not an OpackValue.");
        } catch (Exception exception) {
            throw new DecodeException(exception);
        }
    }
}