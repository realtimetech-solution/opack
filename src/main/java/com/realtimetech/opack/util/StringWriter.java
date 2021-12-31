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

package com.realtimetech.opack.util;

import java.io.IOException;
import java.io.Writer;

public class StringWriter extends Writer {
    private char[] chars;

    private int currentIndex;

    private int currentSize;

    /**
     * Calls {@code new StringWriter(1024)}
     */
    public StringWriter() {
        this(1024);
    }

    /**
     * Constructs a StringWriter with initial size.
     *
     * @param initialSize the initial size
     */
    public StringWriter(int initialSize) {
        this.currentIndex = -1;
        this.currentSize = Math.min(initialSize >> 1, 1);

        this.growArray(initialSize);
    }

    /**
     * If the needSize is larger than the current size, double the capacity.
     *
     * @param needSize the needed size
     */
    private void growArray(int needSize) {
        char[] oldObjects = this.chars;

        while (needSize >= this.currentSize){
            this.currentSize = this.currentSize << 1;
        }
        this.chars = new char[this.currentSize];

        if (oldObjects != null) {
            System.arraycopy(oldObjects, 0, this.chars, 0, currentIndex + 1);
        }
    }

    /**
     * Writes a single character.
     *
     * @param object the character to write
     */
    public void write(char object) {
        int need = this.currentIndex + 1;
        if (need >= this.currentSize) {
            growArray(need);
        }

        this.currentIndex = need;

        this.chars[this.currentIndex] = object;
    }

    /**
     * Writes an array of characters.
     *
     * @param src the source array to write
     */
    public void write(char[] src) {
        this.write(src, 0, src.length);
    }

    /**
     * Writes a portion of an array of characters.
     *
     * @param src    the source array to write
     * @param offset the starting position in the source array
     * @param length the number of characters to write
     */
    @Override
    public void write(char[] src, int offset, int length) {
        int need = this.currentIndex + length;
        if (need >= this.currentSize) {
            growArray(need);
        }

        System.arraycopy(src, offset, this.chars, this.currentIndex + 1, length);

        this.currentIndex = need;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        this.reset();
    }

    /**
     * Returns the current string length of this string writer.
     *
     * @return string length
     */
    public int getLength() {
        return this.currentIndex + 1;
    }

    /**
     * Reset this string writer
     */
    public void reset() {
        this.currentIndex = -1;
    }

    /**
     * Returns a string created through this string writer.
     *
     * @return created string
     */
    @Override
    public String toString() {
        return new String(this.chars, 0, this.currentIndex + 1);
    }

    /**
     * Returns an array containing all the characters in this writer in proper sequence.
     *
     * @return an array containing all the characters in this writer in proper sequence
     */
    public char[] toCharArray() {
        char[] charArray = new char[this.currentIndex + 1];
        System.arraycopy(this.chars, 0, charArray, 0, this.currentIndex + 1);
        return charArray;
    }
}