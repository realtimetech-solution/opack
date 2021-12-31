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
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.util.OpackArrayConverter;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.util.structure.NativeList;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class DenseCodec extends OpackCodec<InputStream, OutputStream> {
    public final static class Builder {
        int encodeOutputBufferInitialSize;
        int encodeStackInitialSize;
        int decodeStackInitialSize;

        public Builder() {
            this.encodeOutputBufferInitialSize = 1024;
            this.encodeStackInitialSize = 128;
            this.decodeStackInitialSize = 128;
        }

        public Builder setEncodeOutputBufferInitialSize(int encodeOutputBufferInitialSize) {
            this.encodeOutputBufferInitialSize = encodeOutputBufferInitialSize;
            return this;
        }

        public Builder setEncodeStackInitialSize(int encodeStackInitialSize) {
            this.encodeStackInitialSize = encodeStackInitialSize;
            return this;
        }

        public Builder setDecodeStackInitialSize(int decodeStackInitialSize) {
            this.decodeStackInitialSize = decodeStackInitialSize;
            return this;
        }

        public DenseCodec create() {
            return new DenseCodec(this);
        }
    }

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

    final ByteArrayOutputStream encodeByteArrayStream;
    final FastStack<Object> encodeStack;

    final FastStack<OpackValue> decodeStack;
    final FastStack<Object[]> decodeContextStack;

    /**
     * Constructs the DenseCodec with the builder of DenseCodec.
     *
     * @param builder the builder of DenseCodec
     */
    DenseCodec(Builder builder) {
        super();

        this.encodeByteArrayStream = new ByteArrayOutputStream(builder.encodeOutputBufferInitialSize);
        this.encodeStack = new FastStack<>(builder.encodeStackInitialSize);

        this.decodeStack = new FastStack<>(builder.decodeStackInitialSize);
        this.decodeContextStack = new FastStack<>(builder.decodeStackInitialSize);
    }


    /**
     * Encodes the OpackValue to byte array through dense codec.
     *
     * @param outputStream the stream to encode
     * @param opackValue   the OpackValue to encode
     * @throws IOException              if an I/O error occurs when writing to byte stream
     * @throws IllegalArgumentException if the type of data to be encoded is not allowed in dense format
     */
    @Override
    protected void doEncode(OutputStream outputStream, OpackValue opackValue) throws IOException {
        DenseWriter denseWriter = new DenseWriter(outputStream);

        this.encodeStack.push(opackValue);
        this.encodeByteArrayStream.reset();

        while (!this.encodeStack.isEmpty()) {
            Object object = this.encodeStack.pop();

            if (object == null) {
                denseWriter.writeByte(CONST_TYPE_NULL);
                continue;
            }

            Class<?> objectType = object.getClass();

            if (ReflectionUtil.isWrapperType(objectType)) {
                objectType = ReflectionUtil.convertWrapperClassToPrimitiveClass(objectType);
            }

            if (objectType == OpackObject.class) {
                OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) object;
                int size = opackObject.size();

                denseWriter.writeByte(CONST_TYPE_OPACK_OBJECT);
                denseWriter.writeInt(size);

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

                    denseWriter.writeByte(CONST_TYPE_OPACK_ARRAY);
                    denseWriter.writeInt(length);

                    boolean optimized = true;

                    if (opackArrayList instanceof NativeList) {
                        NativeList nativeList = (NativeList) opackArrayList;
                        Object arrayObject = nativeList.getArrayObject();
                        Class<?> arrayType = arrayObject.getClass();

                        if (arrayType == boolean[].class) {
                            boolean[] array = (boolean[]) arrayObject;
                            denseWriter.writeByte(CONST_PRIMITIVE_BOOLEAN_NATIVE_ARRAY);
                            for (boolean value : array) {
                                denseWriter.writeByte(value ? 1 : 0);
                            }
                        } else if (arrayType == byte[].class) {
                            byte[] array = (byte[]) arrayObject;
                            denseWriter.writeByte(CONST_PRIMITIVE_BYTE_NATIVE_ARRAY);
                            for (byte value : array) {
                                denseWriter.writeByte(value);
                            }
                        } else if (arrayType == char[].class) {
                            char[] array = (char[]) arrayObject;
                            denseWriter.writeByte(CONST_PRIMITIVE_CHARACTER_NATIVE_ARRAY);
                            for (char value : array) {
                                denseWriter.writeChar(value);
                            }
                        } else if (arrayType == short[].class) {
                            short[] array = (short[]) arrayObject;
                            denseWriter.writeByte(CONST_PRIMITIVE_SHORT_NATIVE_ARRAY);
                            for (short value : array) {
                                denseWriter.writeShort(value);
                            }
                        } else if (arrayType == int[].class) {
                            int[] array = (int[]) arrayObject;
                            denseWriter.writeByte(CONST_PRIMITIVE_INTEGER_NATIVE_ARRAY);
                            for (int value : array) {
                                denseWriter.writeInt(value);
                            }
                        } else if (arrayType == float[].class) {
                            float[] array = (float[]) arrayObject;
                            denseWriter.writeByte(CONST_PRIMITIVE_FLOAT_NATIVE_ARRAY);
                            for (float value : array) {
                                denseWriter.writeFloat(value);
                            }
                        } else if (arrayType == long[].class) {
                            long[] array = (long[]) arrayObject;
                            denseWriter.writeByte(CONST_PRIMITIVE_LONG_NATIVE_ARRAY);
                            for (long value : array) {
                                denseWriter.writeLong(value);
                            }
                        } else if (arrayType == double[].class) {
                            double[] array = (double[]) arrayObject;
                            denseWriter.writeByte(CONST_PRIMITIVE_DOUBLE_NATIVE_ARRAY);
                            for (double value : array) {
                                denseWriter.writeDouble(value);
                            }
                        } else if (arrayType == Boolean[].class) {
                            Boolean[] array = (Boolean[]) arrayObject;
                            denseWriter.writeByte(CONST_WRAPPER_BOOLEAN_NATIVE_ARRAY);
                            for (Boolean value : array) {
                                if (value == null) {
                                    denseWriter.writeByte(0);
                                } else {
                                    denseWriter.writeByte(1);
                                    denseWriter.writeByte(value ? 1 : 0);
                                }
                            }
                        } else if (arrayType == Byte[].class) {
                            Byte[] array = (Byte[]) arrayObject;
                            denseWriter.writeByte(CONST_WRAPPER_BYTE_NATIVE_ARRAY);
                            for (Byte value : array) {
                                if (value == null) {
                                    denseWriter.writeByte(0);
                                } else {
                                    denseWriter.writeByte(1);
                                    denseWriter.writeByte(value);
                                }
                            }
                        } else if (arrayType == Character[].class) {
                            Character[] array = (Character[]) arrayObject;
                            denseWriter.writeByte(CONST_WRAPPER_CHARACTER_NATIVE_ARRAY);
                            for (Character value : array) {
                                if (value == null) {
                                    denseWriter.writeByte(0);
                                } else {
                                    denseWriter.writeByte(1);
                                    denseWriter.writeChar(value);
                                }
                            }
                        } else if (arrayType == Short[].class) {
                            Short[] array = (Short[]) arrayObject;
                            denseWriter.writeByte(CONST_WRAPPER_SHORT_NATIVE_ARRAY);
                            for (Short value : array) {
                                if (value == null) {
                                    denseWriter.writeByte(0);
                                } else {
                                    denseWriter.writeByte(1);
                                    denseWriter.writeShort(value);
                                }
                            }
                        } else if (arrayType == Integer[].class) {
                            Integer[] array = (Integer[]) arrayObject;
                            denseWriter.writeByte(CONST_WRAPPER_INTEGER_NATIVE_ARRAY);
                            for (Integer value : array) {
                                if (value == null) {
                                    denseWriter.writeByte(0);
                                } else {
                                    denseWriter.writeByte(1);
                                    denseWriter.writeInt(value);
                                }
                            }
                        } else if (arrayType == Float[].class) {
                            Float[] array = (Float[]) arrayObject;
                            denseWriter.writeByte(CONST_WRAPPER_FLOAT_NATIVE_ARRAY);
                            for (Float value : array) {
                                if (value == null) {
                                    denseWriter.writeByte(0);
                                } else {
                                    denseWriter.writeByte(1);
                                    denseWriter.writeFloat(value);
                                }
                            }
                        } else if (arrayType == Long[].class) {
                            Long[] array = (Long[]) arrayObject;
                            denseWriter.writeByte(CONST_WRAPPER_LONG_NATIVE_ARRAY);
                            for (Long value : array) {
                                if (value == null) {
                                    denseWriter.writeByte(0);
                                } else {
                                    denseWriter.writeByte(1);
                                    denseWriter.writeLong(value);
                                }
                            }
                        } else if (arrayType == Double[].class) {
                            Double[] array = (Double[]) arrayObject;
                            denseWriter.writeByte(CONST_WRAPPER_DOUBLE_NATIVE_ARRAY);
                            for (Double value : array) {
                                if (value == null) {
                                    denseWriter.writeByte(0);
                                } else {
                                    denseWriter.writeByte(1);
                                    denseWriter.writeDouble(value);
                                }
                            }
                        } else {
                            optimized = false;
                        }
                    } else {
                        optimized = false;
                    }

                    if (!optimized) {
                        denseWriter.writeByte(CONST_NO_NATIVE_ARRAY);

                        for (int index = length - 1; index >= 0; index--) {
                            Object value = opackArray.get(index);
                            encodeStack.push(value);
                        }
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalStateException("Failed to access the native list object in OpackArray");
                }
            } else {
                if (objectType == boolean.class) {
                    denseWriter.writeByte(CONST_TYPE_BOOLEAN);
                    denseWriter.writeByte((boolean) object ? 1 : 0);
                } else if (objectType == byte.class) {
                    denseWriter.writeByte(CONST_TYPE_BYTE);
                    denseWriter.writeByte((byte) object);
                } else if (objectType == char.class) {
                    denseWriter.writeByte(CONST_TYPE_CHARACTER);
                    denseWriter.writeChar((char) object);
                } else if (objectType == short.class) {
                    denseWriter.writeByte(CONST_TYPE_SHORT);
                    denseWriter.writeShort((short) object);
                } else if (objectType == int.class) {
                    denseWriter.writeByte(CONST_TYPE_INTEGER);
                    denseWriter.writeInt((int) object);
                } else if (objectType == float.class) {
                    denseWriter.writeByte(CONST_TYPE_FLOAT);
                    denseWriter.writeFloat((float) object);
                } else if (objectType == long.class) {
                    denseWriter.writeByte(CONST_TYPE_LONG);
                    denseWriter.writeLong((long) object);
                } else if (objectType == double.class) {
                    denseWriter.writeByte(CONST_TYPE_DOUBLE);
                    denseWriter.writeDouble((double) object);
                } else if (objectType == String.class) {
                    String string = (String) object;
                    byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

                    denseWriter.writeByte(CONST_TYPE_STRING);
                    denseWriter.writeInt(bytes.length);
                    denseWriter.writeBytes(bytes);
                } else {
                    throw new IllegalArgumentException(objectType + " is not allowed in dense format. (unknown literal object type)");
                }
            }
        }
    }

    public synchronized byte[] encode(OpackValue opackValue) throws EncodeException {
        this.encodeByteArrayStream.reset();
        this.encode(this.encodeByteArrayStream, opackValue);
        return this.encodeByteArrayStream.toByteArray();
    }

    /**
     * Decodes one block to OpackValue, (basic block protocol: header(1 byte), data (variable))
     * If data of block to be decoded is OpackObject or OpackArray(excluding primitive array), returns CONTEXT_BRANCH_CONTEXT_OBJECT for linear decoding.
     *
     * @param denseReader the byte buffer that wraps the data
     * @return opack value or CONTEXT_BRANCH_CONTEXT_OBJECT
     * @throws IllegalArgumentException if the type of data to be decoded is not allowed in dense format; if unknown block header is parsed
     */
    Object decodeBlock(DenseReader denseReader) throws IOException {
        byte b = (byte) denseReader.readByte();

        if (b == CONST_TYPE_BOOLEAN) {
            return (byte) denseReader.readByte() == 1;
        } else if (b == CONST_TYPE_BYTE) {
            return (byte) denseReader.readByte();
        } else if (b == CONST_TYPE_CHARACTER) {
            return denseReader.readChar();
        } else if (b == CONST_TYPE_SHORT) {
            return denseReader.readShort();
        } else if (b == CONST_TYPE_INTEGER) {
            return denseReader.readInt();
        } else if (b == CONST_TYPE_FLOAT) {
            return denseReader.readFloat();
        } else if (b == CONST_TYPE_LONG) {
            return denseReader.readLong();
        } else if (b == CONST_TYPE_DOUBLE) {
            return denseReader.readDouble();
        } else if (b == CONST_TYPE_NULL) {
            return null;
        } else if (b == CONST_TYPE_STRING) {
            int length = denseReader.readInt();
            byte[] bytes = new byte[length];
            denseReader.readBytes(bytes);

            return new String(bytes, StandardCharsets.UTF_8);
        } else if (b == CONST_TYPE_OPACK_OBJECT) {
            int size = denseReader.readInt();
            OpackObject<Object, Object> opackObject = new OpackObject<>(size);

            decodeContextStack.push(new Object[]{size, 0, CONTEXT_NULL_OBJECT, CONTEXT_NULL_OBJECT});
            decodeStack.push(opackObject);

            return CONTEXT_BRANCH_CONTEXT_OBJECT;
        } else if (b == CONST_TYPE_OPACK_ARRAY) {
            int length = denseReader.readInt();

            byte nativeType = (byte) denseReader.readByte();

            if (nativeType == CONST_NO_NATIVE_ARRAY) {
                OpackArray<Object> opackArray = new OpackArray<>(length);

                decodeContextStack.push(new Object[]{length, 0});
                decodeStack.push(opackArray);

                return CONTEXT_BRANCH_CONTEXT_OBJECT;
            } else {
                if (nativeType == CONST_PRIMITIVE_BOOLEAN_NATIVE_ARRAY) {
                    boolean[] array = new boolean[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = denseReader.readByte() == 1;
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_BYTE_NATIVE_ARRAY) {
                    byte[] array = new byte[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = (byte) denseReader.readByte();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_CHARACTER_NATIVE_ARRAY) {
                    char[] array = new char[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = denseReader.readChar();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_SHORT_NATIVE_ARRAY) {
                    short[] array = new short[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = denseReader.readShort();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_INTEGER_NATIVE_ARRAY) {
                    int[] array = new int[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = denseReader.readInt();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_FLOAT_NATIVE_ARRAY) {
                    float[] array = new float[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = denseReader.readFloat();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_LONG_NATIVE_ARRAY) {
                    long[] array = new long[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = denseReader.readLong();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_PRIMITIVE_DOUBLE_NATIVE_ARRAY) {
                    double[] array = new double[length];
                    for (int index = 0; index < array.length; index++) {
                        array[index] = denseReader.readDouble();
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_BOOLEAN_NATIVE_ARRAY) {
                    Boolean[] array = new Boolean[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = denseReader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = denseReader.readByte() == 1;
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_BYTE_NATIVE_ARRAY) {
                    Byte[] array = new Byte[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = denseReader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = (byte) denseReader.readByte();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_CHARACTER_NATIVE_ARRAY) {
                    Character[] array = new Character[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = denseReader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = (char) denseReader.readChar();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_SHORT_NATIVE_ARRAY) {
                    Short[] array = new Short[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = denseReader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = denseReader.readShort();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_INTEGER_NATIVE_ARRAY) {
                    Integer[] array = new Integer[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = denseReader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = denseReader.readInt();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_FLOAT_NATIVE_ARRAY) {
                    Float[] array = new Float[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = denseReader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = denseReader.readFloat();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_LONG_NATIVE_ARRAY) {
                    Long[] array = new Long[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = denseReader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = denseReader.readLong();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_WRAPPER_DOUBLE_NATIVE_ARRAY) {
                    Double[] array = new Double[length];
                    for (int index = 0; index < array.length; index++) {
                        boolean nullFlag = denseReader.readByte() == 1;
                        if (nullFlag) {
                            array[index] = denseReader.readDouble();
                        }
                    }
                    return OpackArray.createWithArrayObject(array);
                } else {
                    throw new IllegalArgumentException(nativeType + " is not allowed in dense format. (unknown native type)");
                }
            }
        }

        throw new IllegalArgumentException(b + " is not registered block header binary in dense codec. (unknown block header)");
    }

    public static void main(String[] args) {
        Number[] numbers = (Number[]) Array.newInstance(Integer.class, 10);
        numbers[0] = 10;
        System.out.println(numbers);
    }

    /**
     * Decodes the byte array encoded through the dense codec to OpackValue.
     *
     * @param inputStream the stream to decode
     * @return opack value
     * @throws IllegalArgumentException if the decoded value is not a opack value
     */
    @Override
    protected OpackValue doDecode(InputStream inputStream) throws IOException {
        DenseReader denseReader = new DenseReader(inputStream);

        this.decodeStack.reset();
        this.decodeContextStack.reset();

        decodeBlock(denseReader);
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
                        key = decodeBlock(denseReader);
                        if (key == CONTEXT_BRANCH_CONTEXT_OBJECT) {
                            context[2] = this.decodeStack.peek();
                            bypass = true;
                            break;
                        } else {
                            context[2] = key;
                        }
                    }

                    if (value == CONTEXT_NULL_OBJECT) {
                        value = decodeBlock(denseReader);
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
                    Object value = decodeBlock(denseReader);

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

    public OpackValue decode(byte[] bytes) throws DecodeException {
        return this.decode(new ByteArrayInputStream(bytes));
    }
}
