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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public final class FastJsonDouble {
    private static final int SIGNIFICAND_WIDTH = 53;
    private static final int EXPONENT_BIAS = 1023;

    // 10^MIN_TEN_EXPONENT ~ 10^MAX_TEN_EXPONENT
    private static final int MIN_TEN_EXPONENT = -325;   // 2.2250738585072014e-308 + Denormalized
    private static final int MAX_TEN_EXPONENT = 308;    // 1.7976931348623157e+308

    private static final long @NotNull [] NORMALIZED_MANTISSA_TABLE = new long[-MIN_TEN_EXPONENT + MAX_TEN_EXPONENT + 1];

    static {
        MathContext mathContext = new MathContext(200, RoundingMode.DOWN);
        BigDecimal binaryLog10 = new BigDecimal("3.32192809488736234787031942948939017586483139302458012", mathContext);

        for (int exponent = MIN_TEN_EXPONENT; exponent <= MAX_TEN_EXPONENT; exponent++) {
            BigDecimal exponentDecimal = new BigDecimal(exponent);
            BigDecimal logProduct = exponentDecimal.multiply(binaryLog10, mathContext);
            int binaryExponent = logProduct.setScale(0, RoundingMode.FLOOR).intValue() + 1;
            // Goal: normalizedMantissa = floor(10^exponentDecimal * 2^(64 - binaryExponent))

            BigInteger normalizedMantissa;
            int shiftAmount = 64 - binaryExponent;

            if (exponent >= 0) {
                BigInteger powerOfTen = BigInteger.TEN.pow(exponent);

                if (shiftAmount >= 0) {
                    normalizedMantissa = powerOfTen.shiftLeft(shiftAmount);
                } else {
                    normalizedMantissa = powerOfTen.shiftRight(-shiftAmount);
                }
            } else {
                // Negative: 10^(exponent) = 1 / 10^(|exponent|)
                int positiveTenExponent = -exponent;
                BigInteger powerOfTen = BigInteger.TEN.pow(positiveTenExponent);

                if (shiftAmount >= 0) {
                    // normalizedMantissa = floor(2^(64 - binaryExponent) / 10^(|exponent|))
                    BigInteger powerOfTwo = BigInteger.ONE.shiftLeft(shiftAmount);
                    BigDecimal numeratorDecimal = new BigDecimal(powerOfTwo);
                    BigDecimal denominatorDecimal = new BigDecimal(powerOfTen);
                    BigDecimal divisionResult = numeratorDecimal.divide(denominatorDecimal, mathContext);

                    normalizedMantissa = divisionResult.setScale(0, RoundingMode.DOWN).toBigInteger();
                } else {
                    // Negative: 2^(64 - binaryExponent) is a fraction
                    BigDecimal powerOfTwoDecimal = BigDecimal.ONE.divide(
                            new BigDecimal(BigInteger.ONE.shiftLeft(-shiftAmount), mathContext),
                            mathContext
                    ); // numeratorDecimal
                    BigDecimal denominatorDecimal = new BigDecimal(powerOfTen);
                    BigDecimal divisionResult = powerOfTwoDecimal.divide(denominatorDecimal, mathContext);
                    normalizedMantissa = divisionResult.setScale(0, RoundingMode.DOWN).toBigInteger();
                }
            }

            NORMALIZED_MANTISSA_TABLE[exponent - MIN_TEN_EXPONENT] = normalizedMantissa.longValue();
        }
    }

    private static final double @NotNull [] POWER_TABLE = new double[23];

    static {
        for (int i = 0; i < POWER_TABLE.length; i++) {
            POWER_TABLE[i] = Math.pow(10, i);
        }
    }

    /**
     * Parses the given string into a {@link Number}.
     *
     * @param string the string containing the numeric data to be parsed
     * @return the parsed number as a {@link Number}, either as a {@code Double} or a {@code BigDecimal} for large values
     * @throws NumberFormatException if the string does not contain a parsable numeric value
     */
    public static @NotNull Number parseDouble(@NotNull String string) throws NumberFormatException {
        return parseDouble(string.toCharArray(), 0, string.length());
    }

    /**
     * Parses the given string into a {@link Number}
     *
     * @param charArray the character array containing the numeric data
     * @param offset    the starting index within the character array
     * @param count     the number of characters to parse starting from the offset
     * @return the parsed number as a {@link Number}, either as a {@code Double} or a {@code BigDecimal} for large values
     * @throws NumberFormatException if the content is not a valid numeric format
     */
    public static @NotNull Number parseDouble(char @NotNull [] charArray, int offset, int count) throws NumberFormatException {
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

        long mantissa = 0;
        int mantissaDigits = 0;
        int exponent = 0;

        int significandDigits = -1;
        int fractionDigits = 0;

        while (index < end) {
            char character = charArray[index++];

            if (character >= '0' && character <= '9') {
                int digit = character - '0';
                long previousMantissa = mantissa;

                mantissa = mantissa * 10 + digit;
                mantissaDigits++;

                if (mantissa < previousMantissa) {
                    return new BigDecimal(new String(charArray, offset, count));
                }
            } else if (character == '.') {
                if (significandDigits != -1) {
                    throw new NumberFormatException("Too many mantissa parts: \"" + new String(charArray, offset, count) + "\"");
                }
                significandDigits = mantissaDigits;
            } else {
                index--;
                break;
            }
        }

        if (significandDigits == -1) {
            significandDigits = mantissaDigits;
        }

        fractionDigits = mantissaDigits - significandDigits;

        if (index < end && (charArray[index] == 'e' || charArray[index] == 'E')) {
            index++;

            if (index >= end) {
                throw new NumberFormatException("No exponent digits: \"" + new String(charArray, offset, count) + "\"");
            }

            boolean exponentNegative = false;
            char exponentNextCharacter = charArray[index];

            if (exponentNextCharacter == '-') {
                exponentNegative = true;
                index++;
            }

            if (exponentNextCharacter == '+') {
                index++;
            }

            if (charArray[index] < '0' || charArray[index] > '9') {
                throw new NumberFormatException("No exponent digits: \"" + new String(charArray, offset, count) + "\"");
            }

            while (index < end) {
                char character = charArray[index++];

                if (character < '0' || character > '9') {
                    break;
                }

                int digit = character - '0';
                int previousExponent = exponent;

                exponent = exponent * 10 + digit;

                if (exponent < previousExponent) {
                    return new BigDecimal(new String(charArray, offset, count));
                }
            }

            if (exponentNegative) {
                exponent = -exponent;
            }
        }

        if (index != end) {
            throw new NumberFormatException("Expected end of number but remained: \"" + new String(charArray, offset, count) + "\"");
        }

        exponent -= fractionDigits;

        if (exponent < MIN_TEN_EXPONENT || exponent > MAX_TEN_EXPONENT) {
            return new BigDecimal(new String(charArray, offset, count));
        }

        // 1. Fast Path
        if (exponent >= -22 && exponent <= 22 && mantissa < (1L << SIGNIFICAND_WIDTH) - 1) {
            double value = (double) mantissa;

            if (exponent > 0) {
                value *= POWER_TABLE[exponent];
            } else {
                value /= POWER_TABLE[-exponent];
            }

            return negative ? -value : value;
        }

        // 2. Using Daniel Lemire's `fast_float` algorithm
        long factorMantissa = NORMALIZED_MANTISSA_TABLE[exponent - MIN_TEN_EXPONENT];
        long computedExponent = (((152170L + 65536L) * exponent) >> 16) + EXPONENT_BIAS + 64;
        int leadingZeros = Long.numberOfLeadingZeros(mantissa);
        long shiftedMantissa = mantissa << leadingZeros;
        long upper = unsignedMultiplyHigh(shiftedMantissa, factorMantissa);
        long upperBit = upper >>> 63;
        long computedMantissa = upper >>> (upperBit + 9);

        leadingZeros += (int) (1 ^ upperBit);

        if (((upper & 0x1ff) != 0x1ff) && (((upper & 0x1ff) != 0) || ((computedMantissa & 3) != 1))) {
            computedMantissa += 1;
            computedMantissa >>>= 1;

            if (computedMantissa >= (1L << SIGNIFICAND_WIDTH)) {
                computedMantissa = (1L << (SIGNIFICAND_WIDTH - 1));
                leadingZeros--;
            }

            computedMantissa &= ~(1L << (SIGNIFICAND_WIDTH - 1));

            long realExponent = computedExponent - leadingZeros;

            if (realExponent >= 1 && realExponent <= (MAX_TEN_EXPONENT + EXPONENT_BIAS)) {
                long bits = computedMantissa | (realExponent << (SIGNIFICAND_WIDTH - 1)) | (negative ? (1L << 63) : 0L);

                return Double.longBitsToDouble(bits);
            }
        }

        // 3. Using JDK built-in method
        return Double.parseDouble(new String(charArray, offset, count));
    }

    private static long unsignedMultiplyHigh(long x, long y) {
        long x0 = x & 0xffffffffL, x1 = x >>> 32;
        long y0 = y & 0xffffffffL, y1 = y >>> 32;
        long p11 = x1 * y1, p01 = x0 * y1;
        long p10 = x1 * y0, p00 = x0 * y0;

        long middle = p10 + (p00 >>> 32) + (p01 & 0xffffffffL);

        return p11 + (middle >>> 32) + (p01 >>> 32);
    }
}
