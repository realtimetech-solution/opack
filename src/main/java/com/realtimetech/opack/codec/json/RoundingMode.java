/*
 * Copyright (C) 2025 REALTIMETECH All Rights Reserved
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

public enum RoundingMode {
    CONSERVATIVE {
        @Override
        public boolean acceptUpperBound(boolean even) {
            return false;
        }

        @Override
        public boolean acceptLowerBound(boolean even) {
            return false;
        }
    },
    ROUND_EVEN {
        @Override
        public boolean acceptUpperBound(boolean even) {
            return even;
        }

        @Override
        public boolean acceptLowerBound(boolean even) {
            return even;
        }
    };

    public abstract boolean acceptUpperBound(boolean even);

    public abstract boolean acceptLowerBound(boolean even);
}