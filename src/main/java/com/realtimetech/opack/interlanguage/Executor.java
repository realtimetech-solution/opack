package com.realtimetech.opack.interlanguage;

import com.realtimetech.opack.value.OpackValue;

import java.util.Stack;

public class Executor {
    public void sandbox() {
        Stack<CallContext> callStack = new Stack<>();
        Stack<OpackValue> valueStack = new Stack<>();
        Stack<Object> objectStack = new Stack<>();

        while (!callStack.empty()) {

        }
    }
}
