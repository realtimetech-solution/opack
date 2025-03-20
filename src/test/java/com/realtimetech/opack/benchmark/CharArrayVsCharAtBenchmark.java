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

package com.realtimetech.opack.benchmark;

public class CharArrayVsCharAtBenchmark {
    private static final int TINY_SIZE = 10;
    private static final int TINY_ITERATIONS = 1_000_000_000;
    private static final int SMALL_SIZE = 100;
    private static final int SMALL_ITERATIONS = 100_000_000;
    private static final int LARGE_SIZE = 1_000_000;
    private static final int LARGE_ITERATIONS = 10_000;

    public static void main(String[] args) {
        String tinyStr = generateString(TINY_SIZE);
        String smallStr = generateString(SMALL_SIZE);
        String largeStr = generateString(LARGE_SIZE);

        System.out.println("=== Tiny String Benchmark ===");
        benchmark(tinyStr, TINY_ITERATIONS);

        System.out.println("=== Small String Benchmark ===");
        benchmark(smallStr, SMALL_ITERATIONS);

        System.out.println("=== Large String Benchmark ===");
        benchmark(largeStr, LARGE_ITERATIONS);
    }

    private static String generateString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append((char) ('a' + i % 26));
        return sb.toString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void benchmark(String str, int iterations) {
        // Warm-up
        charAtMethod(str);
        toCharArrayMethod(str);

        // Measure charAt()
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++)
            charAtMethod(str);
        long durationCharAt = System.nanoTime() - start;
        System.out.printf("charAt()      : %d ms\n", durationCharAt / 1_000_000);

        // Measure toCharArray()
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++)
            toCharArrayMethod(str);
        long durationToCharArray = System.nanoTime() - start;
        System.out.printf("toCharArray() : %d ms\n", durationToCharArray / 1_000_000);
    }

    private static int charAtMethod(String str) {
        int sum = 0;
        int len = str.length();
        for (int i = 0; i < len; i++)
            sum += str.charAt(i);
        return sum;
    }

    private static int toCharArrayMethod(String str) {
        int sum = 0;
        char[] chars = str.toCharArray();
        int len = chars.length;
        for (char aChar : chars) sum += aChar;
        return sum;
    }
}
