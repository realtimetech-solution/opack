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
