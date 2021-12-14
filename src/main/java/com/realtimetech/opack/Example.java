package com.realtimetech.opack;

import com.realtimetech.opack.annotation.ExplicitType;
import com.realtimetech.opack.annotation.Transform;
import com.realtimetech.opack.value.OpackArray;
import com.realtimetech.opack.value.OpackObject;
import com.realtimetech.opack.value.OpackValue;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Example {
    private static Random RANDOM = new Random();

    private String stringValue;
    private String[] stringArray;
    private String[][] stringArrayArray;

    private Integer integerValue;
    private Integer[] integerArray;
    private Integer[][] integerArrayArray;

    private DataObject object;
    private DataObject[] objectArray;
    private DataObject[][] objectArrayArray;

    private byte[] bigByteArray;
    private byte[][] bigByteArrayArray;

    @Transform(transformer = StringTransformer.class)
    private byte[] transformByteArray;

    private OpackArray opackArray;
    private OpackObject opackObject;

    @ExplicitType(type = OpackObject.class)
    private OpackValue opackValue;

    private OpackObject[] opackObjectArray;
    private OpackArray[] opackArrayArray;


//        @ExplicitType(type = ArrayList.class)
//        public List<String> stringLinkedList;

//        @Transform(transformer = ListOpackTransformer.class)
//        public List<OpackValue> stringLinkedList;


    public Example() {
        stringValue = "Hello, World";
        stringArray = new String[10];
        for(int i = 0; i < stringArray.length; i++){
            stringArray[i] = "stringArray, " + i;
        }
        stringArrayArray = new String[RANDOM.nextInt(10) + 2][];
        for(int i = 0; i < stringArrayArray.length; i++){
            stringArrayArray[i] = new String[RANDOM.nextInt(10) + 2];
            for(int j = 0; j < stringArrayArray[i].length; j++){
                stringArrayArray[i][j] = "stringArrayArray, " + j;
            }
        }

        integerValue = RANDOM.nextInt();
        integerArray = new Integer[10];
        for(int i = 0; i < integerArray.length; i++){
            integerArray[i] = RANDOM.nextInt();
        }
        integerArrayArray = new Integer[RANDOM.nextInt(10) + 2][];
        for(int i = 0; i < integerArrayArray.length; i++){
            integerArrayArray[i] = new Integer[RANDOM.nextInt(10) + 2];
            for(int j = 0; j < integerArrayArray[i].length; j++){
                integerArrayArray[i][j] = RANDOM.nextInt();
            }
        }

        object = new DataObject();
        objectArray = new DataObject[10];
        for(int i = 0; i < objectArray.length; i++){
            objectArray[i] = new DataObject();
        }
        objectArrayArray = new DataObject[RANDOM.nextInt(10) + 2][];
        for(int i = 0; i < objectArrayArray.length; i++){
            objectArrayArray[i] = new DataObject[RANDOM.nextInt(10) + 2];
            for(int j = 0; j < objectArrayArray[i].length; j++){
                objectArrayArray[i][j] = new DataObject();
            }
        }

        bigByteArray = new byte[1024 * 1024];
        RANDOM.nextBytes(bigByteArray);
        bigByteArrayArray = new byte[1024][];
        for(int i = 0; i < bigByteArrayArray.length; i++){
            bigByteArrayArray[i] = new byte[RANDOM.nextInt(512) + 512];
            RANDOM.nextBytes(bigByteArrayArray[i]);
        }

        transformByteArray = new String("also accessible via reflection. But only generics-related info is only available for the class in general - not for specific instances of the class. To make a concrete example:").getBytes(StandardCharsets.UTF_8);

        opackArray = new OpackArray();
        {
            opackArray.add("A");
            opackArray.add("B");
            opackArray.add("C");
            opackArray.add("D");
            opackArray.add(19990218);
            opackArray.add(RANDOM.nextInt());
            opackArray.add(RANDOM.nextFloat());
            opackArray.add(RANDOM.nextDouble());
            opackArray.add(RANDOM.nextBoolean());
        }

        opackObject = new OpackObject();
        {
            opackObject.put("A", RANDOM.nextInt());
            opackObject.put("B", RANDOM.nextInt());
            opackObject.put("C", RANDOM.nextInt());
            opackObject.put(RANDOM.nextInt(), "A");
            opackObject.put(RANDOM.nextInt(), "B");
            opackObject.put(RANDOM.nextInt(), "C");
        }

        opackValue = new OpackArray();
        ((OpackArray)opackValue).add("A");
        ((OpackArray)opackValue).add("B");
        ((OpackArray)opackValue).add("C");

        opackObjectArray = new OpackObject[5];
        for(int i = 0; i < opackObjectArray.length; i++){
            opackObjectArray[i] = new OpackObject();
            opackObjectArray[i].put("index" + i, RANDOM.nextInt());
        }

        opackArrayArray = new OpackArray[5];
        for(int i = 0; i < opackArrayArray.length; i++){
            opackArrayArray[i] = new OpackArray();
            opackArrayArray[i].add(i);
        }
    }

    public String getStringValue() {
        return stringValue;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public String[][] getStringArrayArray() {
        return stringArrayArray;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public Integer[] getIntegerArray() {
        return integerArray;
    }

    public Integer[][] getIntegerArrayArray() {
        return integerArrayArray;
    }

    public DataObject getObject() {
        return object;
    }

    public DataObject[] getObjectArray() {
        return objectArray;
    }

    public DataObject[][] getObjectArrayArray() {
        return objectArrayArray;
    }

    public byte[] getBigByteArray() {
        return bigByteArray;
    }

    public byte[] getTransformByteArray() {
        return transformByteArray;
    }

    public OpackArray getOpackArray() {
        return opackArray;
    }

    public OpackObject getOpackObject() {
        return opackObject;
    }

    public OpackValue getOpackValue() {
        return opackValue;
    }

    public OpackObject[] getOpackObjectArray() {
        return opackObjectArray;
    }

    public OpackArray[] getOpackArrayArray() {
        return opackArrayArray;
    }
}