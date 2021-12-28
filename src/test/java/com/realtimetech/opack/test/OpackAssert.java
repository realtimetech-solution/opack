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

package com.realtimetech.opack.test;

import com.realtimetech.opack.annotation.Ignore;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackValue;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class OpackAssert {
    public static class FieldAssertException extends AssertException {
        public FieldAssertException(Class<?> parentClass, String fieldName, AssertException cause) {
            super("Field assert failure (" + parentClass.getSimpleName() + " class " + fieldName + " field)");
            initCause(cause);
        }
    }

    public static class AssertException extends Exception {
        public AssertException(String message) {
            super(message);
        }
    }

    public static void assertEquals(Object original, Object target) throws AssertException {
        OpackAssert.assertSingleValue(original, target);
    }

    static void assertObject(Object original, Object target) throws AssertException {
        for (Field field : original.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Ignore.class) && !Modifier.isTransient(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    Object originalObject = field.get(original);
                    Object targetObject = field.get(target);

                    OpackAssert.assertSingleValue(originalObject, targetObject);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Assert failure, throw illegal access exception when get field value");
                } catch (AssertException exception) {
                    throw new FieldAssertException(original.getClass(), field.getName(), exception);
                }
            }
        }
    }

    static void throwException(Object originalObject, Object targetObject) throws AssertException {
        OpackAssert.throwException("value", originalObject, targetObject);
    }

    static void throwException(String property, Object originalValue, Object targetValue) throws AssertException {
        throw new AssertException("Assert failure, expected " + property + " is \"" + originalValue + "\" but got \"" + targetValue + "\"");
    }

    static void throwExceptionNotContains(Object originalValue) throws AssertException {
        throw new AssertException("Assert failure, originally contains " + originalValue + " but not contains");
    }

    static void assertSingleValue(Object originalObject, Object targetObject) throws AssertException {
        if (originalObject == null || targetObject == null) {
            if (originalObject == null && targetObject == null) {
                return;
            }

            OpackAssert.throwException(originalObject, targetObject);
        }

        if (originalObject.getClass() == targetObject.getClass()) {
            if (originalObject.getClass().isArray()) {
                int originalLength = Array.getLength(originalObject);
                int targetLength = Array.getLength(targetObject);

                if (originalLength == targetLength) {
                    for (int index = 0; index < originalLength; index++) {
                        Object originalElementObject = Array.get(originalObject, index);
                        Object targetElementObject = Array.get(targetObject, index);

                        if (originalElementObject == null || targetElementObject == null) {
                            if (originalElementObject == null && targetElementObject == null) {
                                continue;
                            }

                            OpackAssert.throwException(originalElementObject, targetElementObject);
                        }

                        if (originalElementObject.getClass() != targetElementObject.getClass()) {
                            OpackAssert.throwException("element", originalElementObject, targetElementObject);
                        }

                        OpackAssert.assertSingleValue(originalElementObject, targetElementObject);
                    }
                } else {
                    OpackAssert.throwException("length", originalLength, targetLength);
                }
            } else if (originalObject instanceof Map && targetObject instanceof Map) {
                Map<?, ?> originalMap = (Map<?, ?>) originalObject;
                Map<?, ?> targetMap = (Map<?, ?>) targetObject;
                int originalLength = originalMap.keySet().size();
                int targetLength = targetMap.keySet().size();

                if (originalLength == targetLength) {
                    for (Object originalKeyObject : originalMap.keySet()) {
                        if (targetMap.containsKey(originalKeyObject)) {
                            Object originalValueObject = originalMap.get(originalKeyObject);
                            Object targetValueObject = targetMap.get(originalKeyObject);

                            OpackAssert.assertSingleValue(originalValueObject, targetValueObject);
                        } else {
                            OpackAssert.throwExceptionNotContains(originalKeyObject);
                        }
                    }
                } else {
                    OpackAssert.throwException("length", originalLength, targetLength);
                }
            } else if (originalObject instanceof List && targetObject instanceof List) {
                List<?> originalList = (List<?>) originalObject;
                List<?> targetList = (List<?>) targetObject;
                int originalLength = originalList.size();
                int targetLength = targetList.size();

                if (originalLength == targetLength) {
                    for (int i = 0; i < originalList.size(); i++) {
                        OpackAssert.assertSingleValue(originalList.get(i), targetList.get(i));
                    }
                } else {
                    OpackAssert.throwException("length", originalLength, targetLength);
                }
            } else if (originalObject.getClass().isEnum() && targetObject.getClass().isEnum()) {
                if (!originalObject.equals(targetObject)) {
                    OpackAssert.throwException(originalObject, targetObject);
                }
            } else if (OpackValue.isAllowType(originalObject.getClass())) {
                if (!originalObject.equals(targetObject)) {
                    OpackAssert.throwException(originalObject, targetObject);
                }
            } else {
                OpackAssert.assertObject(originalObject, targetObject);
            }
        } else {
            OpackAssert.throwException("type", originalObject.getClass(), targetObject.getClass());
        }
    }
}
