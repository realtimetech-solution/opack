package com.realtimetech.opack;

import com.realtimetech.opack.value.OpackBool;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackString;
import com.realtimetech.opack.value.OpackValue;

public class Sample {
    public static void main(String[] args) {
        OpackObject opackObject = new OpackObject();
        opackObject.put(new OpackString("Hi"), new OpackBool(false));
        OpackValue opackValue = opackObject.get(new OpackString("Hi"));

        System.out.println(opackValue);
    }
}
