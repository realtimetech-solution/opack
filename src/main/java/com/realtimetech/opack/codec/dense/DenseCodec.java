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

import com.realtimetech.opack.codec.OpackCodec;
import com.realtimetech.opack.codec.dense.reader.ByteArrayReader;
import com.realtimetech.opack.codec.dense.reader.Reader;
import com.realtimetech.opack.codec.dense.writer.ByteArrayWriter;
import com.realtimetech.opack.codec.dense.writer.Writer;
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.util.OpackArrayConverter;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.util.structure.NativeList;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public final class DenseCodec extends OpackCodec<Reader, Writer> {
    public final static class Builder {
        private int encodeStackInitialSize;
        private int decodeStackInitialSize;

        boolean ignoreVersionCompare;

        public Builder() {
            this.encodeStackInitialSize = 128;
            this.decodeStackInitialSize = 128;

            this.ignoreVersionCompare = false;
        }

        public Builder setEncodeStackInitialSize(int encodeStackInitialSize) {
            this.encodeStackInitialSize = encodeStackInitialSize;
            return this;
        }

        public Builder setDecodeStackInitialSize(int decodeStackInitialSize) {
            this.decodeStackInitialSize = decodeStackInitialSize;
            return this;
        }

        public Builder setIgnoreVersionCompare(boolean ignoreVersionCompare) {
            this.ignoreVersionCompare = ignoreVersionCompare;
            return this;
        }

        public DenseCodec create() {
            return new DenseCodec(this);
        }
    }

    /*
        DO NOT CHANGE CLASSIFIER
     */
    private static final byte[] CONST_DENSE_CODEC_CLASSIFIER = new byte[]{0x20, 0x22, 'D', 'S'};

    /*
        !! IMPORTANT !!
        If the structure of Dense Codec changes, you must change(increase) the version
     */
    private static final byte[] CONST_DENSE_CODEC_VERSION = new byte[]{0x00, 0x01};

    private static final byte CONST_TYPE_OPACK_OBJECT = 0x00;
    private static final byte CONST_TYPE_OPACK_ARRAY = 0x01;

    private static final byte CONST_TYPE_BOOLEAN = 0x10;
    private static final byte CONST_TYPE_BYTE = 0x11;
    private static final byte CONST_TYPE_CHARACTER = 0x12;
    private static final byte CONST_TYPE_SHORT = 0x13;
    private static final byte CONST_TYPE_INTEGER = 0x14;
    private static final byte CONST_TYPE_FLOAT = 0x15;
    private static final byte CONST_TYPE_LONG = 0x16;
    private static final byte CONST_TYPE_DOUBLE = 0x17;
    private static final byte CONST_TYPE_NULL = 0x18;
    private static final byte CONST_TYPE_STRING = 0x19;

    private static final byte CONST_PRIMITIVE_BOOLEAN_NATIVE_ARRAY = 0x20;
    private static final byte CONST_PRIMITIVE_BYTE_NATIVE_ARRAY = 0x21;
    private static final byte CONST_PRIMITIVE_CHARACTER_NATIVE_ARRAY = 0x22;
    private static final byte CONST_PRIMITIVE_SHORT_NATIVE_ARRAY = 0x23;
    private static final byte CONST_PRIMITIVE_INTEGER_NATIVE_ARRAY = 0x24;
    private static final byte CONST_PRIMITIVE_FLOAT_NATIVE_ARRAY = 0x25;
    private static final byte CONST_PRIMITIVE_LONG_NATIVE_ARRAY = 0x26;
    private static final byte CONST_PRIMITIVE_DOUBLE_NATIVE_ARRAY = 0x27;

    private static final byte CONST_WRAPPER_BOOLEAN_NATIVE_ARRAY = 0x30;
    private static final byte CONST_WRAPPER_BYTE_NATIVE_ARRAY = 0x31;
    private static final byte CONST_WRAPPER_CHARACTER_NATIVE_ARRAY = 0x32;
    private static final byte CONST_WRAPPER_SHORT_NATIVE_ARRAY = 0x33;
    private static final byte CONST_WRAPPER_INTEGER_NATIVE_ARRAY = 0x34;
    private static final byte CONST_WRAPPER_FLOAT_NATIVE_ARRAY = 0x35;
    private static final byte CONST_WRAPPER_LONG_NATIVE_ARRAY = 0x36;
    private static final byte CONST_WRAPPER_DOUBLE_NATIVE_ARRAY = 0x37;

    private static final byte CONST_NO_NATIVE_ARRAY = 0x0F;

    private static final Object CONTEXT_NULL_OBJECT = new Object();
    private static final Object CONTEXT_BRANCH_CONTEXT_OBJECT = new Object();

    private final FastStack<Object> encodeStack;

    private final FastStack<OpackValue> decodeStack;
    private final FastStack<Object[]> decodeContextStack;

    private final boolean ignoreVersionCompare;

    /**
     * Constructs the DenseCodec with the builder of DenseCodec.
     *
     * @param builder the builder of DenseCodec
     */
    private DenseCodec(Builder builder) {
        super();

        this.encodeStack = new FastStack<>(builder.encodeStackInitialSize);

        this.decodeStack = new FastStack<>(builder.decodeStackInitialSize);
        this.decodeContextStack = new FastStack<>(builder.decodeStackInitialSize);

        this.ignoreVersionCompare = builder.ignoreVersionCompare;
    }

    /**
     * Encodes the OpackValue to bytes through dense codec.
     *
     * @param writer     the writer to write the encoded data
     * @param opackValue the OpackValue to encode
     * @throws IOException              if an I/O error occurs when writing to byte stream
     * @throws IllegalArgumentException if the type of data to be encoded is not allowed in dense format
     */
    @Override
    protected void doEncode(Writer writer, OpackValue opackValue) throws IOException {
        writer.writeBytes(CONST_DENSE_CODEC_CLASSIFIER);
        writer.writeBytes(CONST_DENSE_CODEC_VERSION);

        this.encodeStack.push(opackValue);

        while (!this.encodeStack.isEmpty()) {
            Object object = this.encodeStack.pop();

            if (object == null) {
                writer.writeByte(CONST_TYPE_NULL);
                continue;
            }

            Class<?> objectType = object.getClass();

            if (ReflectionUtil.isWrapperType(objectType)) {
                objectType = ReflectionUtil.convertWrapperClassToPrimitiveClass(objectType);
            }

            if (objectType == OpackObject.class) {
                OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) object;
                int size = opackObject.size();

                writer.writeByte(CONST_TYPE_OPACK_OBJECT);
                writer.writeInt(size);

                for (Object key : opackObject.keySet()) {
                    Object value = opackObject.get(key);
                    this.encodeStack.push(value);
                    this.encodeStack.push(key);
                }
            } else if (objectType == OpackArray.class) {
                OpackArray<Object> opackArray = (OpackArray<Object>) object;
                int length = opackArray.length();

                try {
                    List<?> opackArrayList = OpackArrayConverter.getOpackArrayList(opackArray);

                    writer.writeByte(CONST_TYPE_OPACK_ARRAY);
                    writer.writeInt(length);

                    boolean optimized = false;

                    if (opackArrayList instanceof NativeList) {
                        NativeList nativeList = (NativeList) opackArrayList;
                        Object arrayObject = nativeList.getArrayObject();
                        Class<?> arrayType = arrayObject.getClass();

                        if (arrayType == boolean[].class) {
                            boolean[] array = (boolean[]) arrayObject;

                            writer.writeByte(CONST_PRIMITIVE_BOOLEAN_NATIVE_ARRAY);

                            for (boolean value : array) {
                                writer.writeByte(value ? 1 : 0);
                            }

                            optimized = true;
                        } else if (arrayType == byte[].class) {
                            byte[] array = (byte[]) arrayObject;

                            writer.writeByte(CONST_PRIMITIVE_BYTE_NATIVE_ARRAY);

                            for (byte value : array) {
                                writer.writeByte(value);
                            }

                            optimized = true;
                        } else if (arrayType == char[].class) {
                            char[] array = (char[]) arrayObject;

                            writer.writeByte(CONST_PRIMITIVE_CHARACTER_NATIVE_ARRAY);

                            for (char value : array) {
                                writer.writeChar(value);
                            }

                            optimized = true;
                        } else if (arrayType == short[].class) {
                            short[] array = (short[]) arrayObject;

                            writer.writeByte(CONST_PRIMITIVE_SHORT_NATIVE_ARRAY);

                            for (short value : array) {
                                writer.writeShort(value);
                            }

                            optimized = true;
                        } else if (arrayType == int[].class) {
                            int[] array = (int[]) arrayObject;

                            writer.writeByte(CONST_PRIMITIVE_INTEGER_NATIVE_ARRAY);

                            for (int value : array) {
                                writer.writeInt(value);
                            }

                            optimized = true;
                        } else if (arrayType == float[].class) {
                            float[] array = (float[]) arrayObject;

                            writer.writeByte(CONST_PRIMITIVE_FLOAT_NATIVE_ARRAY);

                            for (float value : array) {
                                writer.writeFloat(value);
                            }

                            optimized = true;
                        } else if (arrayType == long[].class) {
                            long[] array = (long[]) arrayObject;

                            writer.writeByte(CONST_PRIMITIVE_LONG_NATIVE_ARRAY);

                            for (long value : array) {
                                writer.writeLong(value);
                            }

                            optimized = true;
                        } else if (arrayType == double[].class) {
                            double[] array = (double[]) arrayObject;

                            writer.writeByte(CONST_PRIMITIVE_DOUBLE_NATIVE_ARRAY);

                            for (double value : array) {
                                writer.writeDouble(value);
                            }

                            optimized = true;
                        } else if (arrayType == Boolean[].class) {
                            Boolean[] array = (Boolean[]) arrayObject;

                            writer.writeByte(CONST_WRAPPER_BOOLEAN_NATIVE_ARRAY);

                            for (Boolean value : array) {
                                if (value == null) {
                                    writer.writeByte(0);
                                } else {
                                    writer.writeByte(1);
                                    writer.writeByte(value ? 1 : 0);
                                }
                            }

                            optimized = true;
                        } else if (arrayType == Byte[].class) {
                            Byte[] array = (Byte[]) arrayObject;

                            writer.writeByte(CONST_WRAPPER_BYTE_NATIVE_ARRAY);

                            for (Byte value : array) {
                                if (value == null) {
                                    writer.writeByte(0);
                                } else {
                                    writer.writeByte(1);
                                    writer.writeByte(value);
                                }
                            }

                            optimized = true;
                        } else if (arrayType == Character[].class) {
                            Character[] array = (Character[]) arrayObject;

                            writer.writeByte(CONST_WRAPPER_CHARACTER_NATIVE_ARRAY);

                            for (Character value : array) {
                                if (value == null) {
                                    writer.writeByte(0);
                                } else {
                                    writer.writeByte(1);
                                    writer.writeChar(value);
                                }
                            }

                            optimized = true;
                        } else if (arrayType == Short[].class) {
                            Short[] array = (Short[]) arrayObject;

                            writer.writeByte(CONST_WRAPPER_SHORT_NATIVE_ARRAY);

                            for (Short value : array) {
                                if (value == null) {
                                    writer.writeByte(0);
                                } else {
                                    writer.writeByte(1);
                                    writer.writeShort(value);
                                }
                            }

                            optimized = true;
                        } else if (arrayType == Integer[].class) {
                            Integer[] array = (Integer[]) arrayObject;

                            writer.writeByte(CONST_WRAPPER_INTEGER_NATIVE_ARRAY);

                            for (Integer value : array) {
                                if (value == null) {
                                    writer.writeByte(0);
                                } else {
                                    writer.writeByte(1);
                                    writer.writeInt(value);
                                }
                            }

                            optimized = true;
                        } else if (arrayType == Float[].class) {
                            Float[] array = (Float[]) arrayObject;

                            writer.writeByte(CONST_WRAPPER_FLOAT_NATIVE_ARRAY);

                            for (Float value : array) {
                                if (value == null) {
                                    writer.writeByte(0);
                                } else {
                                    writer.writeByte(1);
                                    writer.writeFloat(value);
                                }
                            }

                            optimized = true;
                        } else if (arrayType == Long[].class) {
                            Long[] array = (Long[]) arrayObject;

                            writer.writeByte(CONST_WRAPPER_LONG_NATIVE_ARRAY);

                            for (Long value : array) {
                                if (value == null) {
                                    writer.writeByte(0);
                                } else {
                                    writer.writeByte(1);
                                    writer.writeLong(value);
                                }
                            }

                            optimized = true;
                        } else if (arrayType == Double[].class) {
                            Double[] array = (Double[]) arrayObject;

                            writer.writeByte(CONST_WRAPPER_DOUBLE_NATIVE_ARRAY);

                            for (Double value : array) {
                                if (value == null) {
                                    writer.writeByte(0);
                                } else {
                                    writer.writeByte(1);
                                    writer.writeDouble(value);
                                }
                            }

                            optimized = true;
                        }
                    }

                    if (!optimized) {
                        writer.writeByte(CONST_NO_NATIVE_ARRAY);

                        for (int index = length - 1; index >= 0; index--) {
                            Object value = opackArray.get(index);
                            encodeStack.push(value);
                        }
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalStateException("Failed to access the native list object in OpackArray.");
                }
            } else {
                if (objectType == boolean.class) {
                    writer.writeByte(CONST_TYPE_BOOLEAN);
                    writer.writeByte((boolean) object ? 1 : 0);
                } else if (objectType == byte.class) {
                    writer.writeByte(CONST_TYPE_BYTE);
                    writer.writeByte((byte) object);
                } else if (objectType == char.class) {
                    writer.writeByte(CONST_TYPE_CHARACTER);
                    writer.writeChar((char) object);
                } else if (objectType == short.class) {
                    writer.writeByte(CONST_TYPE_SHORT);
                    writer.writeShort((short) object);
                } else if (objectType == int.class) {
                    writer.writeByte(CONST_TYPE_INTEGER);
                    writer.writeInt((int) object);
                } else if (objectType == float.class) {
                    writer.writeByte(CONST_TYPE_FLOAT);
                    writer.writeFloat((float) object);
                } else if (objectType == long.class) {
                    writer.writeByte(CONST_TYPE_LONG);
                    writer.writeLong((long) object);
                } else if (objectType == double.class) {
                    writer.writeByte(CONST_TYPE_DOUBLE);
                    writer.writeDouble((double) object);
                } else if (objectType == String.class) {
                    String string = (String) object;
                    byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

                    writer.writeByte(CONST_TYPE_STRING);
                    writer.writeInt(bytes.length);
                    writer.writeBytes(bytes);
                } else {
                    throw new IllegalArgumentException(objectType + " is not allowed in dense format. (unknown literal object type).");
                }
            }
        }
    }

    /**
     * Encodes the OpackValue to bytes through dense codec.
     *
     * @param opackValue the OpackValue to encode
     * @return returns encoded bytes
     * @throws EncodeException if a problem occurs during encoding; if the type of data to be encoded is not allowed in specific codec
     */
    public byte[] encode(OpackValue opackValue) throws EncodeException {
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter();

        this.encode(byteArrayWriter, opackValue);

        return byteArrayWriter.toByteArray();
    }

    /**
     * Decodes one block to OpackValue. (basic block protocol: header(1 byte), data (variable))
     * If data of block to be decoded is OpackObject or OpackArray(excluding primitive array), returns CONTEXT_BRANCH_CONTEXT_OBJECT for linear decoding.
     *
     * @param reader the byte reader that wraps the data
     * @return opack value or CONTEXT_BRANCH_CONTEXT_OBJECT
     * @throws IllegalArgumentException if the type of data to be decoded is not allowed in dense format; if unknown block header is parsed
     */
    Object decodeBlock(Reader reader) throws IOException {
        byte b = (byte) reader.readByte();

        if (b == CONST_TYPE_BOOLEAN) {
            return (byte) reader.readByte() == 1;
        } else if (b == CONST_TYPE_BYTE) {
            return (byte) reader.readByte();
        } else if (b == CONST_TYPE_CHARACTER) {
            return reader.readChar();
        } else if (b == CONST_TYPE_SHORT) {
            return reader.readShort();
        } else if (b == CONST_TYPE_INTEGER) {
            return reader.readInt();
        } else if (b == CONST_TYPE_FLOAT) {
            return reader.readFloat();
        } else if (b == CONST_TYPE_LONG) {
            return reader.readLong();
        } else if (b == CONST_TYPE_DOUBLE) {
            return reader.readDouble();
        } else if (b == CONST_TYPE_NULL) {
            return null;
        } else if (b == CONST_TYPE_STRING) {
            int length = reader.readInt();
            byte[] bytes = new byte[length];
            reader.readBytes(bytes);

            return new String(bytes, StandardCharsets.UTF_8);
        } else if (b == CONST_TYPE_OPACK_OBJECT) {
            int size = reader.readInt();
            OpackObject<Object, Object> opackObject = new OpackObject<>(size);

            decodeContextStack.push(new Object[]{size, 0, CONTEXT_NULL_OBJECT, CONTEXT_NULL_OBJECT});
            decodeStack.push(opackObject);

            return CONTEXT_BRANCH_CONTEXT_OBJECT;
        } else if (b == CONST_TYPE_OPACK_ARRAY) {
            int length = reader.readInt();

            byte nativeType = (byte) reader.readByte();

            if (nativeType == CONST_NO_NATIVE_ARRAY) {
                OpackArray<Object> opackArray = new OpackArray<>(length);

                decodeContextStack.push(new Object[]{length, 0});
                decodeStack.push(opackArray);

                return CONTEXT_BRANCH_CONTEXT_OBJECT;
            } else {
                if (nativeType == CONST_PRIMITIVE_BOOLEAN_NATIVE_ARRAY) {
                    boolean[] array = new boolean[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = reader.readByte() == 1;
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_BYTE_NATIVE_ARRAY) {
                    byte[] array = new byte[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = (byte) reader.readByte();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_CHARACTER_NATIVE_ARRAY) {
                    char[] array = new char[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = reader.readChar();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_SHORT_NATIVE_ARRAY) {
                    short[] array = new short[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = reader.readShort();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_INTEGER_NATIVE_ARRAY) {
                    int[] array = new int[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = reader.readInt();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_FLOAT_NATIVE_ARRAY) {
                    float[] array = new float[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = reader.readFloat();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_LONG_NATIVE_ARRAY) {
                    long[] array = new long[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = reader.readLong();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_DOUBLE_NATIVE_ARRAY) {
                    double[] array = new double[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = reader.readDouble();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_BOOLEAN_NATIVE_ARRAY) {
                    Boolean[] array = new Boolean[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = reader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = reader.readByte() == 1;
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_BYTE_NATIVE_ARRAY) {
                    Byte[] array = new Byte[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = reader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = (byte) reader.readByte();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_CHARACTER_NATIVE_ARRAY) {
                    Character[] array = new Character[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = reader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = reader.readChar();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_SHORT_NATIVE_ARRAY) {
                    Short[] array = new Short[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = reader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = reader.readShort();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_INTEGER_NATIVE_ARRAY) {
                    Integer[] array = new Integer[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = reader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = reader.readInt();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_FLOAT_NATIVE_ARRAY) {
                    Float[] array = new Float[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = reader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = reader.readFloat();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_LONG_NATIVE_ARRAY) {
                    Long[] array = new Long[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = reader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = reader.readLong();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_DOUBLE_NATIVE_ARRAY) {
                    Double[] array = new Double[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = reader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = reader.readDouble();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else {
                    throw new IllegalArgumentException(nativeType + " is not registered native array type binary in dense format. (unknown native array type)");
                }
            }
        }

        throw new IllegalArgumentException(b + " is not registered block header binary in dense codec. (unknown block header)");
    }

    /**
     * Decodes the byte array encoded through the dense codec to OpackValue.
     *
     * @param reader the reader to decode
     * @return opack value
     * @throws IllegalArgumentException if the decoded value is not a opack value
     */
    @Override
    protected OpackValue doDecode(Reader reader) throws IOException {
        byte[] classifier = new byte[CONST_DENSE_CODEC_CLASSIFIER.length];
        reader.readBytes(classifier);

        if (!Arrays.equals(CONST_DENSE_CODEC_CLASSIFIER, classifier)) {
            throw new IllegalArgumentException("Decoding data is not dense format data. (Expected " + Arrays.toString(CONST_DENSE_CODEC_CLASSIFIER) + ", got " + Arrays.toString(classifier) + ")");
        }

        byte[] version = new byte[CONST_DENSE_CODEC_VERSION.length];
        reader.readBytes(version);

        if (!this.ignoreVersionCompare) {
            if (!Arrays.equals(CONST_DENSE_CODEC_VERSION, version)) {
                throw new IllegalArgumentException("Decoding data does not match current version of dense codec. (Expected " + Arrays.toString(CONST_DENSE_CODEC_VERSION) + ", got " + Arrays.toString(version) + ")");
            }
        }

        this.decodeStack.reset();
        this.decodeContextStack.reset();

        this.decodeBlock(reader);
        OpackValue rootValue = this.decodeStack.peek();

        while (!this.decodeStack.isEmpty()) {
            OpackValue opackValue = this.decodeStack.peek();
            Object[] context = this.decodeContextStack.peek();

            Integer size = (Integer) context[0];
            Integer offset = (Integer) context[1];

            boolean bypass = false;
            int index = offset;

            if (opackValue instanceof OpackObject) {
                OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) opackValue;

                for (; index < size; index++) {
                    Object key = context[2];
                    Object value = context[3];

                    if (key == CONTEXT_NULL_OBJECT) {
                        key = this.decodeBlock(reader);
                        if (key == CONTEXT_BRANCH_CONTEXT_OBJECT) {
                            context[2] = this.decodeStack.peek();
                            bypass = true;
                            break;
                        } else {
                            context[2] = key;
                        }
                    }

                    if (value == CONTEXT_NULL_OBJECT) {
                        value = this.decodeBlock(reader);
                        if (value == CONTEXT_BRANCH_CONTEXT_OBJECT) {
                            context[3] = this.decodeStack.peek();
                            bypass = true;
                            break;
                        } else {
                            context[3] = value;
                        }
                    }

                    opackObject.put(key, value);
                    context[2] = CONTEXT_NULL_OBJECT;
                    context[3] = CONTEXT_NULL_OBJECT;
                }
            } else if (opackValue instanceof OpackArray) {
                OpackArray<Object> opackArray = (OpackArray<Object>) opackValue;

                for (; index < size; index++) {
                    Object value = this.decodeBlock(reader);

                    if (value == CONTEXT_BRANCH_CONTEXT_OBJECT) {
                        index++;
                        opackArray.add(this.decodeStack.peek());

                        bypass = true;
                        break;
                    } else {
                        opackArray.add(value);
                    }
                }
            } else {
                throw new IllegalArgumentException(opackValue.getClass() + " is not a type of opack value. (unknown opack value type)");
            }

            if (!bypass) {
                this.decodeStack.pop();
                this.decodeContextStack.pop();
            } else {
                context[1] = index;
            }
        }

        return rootValue;
    }

    /**
     * Decodes the byte array encoded through the dense codec to OpackValue.
     *
     * @param bytes the bytes to decode
     * @return the decoded opack value
     * @throws DecodeException if a problem occurs during decoding; if the type of data to be decoded is not allowed in specific codec
     */
    public OpackValue decode(byte[] bytes) throws DecodeException {
        ByteArrayReader byteArrayReader = new ByteArrayReader(bytes);

        return this.decode(byteArrayReader);
    }
}
