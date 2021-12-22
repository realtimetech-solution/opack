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

package com.realtimetech.opack.util;

public class StringWriter {
	private int blockSize;

	private char[] chars;

	private int currentIndex;
	private int scope;

	private int currentSize;

	public StringWriter() {
		this(1024);
	}

	public StringWriter(int blockSize) {
		this.blockSize = blockSize;
		this.currentIndex = -1;
		this.scope = 0;
		this.currentSize = 0;

		this.growArray(0);
	}

	private void growArray(int needSize) {
		this.scope = needSize / blockSize;

		char[] oldObjects = this.chars;

		this.currentSize = (this.scope + 1) * this.blockSize;
		this.chars = new char[this.currentSize];

		if (oldObjects != null) {
			System.arraycopy(oldObjects, 0, this.chars, 0, currentIndex + 1);
		}
	}

	public void write(char object) {
		int need = this.currentIndex + 1;
		if (need >= this.currentSize) {
			growArray(need);
		}

		this.currentIndex = need;

		this.chars[this.currentIndex] = object;
	}

	
	public void write(char[] src) {
		this.write(src, 0, src.length);
	}

	public void write(char[] src, int offset, int length) {
		int size = length;
		int need = this.currentIndex + size;
		if (need >= this.currentSize) {
			growArray(need);
		}

		for (int index = 1; index <= size; index++) {
			this.chars[this.currentIndex + index] = src[offset + index - 1];
		}

		this.currentIndex = need;
	}

	public int getLength() {
		return this.currentIndex + 1;
	}

	public void reset() {
		this.currentIndex = -1;
	}

	public String toString() {
		return new String(this.chars, 0, this.currentIndex + 1);
	}
	public char[] toCharArray() {
		char[] charArray = new char[this.currentIndex + 1];
		System.arraycopy(this.chars, 0, charArray, 0, this.currentIndex + 1);
		return charArray;
	}
}