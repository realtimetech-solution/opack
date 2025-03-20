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
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.UnsafeOpackValue;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.util.structure.NativeList;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public final class DenseCodec extends OpackCodec<Reader, Writer> {
    public final static class Builder {
        /**
         * Creates a new instance of the builder class
         *
         * @return the created builder
         */
        public static @NotNull Builder create() {
            return new Builder();
        }

        private int encodeStackInitialSize;
        private int decodeStackInitialSize;

        boolean ignoreVersionCompare;

        Builder() {
            this.encodeStackInitialSize = 128;
            this.decodeStackInitialSize = 128;

            this.ignoreVersionCompare = false;
        }

        /**
         * Sets the initial size of the encoder stack
         *
         * @param encodeStackInitialSize the initial size to set for the encode stack
         * @return the current instance of the builder for method chaining
         */
        public @NotNull Builder setEncodeStackInitialSize(int encodeStackInitialSize) {
            this.encodeStackInitialSize = encodeStackInitialSize;
            return this;
        }

        /**
         * Sets the initial size for the decode stack
         *
         * @param decodeStackInitialSize the initial size to set for the decode stack
         * @return the current instance of the builder for method chaining
         */
        public @NotNull Builder setDecodeStackInitialSize(int decodeStackInitialSize) {
            this.decodeStackInitialSize = decodeStackInitialSize;
            return this;
        }

        /**
         * Sets whether version comparison should be ignored
         *
         * @param ignoreVersionCompare the flag indicating whether to ignore version comparison
         * @return the current instance of the builder for method chaining
         */
        public @NotNull Builder setIgnoreVersionCompare(boolean ignoreVersionCompare) {
            this.ignoreVersionCompare = ignoreVersionCompare;
            return this;
        }

        /**
         * Build the {@link DenseCodec DenseCodec}
         *
         * @return the created {@link DenseCodec DenseCodec}
         */
        public @NotNull DenseCodec build() {
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

    private final @NotNull FastStack<@Nullable Object> encodeStack;

    private final @NotNull FastStack<@Nullable Object> decodeStack;
    private final @NotNull FastStack<@NotNull Object @NotNull []> decodeContextStack;

    private final boolean ignoreVersionCompare;

    /**
     * Constructs the DenseCodec with the builder of DenseCodec
     *
     * @param builder the builder of DenseCodec
     */
    DenseCodec(@NotNull Builder builder) {
        super();

        this.encodeStack = new FastStack<>(builder.encodeStackInitialSize);

        this.decodeStack = new FastStack<>(builder.decodeStackInitialSize);
        this.decodeContextStack = new FastStack<>(builder.decodeStackInitialSize);

        this.ignoreVersionCompare = builder.ignoreVersionCompare;
    }


    /**
     * Encodes the {@link OpackValue OpackValue} into dense bytes
     *
     * @param opackValue the opack value to encode
     * @return the encoded dense bytes
     * @throws EncodeException if a problem occurs during encoding, if the type of data to be encoded is not allowed in a specific codec
     */
    public synchronized byte @NotNull [] encode(@NotNull OpackValue opackValue) throws EncodeException {
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter();
        this.encode(byteArrayWriter, opackValue);
        return byteArrayWriter.toByteArray();
    }

    /**
     * Encodes the {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue} into dense bytes
     *
     * @param object the object to encode
     * @return the encoded dense bytes
     * @throws EncodeException if a problem occurs during encoding, if the type of data to be encoded is not allowed in a specific codec
     */
    public synchronized byte @NotNull [] encodeObject(@NotNull Object object) throws EncodeException {
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter();
        this.encodeObject(byteArrayWriter, object);
        return byteArrayWriter.toByteArray();
    }


    /**
     * Encodes the {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue} into dense bytes
     *
     * @param writer the writer to store an encoded result
     * @param object the object to encode
     * @throws EncodeException if a problem occurs during encoding
     */
    @Override
    protected void encodeObject(@NotNull Writer writer, @Nullable Object object) throws EncodeException {
        try {
            writer.writeBytes(CONST_DENSE_CODEC_CLASSIFIER);
            writer.writeBytes(CONST_DENSE_CODEC_VERSION);

            this.encodeStack.push(object);

            while (!this.encodeStack.isEmpty()) {
                Object currentObject = this.encodeStack.pop();

                if (currentObject == null) {
                    writer.writeByte(CONST_TYPE_NULL);
                    continue;
                }

                Class<?> objectType = currentObject.getClass();

                if (ReflectionUtil.isWrapperType(objectType)) {
                    objectType = ReflectionUtil.convertWrapperClassToPrimitiveClass(objectType);
                }

                if (objectType == OpackObject.class) {
                    OpackObject opackObject = (OpackObject) currentObject;
                    int size = opackObject.size();

                    writer.writeByte(CONST_TYPE_OPACK_OBJECT);
                    writer.writeInt(size);

                    for (Object key : opackObject.keySet()) {
                        Object value = opackObject.get(key);
                        this.encodeStack.push(value);
                        this.encodeStack.push(key);
                    }
                } else if (objectType == OpackArray.class) {
                    OpackArray opackArray = (OpackArray) currentObject;
                    int length = opackArray.length();

                    List<?> opackArrayList = UnsafeOpackValue.getList(opackArray);

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
                } else {
                    if (objectType == boolean.class) {
                        writer.writeByte(CONST_TYPE_BOOLEAN);
                        writer.writeByte((boolean) currentObject ? 1 : 0);
                    } else if (objectType == byte.class) {
                        writer.writeByte(CONST_TYPE_BYTE);
                        writer.writeByte((byte) currentObject);
                    } else if (objectType == char.class) {
                        writer.writeByte(CONST_TYPE_CHARACTER);
                        writer.writeChar((char) currentObject);
                    } else if (objectType == short.class) {
                        writer.writeByte(CONST_TYPE_SHORT);
                        writer.writeShort((short) currentObject);
                    } else if (objectType == int.class) {
                        writer.writeByte(CONST_TYPE_INTEGER);
                        writer.writeInt((int) currentObject);
                    } else if (objectType == float.class) {
                        writer.writeByte(CONST_TYPE_FLOAT);
                        writer.writeFloat((float) currentObject);
                    } else if (objectType == long.class) {
                        writer.writeByte(CONST_TYPE_LONG);
                        writer.writeLong((long) currentObject);
                    } else if (objectType == double.class) {
                        writer.writeByte(CONST_TYPE_DOUBLE);
                        writer.writeDouble((double) currentObject);
                    } else if (objectType == String.class) {
                        String string = (String) currentObject;
                        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

                        writer.writeByte(CONST_TYPE_STRING);
                        writer.writeInt(bytes.length);
                        writer.writeBytes(bytes);
                    } else {
                        throw new EncodeException(objectType + " is not allowed in dense format. (unknown literal currentObject type).");
                    }
                }
            }
        } catch (IOException ioException) {
            throw new EncodeException(ioException);
        }
    }


    /**
     * Decodes the dense bytes into {@link OpackValue OpackValue}
     *
     * @param bytes the bytes to decode
     * @return the decoded result
     * @throws DecodeException if a problem occurs during decoding, if the type of data to be decoded is not allowed in a specific codec
     */
    public @NotNull OpackValue decode(byte @NotNull [] bytes) throws DecodeException {
        ByteArrayReader byteArrayReader = new ByteArrayReader(bytes);

        return this.decode(byteArrayReader);
    }

    /**
     * Decodes the dense bytes into {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue}
     *
     * @param bytes the bytes to decode
     * @return the decoded result
     * @throws DecodeException if a problem occurs during decoding, if the type of data to be decoded is not allowed in a specific codec
     */
    public @Nullable Object decodeObject(byte @NotNull [] bytes) throws DecodeException {
        ByteArrayReader byteArrayReader = new ByteArrayReader(bytes);

        return this.decodeObject(byteArrayReader);
    }


    /**
     * Decodes the dense bytes into {@link OpackValue#isAllowType(Class) Objects of the type allowed by OpackValue}
     *
     * @param reader the reader to load an encoded result
     * @return the decoded result
     * @throws DecodeException if a problem occurs during decoding
     */
    @Override
    protected @Nullable Object decodeObject(@NotNull Reader reader) throws DecodeException {
        try {
            byte[] classifier = new byte[CONST_DENSE_CODEC_CLASSIFIER.length];
            reader.readBytes(classifier);

            if (!Arrays.equals(CONST_DENSE_CODEC_CLASSIFIER, classifier)) {
                throw new DecodeException("Decoding data is not dense format data. (Expected " + Arrays.toString(CONST_DENSE_CODEC_CLASSIFIER) + ", got " + Arrays.toString(classifier) + ")");
            }

            byte[] version = new byte[CONST_DENSE_CODEC_VERSION.length];
            reader.readBytes(version);

            if (!this.ignoreVersionCompare) {
                if (!Arrays.equals(CONST_DENSE_CODEC_VERSION, version)) {
                    throw new DecodeException("Decoding data does not match current version of dense codec. (Expected " + Arrays.toString(CONST_DENSE_CODEC_VERSION) + ", got " + Arrays.toString(version) + ")");
                }
            }

            this.decodeStack.reset();
            this.decodeContextStack.reset();

            Object decodeResult = this.decodeBlock(reader);
            Object rootValue = this.decodeStack.peek();

            if (decodeResult != CONTEXT_BRANCH_CONTEXT_OBJECT) {
                return rootValue;
            }

            while (!this.decodeStack.isEmpty()) {
                Object currentValue = this.decodeStack.peek();
                Object[] context = this.decodeContextStack.peek();

                Integer size = (Integer) context[0];
                Integer offset = (Integer) context[1];

                boolean bypass = false;
                int index = offset;

                if (currentValue instanceof OpackObject) {
                    OpackObject opackObject = (OpackObject) currentValue;

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
                } else if (currentValue instanceof OpackArray) {
                    OpackArray opackArray = (OpackArray) currentValue;

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
                    assert currentValue != null;

                    throw new DecodeException(currentValue.getClass() + " is not a type of opack value. (unknown opack value type)");
                }

                if (!bypass) {
                    this.decodeStack.pop();
                    this.decodeContextStack.pop();
                } else {
                    context[1] = index;
                }
            }

            return rootValue;
        } catch (IOException ioException) {
            throw new DecodeException(ioException);
        }
    }

    /**
     * Decodes one block to OpackValue (basic block protocol: header(1 byte), data (variable))
     * If data of the block to be decoded is OpackObject or OpackArray(excluding a primitive array),
     * returns CONTEXT_BRANCH_CONTEXT_OBJECT for linear decoding
     *
     * @param reader the reader to load an encoded result
     * @return the decoded object or CONTEXT_BRANCH_CONTEXT_OBJECT
     * @throws IllegalArgumentException if the type of data to be decoded is not allowed in dense format,
     *                                  if an unknown block header is parsed
     */
    private @Nullable Object decodeBlock(@NotNull Reader reader) throws DecodeException, IOException {
        byte readByte = (byte) reader.readByte();

        if (readByte == CONST_TYPE_BOOLEAN) {
            return (byte) reader.readByte() == 1;
        } else if (readByte == CONST_TYPE_BYTE) {
            return (byte) reader.readByte();
        } else if (readByte == CONST_TYPE_CHARACTER) {
            return reader.readChar();
        } else if (readByte == CONST_TYPE_SHORT) {
            return reader.readShort();
        } else if (readByte == CONST_TYPE_INTEGER) {
            return reader.readInt();
        } else if (readByte == CONST_TYPE_FLOAT) {
            return reader.readFloat();
        } else if (readByte == CONST_TYPE_LONG) {
            return reader.readLong();
        } else if (readByte == CONST_TYPE_DOUBLE) {
            return reader.readDouble();
        } else if (readByte == CONST_TYPE_NULL) {
            return null;
        } else if (readByte == CONST_TYPE_STRING) {
            int length = reader.readInt();
            byte[] bytes = new byte[length];
            reader.readBytes(bytes);

            return new String(bytes, StandardCharsets.UTF_8);
        } else if (readByte == CONST_TYPE_OPACK_OBJECT) {
            int size = reader.readInt();
            OpackObject opackObject = new OpackObject(size);

            decodeContextStack.push(new Object[]{size, 0, CONTEXT_NULL_OBJECT, CONTEXT_NULL_OBJECT});
            decodeStack.push(opackObject);

            return CONTEXT_BRANCH_CONTEXT_OBJECT;
        } else if (readByte == CONST_TYPE_OPACK_ARRAY) {
            int length = reader.readInt();

            byte nativeType = (byte) reader.readByte();

            if (nativeType == CONST_NO_NATIVE_ARRAY) {
                OpackArray opackArray = new OpackArray(length);

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
                    throw new DecodeException(nativeType + " is not registered native array type binary in dense format. (unknown native array type)");
                }
            }
        }

        throw new DecodeException(readByte + " is not registered block header binary in dense codec. (unknown block header)");
    }
}
