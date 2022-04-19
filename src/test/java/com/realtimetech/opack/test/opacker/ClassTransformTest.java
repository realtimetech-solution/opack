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

package com.realtimetech.opack.test.opacker;

import com.realtimetech.opack.Opacker;
import com.realtimetech.opack.annotation.Type;
import com.realtimetech.opack.annotation.Transform;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.transformer.Transformer;
import com.realtimetech.opack.util.ReflectionUtil;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

public class ClassTransformTest {
    public static class ClassTransformer implements Transformer {
        @Override
        public Object serialize(Opacker opacker, Object value) throws SerializeException {
            if (value instanceof ClassTransformInheritable) {
                ClassTransformInheritable classTransformInheritable = (ClassTransformInheritable) value;
                return new String(classTransformInheritable.bytes, StandardCharsets.UTF_8);
            }
            if (value instanceof ClassTransformNoInheritable) {
                ClassTransformNoInheritable classTransformInheritable = (ClassTransformNoInheritable) value;
                return new String(classTransformInheritable.bytes, StandardCharsets.UTF_8);
            }

            return value;
        }

        @Override
        public Object deserialize(Opacker opacker, Class<?> goalType, Object value) throws DeserializeException {
            if (value instanceof String && ClassTransformInheritable.class.isAssignableFrom(goalType)) {
                try {
                    ClassTransformInheritable classTransformInheritable = (ClassTransformInheritable) ReflectionUtil.createInstanceUnsafe(goalType);
                    classTransformInheritable.setBytes(((String) value).getBytes(StandardCharsets.UTF_8));
                    return classTransformInheritable;
                } catch (InvocationTargetException | IllegalAccessException | InstantiationException exception) {
                    throw new DeserializeException(exception);
                }
            }

            if (value instanceof String && ClassTransformNoInheritable.class.isAssignableFrom(goalType)) {
                try {
                    ClassTransformNoInheritable classTransformNoInheritable = (ClassTransformNoInheritable) ReflectionUtil.createInstanceUnsafe(goalType);
                    classTransformNoInheritable.setBytes(((String) value).getBytes(StandardCharsets.UTF_8));
                    return classTransformNoInheritable;
                } catch (InvocationTargetException | IllegalAccessException | InstantiationException exception) {
                    throw new DeserializeException(exception);
                }
            }

            return value;
        }
    }

    @Transform(transformer = ClassTransformer.class, inheritable = true)
    public static class ClassTransformInheritable {
        private byte[] bytes;

        public ClassTransformInheritable(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }


    @Transform(transformer = ClassTransformer.class, inheritable = false)
    public static class ClassTransformNoInheritable {
        private byte[] bytes;

        public ClassTransformNoInheritable(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    public static class ClassTransformInheritableChild extends ClassTransformInheritable {
        public ClassTransformInheritableChild(byte[] bytes) {
            super(bytes);
        }
    }

    public static class ClassTransformNoInheritableChild extends ClassTransformNoInheritable {
        public ClassTransformNoInheritableChild(byte[] bytes) {
            super(bytes);
        }
    }

    public static class ClassTransformClass {
        private ClassTransformInheritable classTransformInheritableValue;
        private ClassTransformInheritableChild classTransformInheritableChildValue;
        @Type(ClassTransformInheritable.class)
        private Object explicitClassTransformInheritableValue;
        @Type(ClassTransformInheritableChild.class)
        private Object explicitClassTransformInheritableChildValue;

        private ClassTransformNoInheritable classTransformNoInheritableValue;
        private ClassTransformNoInheritableChild classTransformNoInheritableChildValue;
        @Type(ClassTransformNoInheritable.class)
        private Object explicitClassTransformNoInheritableValue;
        @Type(ClassTransformNoInheritableChild.class)
        private Object explicitClassTransformNoInheritableChildValue;

        public ClassTransformClass() {
            byte[] bytes = "Thank you for visiting Realtime Tech Co., Ltd. Realtime Tech Co., Ltd., since its founding in 2000 by Professor, has entered the DBMS field monopolized by foreign global companies. Its high stability and performance are recognized by supplying Kairos, a product, to various fields of application in the military, government and civilian sectors. In particular, we are leading the 4D computing market by developing real-time spatial DBMS and moving object trajectory management solutions. Based on these system SW and 4D computing technologies and experiences, we are making efforts to advance into AI and big data fields and take another leap forward as a company leading digital transformation such as smart factories and smart cities.".getBytes(StandardCharsets.UTF_8);

            this.classTransformInheritableValue = new ClassTransformInheritable(bytes);
            this.classTransformInheritableChildValue = new ClassTransformInheritableChild(bytes);
            this.explicitClassTransformInheritableValue = new ClassTransformInheritable(bytes);
            this.explicitClassTransformInheritableChildValue = new ClassTransformInheritableChild(bytes);

            this.classTransformNoInheritableValue = new ClassTransformNoInheritable(bytes);
            this.classTransformNoInheritableChildValue = new ClassTransformNoInheritableChild(bytes);
            this.explicitClassTransformNoInheritableValue = new ClassTransformNoInheritable(bytes);
            this.explicitClassTransformNoInheritableChildValue = new ClassTransformNoInheritableChild(bytes);
        }
    }

    @Test
    public void test() throws SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        ClassTransformClass originalObject = new ClassTransformClass();

        OpackValue serialized = opacker.serialize(originalObject);

        Assertions.assertEquals(((OpackObject) serialized).get("classTransformInheritableValue").getClass(), String.class);
        Assertions.assertEquals(((OpackObject) serialized).get("classTransformInheritableChildValue").getClass(), String.class);
        Assertions.assertEquals(((OpackObject) serialized).get("explicitClassTransformInheritableValue").getClass(), String.class);
        Assertions.assertEquals(((OpackObject) serialized).get("explicitClassTransformInheritableChildValue").getClass(), String.class);

        Assertions.assertEquals(((OpackObject) serialized).get("classTransformNoInheritableValue").getClass(), String.class);
        Assertions.assertNotEquals(((OpackObject) serialized).get("classTransformNoInheritableChildValue").getClass(), String.class);
        Assertions.assertEquals(((OpackObject) serialized).get("explicitClassTransformNoInheritableValue").getClass(), String.class);
        Assertions.assertNotEquals(((OpackObject) serialized).get("explicitClassTransformNoInheritableChildValue").getClass(), String.class);

        ClassTransformClass deserialized = opacker.deserialize(ClassTransformClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
