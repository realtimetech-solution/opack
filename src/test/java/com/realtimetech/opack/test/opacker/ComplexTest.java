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
import com.realtimetech.opack.annotation.ExplicitType;
import com.realtimetech.opack.exception.DeserializeException;
import com.realtimetech.opack.exception.SerializeException;
import com.realtimetech.opack.test.OpackAssert;
import com.realtimetech.opack.value.OpackValue;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;

public class ComplexTest {
    static final Random RANDOM = new Random();

    public static class ComplexClass {
        private WrapperTest.WrapperClass wrapperClassValue;
        private WrapperTest.WrapperClass[] wrapperClassArrayValue;

        private StringTest.StringClass stringClassValue;
        private StringTest.StringClass[] stringClassArrayValue;

        private PrimitiveTest.PrimitiveClass primitiveClassValue;
        private PrimitiveTest.PrimitiveClass[] primitiveClassArrayValue;

        private ObjectTest.ObjectClass objectClassValue;
        private ObjectTest.ObjectClass[] objectClassArrayValue;

        private ExplicitObjectTest.ExplicitObjectClass explicitObjectClassValue;
        private ExplicitObjectTest.ExplicitObjectClass[] explicitObjectClassArrayValue;

        private ListTest.ListClass listClassValue;
        private ListTest.ListClass[] listClassArrayValue;

        private FieldTransformTest.FieldTransformClass fieldTransformClassValue;
        private FieldTransformTest.FieldTransformClass[] fieldTransformClassArrayValue;

        private ClassTransformTest.ClassTransformClass classTransformClassValue;
        private ClassTransformTest.ClassTransformClass[] classTransformClassArrayValue;

        public ComplexClass() {
            int length = RANDOM.nextInt(5) + 5;

            this.wrapperClassValue = new WrapperTest.WrapperClass();
            this.wrapperClassArrayValue = new WrapperTest.WrapperClass[length];
            for (int index = 0; index < length; index++) {
                this.wrapperClassArrayValue[index] = new WrapperTest.WrapperClass();
            }

            this.stringClassValue = new StringTest.StringClass();
            this.stringClassArrayValue = new StringTest.StringClass[length];
            for (int index = 0; index < length; index++) {
                this.stringClassArrayValue[index] = new StringTest.StringClass();
            }

            this.primitiveClassValue = new PrimitiveTest.PrimitiveClass();
            this.primitiveClassArrayValue = new PrimitiveTest.PrimitiveClass[length];
            for (int index = 0; index < length; index++) {
                this.primitiveClassArrayValue[index] = new PrimitiveTest.PrimitiveClass();
            }

            this.objectClassValue = new ObjectTest.ObjectClass();
            this.objectClassArrayValue = new ObjectTest.ObjectClass[length];
            for (int index = 0; index < length; index++) {
                this.objectClassArrayValue[index] = new ObjectTest.ObjectClass();
            }

            this.explicitObjectClassValue = new ExplicitObjectTest.ExplicitObjectClass();
            this.explicitObjectClassArrayValue = new ExplicitObjectTest.ExplicitObjectClass[length];
            for (int index = 0; index < length; index++) {
                this.explicitObjectClassArrayValue[index] = new ExplicitObjectTest.ExplicitObjectClass();
            }

            this.listClassValue = new ListTest.ListClass();
            this.listClassArrayValue = new ListTest.ListClass[length];
            for (int index = 0; index < length; index++) {
                this.listClassArrayValue[index] = new ListTest.ListClass();
            }

            this.fieldTransformClassValue = new FieldTransformTest.FieldTransformClass();
            this.fieldTransformClassArrayValue = new FieldTransformTest.FieldTransformClass[length];
            for (int index = 0; index < length; index++) {
                this.fieldTransformClassArrayValue[index] = new FieldTransformTest.FieldTransformClass();
            }

            this.classTransformClassValue = new ClassTransformTest.ClassTransformClass();
            this.classTransformClassArrayValue = new ClassTransformTest.ClassTransformClass[length];
            for (int index = 0; index < length; index++) {
                this.classTransformClassArrayValue[index] = new ClassTransformTest.ClassTransformClass();
            }
        }
    }

    @Test
    public void test() throws InstantiationException, SerializeException, DeserializeException, OpackAssert.AssertException {
        Opacker opacker = new Opacker.Builder().create();
        ComplexClass originalObject = new ComplexClass();

        OpackValue serialized = opacker.serialize(originalObject);
        ComplexClass deserialized = opacker.deserialize(ComplexClass.class, serialized);

        OpackAssert.assertEquals(originalObject, deserialized);
    }
}
