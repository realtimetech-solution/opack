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

package com.realtimetech.opack.codec.json;

import com.realtimetech.opack.codec.OpackCodec;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.StringWriter;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

import java.io.IOException;
import java.io.Writer;

public final class JsonCodec extends OpackCodec<String, Writer> {
    public final static class Builder {
        int encodeStackInitialSize;
        int encodeStringBufferSize;
        int decodeStackInitialSize;

        boolean allowOpackValueToKeyValue;
        boolean enableConvertCharacterToString;
        boolean usePrettyFormat;

        public Builder() {
            this.allowOpackValueToKeyValue = false;
            this.enableConvertCharacterToString = false;
            this.usePrettyFormat = false;

            this.encodeStringBufferSize = 1024;
            this.encodeStackInitialSize = 128;
            this.decodeStackInitialSize = 128;
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

        public Builder setAllowOpackValueToKeyValue(boolean allowOpackValueToKeyValue) {
            this.allowOpackValueToKeyValue = allowOpackValueToKeyValue;
            return this;
        }

        public Builder setEnableConvertCharacterToString(boolean enableConvertCharacterToString) {
            this.enableConvertCharacterToString = enableConvertCharacterToString;
            return this;
        }

        public Builder setUsePrettyFormat(boolean usePrettyFormat) {
            this.usePrettyFormat = usePrettyFormat;
            return this;
        }

        /**
         * Create the {@link JsonCodec JsonCodec}.
         *
         * @return created json codec
         */
        public JsonCodec create() {
            return new JsonCodec(this);
        }
    }

    private static final char[] CONST_U2028 = "\\u2028".toCharArray();
    private static final char[] CONST_U2029 = "\\u2029".toCharArray();

    private static final char[] CONST_PRETTY_LINE_CHARACTER = new char[]{'\r', '\n'};
    private static final char[] CONST_PRETTY_INDENT_CHARACTER = new char[]{' ', ' ', ' ', ' '};
    private static final char[] CONST_PRETTY_SPACE_CHARACTER = new char[]{' '};

    private static final char[] CONST_NULL_CHARACTER = new char[]{'n', 'u', 'l', 'l'};

    private static final char[] CONST_SEPARATOR_CHARACTER = new char[]{','};

    private static final char[] CONST_OBJECT_OPEN_CHARACTER = new char[]{'{'};
    private static final char[] CONST_OBJECT_MAP_CHARACTER = new char[]{':'};
    private static final char[] CONST_OBJECT_CLOSE_CHARACTER = new char[]{'}'};

    private static final char[] CONST_ARRAY_OPEN_CHARACTER = new char[]{'['};
    private static final char[] CONST_ARRAY_CLOSE_CHARACTER = new char[]{']'};

    private static final char[] CONST_STRING_OPEN_CHARACTER = new char[]{'\"'};
    private static final char[] CONST_STRING_CLOSE_CHARACTER = new char[]{'\"'};

    private static final char[][] CONST_REPLACEMENT_CHARACTERS;

    static {
        CONST_REPLACEMENT_CHARACTERS = new char[128][];
        for (int i = 0; i <= 0x1F; i++) {
            CONST_REPLACEMENT_CHARACTERS[i] = String.format("\\u%04x", i).toCharArray();
        }
        CONST_REPLACEMENT_CHARACTERS['"'] = new char[]{'\\', '\"'};
        CONST_REPLACEMENT_CHARACTERS['\\'] = new char[]{'\\', '\\'};
        CONST_REPLACEMENT_CHARACTERS['\t'] = new char[]{'\\', 't'};
        CONST_REPLACEMENT_CHARACTERS['\b'] = new char[]{'\\', 'b'};
        CONST_REPLACEMENT_CHARACTERS['\n'] = new char[]{'\\', 'n'};
        CONST_REPLACEMENT_CHARACTERS['\r'] = new char[]{'\\', 'r'};
        CONST_REPLACEMENT_CHARACTERS['\f'] = new char[]{'\\', 'f'};
    }

    final StringWriter encodeLiteralStringWriter;
    final StringWriter encodeStringWriter;
    final FastStack<Object> encodeStack;

    final FastStack<Integer> decodeBaseStack;
    final FastStack<Object> decodeValueStack;
    final StringWriter decodeStringWriter;

    final boolean allowOpackValueToKeyValue;
    final boolean enableConvertCharacterToString;
    final boolean usePrettyFormat;

    /**
     * Constructs the JsonCodec with the builder of JsonCodec.
     *
     * @param builder the builder of JsonCodec
     */
    JsonCodec(Builder builder) {
        super();

        this.encodeLiteralStringWriter = new StringWriter(builder.encodeStringBufferSize);
        this.encodeStringWriter = new StringWriter(builder.encodeStringBufferSize);
        this.encodeStack = new FastStack<>(builder.encodeStackInitialSize);

        this.decodeBaseStack = new FastStack<>(builder.decodeStackInitialSize);
        this.decodeValueStack = new FastStack<>(builder.decodeStackInitialSize);
        this.decodeStringWriter = new StringWriter();

        this.allowOpackValueToKeyValue = builder.allowOpackValueToKeyValue;
        this.enableConvertCharacterToString = builder.enableConvertCharacterToString;
        this.usePrettyFormat = builder.usePrettyFormat;
    }

    /**
     * Encodes the literal object.
     *
     * @param writer the string writer for writing encoded object
     * @param object the object to encode
     * @return whether object is encoded
     * @throws IllegalArgumentException if the type of data to be encoded is not allowed in json format
     * @throws ArithmeticException      if the data to be encoded is infinite
     */
    boolean encodeLiteral(Writer writer, Object object) throws IOException {
        if (object == null) {
            writer.write(CONST_NULL_CHARACTER);
            return true;
        }

        Class<?> objectType = object.getClass();

        if (objectType == OpackObject.class) {
            this.encodeStack.push(object);

            return false;
        } else if (objectType == OpackArray.class) {
            this.encodeStack.push(object);

            return false;
        } else if (objectType == String.class) {
            String string = (String) object;
            char[] charArray = string.toCharArray();

            writer.write(CONST_STRING_OPEN_CHARACTER);

            int last = 0;
            int length = charArray.length;

            for (int index = 0; index < length; index++) {
                char character = charArray[index];
                char[] replacement = null;

                if (character < CONST_REPLACEMENT_CHARACTERS.length) {
                    replacement = CONST_REPLACEMENT_CHARACTERS[character];
                } else if (character == '\u2028') {
                    replacement = CONST_U2028;
                } else if (character == '\u2029') {
                    replacement = CONST_U2029;
                }

                if (replacement != null) {
                    if (last < index) {
                        writer.write(charArray, last, index - last);
                    }

                    writer.write(replacement);
                    last = index + 1;
                }
            }

            if (last < length) {
                writer.write(charArray, last, length - last);
            }

            writer.write(CONST_STRING_CLOSE_CHARACTER);
        } else {
            if (!OpackValue.isAllowType(objectType)) {
                throw new IllegalArgumentException(objectType + " is not allowed in json format.");
            }

            Class<?> numberType = objectType;

            if (ReflectionUtil.isPrimitiveType(objectType)) {
                numberType = ReflectionUtil.convertPrimitiveTypeToWrapperType(objectType);
            }

            // Asserts
            if (numberType == Double.class) {
                Double doubleValue = (Double) object;
                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || !Double.isFinite(doubleValue)) {
                    throw new ArithmeticException("Only finite values are allowed in json format.");
                }
            } else if (numberType == Float.class) {
                Float floatValue = (Float) object;
                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue) || !Float.isFinite(floatValue)) {
                    throw new ArithmeticException("Only finite values are allowed in json format.");
                }
            }

            if (numberType == Character.class) {
                if (enableConvertCharacterToString) {
                    writer.write(CONST_STRING_OPEN_CHARACTER);
                    writer.write(object.toString().toCharArray());
                    writer.write(CONST_STRING_CLOSE_CHARACTER);
                } else {
                    int convertToInt = ((char) object);
                    writer.write(((Integer) convertToInt).toString().toCharArray());
                }
            } else {
                writer.write(object.toString().toCharArray());
            }
        }

        return true;
    }

    /**
     * Encodes the OpackValue to json string.
     *
     * @param opackValue the OpackValue to encode
     * @throws IllegalArgumentException if the type of data to be encoded is not allowed in json format
     */
    @Override
    protected void doEncode(Writer writer, OpackValue opackValue) throws IOException {
        this.encodeLiteralStringWriter.reset();
        this.encodeStack.reset();

        FastStack<Integer> prettyIndentStack = null;

        if (this.usePrettyFormat) {
            prettyIndentStack = new FastStack<>();
            prettyIndentStack.push(0);
        }

        this.encodeStack.push(opackValue);


        while (!this.encodeStack.isEmpty()) {
            Object object = this.encodeStack.pop();

            Class<?> objectType = object == null ? null : object.getClass();
            if (objectType == char[].class) {
                writer.write((char[]) object);
            } else if (objectType == OpackObject.class) {
                OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) object;
                int currentIndent = -1;
                if (this.usePrettyFormat) {
                    currentIndent = prettyIndentStack.pop();
                }

                writer.write(CONST_OBJECT_OPEN_CHARACTER);
                this.encodeStack.push(CONST_OBJECT_CLOSE_CHARACTER);
                if (this.usePrettyFormat && currentIndent != -1) {
                    writer.write(CONST_PRETTY_LINE_CHARACTER);
                    for (int i = 0; i < currentIndent; i++) {
                        this.encodeStack.push(CONST_PRETTY_INDENT_CHARACTER);
                    }
                    this.encodeStack.push(CONST_PRETTY_LINE_CHARACTER);
                }

                int count = 0;
                for (Object key : opackObject.keySet()) {
                    Object value = opackObject.get(key);

                    if (count != 0) {
                        if (this.usePrettyFormat) {
                            if (currentIndent != -1) {
                                this.encodeStack.push(CONST_PRETTY_LINE_CHARACTER);
                            } else {
                                this.encodeStack.push(CONST_PRETTY_SPACE_CHARACTER);
                            }
                        }
                        this.encodeStack.push(CONST_SEPARATOR_CHARACTER);
                    }

                    if (!this.allowOpackValueToKeyValue && key instanceof OpackValue) {
                        throw new IllegalArgumentException("Object type keys are not allowed in json format.");
                    }

                    if (this.usePrettyFormat) {
                        if (value instanceof OpackObject) {
                            prettyIndentStack.push(currentIndent == -1 ? -1 : currentIndent + 1);
                        }
                        if (key instanceof OpackObject) {
                            prettyIndentStack.push(currentIndent == -1 ? -1 : currentIndent + 1);
                        }
                    }
                    this.encodeStack.push(value);
                    if (this.usePrettyFormat) {
                        this.encodeStack.push(CONST_PRETTY_SPACE_CHARACTER);
                    }
                    this.encodeStack.push(CONST_OBJECT_MAP_CHARACTER);
                    this.encodeStack.push(key);
                    if (this.usePrettyFormat && currentIndent != -1) {
                        for (int i = 0; i < currentIndent + 1; i++) {
                            this.encodeStack.push(CONST_PRETTY_INDENT_CHARACTER);
                        }
                    }

                    count++;
                }
            } else if (objectType == OpackArray.class) {
                OpackArray<Object> opackArray = (OpackArray<Object>) object;
                this.encodeLiteralStringWriter.reset();

                writer.write(CONST_ARRAY_OPEN_CHARACTER);

                int size = opackArray.length();
                int reverseStart = this.encodeStack.getSize();

                for (int index = 0; index < size; index++) {
                    Object value = opackArray.get(index);

                    if (!this.encodeLiteral(this.encodeLiteralStringWriter, value)) {
                        if (this.usePrettyFormat) {
                            if (value instanceof OpackObject) {
                                prettyIndentStack.push(-1);
                            }
                        }

                        this.encodeStack.push(this.encodeLiteralStringWriter.toCharArray());
                        this.encodeStack.swap(this.encodeStack.getSize() - 1, this.encodeStack.getSize() - 2);
                        if (index != size - 1) {
                            this.encodeStack.push(CONST_SEPARATOR_CHARACTER);
                            if (this.usePrettyFormat) {
                                this.encodeStack.push(CONST_PRETTY_SPACE_CHARACTER);
                            }
                        }

                        this.encodeLiteralStringWriter.reset();
                    } else {
                        if (index != size - 1) {
                            this.encodeLiteralStringWriter.write(CONST_SEPARATOR_CHARACTER);
                            if (this.usePrettyFormat) {
                                this.encodeLiteralStringWriter.write(CONST_PRETTY_SPACE_CHARACTER);
                            }
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
                this.encodeLiteral(writer, object);
            }
        }
    }

    public synchronized String encode(OpackValue opackValue) throws EncodeException {
        this.encodeStringWriter.reset();
        this.encode(this.encodeStringWriter, opackValue);
        return this.encodeStringWriter.toString();
    }

    /**
     * Decodes the json string to {@link OpackValue OpackValue}.
     *
     * @param data the json string to decode
     * @return OpackValue
     * @throws IOException if there is a syntax problem with the json string; if the json string has a unicode whose unknown pattern
     */
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
                        throw new IOException("Expected literal value, but got close syntax character at " + pointer + "(" + currentChar + ").");
                    }

                    this.decodeBaseStack.pop();
                    stackMerge = true;

                    break;
                }
                case ',':
                case ':': {
                    if (literalMode) {
                        throw new IOException("Expected literal value, but got syntax character at " + pointer + "(" + currentChar + ").");
                    }

                    int baseIndex = this.decodeBaseStack.peek();
                    int valueSize = this.decodeValueStack.getSize() - baseIndex - 1;
                    Object object = this.decodeValueStack.get(baseIndex);
                    Class<?> objectType = object.getClass();

                    switch (currentChar) {
                        case ',': {
                            if (objectType == OpackObject.class) {
                                if (valueSize != 0) {
                                    throw new IOException("The map type cannot contain items that do not exist. at " + pointer + "(" + currentChar + ").");
                                }
                            }

                            break;
                        }
                        case ':': {
                            if (objectType == OpackObject.class) {
                                if (valueSize != 1) {
                                    throw new IOException("The map item must have a key. at " + pointer + "(" + currentChar + ").");
                                }
                            }
                            if (objectType == OpackArray.class) {
                                throw new IOException("The array type cannot contain colons. at " + pointer + "(" + currentChar + ").");
                            }

                            break;
                        }
                    }

                    literalMode = true;
                    break;
                }

                case ' ':
                case '\r':
                case '\n':
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
                                            for (int i = 0; i < 4; i++) {
                                                char unicode = charArray[pointer++];
                                                result <<= 4;
                                                if (unicode >= '0' && unicode <= '9') {
                                                    result += (unicode - '0');
                                                } else if (unicode >= 'a' && unicode <= 'f') {
                                                    result += (unicode - 'a' + 10);
                                                } else if (unicode >= 'A' && unicode <= 'F') {
                                                    result += (unicode - 'A' + 10);
                                                } else {
                                                    throw new IOException("Parsed unknown unicode pattern at " + pointer + "(" + unicode + ").");
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
                            throw new IOException("This value is not an opack value. Unknown value at " + pointer + "(" + currentChar + ").");
                        }
                    } else {
                        throw new IOException("Parsed unknown character at " + pointer + "(" + currentChar + ").");
                    }
                }
            }

            if (stackMerge && !this.decodeBaseStack.isEmpty()) {
                literalMode = false;

                //Let's merge
                int baseIndex = this.decodeBaseStack.peek();
                int valueSize = this.decodeValueStack.getSize() - baseIndex - 1;
                Object object = this.decodeValueStack.get(baseIndex);
                Class<?> objectType = object.getClass();

                if (objectType == OpackObject.class) {
                    if (valueSize == 2) {
                        OpackObject<Object, Object> opackObject = (OpackObject<Object, Object>) object;

                        Object value = this.decodeValueStack.pop();
                        Object key = this.decodeValueStack.pop();

                        opackObject.put(key, value);
                    }
                } else if (objectType == OpackArray.class) {
                    if (valueSize == 1) {
                        OpackArray<Object> opackArray = (OpackArray<Object>) object;
                        Object value = this.decodeValueStack.pop();

                        opackArray.add(value);
                    }
                } else {
                    throw new IOException("Caught corrupted stack, got " + objectType.getSimpleName() + ".");
                }
            }
        }

        return (OpackValue) this.decodeValueStack.get(0);
    }
}