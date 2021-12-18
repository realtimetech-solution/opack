package com.realtimetech.opack.util;

import com.realtimetech.opack.value.OpackArray;

import java.lang.reflect.Array;

public class StringWriter {
	private int raiseSize;

	private char[] chars;

	private int currentIndex;
	private int scope;

	private int currentSize;

	public StringWriter() {
		this(1024);
	}

	public StringWriter(int raiseSize) {
		this.raiseSize = raiseSize;
		this.currentIndex = -1;
		this.scope = 0;
		this.currentSize = 0;

		this.raise(0);
	}

	private void raise(int needSize) {
		this.scope = needSize / raiseSize;

		char[] oldObjects = this.chars;

		this.currentSize = (this.scope + 1) * this.raiseSize;
		this.chars = new char[this.currentSize];

		if (oldObjects != null) {
			System.arraycopy(oldObjects, 0, this.chars, 0, currentIndex + 1);
		}
	}

	public void write(char object) {
		int need = this.currentIndex + 1;
		if (need >= this.currentSize) {
			raise(need);
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
			raise(need);
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