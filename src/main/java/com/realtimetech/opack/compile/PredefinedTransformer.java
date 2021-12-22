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

package com.realtimetech.opack.compile;

import com.realtimetech.opack.transformer.Transformer;
import org.jetbrains.annotations.NotNull;

public class PredefinedTransformer {
    final Transformer transformer;
    final boolean inheritable;

    public PredefinedTransformer(@NotNull Transformer transformer, boolean inheritable) {
        this.transformer = transformer;
        this.inheritable = inheritable;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public boolean isInheritable() {
        return inheritable;
    }
}