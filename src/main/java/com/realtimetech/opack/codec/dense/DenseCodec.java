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
    private static final byte CONST_TYPE_VOID_NULL = 0x18;
    private static final byte CONST_TYPE_STRING = 0x19;

    private static final byte CONST_BYTE_NATIVE_ARRAY = 0x21;
    private static final byte CONST_CHARACTER_NATIVE_ARRAY = 0x22;
    private static final byte CONST_SHORT_NATIVE_ARRAY = 0x23;
    private static final byte CONST_INTEGER_NATIVE_ARRAY = 0x24;
    private static final byte CONST_FLOAT_NATIVE_ARRAY = 0x25;
    private static final byte CONST_LONG_NATIVE_ARRAY = 0x26;
    private static final byte CONST_DOUBLE_NATIVE_ARRAY = 0x27;
    private static final byte CONST_NO_NATIVE_ARRAY = 0x2F;

    @Override
    protected byte[] doEncode(OpackValue opackValue) throws IOException {
        FastStack<Object> objectStack = new FastStack<>();

        objectStack.push(opackValue);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] byte8Buffer = new byte[8];
        byte[] byte4Buffer = new byte[4];
        byte[] byte2Buffer = new byte[2];

        while (!objectStack.isEmpty()) {
            Object object = objectStack.pop();

            if (object == null) {
                byteArrayOutputStream.write(CONST_TYPE_VOID_NULL);
                byteArrayOutputStream.write(0);
                continue;
            }

            Class<?> type = object.getClass();

            if (ReflectionUtil.isWrapperClass(type)) {
                type = ReflectionUtil.getPrimitiveClassOfWrapperClass(type);
            }

            if (type == OpackObject.class) {
                OpackObject opackObject = (OpackObject) object;

                byteArrayOutputStream.write(CONST_TYPE_OPACK_OBJECT);
                int size = opackObject.size();
                ByteBuffer.wrap(byte4Buffer).putInt(size);
                byteArrayOutputStream.write(byte4Buffer);

                for (Object key : opackObject.keySet()) {
                    Object value = opackObject.get(key);
                    objectStack.push(value);
                    objectStack.push(key);
                }
            } else if (type == OpackArray.class) {
                OpackArray opackArray = (OpackArray) object;

                List<?> opackArrayList = null;
                try {
                    opackArrayList = OpackArrayConverter.getOpackArrayList(opackArray);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                byteArrayOutputStream.write(CONST_TYPE_OPACK_ARRAY);
                int length = opackArray.length();

                ByteBuffer.wrap(byte4Buffer).putInt(length);
                byteArrayOutputStream.write(byte4Buffer);

                boolean optimized = false;

                if (opackArrayList instanceof PrimitiveList) {
                    PrimitiveList primitiveList = (PrimitiveList) opackArrayList;
                    Object arrayObject = primitiveList.getArrayObject();
                    Class<?> arrayType = arrayObject.getClass();

                    if (arrayType == byte[].class) {
                        byteArrayOutputStream.write(CONST_BYTE_NATIVE_ARRAY);
                        byte[] array = (byte[]) arrayObject;
                        byteArrayOutputStream.write(array);
                        optimized = true;
                    } else if (arrayType == char[].class) {
                        byteArrayOutputStream.write(CONST_CHARACTER_NATIVE_ARRAY);
                        char[] array = (char[]) arrayObject;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
                        CharBuffer typeBuffer = byteBuffer.asCharBuffer();
                        typeBuffer.put(array);

                        byteArrayOutputStream.write(byteBuffer.array());
                        optimized = true;
                    } else if (arrayType == short[].class) {
                        byteArrayOutputStream.write(CONST_SHORT_NATIVE_ARRAY);
                        short[] array = (short[]) arrayObject;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 2);
                        ShortBuffer typeBuffer = byteBuffer.asShortBuffer();
                        typeBuffer.put(array);

                        byteArrayOutputStream.write(byteBuffer.array());
                        optimized = true;
                    } else if (arrayType == int[].class) {
                        byteArrayOutputStream.write(CONST_INTEGER_NATIVE_ARRAY);
                        int[] array = (int[]) arrayObject;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);
                        IntBuffer typeBuffer = byteBuffer.asIntBuffer();
                        typeBuffer.put(array);

                        byteArrayOutputStream.write(byteBuffer.array());
                        optimized = true;
                    } else if (arrayType == float[].class) {
                        byteArrayOutputStream.write(CONST_FLOAT_NATIVE_ARRAY);
                        float[] array = (float[]) arrayObject;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);
                        FloatBuffer typeBuffer = byteBuffer.asFloatBuffer();
                        typeBuffer.put(array);

                        byteArrayOutputStream.write(byteBuffer.array());
                        optimized = true;
                    } else if (arrayType == long[].class) {
                        byteArrayOutputStream.write(CONST_LONG_NATIVE_ARRAY);
                        long[] array = (long[]) arrayObject;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 8);
                        LongBuffer typeBuffer = byteBuffer.asLongBuffer();
                        typeBuffer.put(array);

                        byteArrayOutputStream.write(byteBuffer.array());
                        optimized = true;
                    } else if (arrayType == double[].class) {
                        byteArrayOutputStream.write(CONST_DOUBLE_NATIVE_ARRAY);
                        double[] array = (double[]) arrayObject;
                        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 8);
                        DoubleBuffer typeBuffer = byteBuffer.asDoubleBuffer();
                        typeBuffer.put(array);

                        byteArrayOutputStream.write(byteBuffer.array());
                        optimized = true;
                    }
                }

                if (!optimized) {
                    byteArrayOutputStream.write(CONST_NO_NATIVE_ARRAY);

                    for (int index = length - 1; index >= 0; index--) {
                        Object value = opackArray.get(index);
                        objectStack.push(value);
                    }
                }
            } else {
                if (type == boolean.class) {
                    byteArrayOutputStream.write(CONST_TYPE_BOOLEAN);
                    byteArrayOutputStream.write((boolean) object ? 1 : 0);
                } else if (type == byte.class) {
                    byteArrayOutputStream.write(CONST_TYPE_BYTE);
                    byteArrayOutputStream.write((byte) object);
                } else if (type == char.class) {
                    byteArrayOutputStream.write(CONST_TYPE_CHARACTER);
                    byteArrayOutputStream.write((char) object);
                } else if (type == short.class) {
                    byteArrayOutputStream.write(CONST_TYPE_SHORT);
                    short value = (short) object;

                    ByteBuffer.wrap(byte2Buffer).putShort(value);
                    byteArrayOutputStream.write(byte2Buffer);
                } else if (type == int.class) {
                    byteArrayOutputStream.write(CONST_TYPE_INTEGER);
                    int value = (int) object;

                    ByteBuffer.wrap(byte4Buffer).putInt(value);
                    byteArrayOutputStream.write(byte4Buffer);
                } else if (type == float.class) {
                    byteArrayOutputStream.write(CONST_TYPE_FLOAT);
                    float value = (float) object;

                    ByteBuffer.wrap(byte4Buffer).putFloat(value);
                    byteArrayOutputStream.write(byte4Buffer);
                } else if (type == long.class) {
                    byteArrayOutputStream.write(CONST_TYPE_LONG);
                    long value = (long) object;

                    ByteBuffer.wrap(byte8Buffer).putDouble(value);
                    byteArrayOutputStream.write(byte8Buffer);
                } else if (type == double.class) {
                    byteArrayOutputStream.write(CONST_TYPE_DOUBLE);
                    double value = (double) object;

                    ByteBuffer.wrap(byte8Buffer).putDouble(value);
                    byteArrayOutputStream.write(byte8Buffer);
                } else if (type == void.class) {
                    byteArrayOutputStream.write(CONST_TYPE_VOID_NULL);
                    byteArrayOutputStream.write(1);
                } else if (type == String.class) {
                    byteArrayOutputStream.write(CONST_TYPE_STRING);
                    String value = (String) object;

                    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
                    ByteBuffer.wrap(byte4Buffer).putInt(bytes.length);
                    byteArrayOutputStream.write(byte4Buffer);
                    byteArrayOutputStream.write(bytes);
                }
            }
        }

        return byteArrayOutputStream.toByteArray();
    }

    int pointer;
    FastStack<OpackValue> baseStack = new FastStack<>();
    FastStack<Object[]> contextStack = new FastStack<>();

    final Object CONTEXT_NULL_OBJECT = new Object();


    public Object parse(byte[] data, ByteBuffer byteBuffer) {
        byte b = data[pointer++];

        if (b == CONST_TYPE_BOOLEAN) {
            byte bool = data[pointer++];
            return bool == 1;
        } else if (b == CONST_TYPE_BYTE) {
            return data[pointer++];
        } else if (b == CONST_TYPE_CHARACTER) {
            return (char) data[pointer++];
        } else if (b == CONST_TYPE_SHORT) {
            short value = byteBuffer.getShort(pointer);
            pointer += 2;
            return value;
        } else if (b == CONST_TYPE_INTEGER) {
            int value = byteBuffer.getInt(pointer);
            pointer += 4;
            return value;
        } else if (b == CONST_TYPE_FLOAT) {
            float value = byteBuffer.getFloat(pointer);
            pointer += 4;
            return value;
        } else if (b == CONST_TYPE_LONG) {
            long value = byteBuffer.getLong(pointer);
            pointer += 8;
            return value;
        } else if (b == CONST_TYPE_DOUBLE) {
            double value = byteBuffer.getDouble(pointer);
            pointer += 8;
            return value;
        } else if (b == CONST_TYPE_VOID_NULL) {
            byte bool = data[pointer++];
            return null;
        } else if (b == CONST_TYPE_STRING) {
            int length = byteBuffer.getInt(pointer);
            pointer += 4;
            byte[] bytes = new byte[length];
            byteBuffer.position(pointer).get(bytes, 0, length);
            byteBuffer.position(0);

            pointer += length;

            return new String(bytes, StandardCharsets.UTF_8);
        } else if (b == CONST_TYPE_OPACK_OBJECT) {
            int size = byteBuffer.getInt(pointer);
            pointer += 4;
            OpackObject opackObject = new OpackObject<>(size);

            contextStack.push(new Object[]{size, 0, CONTEXT_NULL_OBJECT, CONTEXT_NULL_OBJECT});
            baseStack.push(opackObject);

            return opackObject;
        } else if (b == CONST_TYPE_OPACK_ARRAY) {
            int length = byteBuffer.getInt(pointer);
            pointer += 4;

            byte nativeType = byteBuffer.get(pointer);
            pointer += 1;

            if (nativeType == CONST_NO_NATIVE_ARRAY) {
                OpackArray opackArray = new OpackArray<>(length);

                contextStack.push(new Object[]{length, 0});
                baseStack.push(opackArray);

                return opackArray;
            } else {
                if (nativeType == CONST_BYTE_NATIVE_ARRAY) {
                    byte[] array = new byte[length];
                    byteBuffer.position(pointer).get(array,0, length);
                    pointer += length;

                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_CHARACTER_NATIVE_ARRAY) {
                    char[] array = new char[length];
                    byteBuffer.position(pointer).asCharBuffer().get(array,0, length);
                    pointer += length;

                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_SHORT_NATIVE_ARRAY) {
                    short[] array = new short[length];
                    byteBuffer.position(pointer).asShortBuffer().get(array,0, length);
                    pointer += length;

                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_INTEGER_NATIVE_ARRAY) {
                    int[] array = new int[length];
                    byteBuffer.position(pointer).asIntBuffer().get(array,0, length);
                    pointer += length;

                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_FLOAT_NATIVE_ARRAY) {
                    float[] array = new float[length];
                    byteBuffer.position(pointer).asFloatBuffer().get(array,0, length);
                    pointer += length;

                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_LONG_NATIVE_ARRAY) {
                    long[] array = new long[length];
                    byteBuffer.position(pointer).asLongBuffer().get(array,0, length);
                    pointer += length;

                    return OpackArray.createWithArrayObject(array);
                } else if (nativeType == CONST_DOUBLE_NATIVE_ARRAY) {
                    double[] array = new double[length];
                    byteBuffer.position(pointer).asDoubleBuffer().get(array,0, length);
                    pointer += length;

                    return OpackArray.createWithArrayObject(array);
                }
                byteBuffer.position(0);
            }

        }

        throw new Error("S");
    }

    @Override
    protected OpackValue doDecode(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        baseStack.reset();
        contextStack.reset();

        pointer = 0;

        OpackValue root = (OpackValue) parse(data, byteBuffer);

        while (!baseStack.isEmpty()) {
            OpackValue opackValue = (OpackValue) baseStack.peek();
            Object[] context = contextStack.peek();
            Integer size = (Integer) context[0];
            Integer offset = (Integer) context[1];
//            System.out.println(" ---------- Read");
//            System.out.println("   O :  " + opackValue);
//            System.out.println("   I :  " + seekIndex);
//            System.out.println("   S :  " + size);

            if (opackValue instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) opackValue;

                boolean bypass = false;
                int index = offset;
                for (index = offset; index < size; index++) {
                    Object key = context[2];
                    Object value = context[3];

                    if (key == CONTEXT_NULL_OBJECT) {
                        key = parse(data, byteBuffer);
                        context[2] = key;
                        if (key instanceof OpackValue) {
                            bypass = true;
                            break;
                        }
                    }
                    if (value == CONTEXT_NULL_OBJECT) {
                        value = parse(data, byteBuffer);
                        context[3] = value;
                        if (value instanceof OpackValue) {
                            bypass = true;
                            break;
                        }
                    }

                    opackObject.put(key, value);
                    context[2] = CONTEXT_NULL_OBJECT;
                    context[3] = CONTEXT_NULL_OBJECT;
                }

                if (!bypass) {
                    baseStack.pop();
                    contextStack.pop();
                } else {
                    context[1] = index;
                }
            } else if (opackValue instanceof OpackArray) {
                OpackArray opackArray = (OpackArray) opackValue;

                boolean bypass = false;
                int index = offset;
                for (index = offset; index < size; index++) {
                    Object value = parse(data, byteBuffer);
                    opackArray.add(value);

                    if (value instanceof OpackValue) {
                        bypass = true;
                        break;
                    }
                }

                if (!bypass) {
                    baseStack.pop();
                    contextStack.pop();
                } else {
                    context[1] = index + 1;
                }
            }
        }

        return root;
    }
}
