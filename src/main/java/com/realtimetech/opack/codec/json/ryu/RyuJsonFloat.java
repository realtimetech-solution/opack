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

public final class RyuJsonFloat {
    // Constants for internal float representation
    private static final int FLOAT_MANTISSA_BITS = 23;
    private static final int FLOAT_MANTISSA_MASK = (1 << FLOAT_MANTISSA_BITS) - 1;
    private static final int FLOAT_EXPONENT_BITS = 8;
    private static final int FLOAT_EXPONENT_MASK = (1 << FLOAT_EXPONENT_BITS) - 1;
    private static final int FLOAT_EXPONENT_BIAS = (1 << (FLOAT_EXPONENT_BITS - 1)) - 1;

    // Logarithmic constants
    private static final long LOG10_2_DENOMINATOR = 10000000L;
    private static final long LOG10_2_NUMERATOR = (long) (LOG10_2_DENOMINATOR * Math.log10(2));
    private static final long LOG10_5_DENOMINATOR = 10000000L;
    private static final long LOG10_5_NUMERATOR = (long) (LOG10_5_DENOMINATOR * Math.log10(5));
    private static final long LOG2_5_DENOMINATOR = 10000000L;
    private static final long LOG2_5_NUMERATOR = (long) (LOG2_5_DENOMINATOR * (Math.log(5) / Math.log(2)));

    // Table sizes for precomputed powers
    private static final int POSITIVE_POWER5_TABLE_SIZE = 47;
    private static final int NEGATIVE_POWER5_TABLE_SIZE = 31;

    // Precomputed split representations of powers of 5
    private static final int POWER5_BIT_COUNT = 61;
    private static final int POWER5_HALF_BIT_COUNT = 31;
    private static final int[][] POWER5_SPLIT = new int[POSITIVE_POWER5_TABLE_SIZE][2];

    // Precomputed split representations of the inverse powers of 5
    private static final int POWER5_INVERSE_BIT_COUNT = 59;
    private static final int POWER5_INVERSE_HALF_BIT_COUNT = 31;
    private static final int[][] POWER5_INVERSE_SPLIT = new int[NEGATIVE_POWER5_TABLE_SIZE][2];

    static {
        // Precompute the split values for 5^i and its inverse.
        BigInteger mask = BigInteger.valueOf(1).shiftLeft(POWER5_HALF_BIT_COUNT).subtract(BigInteger.ONE);
        BigInteger maskInverse = BigInteger.valueOf(1).shiftLeft(POWER5_INVERSE_HALF_BIT_COUNT).subtract(BigInteger.ONE);
        int maxIterations = Math.max(POSITIVE_POWER5_TABLE_SIZE, NEGATIVE_POWER5_TABLE_SIZE);

        for (int i = 0; i < maxIterations; i++) {
            BigInteger powerOf5 = BigInteger.valueOf(5).pow(i);
            int powerOf5BitLength = powerOf5.bitLength();
            int expectedPow5Bits = power5Bits(i);

            if (expectedPow5Bits != powerOf5BitLength) {
                throw new IllegalStateException(powerOf5BitLength + " != " + expectedPow5Bits);
            }

            // Split 5^i into two parts.
            POWER5_SPLIT[i][0] = powerOf5.shiftRight(powerOf5BitLength - POWER5_BIT_COUNT + POWER5_HALF_BIT_COUNT)
                    .intValueExact();
            POWER5_SPLIT[i][1] = powerOf5.shiftRight(powerOf5BitLength - POWER5_BIT_COUNT)
                    .and(mask)
                    .intValueExact();

            if (i < NEGATIVE_POWER5_TABLE_SIZE) {
                int shift = powerOf5BitLength - 1 + POWER5_INVERSE_BIT_COUNT;
                BigInteger inv = BigInteger.ONE.shiftLeft(shift).divide(powerOf5).add(BigInteger.ONE);
                POWER5_INVERSE_SPLIT[i][0] = inv.shiftRight(POWER5_INVERSE_HALF_BIT_COUNT).intValueExact();
                POWER5_INVERSE_SPLIT[i][1] = inv.and(maskInverse).intValueExact();
            }
        }
    }

    public static String toString(float value) {
        return toString(value, RoundingMode.ROUND_EVEN);
    }

    /**
     * Converts a float value to a string using the Ryu algorithm
     *
     * @param value        the float value to convert
     * @param roundingMode the rounding mode to apply
     * @return the string representation of the float value
     */
    public static String toString(float value, @NotNull RoundingMode roundingMode) {
        // Handle special cases: NaN, Infinity
        if (Float.isNaN(value)) {
            return "NaN";
        } else if (value == Float.POSITIVE_INFINITY) {
            return "Infinity";
        } else if (value == Float.NEGATIVE_INFINITY) {
            return "-Infinity";
        }

        int rawBits = Float.floatToIntBits(value);
        if (rawBits == 0) {
            return "0.0";
        } else if (rawBits == 0x80000000) {
            return "-0.0";
        }

        int ieeeExponent = (rawBits >> FLOAT_MANTISSA_BITS) & FLOAT_EXPONENT_MASK;
        int ieeeMantissa = rawBits & FLOAT_MANTISSA_MASK;

        int binaryExponent;
        int mantissa;
        if (ieeeExponent == 0) {
            // Subnormal: no implicit leading 1
            binaryExponent = 1 - FLOAT_EXPONENT_BIAS - FLOAT_MANTISSA_BITS;
            mantissa = ieeeMantissa;
        } else {
            binaryExponent = ieeeExponent - FLOAT_EXPONENT_BIAS - FLOAT_MANTISSA_BITS;
            mantissa = ieeeMantissa | (1 << FLOAT_MANTISSA_BITS);
        }

        boolean isNegative = rawBits < 0;
        boolean isMantissaEven = (mantissa & 1) == 0;

        // Scale up mantissa for Ryu algorithm (multiplying by 4)
        final int scaledValue = 4 * mantissa;
        final int scaledValuePlus = 4 * mantissa + 2;
        final int scaledValueMinus = 4 * mantissa - (((mantissa != (1 << FLOAT_MANTISSA_BITS)) || (ieeeExponent <= 1)) ? 2 : 1);
        binaryExponent -= 2;

        int decimalValue, decimalUpperBound, decimalLowerBound;
        final int decimalExponent;
        boolean hasTrailingZerosValue;
        boolean hasTrailingZerosUpper;
        boolean hasTrailingZerosLower;

        int lastRemovedDigit = 0;

        if (binaryExponent >= 0) {
            // For positive binary exponent
            final int q = (int) (binaryExponent * LOG10_2_NUMERATOR / LOG10_2_DENOMINATOR);
            final int inverseShift = POWER5_INVERSE_BIT_COUNT + power5Bits(q) - 1;
            final int totalShift = -binaryExponent + q + inverseShift;

            decimalValue = (int) multiplyPower5InverseDividePower2(scaledValue, q, totalShift);
            decimalUpperBound = (int) multiplyPower5InverseDividePower2(scaledValuePlus, q, totalShift);
            decimalLowerBound = (int) multiplyPower5InverseDividePower2(scaledValueMinus, q, totalShift);
            decimalExponent = q;

            if (q != 0 && ((decimalUpperBound - 1) / 10 <= decimalLowerBound / 10)) {
                int l = POWER5_INVERSE_BIT_COUNT + power5Bits(q - 1) - 1;
                lastRemovedDigit = (int) (multiplyPower5InverseDividePower2(scaledValue, q - 1, -binaryExponent + q - 1 + l) % 10);
            }

            hasTrailingZerosUpper = power5Factor(scaledValuePlus) >= q;
            hasTrailingZerosValue = power5Factor(scaledValue) >= q;
            hasTrailingZerosLower = power5Factor(scaledValueMinus) >= q;
        } else {
            // For negative binary exponent
            final int q = (int) (-binaryExponent * LOG10_5_NUMERATOR / LOG10_5_DENOMINATOR);
            final int powerIndex = -binaryExponent - q;
            final int power5BitDifference = power5Bits(powerIndex) - POWER5_BIT_COUNT;
            int shift = q - power5BitDifference;

            decimalValue = (int) multiplyPower5DividePower2(scaledValue, powerIndex, shift);
            decimalUpperBound = (int) multiplyPower5DividePower2(scaledValuePlus, powerIndex, shift);
            decimalLowerBound = (int) multiplyPower5DividePower2(scaledValueMinus, powerIndex, shift);
            decimalExponent = q + binaryExponent;

            if (q != 0 && ((decimalUpperBound - 1) / 10 <= decimalLowerBound / 10)) {
                shift = q - 1 - (power5Bits(powerIndex + 1) - POWER5_BIT_COUNT);
                lastRemovedDigit = (int) (multiplyPower5DividePower2(scaledValue, powerIndex + 1, shift) % 10);
            }

            hasTrailingZerosUpper = (1 >= q);
            hasTrailingZerosValue = (q < FLOAT_MANTISSA_BITS) && ((scaledValue & ((1 << (q - 1)) - 1)) == 0);
            hasTrailingZerosLower = (((scaledValueMinus % 2 == 1) ? 0 : 1) >= q);
        }

        final int decimalDigitsCount = getDecimalLength(decimalUpperBound);
        int finalDecimalExponent = decimalExponent + decimalDigitsCount - 1;
        boolean useScientificNotation = !((finalDecimalExponent >= -3) && (finalDecimalExponent < 7));

        int removedDigits = 0;

        if (hasTrailingZerosUpper && !roundingMode.acceptUpperBound(isMantissaEven)) {
            decimalUpperBound--;
        }

        while (decimalUpperBound / 10 > decimalLowerBound / 10) {
            if ((decimalUpperBound < 100) && useScientificNotation) {
                break;
            }

            hasTrailingZerosLower &= (decimalLowerBound % 10 == 0);
            lastRemovedDigit = decimalValue % 10;

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

                lastRemovedDigit = decimalValue % 10;

                decimalValue /= 10;
                decimalUpperBound /= 10;
                decimalLowerBound /= 10;

                removedDigits++;
            }
        }

        if (hasTrailingZerosValue && (lastRemovedDigit == 5) && (decimalValue % 2 == 0)) {
            lastRemovedDigit = 4;
        }

        int roundedDecimal = decimalValue + (((decimalValue == decimalLowerBound &&
                !(hasTrailingZerosLower && roundingMode.acceptLowerBound(isMantissaEven)))
                || (lastRemovedDigit >= 5)) ? 1 : 0);
        int outputLength = decimalDigitsCount - removedDigits;

        char[] result = new char[15];
        int index = 0;

        if (isNegative) {
            result[index++] = '-';
        }

        if (useScientificNotation) {
            // Scientific notation: first digit, decimal point, rest of digits and exponent part.
            for (int i = 0; i < outputLength - 1; i++) {
                int digit = roundedDecimal % 10;
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

            if (finalDecimalExponent >= 10) {
                result[index++] = (char) ('0' + finalDecimalExponent / 10);
            }

            result[index++] = (char) ('0' + finalDecimalExponent % 10);
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
        }

        return new String(result, 0, index);
    }

    /**
     * Computes the number of bits required for 5^e
     */
    private static int power5Bits(int e) {
        return e == 0 ? 1 : (int) ((e * LOG2_5_NUMERATOR + LOG2_5_DENOMINATOR - 1) / LOG2_5_DENOMINATOR);
    }

    /**
     * Returns the number of digits in the given value.
     */
    private static int getDecimalLength(int value) {
        if (value >= 1000000000) return 10;
        if (value >= 100000000) return 9;
        if (value >= 10000000) return 8;
        if (value >= 1000000) return 7;
        if (value >= 100000) return 6;
        if (value >= 10000) return 5;
        if (value >= 1000) return 4;
        if (value >= 100) return 3;
        if (value >= 10) return 2;

        return 1;
    }

    /**
     * Returns the exponent of the largest power of 5 that divides the given value.
     */
    private static int power5Factor(int value) {
        int count = 0;

        while (value > 0) {
            if (value % 5 != 0) {
                return count;
            }

            value /= 5;
            count++;
        }

        throw new IllegalArgumentException(String.valueOf(value));
    }

    /**
     * Checks if the given value is divisible by 5^q.
     */
    private static long multiplyPower5DividePower2(int value, int powerIndex, int shift) {
        //noinspection DuplicatedCode
        if (shift - POWER5_HALF_BIT_COUNT < 0) {
            throw new IllegalArgumentException();
        }

        long part0 = value * (long) POWER5_SPLIT[powerIndex][0];
        long part1 = value * (long) POWER5_SPLIT[powerIndex][1];

        return (part0 + (part1 >> POWER5_HALF_BIT_COUNT)) >> (shift - POWER5_HALF_BIT_COUNT);
    }

    /**
     * Multiplies the given value by the inverse of 5^q and then divides by 2^shift.
     */
    private static long multiplyPower5InverseDividePower2(int m, int q, int shift) {
        //noinspection DuplicatedCode
        if (shift - POWER5_INVERSE_HALF_BIT_COUNT < 0) {
            throw new IllegalArgumentException();
        }

        long part0 = m * (long) POWER5_INVERSE_SPLIT[q][0];
        long part1 = m * (long) POWER5_INVERSE_SPLIT[q][1];
        return (part0 + (part1 >> POWER5_INVERSE_HALF_BIT_COUNT)) >> (shift - POWER5_INVERSE_HALF_BIT_COUNT);
    }
}