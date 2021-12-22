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

package com.realtimetech.opack.codec;

import com.realtimetech.opack.exception.DecodeException;
import com.realtimetech.opack.exception.EncodeException;
import com.realtimetech.opack.value.OpackValue;

import java.io.IOException;

public abstract class OpackCodec<D> {
    protected abstract D doEncode(OpackValue opackValue) throws IOException;

    protected abstract OpackValue doDecode(D data) throws IOException;

    public synchronized D encode(OpackValue opackValue) throws EncodeException {
        try {
            return this.doEncode(opackValue);
        } catch (Exception exception) {
            throw new EncodeException(exception);
        }
    }

    public synchronized OpackValue decode(D data) throws DecodeException {
        try {
            return this.doDecode(data);
        } catch (Exception exception) {
            throw new DecodeException(exception);
        }
    }
}
