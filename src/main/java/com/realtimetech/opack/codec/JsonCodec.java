package com.realtimetech.opack.codec;

import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.util.StringWriter;
import com.realtimetech.opack.util.structure.FastStack;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.jetbrains.annotations.NotNull;

public class JsonCodec implements OpackCodec<String> {
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

    private enum DecodeMode {
        NONE, OBJECT, ARRAY, STRING, NUMBER
    }

    public JsonCodec() {
    }

    void encodeRecursive(StringWriter stringWriter, Object object) {
        if (object == null) {
            stringWriter.write(JsonCodec.CONST_NULL_CHARACTER);
        }

        Class<?> type = object.getClass();

        if (type == OpackObject.class) {
            OpackObject opackObject = (OpackObject) object;

            stringWriter.write(CONST_OBJECT_OPEN_CHARACTER);
            int count = 0;
            for (Object key : opackObject.keySet()) {
                Object value = opackObject.get(key);

                if (count != 0) {
                    stringWriter.write(CONST_SEPARATOR_CHARACTER);
                }

                if (key instanceof OpackValue) {
                    // Unknown not allowed type to key
                }

                this.encodeRecursive(stringWriter, key);
                stringWriter.write(CONST_OBJECT_MAP_CHARACTER);
                this.encodeRecursive(stringWriter, value);

                count++;
            }
            stringWriter.write(CONST_OBJECT_CLOSE_CHARACTER);
        } else if (type == OpackArray.class) {
            OpackArray opackArray = (OpackArray) object;

            stringWriter.write(CONST_ARRAY_OPEN_CHARACTER);

            int size = opackArray.length();

            for (int index = 0; index < size; index++) {
                Object value = opackArray.get(index);

                if (index != 0) {
                    stringWriter.write(CONST_SEPARATOR_CHARACTER);
                }

                this.encodeRecursive(stringWriter, value);
            }
            stringWriter.write(CONST_ARRAY_CLOSE_CHARACTER);
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
                type = ReflectionUtil.getWrapperClassOfPrimitiveClass(type);
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
    }

    @Override
    public String encode(@NotNull OpackValue opackValue) {
        StringWriter stringWriter = new StringWriter();

        encodeRecursive(stringWriter, opackValue);

        return stringWriter.toString();
    }


    @Override
    public OpackValue decode(String data) {
        FastStack<Object> valueStack = new FastStack<Object>(128);
        FastStack<DecodeMode> modeStack = new FastStack<DecodeMode>(128);
        StringWriter stringWriter = new StringWriter();

        modeStack.push(DecodeMode.NONE);

        int pointer = 0;

        int length = data.length();
        char[] charArray = data.toCharArray();
        boolean decimal = false;
        char lastValidChar = ' ';
        try {
            DecodeMode currentMode = modeStack.peek();
            while (pointer <= length - 1) {
                char currentChar = charArray[pointer];

                if (currentMode == DecodeMode.NONE || currentMode == DecodeMode.OBJECT || currentMode == DecodeMode.ARRAY) {
                    if (!(currentChar == ' ' || currentChar == '\t' || currentChar == '\n')) {
                        if (currentChar == '{') {
                            valueStack.push(new OpackObject<>());
                            modeStack.push(DecodeMode.OBJECT);
                            currentMode = modeStack.peek();
                        } else if (currentChar == '[') {
                            valueStack.push(new OpackArray<>());
                            modeStack.push(DecodeMode.ARRAY);
                            currentMode = modeStack.peek();
                        } else if (currentChar == '\"') {
                            modeStack.push(DecodeMode.STRING);
                            currentMode = modeStack.peek();
                            stringWriter.reset();
                        } else if ((currentChar >= '0' && currentChar <= '9') || currentChar == '-') {
                            modeStack.push(DecodeMode.NUMBER);
                            currentMode = modeStack.peek();
                            decimal = false;
                            stringWriter.reset();
                            pointer--;
                        } else if (currentChar == 't') {
                            valueStack.push(true);
                            pointer += 3;
                        } else if (currentChar == 'f') {
                            valueStack.push(false);
                            pointer += 4;
                        } else if (currentChar == 'n') {
                            valueStack.push(null);
                            pointer += 3;
                        } else {
                            if (currentMode == DecodeMode.OBJECT) {
                                if (currentChar == ',' || currentChar == '}') {
                                    if (lastValidChar != '{') {
                                        Object value = valueStack.pop();
                                        Object key = valueStack.pop();

                                        OpackObject jsonObject = (OpackObject) valueStack.peek();
                                        jsonObject.put(key, value);
                                    }

                                    if (currentChar == '}') {
                                        modeStack.pop();
                                        currentMode = modeStack.peek();
                                    }
                                }
                            } else if (currentMode == DecodeMode.ARRAY) {
                                if (currentChar == ',' || currentChar == ']') {
                                    if (lastValidChar != '[') {
                                        Object value = valueStack.pop();

                                        OpackArray jsonArray = (OpackArray) valueStack.peek();
                                        jsonArray.add(value);
                                    }

                                    if (currentChar == ']') {
                                        modeStack.pop();
                                        currentMode = modeStack.peek();
                                    }
                                }
                            }
                        }

                        lastValidChar = currentChar;
                    }
                } else if (currentMode == DecodeMode.STRING) {
                    if (currentChar == '\\') {
                        char nextChar = charArray[pointer + 1];

                        switch (nextChar) {
                            case '"':
                                stringWriter.write('\"');
                                break;
                            case '\\':
                                stringWriter.write('\\');
                                break;
                            case 'u':
                                char result = 0;
                                for(int i = 2; i <= 5; i++){
                                    char c = charArray[pointer + i];
                                    result <<= 4;
                                    if (c >= '0' && c <= '9') {
                                        result += (c - '0');
                                    } else if (c >= 'a' && c <= 'f') {
                                        result += (c - 'a' + 10);
                                    } else if (c >= 'A' && c <= 'F') {
                                        result += (c - 'A' + 10);
                                    } else {
                                        // Unknown charset unicode
//                                        throw new IOException("An exception occurred at " + (pointer - 1) + " position character(" + charArray[(pointer - 1)] + ")");
                                    }
                                }
                                pointer+=4;
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

                        pointer++;
                    } else if (currentChar == '\"') {
                        valueStack.push(stringWriter.toString());
                        modeStack.pop();
                        currentMode = modeStack.peek();
                    } else {
                        stringWriter.write(currentChar);
                    }
                } else if (currentMode == DecodeMode.NUMBER) {
                    if (!(currentChar >= '0' && currentChar <= '9') && currentChar != 'E' && currentChar != 'e' && currentChar != '+'  && currentChar != '-' && currentChar != 'D' && currentChar != 'd' && currentChar != 'F' && currentChar != 'f' && currentChar != 'L' && currentChar != 'l' && currentChar != 'B' && currentChar != 'b' && currentChar != '.') {
                        modeStack.pop();
                        currentMode = modeStack.peek();

                        if (decimal) {
                            double value = Double.parseDouble(stringWriter.toString());

                            if (value == (double) (float) value) {
                                valueStack.push((float) value);
                            } else {
                                valueStack.push(value);
                            }
                        } else {
                            long value = Long.parseLong(stringWriter.toString());

                            if (value == (long) (int) value) {
                                valueStack.push((int) value);
                            } else {
                                valueStack.push(value);
                            }
                        }

                        pointer--;
                    } else {
                        if (!decimal && currentChar == '.') {
                            decimal = true;
                        }

                        stringWriter.write(currentChar);
                    }
                }

                pointer++;

            }
        } catch (Exception e) {
            e.printStackTrace();
            // Unknown charset unicode
//            throw new IOException("An exception occurred at " + (pointer - 1) + " position character(" + charArray[(pointer - 1)] + ")");
        }

        return (OpackValue) valueStack.pop();
    }

    public static void main(String[] args) {
        OpackObject opackObject = new OpackObject();
        opackObject.put("A", 21378);
        {
            OpackObject o = new OpackObject();

            o.put("bool1", true);
            o.put("bool2", false);

            opackObject.put("B", o);
        }
        opackObject.put("C", 21378);
        opackObject.put("D", 21378);
        {
            OpackArray a = new OpackArray();

            a.add(10);
            a.add("AAA");

            opackObject.put("E", a);
        }

        JsonCodec jsonCodec = new JsonCodec();

        String str = jsonCodec.encode(opackObject);

        OpackValue opackValue = jsonCodec.decode(str);

        System.out.println(opackValue);

    }
}
