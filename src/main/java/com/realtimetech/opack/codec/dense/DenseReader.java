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

package com.realtimetech.opack.codec.dense;

import java.io.IOException;
import java.io.InputStream;

class DenseReader {
    private final InputStream inputStream;

    /**
     * Constructs the DenseReader.
     *
     * @param inputStream an InputStream
     */
    public DenseReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Reads the next byte of data from the input stream.
     * The value byte is returned as an int in the range 0 to 255.
     *
     * @return the byte read, or -1 if the end of the stream has been reached
     * @throws IOException if an I/O exception occurs
     */
    public int readByte() throws IOException {
        return this.inputStream.read();
    }

    /**
     * Reads the next character of data from the input stream.
     *
     * @return the character read
     * @throws IOException if an I/O exception occurs
     */
    public char readChar() throws IOException {
        return (char) (((this.inputStream.read() & 0xFF) << 8) |
                ((this.inputStream.read() & 0xFF) << 0));
    }

    /**
     * Reads the next short of data from the input stream.
     *
     * @return the short read
     * @throws IOException if an I/O exception occurs
     */
    public short readShort() throws IOException {
        return (short) (((this.inputStream.read() & 0xFF) << 8) |
                ((this.inputStream.read() & 0xFF) << 0));
    }

    /**
     * Reads the next int of data from the input stream.
     *
     * @return the int read
     * @throws IOException if an I/O exception occurs
     */
    public int readInt() throws IOException {
        return (((this.inputStream.read() & 0xFF) << 24) |
                ((this.inputStream.read() & 0xFF) << 16) |
                ((this.inputStream.read() & 0xFF) << 8) |
                ((this.inputStream.read() & 0xFF) << 0));
    }

    /**
     * Reads the next float of data from the input stream.
     *
     * @return the float read
     * @throws IOException if an I/O exception occurs
     */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(this.readInt());
    }

    /**
     * Reads the next long of data from the input stream.
     *
     * @return the long read
     * @throws IOException if an I/O exception occurs
     */
    public long readLong() throws IOException {
        return ((((long) this.inputStream.read() & 0xFF) << 56) |
                (((long) this.inputStream.read() & 0xFF) << 48) |
                (((long) this.inputStream.read() & 0xFF) << 40) |
                (((long) this.inputStream.read() & 0xFF) << 32) |
                (((long) this.inputStream.read() & 0xFF) << 24) |
                (((long) this.inputStream.read() & 0xFF) << 16) |
                (((long) this.inputStream.read() & 0xFF) << 8) |
                (((long) this.inputStream.read() & 0xFF) << 0));
    }

    /**
     * Reads the next double of data from the input stream.
     *
     * @return the double read
     * @throws IOException if an I/O exception occurs
     */
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(this.readLong());
    }

    /**
     * Reads the next bytes of data from the input stream.
     *
     * @param bytes the byte array to write the bytes read
     * @throws IOException if an I/O exception occurs
     */
    public void readBytes(byte[] bytes) throws IOException {
        this.inputStream.readNBytes(bytes, 0, bytes.length);
    }
}