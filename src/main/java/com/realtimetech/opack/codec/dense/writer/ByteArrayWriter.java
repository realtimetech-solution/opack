/*
 * Copyright (C) 2022 REALTIMETECH All Rights Reserved
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

package com.realtimetech.opack.codec.dense.writer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ByteArrayWriter implements Writer {
    private byte @NotNull [] bytes;

    private int currentIndex;
    private int actualLength;

    /**
     * Constructs a ByteArrayWriter
     */
    public ByteArrayWriter() {
        this(1024);
    }

    /**
     * Constructs a ByteArrayWriter
     *
     * @param initialSize the initial size
     */
    public ByteArrayWriter(int initialSize) {
        this.bytes = new byte[0];

        this.currentIndex = 0;
        this.actualLength = 1;

        this.increaseArray(initialSize);
    }

    /**
     * Returns the current byte array size
     *
     * @return bytes length
     */
    public int getLength() {
        return this.currentIndex;
    }

    /**
     * Reset this writer
     */
    public void reset() {
        this.currentIndex = 0;
    }

    /**
     * If the needSize is larger than the current size, increase array.
     *
     * @param requireSize the need more size
     */
    private void increaseArray(int requireSize) {
        int need = this.currentIndex + requireSize;

        if (need > this.actualLength) {
            byte[] oldObjects = this.bytes;

            do {
                this.actualLength = this.actualLength << 1;
            } while (need > this.actualLength);
            this.bytes = new byte[this.actualLength];

            if (oldObjects.length != 0) {
                System.arraycopy(oldObjects, 0, this.bytes, 0, this.currentIndex);
            }
        }
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param value the byte
     * @throws IOException if an I/O error occurs; if the output stream has been closed.
     */
    public void writeByte(int value) throws IOException {
        this.increaseArray(1);

        this.bytes[this.currentIndex++] = (byte) value;
    }

    /**
     * Writes the specified character to this output stream.
     *
     * @param value the character
     * @throws IOException if an I/O error occurs; if the output stream has been closed.
     */
    public void writeChar(char value) throws IOException {
        this.increaseArray(2);

        this.bytes[this.currentIndex++] = (byte) ((value >> 8) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 0) & 0xff);
    }

    /**
     * Writes the specified short to this output stream.
     *
     * @param value the short
     * @throws IOException if an I/O error occurs; if the output stream has been closed.
     */
    public void writeShort(short value) throws IOException {
        this.increaseArray(2);

        this.bytes[this.currentIndex++] = (byte) ((value >> 8) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 0) & 0xff);
    }

    /**
     * Writes the specified int to this output stream.
     *
     * @param value the int
     * @throws IOException if an I/O error occurs; if the output stream has been closed.
     */
    public void writeInt(int value) throws IOException {
        this.increaseArray(4);

        this.bytes[this.currentIndex++] = (byte) ((value >> 24) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 16) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 8) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 0) & 0xff);
    }

    /**
     * Writes the specified float to this output stream.
     *
     * @param value the float
     * @throws IOException if an I/O error occurs; if the output stream has been closed.
     */
    public void writeFloat(float value) throws IOException {
        this.writeInt(Float.floatToRawIntBits(value));
    }

    /**
     * Writes the specified long to this output stream.
     *
     * @param value the long
     * @throws IOException if an I/O error occurs; if the output stream has been closed.
     */
    public void writeLong(long value) throws IOException {
        this.increaseArray(8);

        this.bytes[this.currentIndex++] = (byte) ((value >> 56) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 48) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 40) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 32) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 24) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 16) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 8) & 0xff);
        this.bytes[this.currentIndex++] = (byte) ((value >> 0) & 0xff);
    }

    /**
     * Writes the specified double to this output stream.
     *
     * @param value the double
     * @throws IOException if an I/O error occurs; if the output stream has been closed.
     */
    public void writeDouble(double value) throws IOException {
        this.writeLong(Double.doubleToRawLongBits(value));
    }

    /**
     * Writes the specified bytes to this output stream.
     *
     * @param bytes the byte array to write
     * @throws IOException if an I/O error occurs; if the output stream has been closed.
     */
    public void writeBytes(byte @NotNull [] bytes) throws IOException {
        this.increaseArray(bytes.length);

        System.arraycopy(bytes, 0, this.bytes, this.currentIndex, bytes.length);
        this.currentIndex += bytes.length;
    }

    /**
     * @return an array containing all the bytes in this writer in proper sequence
     */
    public byte @NotNull [] toByteArray() {
        byte[] byteArray = new byte[this.currentIndex];
        System.arraycopy(this.bytes, 0, byteArray, 0, this.currentIndex);
        return byteArray;
    }
}
