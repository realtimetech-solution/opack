package com.realtimetech.opack.codec.json;

import com.realtimetech.opack.codec.OpackCodec;
import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.StringWriter;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import com.sun.jdi.InvalidTypeException;

import java.io.IOException;

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

            this.encodeStringBufferSize = 1024 * 4;
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

    final StringWriter encodeLiteralStringWriter;
    final StringWriter encodeStringWriter;
    final FastStack<Object> encodeStack;

    final FastStack<Integer> decodeBaseStack;
    final FastStack<Object> decodeValueStack;
    final StringWriter decodeStringWriter;

    JsonCodec(Builder builder) {
        this.builder = builder;

        this.encodeLiteralStringWriter = new StringWriter(this.builder.encodeStringBufferSize);
        this.encodeStringWriter = new StringWriter(this.builder.encodeStringBufferSize);
        this.encodeStack = new FastStack<Object>(this.builder.encodeStackInitialSize);

        this.decodeBaseStack = new FastStack<Integer>(this.builder.decodeStackInitialSize);
        this.decodeValueStack = new FastStack<Object>(this.builder.decodeStackInitialSize);
        this.decodeStringWriter = new StringWriter();
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
                throw new IllegalArgumentException(type + " is not allow to json encode.");
            }

            Class<?> numberType = type;

            if (ReflectionUtil.isPrimitiveClass(type)) {
                numberType = ReflectionUtil.getWrapperClassOfPrimitiveClass(type);
            }

            // Asserts
            if (numberType == Double.class) {
                Double doubleValue = (Double) object;
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || !Double.isFinite(doubleValue)) {
                    throw new ArithmeticException("Json format allow only finite value.");
                }
            } else if (numberType == Float.class) {
                Float floatValue = (Float) object;
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue) || !Float.isFinite(floatValue)) {
                    throw new ArithmeticException("Json format allow only finite value.");
                }
            }

            stringWriter.write(object.toString().toCharArray());
        }

        return true;
    }

    @Override
    protected String doEncode(OpackValue opackValue) throws IOException {
        this.encodeLiteralStringWriter.reset();
        this.encodeStringWriter.reset();
        this.encodeStack.reset();

        this.encodeStack.push(opackValue);

        while (!this.encodeStack.isEmpty()) {
            Object object = this.encodeStack.pop();

            Class<?> type = object.getClass();
            if (type == char[].class) {
                this.encodeStringWriter.write((char[]) object);
            } else if (type == OpackObject.class) {
                OpackObject opackObject = (OpackObject) object;

                this.encodeStringWriter.write(CONST_OBJECT_OPEN_CHARACTER);
                this.encodeStack.push(CONST_OBJECT_CLOSE_CHARACTER);
                int count = 0;
                for (Object key : opackObject.keySet()) {
                    Object value = opackObject.get(key);

                    if (count != 0) {
                        this.encodeStack.push(CONST_SEPARATOR_CHARACTER);
                    }

                    this.encodeStack.push(value);
                    this.encodeStack.push(CONST_OBJECT_MAP_CHARACTER);
                    this.encodeStack.push(key);

                    count++;
                }
            } else if (type == OpackArray.class) {
                OpackArray opackArray = (OpackArray) object;
                this.encodeLiteralStringWriter.reset();

                this.encodeStringWriter.write(CONST_ARRAY_OPEN_CHARACTER);

                int size = opackArray.length();
                int reverseStart = this.encodeStack.getSize();

                for (int index = 0; index < size; index++) {
                    Object value = opackArray.get(index);

                    if (!this.encodeLiteral(this.encodeLiteralStringWriter, this.encodeStack, value)) {
                        this.encodeStack.push(this.encodeLiteralStringWriter.toCharArray());
                        this.encodeStack.swap(this.encodeStack.getSize() - 1, this.encodeStack.getSize() - 2);
                        if (index != size - 1) {
                            this.encodeStack.push(CONST_SEPARATOR_CHARACTER);
                        }

                        this.encodeLiteralStringWriter.reset();
                    } else {
                        if (index != size - 1) {
                            this.encodeLiteralStringWriter.write(CONST_SEPARATOR_CHARACTER);
                        }
                    }
                }

                if (this.encodeLiteralStringWriter.getLength() > 0) {
                    this.encodeStack.push(this.encodeLiteralStringWriter.toCharArray());
                    this.encodeLiteralStringWriter.reset();
                }

                this.encodeStack.push(CONST_ARRAY_CLOSE_CHARACTER);
                this.encodeStack.reverse(reverseStart, this.encodeStack.getSize() - 1);
            } else {
                this.encodeLiteral(this.encodeStringWriter, this.encodeStack, object);
            }
        }

        return this.encodeStringWriter.toString();
    }

    @Override
    protected OpackValue doDecode(String data) throws IOException {
        this.decodeBaseStack.reset();
        this.decodeValueStack.reset();
        this.decodeStringWriter.reset();

        int pointer = 0;

        int length = data.length();
        char[] charArray = data.toCharArray();
        boolean literalMode = false;

        while (pointer < length) {
            boolean stackMerge = false;
            char currentChar = charArray[pointer++];

            switch (currentChar) {
                /*
                    Syntax Parse
                 */
                case '{': {
                    this.decodeBaseStack.push(this.decodeValueStack.getSize());
                    this.decodeValueStack.push(new OpackObject<>());
                    literalMode = true;

                    break;
                }
                case '[': {
                    this.decodeBaseStack.push(this.decodeValueStack.getSize());
                    this.decodeValueStack.push(new OpackArray<>());
                    literalMode = true;

                    break;
                }
                case '}':
                case ']': {
                    if (this.decodeValueStack.getSize() - 1 != this.decodeBaseStack.peek()) {
                        throw new IOException("Expected literal value but got close syntax character at " + pointer + "(" + currentChar + ")");
                    }

                    this.decodeBaseStack.pop();
                    stackMerge = true;

                    break;
                }
                case ',':
                case ':': {
                    if (literalMode) {
                        throw new IOException("Expected literal value but got syntax character at " + pointer + "(" + currentChar + ")");
                    }

                    int baseIndex = this.decodeBaseStack.peek();
                    int valueSize = this.decodeValueStack.getSize() - baseIndex - 1;
                    Object object = this.decodeValueStack.get(baseIndex);
                    Class<?> type = object.getClass();

                    switch (currentChar) {
                        case ',': {
                            if (type == OpackObject.class) {
                                if (valueSize != 0) {
                                    throw new IOException("There must be a pair of Key and Value. at " + pointer + "(" + currentChar + ")");
                                }
                            }

                            break;
                        }
                        case ':': {
                            if (type == OpackObject.class) {
                                if (valueSize != 1) {
                                    throw new IOException("There is a colon without a key. at " + pointer + "(" + currentChar + ")");
                                }
                            }
                            if (type == OpackArray.class) {
                                throw new IOException("Array type cannot contain colons. at " + pointer + "(" + currentChar + ")");
                            }

                            break;
                        }
                    }

                    literalMode = true;
                    break;
                }

                case ' ':
                case '\t': {
                    // Skip no-meaning character
                    break;
                }
                default: {
                    /*
                        Literal Parse
                     */
                    if (literalMode) {
                        if (currentChar == '\"') {
                            while (pointer < length) {
                                char literalChar = charArray[pointer++];

                                if (literalChar == '\"') {
                                    this.decodeValueStack.push(this.decodeStringWriter.toString());

                                    this.decodeStringWriter.reset();
                                    stackMerge = true;
                                    break;
                                } else if (literalChar == '\\') {
                                    pointer++;
                                    char nextChar = charArray[pointer++];

                                    switch (nextChar) {
                                        case '"':
                                            this.decodeStringWriter.write('\"');
                                            break;
                                        case '\\':
                                            this.decodeStringWriter.write('\\');
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
                                                    throw new IOException("Parsed unknown unicode pattern at " + pointer + "(" + unicode + ")");
                                                }
                                            }
                                            this.decodeStringWriter.write(result);
                                            break;
                                        case 'b':
                                            this.decodeStringWriter.write('\b');
                                            break;
                                        case 'f':
                                            this.decodeStringWriter.write('\f');
                                            break;
                                        case 'n':
                                            this.decodeStringWriter.write('\n');
                                            break;
                                        case 'r':
                                            this.decodeStringWriter.write('\r');
                                            break;
                                        case 't':
                                            this.decodeStringWriter.write('\t');
                                            break;
                                    }
                                } else {
                                    this.decodeStringWriter.write(literalChar);
                                }
                            }
                        } else if ((currentChar >= '0' && currentChar <= '9') || currentChar == '-') {
                            pointer--;

                            boolean decimal = false;
                            while (pointer < length) {
                                char literalChar = charArray[pointer++];

                                if (!(literalChar >= '0' && literalChar <= '9') && literalChar != 'E' && literalChar != 'e' && literalChar != '+' && literalChar != '-' && literalChar != '.') {
                                    if (decimal) {
                                        this.decodeValueStack.push(Double.parseDouble(this.decodeStringWriter.toString()));
                                    } else {
                                        this.decodeValueStack.push(Long.parseLong(this.decodeStringWriter.toString()));
                                    }

                                    pointer--;
                                    this.decodeStringWriter.reset();
                                    stackMerge = true;
                                    break;
                                } else {
                                    if (literalChar == '.') {
                                        decimal = true;
                                    }

                                    this.decodeStringWriter.write(literalChar);
                                }
                            }
                        } else if (currentChar == 't') {
                            this.decodeValueStack.push(true);
                            pointer = pointer + 3;
                            stackMerge = true;
                        } else if (currentChar == 'f') {
                            this.decodeValueStack.push(false);
                            pointer = pointer + 4;
                            stackMerge = true;
                        } else if (currentChar == 'n') {
                            this.decodeValueStack.push(null);
                            pointer = pointer + 3;
                            stackMerge = true;
                        } else {
                            throw new IOException("Parsed unknown character at " + pointer + "(" + currentChar + ")");
                        }
                    } else {
                        throw new IOException("Parsed unknown character at " + pointer + "(" + currentChar + ")");
                    }
                }
            }

            if (stackMerge && !this.decodeBaseStack.isEmpty()) {
                literalMode = false;

                //Let's merge
                int baseIndex = this.decodeBaseStack.peek();
                int valueSize = this.decodeValueStack.getSize() - baseIndex - 1;
                Object object = this.decodeValueStack.get(baseIndex);
                Class<?> type = object.getClass();

                if (type == OpackObject.class) {
                    if (valueSize == 2) {
                        OpackObject opackObject = (OpackObject) object;

                        Object value = this.decodeValueStack.pop();
                        Object key = this.decodeValueStack.pop();

                        opackObject.put(key, value);
                    }
                } else if (type == OpackArray.class) {
                    if (valueSize == 1) {
                        OpackArray opackArray = (OpackArray) object;
                        Object value = this.decodeValueStack.pop();

                        opackArray.add(value);
                    }
                } else {
                    throw new IOException("Caught corrupted stack, got " + type.getSimpleName());
                }
            }
        }

        return (OpackValue) this.decodeValueStack.get(0);
    }
}
