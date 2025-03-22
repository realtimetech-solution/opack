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

// - Original Header
// Copyright 2018 Ulf Adams
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.realtimetech.opack.codec.json.ryu;

import com.realtimetech.opack.codec.json.RoundingMode;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

public final class RyuJsonDouble {
    // Constants for internal double representation
    private static final int DOUBLE_MANTISSA_BITS = 52;
    private static final long DOUBLE_MANTISSA_MASK = (1L << DOUBLE_MANTISSA_BITS) - 1;
    private static final int DOUBLE_EXPONENT_BITS = 11;
    private static final int DOUBLE_EXPONENT_MASK = (1 << DOUBLE_EXPONENT_BITS) - 1;
    private static final int DOUBLE_EXPONENT_BIAS = (1 << (DOUBLE_EXPONENT_BITS - 1)) - 1;

    // Table sizes for precomputed powers
    private static final int POSITIVE_POWER5_TABLE_SIZE = 326;
    private static final int NEGATIVE_POWER5_TABLE_SIZE = 291;

    // Precomputed split representations of powers of 5
    private static final int POWER5_TOTAL_BIT_COUNT = 121;
    private static final int POWER5_QUARTER_BIT_COUNT = 31;
    private static final int[][] POWER5_SPLIT = new int[POSITIVE_POWER5_TABLE_SIZE][4];

    // Precomputed split representations of the inverse powers of 5
    private static final int POWER5_INVERSE_TOTAL_BIT_COUNT = 122; // max 3*31 = 124
    private static final int POWER5_INVERSE_QUARTER_BIT_COUNT = 31;
    private static final int[][] POWER5_INVERSE_SPLIT = new int[NEGATIVE_POWER5_TABLE_SIZE][4];

    static {
        // Precompute the split values for 5^i and its inverse using bit-level partitioning.
        BigInteger mask = BigInteger.ONE.shiftLeft(POWER5_QUARTER_BIT_COUNT).subtract(BigInteger.ONE);
        BigInteger maskInverse = BigInteger.ONE.shiftLeft(POWER5_INVERSE_QUARTER_BIT_COUNT).subtract(BigInteger.ONE);

        for (int i = 0; i < POSITIVE_POWER5_TABLE_SIZE; i++) {
            BigInteger powerOf5 = BigInteger.valueOf(5).pow(i);
            int powerOf5BitLength = powerOf5.bitLength();
            int expectedBitCount = power5Bits(i);

            if (expectedBitCount != powerOf5BitLength) {
                throw new IllegalStateException(powerOf5BitLength + " != " + expectedBitCount);
            }

            // Split 5^i into 4 chunks
            for (int chunk = 0; chunk < 4; chunk++) {
                POWER5_SPLIT[i][chunk] = powerOf5
                        .shiftRight(powerOf5BitLength - POWER5_TOTAL_BIT_COUNT + (3 - chunk) * POWER5_QUARTER_BIT_COUNT)
                        .and(mask)
                        .intValueExact();
            }

            // Fill inverse table when `i` is within bounds
            if (i < POWER5_INVERSE_SPLIT.length) {
                int shiftAmount = powerOf5BitLength - 1 + POWER5_INVERSE_TOTAL_BIT_COUNT;
                BigInteger inverse = BigInteger.ONE.shiftLeft(shiftAmount).divide(powerOf5).add(BigInteger.ONE);

                for (int chunk = 0; chunk < 4; chunk++) {
                    if (chunk == 0) {
                        POWER5_INVERSE_SPLIT[i][chunk] = inverse.shiftRight((3 - chunk) * POWER5_INVERSE_QUARTER_BIT_COUNT).intValueExact();
                    } else {
                        POWER5_INVERSE_SPLIT[i][chunk] = inverse.shiftRight((3 - chunk) * POWER5_INVERSE_QUARTER_BIT_COUNT)
                                .and(maskInverse)
                                .intValueExact();
                    }
                }
            }
        }
    }

    public static String toString(double value) {
        return toString(value, RoundingMode.ROUND_EVEN);
    }

    /**
     * Converts a double value to a string using the Ryu algorithm
     *
     * @param value        the double value to convert
     * @param roundingMode the rounding mode to apply
     * @return the string representation of the double value
     */
    public static String toString(double value, @NotNull RoundingMode roundingMode) {
        // Handle special cases: NaN, Infinity
        if (Double.isNaN(value)) {
            return "NaN";
        } else if (value == Double.POSITIVE_INFINITY) {
            return "Infinity";
        } else if (value == Double.NEGATIVE_INFINITY) {
            return "-Infinity";
        }

        long rawBits = Double.doubleToLongBits(value);
        if (rawBits == 0) {
            return "0.0";
        } else if (rawBits == 0x8000000000000000L) {
            return "-0.0";
        }

        int ieeeExponent = (int) ((rawBits >>> DOUBLE_MANTISSA_BITS) & DOUBLE_EXPONENT_MASK);
        long ieeeMantissa = rawBits & DOUBLE_MANTISSA_MASK;

        int binaryExponent;
        long mantissa;
        if (ieeeExponent == 0) {
            // Subnormal: no implicit leading 1
            binaryExponent = 1 - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS;
            mantissa = ieeeMantissa;
        } else {
            binaryExponent = ieeeExponent - DOUBLE_EXPONENT_BIAS - DOUBLE_MANTISSA_BITS;
            mantissa = ieeeMantissa | (1L << DOUBLE_MANTISSA_BITS);
        }

        boolean isNegative = rawBits < 0;
        boolean isMantissaEven = (mantissa & 1) == 0;

        // Scale up mantissa for Ryu algorithm (multiplying by 4)
        final int minorShift = ((mantissa != (1L << DOUBLE_MANTISSA_BITS)) || (ieeeExponent <= 1)) ? 1 : 0;
        final long scaledMantissa = 4 * mantissa;
        final long scaledMantissaPlus = 4 * mantissa + 2;
        final long scaledMantissaMinus = 4 * mantissa - 1 - minorShift;
        binaryExponent -= 2;

        long decimalValue, decimalUpperBound, decimalLowerBound;
        final int decimalExponent;
        boolean hasTrailingZerosLower = false;
        boolean hasTrailingZerosValue = false;

        if (binaryExponent >= 0) {
            // For positive binary exponent
            final int q = Math.max(0, ((binaryExponent * 78913) >>> 18) - 1);
            final int inverseShift = POWER5_INVERSE_TOTAL_BIT_COUNT + power5Bits(q) - 1;
            final int shift = -binaryExponent + q + inverseShift;

            decimalValue = multiplyPower5InverseDividePower2(scaledMantissa, q, shift);
            decimalUpperBound = multiplyPower5InverseDividePower2(scaledMantissaPlus, q, shift);
            decimalLowerBound = multiplyPower5InverseDividePower2(scaledMantissaMinus, q, shift);
            decimalExponent = q;

            if (q <= 21) {
                if (scaledMantissa % 5 == 0) {
                    hasTrailingZerosValue = isMultipleOfPowerOf5(scaledMantissa, q);
                } else if (roundingMode.acceptUpperBound(isMantissaEven)) {
                    hasTrailingZerosLower = isMultipleOfPowerOf5(scaledMantissaMinus, q);
                } else if (isMultipleOfPowerOf5(scaledMantissaPlus, q)) {
                    decimalUpperBound--;
                }
            }
        } else {
            // For negative binary exponent
            final int q = Math.max(0, ((-binaryExponent * 732923) >>> 20) - 1);
            final int powerIndex = -binaryExponent - q;
            final int power5BitDifference = power5Bits(powerIndex) - POWER5_TOTAL_BIT_COUNT;
            final int shift = q - power5BitDifference;

            decimalValue = multiplyPower5DividePower2(scaledMantissa, powerIndex, shift);
            decimalUpperBound = multiplyPower5DividePower2(scaledMantissaPlus, powerIndex, shift);
            decimalLowerBound = multiplyPower5DividePower2(scaledMantissaMinus, powerIndex, shift);
            decimalExponent = q + binaryExponent;

            if (q <= 1) {
                hasTrailingZerosValue = true;
                if (roundingMode.acceptUpperBound(isMantissaEven)) {
                    hasTrailingZerosLower = (minorShift == 1);
                } else {
                    decimalUpperBound--;
                }
            } else if (q < 63) {
                hasTrailingZerosValue = (scaledMantissa & ((1L << (q - 1)) - 1)) == 0;
            }
        }

        final int decimalDigitsCount = getDecimalLength(decimalUpperBound);
        int finalDecimalExponent = decimalExponent + decimalDigitsCount - 1;
        boolean useScientificNotation = !((finalDecimalExponent >= -3) && (finalDecimalExponent < 7));

        int removedDigits = 0;
        int lastRemovedDigit = 0;
        long roundedDecimal;

        if (hasTrailingZerosLower || hasTrailingZerosValue) {
            while (decimalUpperBound / 10 > decimalLowerBound / 10) {
                if ((decimalUpperBound < 100) && useScientificNotation) {
                    break;
                }

                hasTrailingZerosLower &= (decimalLowerBound % 10 == 0);
                hasTrailingZerosValue &= (lastRemovedDigit == 0);
                lastRemovedDigit = (int) (decimalValue % 10);

                decimalValue /= 10;
                decimalUpperBound /= 10;
                decimalLowerBound /= 10;

                removedDigits++;
            }

            if (hasTrailingZerosLower && roundingMode.acceptLowerBound(isMantissaEven)) {
                while (decimalLowerBound % 10 == 0) {
                    if ((decimalUpperBound < 100) && useScientificNotation) {
                        break;
                    }

                    hasTrailingZerosValue &= (lastRemovedDigit == 0);
                    lastRemovedDigit = (int) (decimalValue % 10);

                    decimalValue /= 10;
                    decimalUpperBound /= 10;
                    decimalLowerBound /= 10;

                    removedDigits++;
                }
            }

            if (hasTrailingZerosValue && (lastRemovedDigit == 5) && (decimalValue % 2 == 0)) {
                lastRemovedDigit = 4;
            }

            roundedDecimal = decimalValue + ((decimalValue == decimalLowerBound &&
                    !(hasTrailingZerosLower && roundingMode.acceptLowerBound(isMantissaEven)))
                    || (lastRemovedDigit >= 5) ? 1 : 0);
        } else {
            while (decimalUpperBound / 10 > decimalLowerBound / 10) {
                if ((decimalUpperBound < 100) && useScientificNotation) {
                    break;
                }

                lastRemovedDigit = (int) (decimalValue % 10);

                decimalValue /= 10;
                decimalUpperBound /= 10;
                decimalLowerBound /= 10;

                removedDigits++;
            }

            roundedDecimal = decimalValue + ((decimalValue == decimalLowerBound || (lastRemovedDigit >= 5)) ? 1 : 0);
        }

        int outputLength = decimalDigitsCount - removedDigits;
        char[] result = new char[24];
        int index = 0;

        if (isNegative) {
            result[index++] = '-';
        }

        if (useScientificNotation) {
            // Scientific notation: first digit, decimal point, rest of digits and exponent part.
            for (int i = 0; i < outputLength - 1; i++) {
                int digit = (int) (roundedDecimal % 10);
                roundedDecimal /= 10;
                result[index + outputLength - i] = (char) ('0' + digit);
            }

            result[index] = (char) ('0' + roundedDecimal % 10);
            result[index + 1] = '.';
            index += outputLength + 1;

            if (outputLength == 1) {
                result[index++] = '0';
            }

            result[index++] = 'E';

            if (finalDecimalExponent < 0) {
                result[index++] = '-';
                finalDecimalExponent = -finalDecimalExponent;
            }

            if (finalDecimalExponent >= 100) {
                result[index++] = (char) ('0' + finalDecimalExponent / 100);
                finalDecimalExponent %= 100;
                result[index++] = (char) ('0' + finalDecimalExponent / 10);
            } else if (finalDecimalExponent >= 10) {
                result[index++] = (char) ('0' + finalDecimalExponent / 10);
            }

            result[index++] = (char) ('0' + finalDecimalExponent % 10);

            return new String(result, 0, index);
        } else {
            // Regular notation: separate integer and fractional parts based on the position of the decimal point.
            //noinspection DuplicatedCode
            if (finalDecimalExponent < 0) {
                result[index++] = '0';
                result[index++] = '.';

                for (int i = -1; i > finalDecimalExponent; i--) {
                    result[index++] = '0';
                }

                int currentIndex = index;
                for (int i = 0; i < outputLength; i++) {
                    result[currentIndex + outputLength - i - 1] = (char) ('0' + roundedDecimal % 10);
                    roundedDecimal /= 10;
                    index++;
                }
            } else if (finalDecimalExponent + 1 >= outputLength) {
                for (int i = 0; i < outputLength; i++) {
                    result[index + outputLength - i - 1] = (char) ('0' + roundedDecimal % 10);
                    roundedDecimal /= 10;
                }

                index += outputLength;

                for (int i = outputLength; i < finalDecimalExponent + 1; i++) {
                    result[index++] = '0';
                }

                result[index++] = '.';
                result[index++] = '0';
            } else {
                int currentIndex = index + 1;
                for (int i = 0; i < outputLength; i++) {
                    if (outputLength - i - 1 == finalDecimalExponent) {
                        result[currentIndex + outputLength - i - 1] = '.';
                        currentIndex--;
                    }

                    result[currentIndex + outputLength - i - 1] = (char) ('0' + roundedDecimal % 10);
                    roundedDecimal /= 10;
                }

                index += outputLength + 1;
            }

            return new String(result, 0, index);
        }
    }

    /**
     * Computes the number of bits required for 5^e
     */
    private static int power5Bits(int e) {
        return ((e * 1217359) >>> 19) + 1;
    }

    /**
     * Returns the number of digits in the given value.
     */
    private static int getDecimalLength(long value) {
        if (value >= 1000000000000000000L) return 19;
        if (value >= 100000000000000000L) return 18;
        if (value >= 10000000000000000L) return 17;
        if (value >= 1000000000000000L) return 16;
        if (value >= 100000000000000L) return 15;
        if (value >= 10000000000000L) return 14;
        if (value >= 1000000000000L) return 13;
        if (value >= 100000000000L) return 12;
        if (value >= 10000000000L) return 11;
        if (value >= 1000000000L) return 10;
        if (value >= 100000000L) return 9;
        if (value >= 10000000L) return 8;
        if (value >= 1000000L) return 7;
        if (value >= 100000L) return 6;
        if (value >= 10000L) return 5;
        if (value >= 1000L) return 4;
        if (value >= 100L) return 3;
        if (value >= 10L) return 2;
        return 1;
    }

    /**
     * Checks if the given value is divisible by 5^q.
     */
    private static boolean isMultipleOfPowerOf5(long value, int q) {
        return power5Factor(value) >= q;
    }

    /**
     * Returns the highest exponent such that 5^exponent divides value.
     */
    private static int power5Factor(long value) {
        if (value % 5 != 0) return 0;
        if (value % 25 != 0) return 1;
        if (value % 125 != 0) return 2;
        if (value % 625 != 0) return 3;
        int count = 4;
        value /= 625;
        while (value > 0) {
            if (value % 5 != 0) {
                return count;
            }
            value /= 5;
            count++;
        }
        throw new IllegalArgumentException("" + value);
    }

    /**
     * Multiplies the given value by 5^powerIndex and then divides by 2^shift.
     */
    private static long multiplyPower5DividePower2(long value, int powerIndex, int shift) {
        long highPart = value >>> 31;
        long lowPart = value & 0x7fffffff;
        long part13 = highPart * POWER5_SPLIT[powerIndex][0];
        long part03 = lowPart * POWER5_SPLIT[powerIndex][0];
        long part12 = highPart * POWER5_SPLIT[powerIndex][1];
        long part02 = lowPart * POWER5_SPLIT[powerIndex][1];
        long part11 = highPart * POWER5_SPLIT[powerIndex][2];
        long part01 = lowPart * POWER5_SPLIT[powerIndex][2];
        long part10 = highPart * POWER5_SPLIT[powerIndex][3];
        long part00 = lowPart * POWER5_SPLIT[powerIndex][3];
        int actualShift = shift - 3 * 31 - 21;

        if (actualShift < 0) {
            throw new IllegalArgumentException("" + actualShift);
        }

        return ((((((((part00 >>> 31) + part01 + part10) >>> 31)
                + part02 + part11) >>> 31)
                + part03 + part12) >>> 21)
                + (part13 << 10)) >>> actualShift;
    }

    /**
     * Multiplies the given value by the inverse of 5^q and then divides by 2^shift.
     */
    private static long multiplyPower5InverseDividePower2(long value, int q, int shift) {
        long highPart = value >>> 31;
        long lowPart = value & 0x7fffffff;
        long part13 = highPart * POWER5_INVERSE_SPLIT[q][0];
        long part03 = lowPart * POWER5_INVERSE_SPLIT[q][0];
        long part12 = highPart * POWER5_INVERSE_SPLIT[q][1];
        long part02 = lowPart * POWER5_INVERSE_SPLIT[q][1];
        long part11 = highPart * POWER5_INVERSE_SPLIT[q][2];
        long part01 = lowPart * POWER5_INVERSE_SPLIT[q][2];
        long part10 = highPart * POWER5_INVERSE_SPLIT[q][3];
        long part00 = lowPart * POWER5_INVERSE_SPLIT[q][3];
        int actualShift = shift - 3 * 31 - 21;

        if (actualShift < 0) {
            throw new IllegalArgumentException("" + actualShift);
        }

        return ((((((((part00 >>> 31) + part01 + part10) >>> 31)
                + part02 + part11) >>> 31)
                + part03 + part12) >>> 21)
                + (part13 << 10)) >>> actualShift;
    }
}
