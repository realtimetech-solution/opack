# opack

<a href="#"><img src="https://github.com/realtimetech-solution/opack/actions/workflows/windows-x64.yml/badge.svg"/></a>

Opack is a Java library that can serialize/deserialize between Java objects and common objects(OpackValue). Also, common objects can be encoded or decoded as JSON or Bytes(Dense).

**We faster then GSON and Kryo.** (See [tests](./src/test/java/com/realtimetech/opack/test/performance))

### Usage
#### 1. Serialize
```java
Opacker opacker = new Opacker.Builder().create();

SomeObject someObject = new SomeObject();

OpackValue opackValue = opacker.serialize(someObject);
```

#### 2. Deserialize
```java
Opacker opacker = new Opacker.Builder()
        .setContextStackInitialSize(128)      // (Optional) Creation size of stack for processing
        .setValueStackInitialSize(512)        // (Optional) Creation size of stack for processing
        .setConvertEnumToOrdinal(false)       // (Optional) Convert Enum to ordinal or name
        .setEnableWrapListElementType(false)  // (Optional) When converting elements of a list, record the type as well
        .setEnableWrapMapElementType(false)   // (Optional) When converting elements of a map, record the type as well
        .create();

OpackValue serializedSomeObject = /** See Serialize Usage **/;

SomeObject someObject = opacker.deserialize(SomeObject.class, serializedSomeObject);
```

#### 3. Json Codec
```java
JsonCodec jsonCodec = new JsonCodec.Builder()
        .setEncodeStackInitialSize(128)       // (Optional) Creation size of stack for processing
        .setEncodeStringBufferSize(1024)      // (Optional) Creation size of stack for processing
        .setDecodeStackInitialSize(128)       // (Optional) Creation size of stack for processing
        .setAllowOpackValueToKeyValue(false)  // (Optional) Accepts Objct or Array as Key of Json Object
        .setPrettyFormat(false)               // (Optional) When encoding, it prints formatted
        .create();

OpackValue opackValue = /** See Serialize Usage **/;

/*
    Encode
 */
String json = jsonCodec.encode(opackValue);
// Or
Writer writer = new StringWriter(); 
jsonCodec.encode(writer, opackValue);

/*
    Decode
 */
OpackValue decodedOpackValue = jsonCodec.decode(json);
```

#### 4. Dense Codec
```java
DenseCodec denseCodec = new DenseCodec.Builder()
        .setDecodeStackInitialSize(128)           // (Optional) Creation size of stack for processing
        .setEncodeStackInitialSize(128)           // (Optional) Creation size of stack for processing
        .setEncodeOutputBufferInitialSize(1024)   // (Optional) Creation size of stack for processing
        .create();

OpackValue opackValue = /** See Serialize Usage **/;

/*
    Encode
 */
byte[] bytes = denseCodec.encode(opackValue);
// Or
OutputStream outputStream = new ByteArrayOutputStream();
denseCodec.encode(outputStream, opackValue);

/*
    Decode
 */
OpackValue decodedOpackValue1 = denseCodec.decode(bytes);
// Or
InputStream inputStream = new ByteArrayInputStream(bytes);
OpackValue decodedOpackValue2 = denseCodec.decode(inputStream);
```

### Advanced Usage
#### 1. Ignore and ExplicitType
```java
public class SomeObject {
    private String stringField;
    private byte[] bytesField;

    /*
        This field is not serialized/deserialized
     */
    @Ignore
    private String verySecretField;

    /*
        This field is serialized/deserialized to explicit type `ArrayList` instead of ambiguous field type `List`
     */
    @ExplicitType(type = ArrayList.class)
    private List<String> listField;
}
```
#### 2. Field Transformer
```java
public class ByteToBase64Transformer implements Transformer {
    @Override
    public Object serialize(Opacker opacker, Object value) throws SerializeException {
        if (value instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) value);
        }

        return value;
    }

    @Override
    public Object deserialize(Opacker opacker, Class<?> goalType, Object value) throws DeserializeException {
        if (value instanceof String) {
            return Base64.getDecoder().decode((String) value);
        }

        return value;
    }
}

public class SomeObject {
    /*
        This field is serialized/deserialized via Base64
     */
    @Transform(transformer = ByteToBase64Transformer.class)
    private byte[] bytesField;
}
```
#### 3. Class Transformer
```java
public class AnimalTransformer implements Transformer {
    /*
        Remove a `sound` from a serialized `Animal`
    */
    @Override
    public Object serialize(Opacker opacker, Object value) throws SerializeException {
        if (value instanceof Animal) {
            Animal animal = (Animal) value;
            OpackValue opackValue = opacker.serialize(animal);

            if (opackValue instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) opackValue;
                opackObject.remove("sound");
                return opackObject;
            }
        }

        return value;
    }

    /*
        Restore `sound` from `Animal` before deserialization
    */
    @Override
    public Object deserialize(Opacker opacker, Class<?> goalType, Object value) throws DeserializeException {
        if (value instanceof OpackObject) {
            if (Animal.class.isAssignableFrom(goalType)) {
                OpackObject opackObject = (OpackObject) value;
                Animal animal = (Animal) opacker.deserialize(goalType, opackObject);
                animal.setSound(animal.bark());
            }
        }

        return value;
    }
}

/*
    When `inheritable` is set to true, it applies to child classes.
*/
@Transform(transformer = AnimalTransformer.class, inheritable = true)
abstract class Animal {
    private String sound;

    public abstract String bark();

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}

public class Dog extends Animal {
    @Override
    public String bark() {
        return "Bow-Wow";
    }
}

public class SomeObject {
    private Dog dogField;
}
```

#### 4. Handling Opack Value
```java
OpackObject<String, OpackValue> rootObject = new OpackObject<>();

{
    OpackArray<Integer> opackArray = new OpackArray<>();
    opackArray.add(Integer.MAX_VALUE);
    rootObject.put("array", opackArray);
}

{
    OpackArray opackArray = OpackArray.createWithArrayObject(new int[]{1, 2, 3, 4, 5, 6});
    rootObject.put("unmodifiable(but, really fast) array", opackArray);
}

{
    OpackObject opackObject = new OpackObject<>();
    opackObject.put("int", 1);
    opackObject.put("float", 1.1f);
    opackObject.put("long", Long.MAX_VALUE);
    opackObject.put("double", 1.1d);

    opackObject.put(1024, "2^10");
    opackObject.put(
            OpackArray.createWithArrayObject(new byte[]{1,2,3,4,5}),
            "a lot of bytes"
    );

    rootObject.put("number_map", opackObject);
}

OpackArray opackArray = (OpackArray) rootObject.get("array");
OpackObject opackObject = (OpackObject) rootObject.get("number_map");

System.out.println("1024 is " + (opackObject.get(1024)));
System.out.println("Array length is " + (opackArray.length()));
System.out.println("First element is " + (opackArray.get(0)));
```


### Download

Gradle:
```gradle
dependencies {
  implementation 'io.github.realtimetech-solution:opack:<release_version>'
}
```

Maven:
```xml
<dependency>
  <groupId>io.github.realtimetech-solution</groupId>
  <artifactId>opack</artifactId>
  <version><release_version></version>
</dependency>
```

### License

Opack uses [Apache License 2.0](./LICENSE.txt). Please, leave your feedback if you have any suggestions!

```
JeongHwan, Park
+821032735003
dev.parkjeonghwan@gmail.com
```
