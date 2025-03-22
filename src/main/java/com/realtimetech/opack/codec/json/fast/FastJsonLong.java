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

package com.realtimetech.opack.codec.json.fast;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public final class FastJsonLong {
    /**
     * Parses the given string into a {@link Number}.
     *
     * @param string the string containing the numeric data to be parsed
     * @return the parsed number as a {@link Number}, either as a {@code long} or a {@code BigInteger} for large values
     * @throws NumberFormatException if the string does not contain a parsable numeric value
     */
    public static @NotNull Number parseLong(@NotNull String string) throws NumberFormatException {
        return parseLong(string.toCharArray(), 0, string.length());
    }

    /**
     * Parses the given string into a {@link Number}
     *
     * @param charArray the character array containing the numeric data
     * @param offset    the starting index within the character array
     * @param count     the number of characters to parse starting from the offset
     * @return the parsed number as a {@link Number}, either as a long or a BigInteger for large values
     * @throws NumberFormatException if the content is not a valid numeric format
     */
    public static @NotNull Number parseLong(char @NotNull [] charArray, int offset, int count) throws NumberFormatException {
        if (count == 0 || charArray.length < offset + count) {
            throw new NumberFormatException("Not allow empty input");
        }

        int index = offset;
        int end = offset + count;

        boolean negative = false;
        char firstCharacter = charArray[index];

        if (firstCharacter == '-') {
            negative = true;
            index++;
        }

        if (firstCharacter == '+') {
            index++;
        }

        if (index >= end) {
            throw new NumberFormatException("Can't have lone \"+\" or \"-\": \"" + new String(charArray, offset, count) + "\"");
        }

        if (charArray[index] == '0' && end > index + 1) {
            throw new NumberFormatException("Leading zeros not allowed: \"" + new String(charArray, offset, count) + "\"");
        }

        long result = 0;

        while (index < end) {
            char character = charArray[index++];

            if (character < '0' || character > '9') {
                throw new NumberFormatException("Invalid digit: \"" + new String(charArray, offset, count) + "\"");
            }

            int digit = character - '0';
            long previousResult = result;

            result = result * 10 + digit;

            if (result < previousResult) {
                return new BigInteger(new String(charArray, offset, count));
            }
        }

        if (index != end) {
            throw new NumberFormatException("Expected end of number but remained: \"" + new String(charArray, offset, count) + "\"");
        }

        if (negative) {
            return -result;
        }

        return result;
    }
}
