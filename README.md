<h1 align="center" style="max-width: 100%; font-weight: bold;">
  <a href="https://github.com/realtimetech-solution/opack"><img width="100px" src=".readme/logo.png" style="max-width: 100%;" alt="logo"/></a><br/>
  Opack
</h1>

<p align="center" style="max-width: 100%;">
  <a href="#"><img src="https://img.shields.io/github/license/realtimetech-solution/opack" alt="license"/></a>
  <a href="https://maven-badges.herokuapp.com/maven-central/com.realtimetech/opack"><img src="https://maven-badges.herokuapp.com/maven-central/com.realtimetech/opack/badge.svg" alt="maven-central-version"/></a>
</p>

Opack is a Java library that can serialize/deserialize between Java objects and common objects(OpackValue). Also, common objects can be encoded or decoded as JSON or Bytes(Dense).

**We faster than GSON and Kryo and Jackson.** (See [tests](./src/test/java/com/realtimetech/opack/test/performance))
<details>
  <summary>Click to see performance benchmark result</summary>

```
# GsonPerformanceTest
	Gson(T)	: 4441ms
	Gson(D)	: 2839ms
	Opack  	: 2756ms

# KryoPerformanceTest
	Kryo 	: 3110ms
	Opack	: 688ms

# JacksonPerformanceTest
	Jackson	: 4626ms
	Opack  	: 3750ms
```

</details>

### Simple flow

<p align="center" style="max-width: 100%;">
  <a href="#"><img width="484" src=".readme/1_serialize_deserialize.png" alt="sample_1"/></a>
</p>

<p align="center" style="max-width: 100%;">
  <a href="#"><img width="484" src=".readme/2_encode_decode.png" alt="sample_2"/></a>
</p>

### Download

Gradle:

```gradle
dependencies {
  implementation 'com.realtimetech:opack:0.2.0'
}
```

Maven:

```xml

<dependency>
    <groupId>com.realtimetech</groupId>
    <artifactId>opack</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Usage

#### 1. Serialize

```java
public class Usage {
    public static void main(String[] arguments) {
        Opacker opacker = Opacker.Builder.create().build();

        SomeObject someObject = new SomeObject();

        OpackValue opackValue = opacker.serialize(someObject);
    }
}
```

#### 2. Deserialize

```java
public class Usage {
    public static void main(String[] arguments) {
        Opacker opacker = Opacker.Builder.create()
                .setContextStackInitialSize(128)                    // (Optional) Creation size of stack for processing
                .setValueStackInitialSize(512)                      // (Optional) Creation size of stack for processing

                .setEnableWrapListElementType(false)                // (Optional) When converting elements of a list, record the type as well
                .setEnableWrapMapElementType(false)                 // (Optional) When converting elements of a map, record the type as well
                .setEnableConvertEnumToOrdinal(false)               // (Optional) Convert enum to ordinal or name
                .setEnableConvertRecursiveDependencyToNull(false)   // (Optional) Convert recursive depandency, record null

                .setClassLoader(Usage.class.getClassLoader())       // (Optional) Class loader for processing
                .build();

        OpackValue serializedSomeObject = null;

        SomeObject someObject = opacker.deserialize(SomeObject.class, serializedSomeObject);
    }
}
```

#### 3. Json Codec

##### General Usage

```java
public class Usage {
    public static void main(String[] arguments) {
        JsonCodec jsonCodec = JsonCodec.Builder.create()
                .setEncodeStackInitialSize(128)             // (Optional) Creation size of stack for processing
                .setEncodeStringBufferSize(1024)            // (Optional) Creation size of stack for processing
                .setDecodeStackInitialSize(128)             // (Optional) Creation size of stack for processing

                .setAllowAnyValueToKey(false)               // (Optional) Accepts non-string value as Key of Json Object
                .setEnableConvertCharacterToString(false)   // (Optional) Convert character to string instead of character int value
                .setUsePrettyFormat(false)                  // (Optional) When encoding, it prints formatted

                .build();

        OpackValue opackValue;

        // Encode Basic
        String json = jsonCodec.encode(opackValue);

        // Encode with Java IO Writer
        Writer writer;
        jsonCodec.encode(writer, opackValue);

        // Decode Basic
        OpackValue decodedOpackValue = jsonCodec.decode(json);
    }
}
```

##### Easy Usage

```java
public class Usage {
    public static void main(String[] arguments) {
        OpackValue opackValue;

        // Encode
        String json = Json.encode(opackValue);

        // Decode
        OpackValue decodedOpackValue = Json.decode(json);
    }
}
```

#### 4. Dense Codec

```java
public class Usage {
    public static void main(String[] arguments) {
        DenseCodec denseCodec = DenseCodec.Builder.create()
                .setEncodeStackInitialSize(128)         // (Optional) Creation size of stack for processing
                .setDecodeStackInitialSize(128)         // (Optional) Creation size of stack for processing

                .setIgnoreVersionCompare(false)         // (Optional) Ignore compare dense codec version in data

                .build();

        OpackValue opackValue;

        // Encode Basic
        byte[] bytes = denseCodec.encode(opackValue);

        // Encode with Java IO OutputStream
        OutputStream outputStream;
        denseCodec.encode(OutputStreamWriter.of(outputStream), opackValue);

        // Encode with ByteArrayWriter
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter();
        denseCodec.encode(byteArrayWriter, opackValue);
        byte[] bytes = byteArrayWriter.toByteArray();

        // Decode Basic
        OpackValue decodedOpackValue = denseCodec.decode(bytes);

        // Decode with Java IO InputStream
        InputStream inputStream;
        OpackValue decodedOpackValue = denseCodec.decode(InputStreamReader.of(inputStream));

        // Decode with ByteArrayReader
        ByteArrayReader byteArrayReader = new ByteArrayReader(bytes);
        OpackValue decodedOpackValue = denseCodec.decode(byteArrayReader);
    }
}
```

### Advanced Usage

#### 1. Ignore and Type and Name

```java
public class SomeObject {
    private String stringField;
    private byte[] bytesField;

    // This field will not serialize/deserialize
    @Ignore
    private String verySecretField;

    // This field will serialize/deserialize to explicit type `ArrayList` instead of ambiguous field type `List`
    @Type(ArrayList.class)
    private List<String> listField;

    // This field will serialize/deserialize to `newFieldName` name instead of actual field name `oldFieldName`
    @Name("newFieldName")
    private String oldFieldName;
}
```

#### 2. Field Transformer

```java
public class ByteToBase64Transformer implements Transformer {
    @Override
    public @Nullable Object serialize(@NotNull Opacker opacker, @NotNull Class<?> originalType, @Nullable Object object) throws SerializeException {
        if (object instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) object);
        }

        return object;
    }

    @Override
    public @Nullable Object deserialize(@NotNull Opacker opacker, @NotNull Class<?> goalType, @Nullable Object object) throws DeserializeException {
        if (object instanceof String) {
            return Base64.getDecoder().decode((String) object);
        }

        return object;
    }
}

public class SomeObject {
    // This field will serialize/deserialize to Base64
    @Transform(transformer = ByteToBase64Transformer.class)
    private byte[] bytesField;
}
```

#### 3. Field With Type

```java
public class SomeObject {
    // This field will serialize with runtime type, and deserialize actual type instead of ambiguous field type `List`
    @WithType
    private List<String> stringListField;

    // This field will serialize with runtime type, and deserialize actual type instead of ambiguous field type `Object`
    @WithType
    private Object[] objectArrayField;
}
```

#### 4. Class Transformer

```java
public class AnimalTransformer implements Transformer {
    // Remove a `sound` from a serialized `Animal`
    @Override
    public @Nullable Object serialize(@NotNull Opacker opacker, @NotNull Class<?> originalType, @Nullable Object object) throws SerializeException {
        if (object instanceof Animal) {
            Animal animal = (Animal) object;
            OpackValue opackValue = opacker.serialize(animal);

            if (opackValue instanceof OpackObject) {
                OpackObject opackObject = (OpackObject) opackValue;
                opackObject.remove("sound");
                return opackObject;
            }
        }

        return object;
    }

    // Restore `sound` from `Animal` before deserialization
    @Override
    public @Nullable Object deserialize(@NotNull Opacker opacker, @NotNull Class<?> goalType, @Nullable Object object) throws DeserializeException {
        if (object instanceof OpackObject) {
            if (Animal.class.isAssignableFrom(goalType)) {
                OpackObject opackObject = (OpackObject) object;
                Animal animal = (Animal) opacker.deserialize(goalType, opackObject);
                animal.setSound(animal.bark());
            }
        }

        return object;
    }
}

// When `inheritable` is set to true, it applies to child classes.
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

#### 5. Handling Opack Value

```java
public class Usage {
    public static void main(String[] arguments) {
        OpackObject rootObject = new OpackObject();

        {
            OpackArray opackArray = new OpackArray();
            opackArray.add(Integer.MAX_VALUE);
            rootObject.put("array", opackArray);
        }

        {
            OpackArray opackArray = OpackArray.createWithArrayObject(new int[]{1, 2, 3, 4, 5, 6});
            rootObject.put("unmodifiable(but, really fast) array", opackArray);
        }

        {
            OpackObject opackObject = new OpackObject();
            opackObject.put("int", 1);
            opackObject.put("float", 1.1f);
            opackObject.put("long", Long.MAX_VALUE);
            opackObject.put("double", 1.1d);

            opackObject.put(1024, "2^10");
            opackObject.put(
                    OpackArray.createWithArrayObject(new byte[]{1, 2, 3, 4, 5}),
                    "a lot of bytes"
            );

            rootObject.put("number_map", opackObject);
        }

        OpackArray opackArray = (OpackArray) rootObject.get("array");
        OpackObject opackObject = (OpackObject) rootObject.get("number_map");

        System.out.println("1024 is " + (opackObject.get(1024)));
        System.out.println("Array length is " + (opackArray.length()));
        System.out.println("First element is " + (opackArray.get(0)));
    }
}
```

### To-Do

- [ ] Separate field transformer and class transformer
- [ ] Add generic into the transformer for type safety
- [ ] Add field pre/post transformer
- [ ] Remove `fieldTransformer` argument of `Opacker.prepareObjectDeserialize`
- [ ] Remove `withType` argument of `Opacker.prepareObjectDeserialize`

### License

Opack uses [Apache License 2.0](./LICENSE). Please leave your feedback if you have any suggestions!

```
Jeonghwan, Park
+821032735003
dev.parkjeonghwan@gmail.com
```