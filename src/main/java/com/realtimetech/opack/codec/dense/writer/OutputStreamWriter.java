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
import java.io.OutputStream;

public class OutputStreamWriter implements Writer {
    /**
     * Create OutputStreamWriter
     *
     * @param outputStream the output stream
     * @return the output stream writer
     */
    public static @NotNull OutputStreamWriter of(@NotNull OutputStream outputStream) {
        return new OutputStreamWriter(outputStream);
    }

    private final @NotNull OutputStream outputStream;

    /**
     * Constructs OutputStreamWriter
     *
     * @param outputStream the output stream
     */
    OutputStreamWriter(@NotNull OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Writes the specified byte to this output stream
     *
     * @param value the byte
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    public void writeByte(int value) throws IOException {
        this.outputStream.write(value);
    }

    /**
     * Writes the specified character to this output stream
     *
     * @param value the character
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    public void writeChar(char value) throws IOException {
        this.outputStream.write((byte) ((value >> 8) & 0xff));
        this.outputStream.write((byte) ((value) & 0xff));
    }

    /**
     * Writes the specified short to this output stream
     *
     * @param value the short
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    public void writeShort(short value) throws IOException {
        this.outputStream.write((byte) ((value >> 8) & 0xff));
        this.outputStream.write((byte) ((value) & 0xff));
    }

    /**
     * Writes the specified int to this output stream
     *
     * @param value the int
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    public void writeInt(int value) throws IOException {
        this.outputStream.write((byte) ((value >> 24) & 0xff));
        this.outputStream.write((byte) ((value >> 16) & 0xff));
        this.outputStream.write((byte) ((value >> 8) & 0xff));
        this.outputStream.write((byte) ((value) & 0xff));
    }

    /**
     * Writes the specified float to this output stream
     *
     * @param value the float
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    public void writeFloat(float value) throws IOException {
        this.writeInt(Float.floatToRawIntBits(value));
    }

    /**
     * Writes the specified long to this output stream
     *
     * @param value the long
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    public void writeLong(long value) throws IOException {
        this.outputStream.write((byte) ((value >> 56) & 0xff));
        this.outputStream.write((byte) ((value >> 48) & 0xff));
        this.outputStream.write((byte) ((value >> 40) & 0xff));
        this.outputStream.write((byte) ((value >> 32) & 0xff));
        this.outputStream.write((byte) ((value >> 24) & 0xff));
        this.outputStream.write((byte) ((value >> 16) & 0xff));
        this.outputStream.write((byte) ((value >> 8) & 0xff));
        this.outputStream.write((byte) ((value) & 0xff));
    }

    /**
     * Writes the specified double to this output stream
     *
     * @param value the double
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    public void writeDouble(double value) throws IOException {
        this.writeLong(Double.doubleToRawLongBits(value));
    }

    /**
     * Writes the specified bytes to this output stream
     *
     * @param bytes the byte array to write
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    public void writeBytes(byte @NotNull [] bytes) throws IOException {
        this.outputStream.write(bytes);
    }
}
