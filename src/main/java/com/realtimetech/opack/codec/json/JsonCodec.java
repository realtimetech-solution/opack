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
import com.realtimetech.opack.util.StringWriter;
import com.realtimetech.opack.util.UnsafeOpackValue;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.util.structure.NativeList;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public final class JsonCodec extends OpackCodec<String, Writer> {
    public final static class Builder {
        private int encodeStackInitialSize;
        private int encodeStringBufferSize;
        private int decodeStackInitialSize;

        private boolean allowAnyValueToKey;
        private boolean enableConvertCharacterToString;
        private boolean usePrettyFormat;

        public Builder() {
            this.allowAnyValueToKey = false;
            this.enableConvertCharacterToString = true;
            this.usePrettyFormat = false;

            this.encodeStringBufferSize = 1024;
            this.encodeStackInitialSize = 128;
            this.decodeStackInitialSize = 128;
        }

        public @NotNull Builder setEncodeStringBufferSize(int encodeStringBufferSize) {
            this.encodeStringBufferSize = encodeStringBufferSize;
            return this;
        }

        public @NotNull Builder setEncodeStackInitialSize(int encodeStackInitialSize) {
            this.encodeStackInitialSize = encodeStackInitialSize;
            return this;
        }

        public @NotNull Builder setDecodeStackInitialSize(int decodeStackInitialSize) {
            this.decodeStackInitialSize = decodeStackInitialSize;
            return this;
        }

        public @NotNull Builder setAllowAnyValueToKey(boolean allowAnyValueToKey) {
            this.allowAnyValueToKey = allowAnyValueToKey;
            return this;
        }

        public @NotNull Builder setEnableConvertCharacterToString(boolean enableConvertCharacterToString) {
            this.enableConvertCharacterToString = enableConvertCharacterToString;
            return this;
        }

        public @NotNull Builder setUsePrettyFormat(boolean usePrettyFormat) {
            this.usePrettyFormat = usePrettyFormat;
            return this;
        }

        /**
         * Create the {@link JsonCodec JsonCodec}
         *
         * @return created json codec
         */
        public @NotNull JsonCodec create() {
            return new JsonCodec(this);
        }
    }

    private static final char[] CONST_U2028 = "\\u2028".toCharArray();
    private static final char[] CONST_U2029 = "\\u2029".toCharArray();

    private static final char[] CONST_PRETTY_LINE_CHARACTER = new char[]{'\r', '\n'};
    private static final char[] CONST_PRETTY_INDENT_CHARACTER = new char[]{' ', ' ', ' ', ' '};
    private static final char[] CONST_PRETTY_SPACE_CHARACTER = new char[]{' '};

    private static final char[] CONST_NULL_CHARACTER = new char[]{'n', 'u', 'l', 'l'};
    private static final char[] CONST_TRUE_CHARACTER = new char[]{'t', 'r', 'u', 'e'};
    private static final char[] CONST_FALSE_CHARACTER = new char[]{'f', 'a', 'l', 's', 'e'};

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

    private final @NotNull StringWriter encodeLiteralStringWriter;
    private final @NotNull StringWriter encodeStringWriter;
    private final @NotNull FastStack<@Nullable Object> encodeStack;

    private final @NotNull FastStack<@NotNull Integer> decodeBaseStack;
    private final @NotNull FastStack<@Nullable Object> decodeValueStack;
    private final @NotNull StringWriter decodeStringWriter;

    private final boolean allowAnyValueToKey;
    private final boolean enableConvertCharacterToString;
    private final boolean usePrettyFormat;

    /**
     * Constructs the JsonCodec with the builder of JsonCodec.
     *
     * @param builder the builder of JsonCodec
     */
    private JsonCodec(@NotNull Builder builder) {
        super();

        this.encodeLiteralStringWriter = new StringWriter(builder.encodeStringBufferSize);
        this.encodeStringWriter = new StringWriter(builder.encodeStringBufferSize);
        this.encodeStack = new FastStack<>(builder.encodeStackInitialSize);

        this.decodeBaseStack = new FastStack<>(builder.decodeStackInitialSize);
        this.decodeValueStack = new FastStack<>(builder.decodeStackInitialSize);
        this.decodeStringWriter = new StringWriter();

        this.allowAnyValueToKey = builder.allowAnyValueToKey;
        this.enableConvertCharacterToString = builder.enableConvertCharacterToString;
        this.usePrettyFormat = builder.usePrettyFormat;
    }

    /**
     * Encodes the literal object
     *
     * @param writer the string writer for writing encoded object
     * @param object the object to encode
     * @return whether object is encoded
     * @throws IllegalArgumentException if the type of data to be encoded is not allowed in json format
     * @throws ArithmeticException      if the data to be encoded is infinite
     */
    private boolean encodeLiteral(final @NotNull Writer writer, @Nullable Object object) throws IOException {
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
        } else if (objectType == String.class || (enableConvertCharacterToString && objectType == Character.class)) {
            String string = objectType == Character.class ? object.toString() : (String) object;
            char[] charArray = string.toCharArray();

            writer.write(CONST_STRING_OPEN_CHARACTER);

            int last = 0;
            int length = charArray.length;

            for (int index = 0; index < length; index++) {
                char character = charArray[index];
                char[] replacement = null;

                // Find escapable character
                if (character < CONST_REPLACEMENT_CHARACTERS.length) {
                    replacement = CONST_REPLACEMENT_CHARACTERS[character];
                } else if (character == '\u2028') {
                    replacement = CONST_U2028;
                } else if (character == '\u2029') {
                    replacement = CONST_U2029;
                }

                if (replacement != null) {
                    if (last < index) {
                        // Write characters from the last index to just before escapable character
                        writer.write(charArray, last, index - last);
                    }

                    writer.write(replacement);
                    last = index + 1;
                }
            }

            if (last < length) {
                // Write remain characters
                writer.write(charArray, last, length - last);
            }

            writer.write(CONST_STRING_CLOSE_CHARACTER);

            return true;
        } else {
            // Asserts
            if (objectType == Double.class) {
                double doubleValue = (Double) object;

                if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || !Double.isFinite(doubleValue)) {
                    throw new ArithmeticException("Only finite values are allowed in json format.");
                }
            } else if (objectType == Float.class) {
                float floatValue = (Float) object;

                if (Float.isNaN(floatValue) || Float.isInfinite(floatValue) || !Float.isFinite(floatValue)) {
                    throw new ArithmeticException("Only finite values are allowed in json format.");
                }
            }

            if (objectType == Character.class) {
                writer.write(Integer.toString((char) object));
            } else {
                writer.write(object.toString());
            }

            return true;
        }
    }

    /**
     * Encodes the {@link NativeList NativeList} to writer
     *
     * @param writer     the writer
     * @param nativeList the native list to encode
     * @throws IllegalArgumentException if the type of data to be encoded is not allowed in json format
     */
    private boolean encodeNativeArray(@NotNull Writer writer, @NotNull NativeList nativeList) throws IOException {
        Object arrayObject = nativeList.getArrayObject();
        Class<?> arrayType = arrayObject.getClass();

        if (arrayType == boolean[].class) {
            boolean[] array = (boolean[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                if (array[index]) {
                    writer.write(CONST_TRUE_CHARACTER);
                } else {
                    writer.write(CONST_FALSE_CHARACTER);
                }
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == byte[].class) {
            byte[] array = (byte[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                writer.write(Byte.toString(array[index]));
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == char[].class) {
            char[] array = (char[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                if (enableConvertCharacterToString) {
                    char character = array[index];
                    char[] replacement = null;

                    if (character < CONST_REPLACEMENT_CHARACTERS.length) {
                        replacement = CONST_REPLACEMENT_CHARACTERS[character];
                    } else if (character == '\u2028') {
                        replacement = CONST_U2028;
                    } else if (character == '\u2029') {
                        replacement = CONST_U2029;
                    }

                    writer.write(CONST_STRING_OPEN_CHARACTER);
                    if (replacement != null) {
                        writer.write(replacement);
                    } else {
                        writer.write(Character.toString(character));
                    }
                    writer.write(CONST_STRING_CLOSE_CHARACTER);
                } else {
                    writer.write(Integer.toString(array[index]));
                }
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == short[].class) {
            short[] array = (short[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                writer.write(Short.toString(array[index]));
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == int[].class) {
            int[] array = (int[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                writer.write(Integer.toString(array[index]));
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == float[].class) {
            float[] array = (float[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                writer.write(Float.toString(array[index]));
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == long[].class) {
            long[] array = (long[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                writer.write(Long.toString(array[index]));
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == double[].class) {
            double[] array = (double[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                writer.write(Double.toString(array[index]));
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == Boolean[].class) {
            Boolean[] array = (Boolean[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                if (array[index] == null) {
                    writer.write(CONST_NULL_CHARACTER);
                } else if (array[index]) {
                    writer.write(CONST_TRUE_CHARACTER);
                } else {
                    writer.write(CONST_FALSE_CHARACTER);
                }
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == Character[].class) {
            Character[] array = (Character[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                if (array[index] == null) {
                    writer.write(CONST_NULL_CHARACTER);
                } else {
                    if (enableConvertCharacterToString) {
                        Character character = array[index];
                        char[] replacement = null;

                        if (character < CONST_REPLACEMENT_CHARACTERS.length) {
                            replacement = CONST_REPLACEMENT_CHARACTERS[character];
                        } else if (character == '\u2028') {
                            replacement = CONST_U2028;
                        } else if (character == '\u2029') {
                            replacement = CONST_U2029;
                        }

                        writer.write(CONST_STRING_OPEN_CHARACTER);
                        if (replacement != null) {
                            writer.write(replacement);
                        } else {
                            writer.write(Character.toString(character));
                        }
                        writer.write(CONST_STRING_CLOSE_CHARACTER);
                    } else {
                        writer.write(Integer.toString(array[index]));
                    }
                }
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        } else if (arrayType == Byte[].class ||
                arrayType == Short[].class ||
                arrayType == Integer[].class ||
                arrayType == Float[].class ||
                arrayType == Long[].class ||
                arrayType == Double[].class) {
            Object[] array = (Object[]) arrayObject;

            writer.write(CONST_ARRAY_OPEN_CHARACTER);

            for (int index = 0; index < array.length; index++) {
                if (index != 0) {
                    writer.write(CONST_SEPARATOR_CHARACTER);
                }

                if (array[index] == null) {
                    writer.write(CONST_NULL_CHARACTER);
                } else {
                    writer.write(array[index].toString());
                }
            }

            writer.write(CONST_ARRAY_CLOSE_CHARACTER);

            return true;
        }

        return false;
    }

    /**
     * Encodes the {@link OpackValue OpackValue} to json string
     *
     * @param opackValue the opack value to encode
     * @throws IllegalArgumentException if the type of data to be encoded is not allowed in json format
     */
    @Override
    protected void encodeObject(@NotNull Writer writer, @Nullable Object opackValue) throws IOException {
        this.encodeLiteralStringWriter.reset();
        this.encodeStack.reset();

        final FastStack<@NotNull Integer> prettyIndentStack;

        if (this.usePrettyFormat) {
            prettyIndentStack = new FastStack<>();
            prettyIndentStack.push(0);
        } else {
            prettyIndentStack = null;
        }

        this.encodeStack.push(opackValue);

        while (!this.encodeStack.isEmpty()) {
            Object object = this.encodeStack.pop();
            Class<?> objectType = object == null ? null : object.getClass();

            if (objectType == char[].class) {
                writer.write((char[]) object);
            } else if (objectType == OpackObject.class) {
                OpackObject opackObject = (OpackObject) object;
                Map<Object, Object> opackObjectMap = null;

                try {
                    opackObjectMap = UnsafeOpackValue.getMap(opackObject);
                } catch (InvocationTargetException | IllegalAccessException exception) {
                    throw new IOException("Can't access opack object map.", exception);
                }

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

                int index = 0;
                for (Map.Entry<Object, Object> entry : opackObjectMap.entrySet()) {
                    Object key = entry.getKey();
                    Object value = entry.getValue();

                    if (index != 0) {
                        if (this.usePrettyFormat) {
                            if (currentIndent != -1) {
                                this.encodeStack.push(CONST_PRETTY_LINE_CHARACTER);
                            } else {
                                this.encodeStack.push(CONST_PRETTY_SPACE_CHARACTER);
                            }
                        }

                        this.encodeStack.push(CONST_SEPARATOR_CHARACTER);
                    }

                    if (!this.allowAnyValueToKey && !(key instanceof String)) {
                        throw new IllegalArgumentException("Only string value allowed in json format.");
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

                    index++;
                }
            } else if (objectType == OpackArray.class) {
                OpackArray opackArray = (OpackArray) object;
                int size = opackArray.length();
                List<Object> opackArrayList = null;

                try {
                    opackArrayList = UnsafeOpackValue.getList(opackArray);
                } catch (InvocationTargetException | IllegalAccessException exception) {
                    throw new IOException("Can't access opack array list.", exception);
                }

                this.encodeLiteralStringWriter.reset();

                boolean optimized = false;

                if (opackArrayList instanceof NativeList) {
                    NativeList nativeList = (NativeList) opackArrayList;

                    optimized = encodeNativeArray(writer, nativeList);
                }

                if (!optimized) {
                    writer.write(CONST_ARRAY_OPEN_CHARACTER);

                    int reverseStart = this.encodeStack.getSize();
                    int index = 0;

                    for (Object value : opackArrayList) {
                        if (!this.encodeLiteral(this.encodeLiteralStringWriter, value)) {
                            if (this.usePrettyFormat) {
                                if (value instanceof OpackObject) {
                                    prettyIndentStack.push(-1);
                                }
                            }

                            if (this.encodeLiteralStringWriter.getLength() > 0) {
                                this.encodeStack.push(this.encodeLiteralStringWriter.toCharArray());
                                this.encodeStack.swap(this.encodeStack.getSize() - 1, this.encodeStack.getSize() - 2);
                            }

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

                        index++;
                    }

                    if (this.encodeLiteralStringWriter.getLength() > 0) {
                        this.encodeStack.push(this.encodeLiteralStringWriter.toCharArray());
                        this.encodeLiteralStringWriter.reset();
                    }

                    this.encodeStack.push(CONST_ARRAY_CLOSE_CHARACTER);
                    this.encodeStack.reverse(reverseStart, this.encodeStack.getSize() - 1);
                }
            } else {
                this.encodeLiteral(writer, object);
            }
        }
    }


    /**
     * Escapes a given character into printable string
     * If the character does not require escaping, it is returned as-is
     *
     * @param c the character that needs to be escaped
     * @return the escaped string representation of the character, or the character itself if no escaping is needed
     */
    private @NotNull String escapeChar(char c) {
        switch (c) {
            case '\b':
                return "\\b";
            case '\f':
                return "\\f";
            case '\n':
                return "\\n";
            case '\r':
                return "\\r";
            case '\t':
                return "\\t";
            case '\\':
                return "\\\\";
            case '\"':
                return "\\\"";
            case '/':
                return "\\/";
            default:
                return Character.toString(c);
        }
    }

    /**
     * Encodes the OpackValue to json string
     *
     * @param opackValue the opack value to encode
     * @return returns encoded string
     * @throws EncodeException if a problem occurs during encoding, if the type of data to be encoded is not allowed in specific codec
     */
    @Deprecated(forRemoval = true)
    public synchronized @NotNull String encode(@NotNull OpackValue opackValue) throws EncodeException {
        this.encodeStringWriter.reset();
        this.fromOpackValue(this.encodeStringWriter, opackValue);

        return this.encodeStringWriter.toString();
    }

    /**
     * Parses the given string into a {@link Number}
     *
     * @param string the input string to parse into a number
     * @return the parsed number, either a {@link Long} or {@link BigInteger}, depending on the value
     * @throws NumberFormatException if the input string is empty, contains invalid characters,
     *                               has leading zeros, or represents an invalid number format
     */
    private @NotNull Number parseLongFast(@NotNull String string) {
        if (string.isEmpty()) {
            throw new NumberFormatException("Not allow empty input");
        }

        int index = 0;
        int length = string.length();
        long limit = -Long.MAX_VALUE;
        boolean negative = false;
        char firstCharacter = string.charAt(index);

        if (firstCharacter == '-') {
            limit = Long.MIN_VALUE;
            negative = true;
            index++;
        }

        if (firstCharacter == '+') {
            index++;
        }

        if (index >= length) {
            throw new NumberFormatException("Can't have lone \"+\" or \"-\": \"" + string + "\"");
        }

        if (string.charAt(index) == '0' && length > index + 1) {
            throw new NumberFormatException("Leading zeros not allowed: \"" + string + "\"");
        }

        long multiplyLimit = limit / 10;
        long result = 0;

        while (index < length) {
            char character = string.charAt(index++);

            if (character < '0' || character > '9') {
                throw new NumberFormatException("Invalid digit: \"" + string + "\"");
            }

            if (result < multiplyLimit) {
                return new BigInteger(string);
            }

            int digit = character - '0';

            result *= 10;

            if (result < limit + digit) {
                return new BigInteger(string);
            }

            result -= digit;
        }

        return negative ? result : -result;
    }

    /**
     * Decodes the json string to {@link OpackValue OpackValue}
     *
     * @param data the json string to decode
     * @return OpackValue
     * @throws IOException if there is a syntax problem with the json string, if the json string has a unicode whose unknown pattern
     */
    @Override
    protected @Nullable Object decodeObject(@NotNull String data) throws IOException {
        this.decodeBaseStack.reset();
        this.decodeValueStack.reset();
        this.decodeStringWriter.reset();

        int pointer = 0;

        int length = data.length();
        char[] charArray = data.toCharArray();
        boolean valueMode = true;
        boolean emptyBase = false;

        int currentContextIndex = -1;
        OpackValue currentContext = null;
        Class<?> currentContextType = null;

        while (pointer < length) {
            char currentChar = charArray[pointer++];

            switch (currentChar) {
                /*
                    Syntax Parse
                 */
                case '{': {
                    if (!valueMode) {
                        throw new IOException("Expected value or comma(,) or colon(:), but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                    }

                    currentContextIndex = this.decodeBaseStack.push(this.decodeValueStack.getSize());
                    currentContext = this.decodeValueStack.push(new OpackObject());
                    currentContextType = currentContext.getClass();
                    emptyBase = true;
                    break;
                }

                case '[': {
                    if (!valueMode) {
                        throw new IOException("Expected value or comma(,), but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                    }

                    currentContextIndex = this.decodeBaseStack.push(this.decodeValueStack.getSize());
                    currentContext = this.decodeValueStack.push(new OpackArray());
                    currentContextType = currentContext.getClass();
                    emptyBase = true;
                    break;
                }

                case '}':
                case ']': {
                    if (valueMode && !emptyBase) {
                        throw new IOException("Expected value, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                    }

                    int valueSize = this.decodeValueStack.getSize() - currentContextIndex - 1;

                    if (currentContextType == OpackObject.class) {
                        if (currentChar != '}') {
                            throw new IOException("Expected character(}), but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                        }

                        OpackObject opackObject = (OpackObject) currentContext;

                        Map<Object, Object> opackObjectMap = null;

                        try {
                            opackObjectMap = UnsafeOpackValue.getMap(opackObject);

                            for (int i = 0; i < valueSize; i += 2) {
                                Object value = this.decodeValueStack.pop();
                                Object key = this.decodeValueStack.pop();

                                if (!allowAnyValueToKey && !(key instanceof String)) {
                                    throw new IllegalArgumentException("Only string value allowed in json format. Key: " + key);
                                }

                                opackObjectMap.put(key, value);
                            }
                        } catch (InvocationTargetException | IllegalAccessException exception) {
                            for (int i = 0; i < valueSize; i += 2) {
                                Object value = this.decodeValueStack.pop();
                                Object key = this.decodeValueStack.pop();

                                if (!allowAnyValueToKey && !(key instanceof String)) {
                                    throw new IllegalArgumentException("Only string value allowed in json format. Key: " + key);
                                }

                                opackObject.put(key, value);
                            }
                        }
                    } else if (currentContextType == OpackArray.class) {
                        if (currentChar != ']') {
                            throw new IOException("Expected character(]), but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                        }

                        OpackArray opackArray = (OpackArray) currentContext;
                        int currentSize = this.decodeValueStack.getSize();

                        List<Object> opackArrayList = null;

                        try {
                            opackArrayList = UnsafeOpackValue.getList(opackArray);

                            for (int i = currentSize - valueSize; i < currentSize; i++) {
                                opackArrayList.add(this.decodeValueStack.get(i));
                            }
                        } catch (InvocationTargetException | IllegalAccessException exception) {
                            for (int i = currentSize - valueSize; i < currentSize; i++) {
                                opackArray.add(this.decodeValueStack.get(i));
                            }
                        }

                        this.decodeValueStack.remove(valueSize);
                    } else {
                        throw new IOException("Caught corrupted stack, got " + (currentContextType == null ? "null" : currentContextType.getSimpleName()) + ".");
                    }

                    this.decodeBaseStack.pop();

                    if (!this.decodeBaseStack.isEmpty()) {
                        currentContextIndex = this.decodeBaseStack.peek();
                        currentContext = (OpackValue) this.decodeValueStack.get(currentContextIndex);
                        currentContextType = currentContext == null ? null : currentContext.getClass();
                    } else {
                        currentContextIndex = -1;
                        currentContext = null;
                        currentContextType = null;
                    }

                    valueMode = false;

                    break;
                }

                case ':': {
                    if (this.decodeBaseStack.isEmpty()) {
                        throw new IOException("Expected end of string, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                    }

                    if (valueMode) {
                        throw new IOException("Expected literal value, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                    }

                    if (currentContextType == OpackArray.class) {
                        throw new IOException("The array type cannot contain colons. at " + pointer + "(" + charArray[pointer - 1] + ").");
                    }

                    valueMode = true;

                    break;
                }

                case ',': {
                    if (this.decodeBaseStack.isEmpty()) {
                        throw new IOException("Expected end of string, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                    }

                    if (valueMode) {
                        throw new IOException("Expected literal value, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                    }

                    if (currentContextType == OpackObject.class) {
                        int valueSize = this.decodeValueStack.getSize() - currentContextIndex - 1;

                        if (valueSize % 2 != 0) {
                            throw new IOException("Expected colons(:), but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                        }
                    }

                    valueMode = true;

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
                    // Literal Value Parse

                    if (!valueMode) {
                        throw new IOException("Parsed unknown character at " + pointer + "(" + charArray[pointer - 1] + ").");
                    }

                    int startAnchor = pointer;

                    switch (currentChar) {
                        case '\"': {
                            boolean pushed = false;

                            // String Literal Parse
                            STRING_LOOP:
                            while (pointer < length) {
                                char literalChar = charArray[pointer++];

                                switch (literalChar) {
                                    case '\0':
                                    case '\b':
                                    case '\f':
                                    case '\n':
                                    case '\r':
                                    case '\t': {
                                        throw new IOException("Not allow unescaped character(" + this.escapeChar(literalChar) + ") in string literal, but got at " + pointer + ".");
                                    }
                                    case '\"': {
                                        this.decodeStringWriter.write(charArray, startAnchor, pointer - startAnchor - 1);

                                        this.decodeValueStack.push(this.decodeStringWriter.toString());
                                        this.decodeStringWriter.reset();

                                        pushed = true;
                                        break STRING_LOOP;
                                    }
                                    case '\\': {
                                        // Escape Character Parse
                                        this.decodeStringWriter.write(charArray, startAnchor, pointer - startAnchor - 1);

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
                                                        result += (char) (unicode - '0');
                                                    } else if (unicode >= 'a' && unicode <= 'f') {
                                                        result += (char) (unicode - 'a' + 10);
                                                    } else if (unicode >= 'A' && unicode <= 'F') {
                                                        result += (char) (unicode - 'A' + 10);
                                                    } else {
                                                        throw new IOException("Parsed unknown unicode pattern character(" + charArray[pointer - 1] + ") at " + pointer + ".");
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
                                            case '/':
                                                this.decodeStringWriter.write('/');
                                                break;
                                            default:
                                                throw new IOException("Parsed unknown escape pattern character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                                        }

                                        startAnchor = pointer;
                                    }
                                }
                            }

                            if (!pushed) {
                                throw new IOException("Expected end of string(\"), but got end of file.");
                            }


                            break;
                        }

                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case '-': {
                            // Number Literal Parse
                            boolean decimal = false;

                            NUMBER_LOOP:
                            while (pointer < length) {
                                char literalChar = charArray[pointer++];

                                switch (literalChar) {
                                    case 'E':
                                    case 'e':
                                    case '.':
                                        char previousChar = charArray[pointer - 2];

                                        if (previousChar < '0' || previousChar > '9') {
                                            throw new IOException("Expected digit, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                                        }

                                        decimal = true;
                                        break;
                                    case '+':
                                    case '-':
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                        break;
                                    default:
                                        pointer--;
                                        break NUMBER_LOOP;
                                }
                            }

                            String numberString = new String(charArray, startAnchor - 1, pointer - startAnchor + 1);

                            if (decimal) {
                                if (charArray[pointer - 1] == '.') {
                                    throw new IOException("A decimal number cannot end with a dot(.) at " + pointer + "(" + charArray[pointer - 1] + ").");
                                }

                                double doubleValue = Double.parseDouble(numberString);

                                if (Double.isFinite(doubleValue)) {
                                    this.decodeValueStack.push(doubleValue);
                                } else {
                                    this.decodeValueStack.push(new BigDecimal(numberString));
                                }
                            } else {
                                this.decodeValueStack.push(this.parseLongFast(numberString));
                            }

                            break;
                        }

                        case 't': {
                            for (int i = 1; i < CONST_TRUE_CHARACTER.length; i++) {
                                if (CONST_TRUE_CHARACTER[i] != charArray[pointer++]) {
                                    throw new IOException("Expected character(" + CONST_TRUE_CHARACTER[i] + ") of true, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                                }
                            }

                            this.decodeValueStack.push(true);

                            break;
                        }

                        case 'f': {
                            for (int i = 1; i < CONST_FALSE_CHARACTER.length; i++) {
                                if (CONST_FALSE_CHARACTER[i] != charArray[pointer++]) {
                                    throw new IOException("Expected character(" + CONST_TRUE_CHARACTER[i] + ") of true, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                                }
                            }

                            this.decodeValueStack.push(false);

                            break;
                        }

                        case 'n': {
                            for (int i = 1; i < CONST_NULL_CHARACTER.length; i++) {
                                if (CONST_NULL_CHARACTER[i] != charArray[pointer++]) {
                                    throw new IOException("Expected character(" + CONST_NULL_CHARACTER[i] + ") of null, but got character(" + charArray[pointer - 1] + ") at " + pointer + ".");
                                }
                            }

                            this.decodeValueStack.push(null);

                            break;
                        }

                        default:
                            throw new IOException("This value is not an opack value. Unknown value at " + pointer + "(" + currentChar + ").");
                    }

                    valueMode = false;
                    emptyBase = false;
                }
            }
        }

        if (currentContext != null) {
            if (currentContextType == OpackObject.class) {
                throw new IOException("Expected end of object(}), but got end of file.");
            } else if (currentContextType == OpackArray.class) {
                throw new IOException("Expected end of array(]), but got end of file.");
            } else {
                throw new IOException("Caught corrupted stack, got " + (currentContextType == null ? "null" : currentContextType.getSimpleName()) + ".");
            }
        }

        if (this.decodeValueStack.isEmpty()) {
            throw new IOException("Empty json.");
        }

        return this.decodeValueStack.get(0);
    }
}