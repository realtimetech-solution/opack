package com.realtimetech.opack.codec.dense;

import com.realtimetech.opack.codec.OpackCodec;
import com.realtimetech.opack.util.OpackArrayConverter;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.util.structure.PrimitiveList;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DenseCodec extends OpackCodec<byte[]> {
    public final static class Builder {
        int encodeStackInitialSize;
        int decodeStackInitialSize;

        public Builder() {
            this.encodeStackInitialSize = 128;
            this.decodeStackInitialSize = 128;
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

    private static final byte CONST_BYTE_NATIVE_ARRAY = 0x21;
    private static final byte CONST_CHARACTER_NATIVE_ARRAY = 0x22;
    private static final byte CONST_SHORT_NATIVE_ARRAY = 0x23;
    private static final byte CONST_INTEGER_NATIVE_ARRAY = 0x24;
    private static final byte CONST_FLOAT_NATIVE_ARRAY = 0x25;
    private static final byte CONST_LONG_NATIVE_ARRAY = 0x26;
    private static final byte CONST_DOUBLE_NATIVE_ARRAY = 0x27;
    private static final byte CONST_NO_NATIVE_ARRAY = 0x2F;

    private static final Object CONTEXT_NULL_OBJECT = new Object();
    private static final Object CONTEXT_BRANCH_CONTEXT_OBJECT = new Object();

    final ByteArrayOutputStream encodeByteArrayStream;
    final FastStack<Object> encodeStack;

    int decodePointer;
    final FastStack<OpackValue> decodeStack;
    final FastStack<Object[]> decodeContextStack;

    final byte[] byte8Buffer;
    final byte[] byte4Buffer;
    final byte[] byte2Buffer;

    DenseCodec(Builder builder) {
        super();

        this.encodeByteArrayStream = new ByteArrayOutputStream();
        this.encodeStack = new FastStack<>(builder.encodeStackInitialSize);

        this.decodePointer = 0;
        this.decodeStack = new FastStack<>(builder.decodeStackInitialSize);
        this.decodeContextStack = new FastStack<>(builder.decodeStackInitialSize);

        this.byte8Buffer = new byte[8];
        this.byte4Buffer = new byte[4];
        this.byte2Buffer = new byte[2];
    }

    @Override
    protected byte[] doEncode(OpackValue opackValue) throws IOException {
        this.encodeStack.push(opackValue);
        this.encodeByteArrayStream.reset();

        while (!this.encodeStack.isEmpty()) {
            Object object = this.encodeStack.pop();

            if (object == null) {
                this.encodeByteArrayStream.write(CONST_TYPE_NULL);
                continue;
            }

            Class<?> type = object.getClass();

            if (ReflectionUtil.isWrapperClass(type)) {
                type = ReflectionUtil.getPrimitiveClassOfWrapperClass(type);
            }

            if (type == OpackObject.class) {
                OpackObject opackObject = (OpackObject) object;
                int size = opackObject.size();

                this.encodeByteArrayStream.write(CONST_TYPE_OPACK_OBJECT);
                ByteBuffer.wrap(byte4Buffer).putInt(size);
                this.encodeByteArrayStream.write(byte4Buffer);

                for (Object key : opackObject.keySet()) {
                    Object value = opackObject.get(key);
                    this.encodeStack.push(value);
                    this.encodeStack.push(key);
                }
            } else if (type == OpackArray.class) {
                OpackArray opackArray = (OpackArray) object;
                int length = opackArray.length();

                try {
                    List<?> opackArrayList = OpackArrayConverter.getOpackArrayList(opackArray);

                    this.encodeByteArrayStream.write(CONST_TYPE_OPACK_ARRAY);
                    ByteBuffer.wrap(byte4Buffer).putInt(length);
                    this.encodeByteArrayStream.write(byte4Buffer);

                    boolean optimized = false;

                    if (opackArrayList instanceof PrimitiveList) {
                        PrimitiveList primitiveList = (PrimitiveList) opackArrayList;
                        Object arrayObject = primitiveList.getArrayObject();
                        Class<?> arrayType = arrayObject.getClass();

                        if (arrayType == byte[].class) {
                            encodeByteArrayStream.write(CONST_BYTE_NATIVE_ARRAY);
                            byte[] array = (byte[]) arrayObject;
                            encodeByteArrayStream.write(array);
                            optimized = true;
                        } else if (arrayType == char[].class) {
                            encodeByteArrayStream.write(CONST_CHARACTER_NATIVE_ARRAY);
                            char[] array = (char[]) arrayObject;
                            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
                            CharBuffer typeBuffer = byteBuffer.asCharBuffer();
                            typeBuffer.put(array);

                            encodeByteArrayStream.write(byteBuffer.array());
                            optimized = true;
                        } else if (arrayType == short[].class) {
                            encodeByteArrayStream.write(CONST_SHORT_NATIVE_ARRAY);
                            short[] array = (short[]) arrayObject;
                            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 2);
                            ShortBuffer typeBuffer = byteBuffer.asShortBuffer();
                            typeBuffer.put(array);

                            encodeByteArrayStream.write(byteBuffer.array());
                            optimized = true;
                        } else if (arrayType == int[].class) {
                            encodeByteArrayStream.write(CONST_INTEGER_NATIVE_ARRAY);
                            int[] array = (int[]) arrayObject;
                            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);
                            IntBuffer typeBuffer = byteBuffer.asIntBuffer();
                            typeBuffer.put(array);

                            encodeByteArrayStream.write(byteBuffer.array());
                            optimized = true;
                        } else if (arrayType == float[].class) {
                            encodeByteArrayStream.write(CONST_FLOAT_NATIVE_ARRAY);
                            float[] array = (float[]) arrayObject;
                            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);
                            FloatBuffer typeBuffer = byteBuffer.asFloatBuffer();
                            typeBuffer.put(array);

                            encodeByteArrayStream.write(byteBuffer.array());
                            optimized = true;
                        } else if (arrayType == long[].class) {
                            encodeByteArrayStream.write(CONST_LONG_NATIVE_ARRAY);
                            long[] array = (long[]) arrayObject;
                            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 8);
                            LongBuffer typeBuffer = byteBuffer.asLongBuffer();
                            typeBuffer.put(array);

                            encodeByteArrayStream.write(byteBuffer.array());
                            optimized = true;
                        } else if (arrayType == double[].class) {
                            encodeByteArrayStream.write(CONST_DOUBLE_NATIVE_ARRAY);
                            double[] array = (double[]) arrayObject;
                            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 8);
                            DoubleBuffer typeBuffer = byteBuffer.asDoubleBuffer();
                            typeBuffer.put(array);

                            encodeByteArrayStream.write(byteBuffer.array());
                            optimized = true;
                        }
                    }

                    if (!optimized) {
                        encodeByteArrayStream.write(CONST_NO_NATIVE_ARRAY);

                        for (int index = length - 1; index >= 0; index--) {
                            Object value = opackArray.get(index);
                            encodeStack.push(value);
                        }
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalStateException("Failed to access the OpackArray native list object.");
                }
            } else {
                if (type == boolean.class) {
                    encodeByteArrayStream.write(CONST_TYPE_BOOLEAN);
                    encodeByteArrayStream.write((boolean) object ? 1 : 0);
                } else if (type == byte.class) {
                    encodeByteArrayStream.write(CONST_TYPE_BYTE);
                    encodeByteArrayStream.write((byte) object);
                } else if (type == char.class) {
                    encodeByteArrayStream.write(CONST_TYPE_CHARACTER);
                    encodeByteArrayStream.write((char) object);
                } else if (type == short.class) {
                    encodeByteArrayStream.write(CONST_TYPE_SHORT);
                    short value = (short) object;

                    ByteBuffer.wrap(byte2Buffer).putShort(value);
                    encodeByteArrayStream.write(byte2Buffer);
                } else if (type == int.class) {
                    encodeByteArrayStream.write(CONST_TYPE_INTEGER);
                    int value = (int) object;

                    ByteBuffer.wrap(byte4Buffer).putInt(value);
                    encodeByteArrayStream.write(byte4Buffer);
                } else if (type == float.class) {
                    encodeByteArrayStream.write(CONST_TYPE_FLOAT);
                    float value = (float) object;

                    ByteBuffer.wrap(byte4Buffer).putFloat(value);
                    encodeByteArrayStream.write(byte4Buffer);
                } else if (type == long.class) {
                    encodeByteArrayStream.write(CONST_TYPE_LONG);
                    long value = (long) object;

                    ByteBuffer.wrap(byte8Buffer).putDouble(value);
                    encodeByteArrayStream.write(byte8Buffer);
                } else if (type == double.class) {
                    encodeByteArrayStream.write(CONST_TYPE_DOUBLE);
                    double value = (double) object;

                    ByteBuffer.wrap(byte8Buffer).putDouble(value);
                    encodeByteArrayStream.write(byte8Buffer);
                } else if (type == String.class) {
                    encodeByteArrayStream.write(CONST_TYPE_STRING);
                    String value = (String) object;

                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                    ByteBuffer.wrap(byte4Buffer).putInt(bytes.length);
                    encodeByteArrayStream.write(byte4Buffer);
                    encodeByteArrayStream.write(bytes);
                } else {
                    throw new IllegalArgumentException(type + " is not allowed in dense format. (unknown literal object type)");
                }
            }
        }

        return encodeByteArrayStream.toByteArray();
    }

    Object decodeBlock(byte[] data, ByteBuffer byteBuffer) {
        byte b = data[decodePointer++];

        if (b == CONST_TYPE_BOOLEAN) {
            byte bool = data[decodePointer++];
            return bool == 1;
        } else if (b == CONST_TYPE_BYTE) {
            return data[decodePointer++];
        } else if (b == CONST_TYPE_CHARACTER) {
            return (char) data[decodePointer++];
        } else if (b == CONST_TYPE_SHORT) {
            short value = byteBuffer.getShort(decodePointer);
            decodePointer += 2;
            return value;
        } else if (b == CONST_TYPE_INTEGER) {
            int value = byteBuffer.getInt(decodePointer);
            decodePointer += 4;
            return value;
        } else if (b == CONST_TYPE_FLOAT) {
            float value = byteBuffer.getFloat(decodePointer);
            decodePointer += 4;
            return value;
        } else if (b == CONST_TYPE_LONG) {
            long value = byteBuffer.getLong(decodePointer);
            decodePointer += 8;
            return value;
        } else if (b == CONST_TYPE_DOUBLE) {
            double value = byteBuffer.getDouble(decodePointer);
            decodePointer += 8;
            return value;
        } else if (b == CONST_TYPE_NULL) {
            return null;
        } else if (b == CONST_TYPE_STRING) {
            int length = byteBuffer.getInt(decodePointer);
            decodePointer += 4;
            byte[] bytes = new byte[length];
            byteBuffer.position(decodePointer).get(bytes, 0, length);
            byteBuffer.position(0);

            decodePointer += length;

            return new String(bytes, StandardCharsets.UTF_8);
        } else if (b == CONST_TYPE_OPACK_OBJECT) {
            int size = byteBuffer.getInt(decodePointer);
            decodePointer += 4;
            OpackObject opackObject = new OpackObject<>(size);

            decodeContextStack.push(new Object[]{size, 0, CONTEXT_NULL_OBJECT, CONTEXT_NULL_OBJECT});
            decodeStack.push(opackObject);

            return CONTEXT_BRANCH_CONTEXT_OBJECT;
        } else if (b == CONST_TYPE_OPACK_ARRAY) {
            int length = byteBuffer.getInt(decodePointer);
            decodePointer += 4;

            byte nativeType = byteBuffer.get(decodePointer);
            decodePointer += 1;

            if (nativeType == CONST_NO_NATIVE_ARRAY) {
                OpackArray opackArray = new OpackArray<>(length);

                decodeContextStack.push(new Object[]{length, 0});
                decodeStack.push(opackArray);

                return CONTEXT_BRANCH_CONTEXT_OBJECT;
            } else {
                if (nativeType == CONST_BYTE_NATIVE_ARRAY) {
                    byte[] array = new byte[length];
                    byteBuffer.position(decodePointer).get(array, 0, length);
                    decodePointer += length;

                    byteBuffer.position(0);
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_CHARACTER_NATIVE_ARRAY) {
                    char[] array = new char[length];
                    byteBuffer.position(decodePointer).asCharBuffer().get(array, 0, length);
                    decodePointer += length;

                    byteBuffer.position(0);
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_SHORT_NATIVE_ARRAY) {
                    short[] array = new short[length];
                    byteBuffer.position(decodePointer).asShortBuffer().get(array, 0, length);
                    decodePointer += length;

                    byteBuffer.position(0);
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_INTEGER_NATIVE_ARRAY) {
                    int[] array = new int[length];
                    byteBuffer.position(decodePointer).asIntBuffer().get(array, 0, length);
                    decodePointer += length;

                    byteBuffer.position(0);
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_FLOAT_NATIVE_ARRAY) {
                    float[] array = new float[length];
                    byteBuffer.position(decodePointer).asFloatBuffer().get(array, 0, length);
                    decodePointer += length;

                    byteBuffer.position(0);
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_LONG_NATIVE_ARRAY) {
                    long[] array = new long[length];
                    byteBuffer.position(decodePointer).asLongBuffer().get(array, 0, length);
                    decodePointer += length;

                    byteBuffer.position(0);
                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_DOUBLE_NATIVE_ARRAY) {
                    double[] array = new double[length];
                    byteBuffer.position(decodePointer).asDoubleBuffer().get(array, 0, length);
                    decodePointer += length;

                    byteBuffer.position(0);
                    return OpackArray.createWithArrayObject(array);
                } else {
                    throw new IllegalStateException(nativeType + " is not allowed in dense format. (unknown native type)");
                }
            }
        }

        throw new IllegalStateException(b + " is not registered block header binary in dense codec. (unknown block header)");
    }

    @Override
    protected OpackValue doDecode(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        this.decodeStack.reset();
        this.decodeContextStack.reset();
        this.decodePointer = 0;

        decodeBlock(data, byteBuffer);
        OpackValue rootValue = this.decodeStack.peek();

        while (!this.decodeStack.isEmpty()) {
            OpackValue opackValue = this.decodeStack.peek();
            Object[] context = this.decodeContextStack.peek();

            Integer size = (Integer) context[0];
            Integer offset = (Integer) context[1];

            boolean bypass = false;
            int index = offset;

            if (opackValue instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) opackValue;

                for (; index < size; index++) {
                    Object key = context[2];
                    Object value = context[3];

                    if (key == CONTEXT_NULL_OBJECT) {
                        key = decodeBlock(data, byteBuffer);
                        if (key == CONTEXT_BRANCH_CONTEXT_OBJECT) {
                            context[2] = this.decodeStack.peek();
                            bypass = true;
                            break;
                        } else {
                            context[2] = key;
                        }
                    }

                    if (value == CONTEXT_NULL_OBJECT) {
                        value = decodeBlock(data, byteBuffer);
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
                OpackArray opackArray = (OpackArray) opackValue;

                for (; index < size; index++) {
                    Object value = decodeBlock(data, byteBuffer);

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
                throw new IllegalArgumentException(opackValue.getClass() +" is not a type of opack value. (Unknown opack value type)");
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
}
