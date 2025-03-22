/*
 * Copyright (C) 2025 REALTIMETECH All Rights Reserved
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

package com.realtimetech.opack.codec.json;

import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for easy to use {@link JsonCodec}
 */
public final class Json {
    private static final @NotNull ThreadLocal<JsonCodec> JSON_CODEC_THREAD_LOCAL = ThreadLocal.withInitial(() -> JsonCodec.Builder.create()
            .setEnableConvertCharacterToString(true)
            .setAllowAnyValueToKey(false)
            .build());

    /**
     * Encodes the {@link OpackValue OpackValue} into JSON string
     *
     * @param opackValue the opack value to encode
     * @return the encoded JSON string
     * @throws EncodeException if a problem occurs during encoding, if the type of data to be encoded is not allowed in a specific codec
     */
    public static @NotNull String encode(@NotNull OpackValue opackValue) throws EncodeException {
        return JSON_CODEC_THREAD_LOCAL.get().encode(opackValue);
    }

    /**
     * Encodes the {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue} into JSON string
     *
     * @param object the object to encode
     * @return the encoded JSON string
     * @throws EncodeException if a problem occurs during encoding, if the type of data to be encoded is not allowed in a specific codec
     */
    public static @NotNull String encodeObject(@NotNull Object object) throws EncodeException {
        return JSON_CODEC_THREAD_LOCAL.get().encodeObject(object);
    }


    /**
     * Decodes the JSON string into {@link OpackValue OpackValue}
     *
     * @param input the input to decode
     * @return the decoded result
     * @throws DecodeException if a problem occurs during decoding
     */
    public static @NotNull OpackValue decode(@NotNull String input) throws DecodeException {
        return JSON_CODEC_THREAD_LOCAL.get().decode(input);
    }

    /**
     * Decodes the JSON string into {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue}
     *
     * @param input the input to decode
     * @return the decoded result
     * @throws DecodeException if a problem occurs during decoding
     */
    public static @Nullable Object decodeObject(@NotNull String input) throws DecodeException {
        return JSON_CODEC_THREAD_LOCAL.get().decodeObject(input);
    }
}
