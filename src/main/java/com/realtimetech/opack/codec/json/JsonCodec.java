package com.realtimetech.opack.codec.json;

import com.realtimetech.opack.codec.OpackCodec;
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.StringWriter;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

public final class JsonCodec extends OpackCodec<String> {
    public final static class Builder {
        boolean allowOpackValueToKeyValue;
        boolean prettyFormat;

        int encodeStackInitialSize;
        int encodeStringBufferSize;
        int decodeStackInitialSize;

        public Builder() {
            this.allowOpackValueToKeyValue = false;
            this.prettyFormat = false;

            this.encodeStringBufferSize = 1024;
            this.encodeStackInitialSize = 128;
            this.decodeStackInitialSize = 128;
        }

        public Builder setAllowOpackValueToKeyValue(boolean allowOpackValueToKeyValue) {
            this.allowOpackValueToKeyValue = allowOpackValueToKeyValue;
            return this;
        }

        public Builder setPrettyFormat(boolean prettyFormat) {
            this.prettyFormat = prettyFormat;
            return this;
        }

        public Builder setEncodeStringBufferSize(int encodeStringBufferSize) {
            this.encodeStringBufferSize = encodeStringBufferSize;
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

        public JsonCodec create() {
            return new JsonCodec(this);
        }

    }

    private static final char[] CONST_U2028 = "\\u2028".toCharArray();
    private static final char[] CONST_U2029 = "\\u2029".toCharArray();

    private static final char[] CONST_NULL_CHARACTER = new char[]{'n', 'u', 'l', 'l'};

    private static final char CONST_SEPARATOR_CHARACTER = ',';

    private static final char CONST_OBJECT_OPEN_CHARACTER = '{';
    private static final char CONST_OBJECT_MAP_CHARACTER = ':';
    private static final char CONST_OBJECT_CLOSE_CHARACTER = '}';

    private static final char CONST_ARRAY_OPEN_CHARACTER = '[';
    private static final char CONST_ARRAY_CLOSE_CHARACTER = ']';

    private static final char CONST_STRING_OPEN_CHARACTER = '\"';
    private static final char CONST_STRING_CLOSE_CHARACTER = '\"';

    private static final char[][] CONST_REPLACEMENT_CHARACTERS;

    static {
        CONST_REPLACEMENT_CHARACTERS = new char[128][];
        for (int i = 0; i <= 0x1f; i++) {
            CONST_REPLACEMENT_CHARACTERS[i] = String.format("\\u%04x", (int) i).toCharArray();
        }
        CONST_REPLACEMENT_CHARACTERS['"'] = new char[]{'\\', '\"'};
        CONST_REPLACEMENT_CHARACTERS['\\'] = new char[]{'\\', '\\'};
        CONST_REPLACEMENT_CHARACTERS['\t'] = new char[]{'\\', 't'};
        CONST_REPLACEMENT_CHARACTERS['\b'] = new char[]{'\\', 'b'};
        CONST_REPLACEMENT_CHARACTERS['\n'] = new char[]{'\\', 'n'};
        CONST_REPLACEMENT_CHARACTERS['\r'] = new char[]{'\\', 'r'};
        CONST_REPLACEMENT_CHARACTERS['\f'] = new char[]{'\\', 'f'};
    }

    final Builder builder;

    JsonCodec(Builder builder) {
        this.builder = builder;
    }

    boolean encodeLiteral(StringWriter stringWriter, FastStack<Object> opackStack, Object object) {
        Class<?> type = object.getClass();

        if (type == OpackObject.class) {
            opackStack.push(object);

            return false;
        } else if (type == OpackArray.class) {
            opackStack.push(object);

            return false;
        } else if (type == String.class) {
            String string = (String) object;
            char[] charArray = string.toCharArray();

            stringWriter.write(CONST_STRING_OPEN_CHARACTER);

            int last = 0;
            int length = charArray.length;

            for (int index = 0; index < length; index++) {
                char character = charArray[index];
                char[] replacement = null;

                if (character < 128) {
                    replacement = CONST_REPLACEMENT_CHARACTERS[character];
                } else if (character == '\u2028') {
                    replacement = CONST_U2028;
                } else if (character == '\u2029') {
                    replacement = CONST_U2029;
                }

                if (replacement != null) {
                    if (last < index) {
                        stringWriter.write(charArray, last, index - last);
                    }

                    stringWriter.write(replacement);
                    last = index + 1;
                }
            }

            if (last < length) {
                stringWriter.write(charArray, last, length - last);
            }

            stringWriter.write(CONST_STRING_CLOSE_CHARACTER);
        } else {
            if (!OpackValue.isAllowType(type)) {
                // Unknown Type Exception
            }

            Class<?> numberType = type;

            if (ReflectionUtil.isPrimitiveClass(type)) {
                numberType = ReflectionUtil.getWrapperClassOfPrimitiveClass(type);
            }

            // Asserts
            if (numberType == Double.class) {
                Double doubleValue = (Double) object;
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || Double.isFinite(doubleValue)) {
                    // Throw not allowed double value
                }
            } else if (numberType == Float.class) {
                Float floatValue = (Float) object;
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue) || Float.isFinite(floatValue)) {
                    // Throw not allowed double value
                }
            }

            stringWriter.write(object.toString().toCharArray());
        }

        return true;
    }

    @Override
    protected String doEncode(OpackValue opackValue) {
        StringWriter localStringWriter = new StringWriter(this.builder.encodeStringBufferSize);
        StringWriter stringWriter = new StringWriter(this.builder.encodeStringBufferSize);
        FastStack<Object> opackStack = new FastStack<Object>(this.builder.encodeStackInitialSize);
        opackStack.push(opackValue);

        while (!opackStack.isEmpty()) {
            Object object = opackStack.pop();

            Class<?> type = object.getClass();
            if (type == char[].class) {
                stringWriter.write((char[]) object);
            } else if (type == OpackObject.class) {
                OpackObject opackObject = (OpackObject) object;

                stringWriter.write(CONST_OBJECT_OPEN_CHARACTER);
                opackStack.push(CONST_OBJECT_CLOSE_CHARACTER);
                int count = 0;
                for (Object key : opackObject.keySet()) {
                    Object value = opackObject.get(key);

                    if (count != 0) {
                        opackStack.push(CONST_SEPARATOR_CHARACTER);
                    }

                    opackStack.push(value);
                    opackStack.push(CONST_OBJECT_MAP_CHARACTER);
                    opackStack.push(key);

                    count++;
                }
            } else if (type == OpackArray.class) {
                OpackArray opackArray = (OpackArray) object;
                localStringWriter.reset();

                stringWriter.write(CONST_ARRAY_OPEN_CHARACTER);

                int size = opackArray.length();
                int reverseStart = opackStack.getSize();

                for (int index = 0; index < size; index++) {
                    Object value = opackArray.get(index);

                    if (!this.encodeLiteral(localStringWriter, opackStack, value)) {
                        opackStack.push(localStringWriter.toCharArray());
                        opackStack.swap(opackStack.getSize() - 1, opackStack.getSize() - 2);
                        if (index != size - 1) {
                            opackStack.push(CONST_SEPARATOR_CHARACTER);
                        }

                        localStringWriter.reset();
                    } else {
                        if (index != size - 1) {
                            localStringWriter.write(CONST_SEPARATOR_CHARACTER);
                        }
                    }
                }

                if (localStringWriter.getLength() > 0) {
                    opackStack.push(localStringWriter.toCharArray());
                    localStringWriter.reset();
                }

                opackStack.push(CONST_ARRAY_CLOSE_CHARACTER);
                opackStack.reverse(reverseStart, opackStack.getSize() - 1);
            } else {
                this.encodeLiteral(stringWriter, opackStack, object);
            }
        }

        return stringWriter.toString();
    }

    @Override
    protected OpackValue doDecode(String data) {
        FastStack<Integer> baseStack = new FastStack<Integer>(this.builder.decodeStackInitialSize);
        FastStack<Object> valueStack = new FastStack<Object>(this.builder.decodeStackInitialSize);
        StringWriter stringWriter = new StringWriter();

        int pointer = 0;

        int length = data.length();
        char[] charArray = data.toCharArray();

        while (pointer < length) {
            boolean stackMerge = false;
            char currentChar = charArray[pointer++];

            if (currentChar == '{') {
                // Start OpackObject Open
                baseStack.push(valueStack.getSize());
                valueStack.push(new OpackObject<>());
            } else if (currentChar == ':') {
                // Active Pair Mode

            } else if (currentChar == '}') {
                // What to do
                baseStack.pop();
                stackMerge = true;
            } else if (currentChar == '[') {
                // Start OpackArray Open
                baseStack.push(valueStack.getSize());
                valueStack.push(new OpackArray<>());
            } else if (currentChar == ']') {
                // What to do
                baseStack.pop();
                stackMerge = true;
            } else {
                if (currentChar == '\"') {
                    while (pointer < length) {
                        char literalChar = charArray[pointer++];

                        if (literalChar == '\"') {
                            // End of String
                            valueStack.push(stringWriter.toString());

                            stringWriter.reset();
                            stackMerge = true;
                            break;
                        } else if (literalChar == '\\') {
                            // Parse escape string

                            pointer++; // Seek for get escape char
                            char nextChar = charArray[pointer++];

                            switch (nextChar) {
                                case '"':
                                    stringWriter.write('\"');
                                    break;
                                case '\\':
                                    stringWriter.write('\\');
                                    break;
                                case 'u':
                                    char result = 0;
                                    for (int i = 0; i < 5; i++) {
                                        char unicode = charArray[pointer++];
                                        result <<= 4;
                                        if (unicode >= '0' && unicode <= '9') {
                                            result += (unicode - '0');
                                        } else if (unicode >= 'a' && unicode <= 'f') {
                                            result += (unicode - 'a' + 10);
                                        } else if (unicode >= 'A' && unicode <= 'F') {
                                            result += (unicode - 'A' + 10);
                                        } else {
                                            // Unknown charset unicode
//                                        throw new IOException("An exception occurred at " + (pointer - 1) + " position character(" + charArray[(pointer - 1)] + ")");
                                        }
                                    }
                                    stringWriter.write(result);
                                    break;
                                case 'b':
                                    stringWriter.write('\b');
                                    break;
                                case 'f':
                                    stringWriter.write('\f');
                                    break;
                                case 'n':
                                    stringWriter.write('\n');
                                    break;
                                case 'r':
                                    stringWriter.write('\r');
                                    break;
                                case 't':
                                    stringWriter.write('\t');
                                    break;
                            }
                        } else {
                            stringWriter.write(literalChar);
                        }
                    }
                } else if ((currentChar >= '0' && currentChar <= '9') || currentChar == '-') {
                    pointer--;

                    boolean decimal = false;
                    while (pointer < length) {
                        char literalChar = charArray[pointer++];

                        if (!(literalChar >= '0' && literalChar <= '9') && literalChar != 'E' && literalChar != 'e' && literalChar != '+' && literalChar != '-' && literalChar != '.') {
                            if (decimal) {
                                valueStack.push(Double.parseDouble(stringWriter.toString()));
                            } else {
                                valueStack.push(Long.parseLong(stringWriter.toString()));
                            }

                            pointer--;
                            stringWriter.reset();
                            stackMerge = true;
                            break;
                        } else {
                            if (literalChar == '.') {
                                decimal = true;
                            }

                            stringWriter.write(literalChar);
                        }
                    }
                } else if (currentChar == 't') {
                    valueStack.push(true);
                    pointer = pointer + 3;
                    stackMerge = true;
                } else if (currentChar == 'f') {
                    valueStack.push(false);
                    pointer = pointer + 4;
                    stackMerge = true;
                } else if (currentChar == 'n') {
                    valueStack.push(null);
                    pointer = pointer + 3;
                    stackMerge = true;
                }
            }

            if (stackMerge && !baseStack.isEmpty()) {
                //Let's merge
                int baseIndex = baseStack.peek();
                int valueSize = valueStack.getSize() - baseIndex - 1;
                Object object = valueStack.get(baseIndex);
                Class<?> type = object.getClass();


                if (type == OpackObject.class) {
                    if (valueSize == 2) {
                        OpackObject opackObject = (OpackObject) object;

                        Object value = valueStack.pop();
                        Object key = valueStack.pop();

                        opackObject.put(key, value);
                    } else {
                        // WHAT
                    }
                } else if (type == OpackArray.class) {
                    if (valueSize == 1) {
                        OpackArray opackArray = (OpackArray) object;
                        Object value = valueStack.pop();

                        opackArray.add(value);
                    } else {
                        // WHAT
                    }
                } else {
                    // WHAT THE F
                }
            }
        }

        return (OpackValue) valueStack.pop();
    }
}
