# opack
Fast object or data serialize and deserialize library

``` Java
OpackValue
	OpackMap	(HashMap<String, OpackValue>)
	OpackArray	(Array<OpackValue>)
	OpackString
	OpackNumber


Opacker opacker = Opacker.Builder.create()
				.set~~~()
				.set~~~()
				.set~~~()
				.build();

OpackValue v = opacker.serialize(someObject);
object o = opacker.deserialize(object.class, v);

OpackCodec codec = new JsonCodec(true, false, 5, Charset.forName("UTF-8"));
byte[] bytes = codec.encode(v);
OpackValue v = codec.decode(bytes);
```

```

public class ConfuseClassB {
    private String string;
}

@Transformer(transformer=DebugPrinter.class)
public class ConfuseClassA {
    @Ignore
    private byte[] bigData;

    @DeserializeType(type=LinkedList.class)
    private List list;

    @Transformer(transformer=Base64Transformer.class)
    private byte[] bytes;

    private String string;

    private HashMap<Object, Object> map;
    private ConfuseClassB object;
}

Serialize ConfuseClassB
	PUSH_CONST			"string"
	PUSH_FIELD			string
	CREATE_OPACK_STRING
	CREATE_OPACK_OBJECT		1

Serialize ConfuseClassA
	PUSH_CONST			"list"
	PUSH_FIELD			list
	CALL				list.class		

	PUSH_CONST			"string"
	PUSH_FIELD			string
	CREATE_OPACK_STRING		

	PUSH_CONST			"object"
	PUSH_FIELD			object
	CALL				ConfuseClassB

	CREATE_OPACK_OBJECT		3
	
Serialize List	// Hum mmmm Iterating
	PUSH				
	JUMP				0
	ARRAY
```
