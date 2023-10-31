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

public interface Writer {
    /**
     * Writes the specified byte to this output stream
     *
     * @param value the byte
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    void writeByte(int value) throws IOException;

    /**
     * Writes the specified character to this output stream
     *
     * @param value the character
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    void writeChar(char value) throws IOException;

    /**
     * Writes the specified short to this output stream
     *
     * @param value the short
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    void writeShort(short value) throws IOException;

    /**
     * Writes the specified int to this output stream
     *
     * @param value the int
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    void writeInt(int value) throws IOException;

    /**
     * Writes the specified float to this output stream
     *
     * @param value the float
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    void writeFloat(float value) throws IOException;

    /**
     * Writes the specified long to this output stream
     *
     * @param value the long
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    void writeLong(long value) throws IOException;

    /**
     * Writes the specified double to this output stream
     *
     * @param value the double
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    void writeDouble(double value) throws IOException;

    /**
     * Writes the specified bytes to this output stream
     *
     * @param bytes the byte array to write
     * @throws IOException if an I/O error occurs, if the output stream has been closed.
     */
    void writeBytes(byte @NotNull [] bytes) throws IOException;
}
