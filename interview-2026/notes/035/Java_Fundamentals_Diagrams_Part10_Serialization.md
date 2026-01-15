# Java Language Fundamentals - Complete Diagrams Guide (Part 10: Serialization)

## 💾 Serialization

---

## 1. Java Serialization

### Serialization Process
```
┌─────────────────────────────────────────────────────────────┐
│              Java Serialization                              │
└─────────────────────────────────────────────────────────────┘

    Object in Memory
    ┌──────────────┐
    │  Person      │
    │  - name      │
    │  - age       │
    │  - address   │
    └──────┬───────┘
           │
           │ ObjectOutputStream
           │ .writeObject()
           ▼
    Serialized Bytes
    ┌──────────────┐
    │  Binary data │
    │  (file/network)│
    └──────┬───────┘
           │
           │ ObjectInputStream
           │ .readObject()
           ▼
    Reconstructed Object
    ┌──────────────┐
    │  Person      │
    │  - name      │
    │  - age       │
    │  - address   │
    └──────────────┘
```

### Basic Serialization
```java
// Serializable class
class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int age;
    private transient String password;  // Not serialized
    
    // Constructor, getters, setters
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

// Serialization
Person person = new Person("John", 30);
try (ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream("person.ser"))) {
    oos.writeObject(person);
}

// Deserialization
try (ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream("person.ser"))) {
    Person deserialized = (Person) ois.readObject();
    System.out.println(deserialized.getName());  // "John"
}
```

---

## 2. Serialization Keywords

### Serializable Interface
```
┌─────────────────────────────────────────────────────────────┐
│              Serializable Interface                         │
└─────────────────────────────────────────────────────────────┘

    implements Serializable
    ┌──────────────────────┐
    │  - Marker interface   │
    │  - No methods         │
    │  - Enables serialization│
    └──────────────────────┘
```

### serialVersionUID
```java
class Person implements Serializable {
    // Version control
    private static final long serialVersionUID = 1L;
    
    private String name;
    private int age;
    
    // If class changes, update serialVersionUID
    // Prevents InvalidClassException
}
```

### transient Keyword
```java
class Person implements Serializable {
    private String name;
    private int age;
    
    // Field not serialized
    private transient String password;
    private transient Date lastLogin;  // Sensitive data
    
    // Static fields are automatically not serialized
    private static int count = 0;
}
```

### Custom Serialization
```java
class Person implements Serializable {
    private String name;
    private int age;
    
    // Custom write
    private void writeObject(ObjectOutputStream oos) 
            throws IOException {
        oos.defaultWriteObject();  // Default serialization
        // Custom logic
        oos.writeObject(name.toUpperCase());
    }
    
    // Custom read
    private void readObject(ObjectInputStream ois) 
            throws IOException, ClassNotFoundException {
        ois.defaultReadObject();  // Default deserialization
        // Custom logic
        name = ((String) ois.readObject()).toLowerCase();
    }
}
```

---

## 3. Externalizable

### Externalizable Interface
```
┌─────────────────────────────────────────────────────────────┐
│              Externalizable                                 │
└─────────────────────────────────────────────────────────────┘

    implements Externalizable
    ┌──────────────────────┐
    │  - Full control       │
    │  - writeExternal()   │
    │  - readExternal()    │
    │  - Must have no-arg   │
    │    constructor       │
    └──────────────────────┘
```

### Externalizable Example
```java
class Person implements Externalizable {
    private String name;
    private int age;
    
    // Required: no-arg constructor
    public Person() {}
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(name);
        out.writeInt(age);
    }
    
    @Override
    public void readExternal(ObjectInput in) 
            throws IOException, ClassNotFoundException {
        name = in.readUTF();
        age = in.readInt();
    }
}
```

---

## 4. JSON Serialization

### JSON Libraries
```
┌─────────────────────────────────────────────────────────────┐
│              JSON Serialization                             │
└─────────────────────────────────────────────────────────────┘

    Object
    ┌──────┐
    │Person│
    └──┬───┘
       │
       │ JSON Library
       │
    ┌──┴───┐
    │      │
Jackson  Gson
    │      │
    │      │
    ▼      ▼
    JSON String
    {"name":"John","age":30}
```

### Jackson Example
```java
// Add dependency: com.fasterxml.jackson.core:jackson-databind

ObjectMapper mapper = new ObjectMapper();

// Serialize to JSON
Person person = new Person("John", 30);
String json = mapper.writeValueAsString(person);
// {"name":"John","age":30}

// Deserialize from JSON
Person deserialized = mapper.readValue(json, Person.class);

// With annotations
class Person {
    @JsonProperty("full_name")
    private String name;
    
    @JsonIgnore
    private String password;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;
}
```

### Gson Example
```java
// Add dependency: com.google.code.gson:gson

Gson gson = new Gson();

// Serialize to JSON
Person person = new Person("John", 30);
String json = gson.toJson(person);
// {"name":"John","age":30}

// Deserialize from JSON
Person deserialized = gson.fromJson(json, Person.class);

// With annotations
class Person {
    @SerializedName("full_name")
    private String name;
    
    @Expose(serialize = false)
    private String password;
}
```

---

## 5. XML Serialization

### JAXB (Java 11+)
```java
// JAXB removed in Java 11, use external library

// Add dependency: jakarta.xml.bind:jakarta.xml.bind-api

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class Person {
    @XmlElement
    private String name;
    
    @XmlElement
    private int age;
}

// Serialize
JAXBContext context = JAXBContext.newInstance(Person.class);
Marshaller marshaller = context.createMarshaller();
marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
marshaller.marshal(person, new File("person.xml"));

// Deserialize
Unmarshaller unmarshaller = context.createUnmarshaller();
Person deserialized = (Person) unmarshaller.unmarshal(new File("person.xml"));
```

---

## 6. Protocol Buffers

### Protocol Buffers
```
┌─────────────────────────────────────────────────────────────┐
│              Protocol Buffers                               │
└─────────────────────────────────────────────────────────────┘

    .proto Definition
    ┌──────────────┐
    │  message     │
    │  Person {    │
    │    string    │
    │    name = 1; │
    │    int32     │
    │    age = 2;  │
    │  }           │
    └──────┬───────┘
           │
           │ protoc compiler
           ▼
    Generated Java Class
    ┌──────────────┐
    │  Person      │
    │  .toByteArray()│
    │  .parseFrom() │
    └──────────────┘
```

### Protocol Buffers Example
```java
// person.proto
// syntax = "proto3";
// message Person {
//     string name = 1;
//     int32 age = 2;
// }

// Generated code usage
Person person = Person.newBuilder()
    .setName("John")
    .setAge(30)
    .build();

// Serialize
byte[] bytes = person.toByteArray();

// Deserialize
Person deserialized = Person.parseFrom(bytes);
```

---

## 7. Serialization Best Practices

### Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Serialization Best Practices                   │
└─────────────────────────────────────────────────────────────┘

✅ Do:
    - Implement Serializable
    - Define serialVersionUID
    - Mark sensitive fields as transient
    - Use custom serialization for complex objects
    - Consider JSON/XML for interoperability
    - Validate deserialized objects

❌ Don't:
    - Serialize sensitive data
    - Serialize large objects
    - Rely on default serialization for complex hierarchies
    - Forget to update serialVersionUID
    - Serialize inner classes
```

### Security Considerations
```java
// ❌ Bad: Serializing sensitive data
class User implements Serializable {
    private String username;
    private String password;  // Should be transient!
}

// ✅ Good: Mark sensitive fields as transient
class User implements Serializable {
    private String username;
    private transient String password;  // Not serialized
}

// ✅ Good: Validate deserialized objects
private void readObject(ObjectInputStream ois) 
        throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    // Validate
    if (age < 0 || age > 150) {
        throw new InvalidObjectException("Invalid age");
    }
}
```

---

## 8. Serialization Comparison

### Comparison Table
```
┌─────────────────────────────────────────────────────────────┐
│              Serialization Comparison                       │
└─────────────────────────────────────────────────────────────┘

Java Serialization:
    - Built-in
    - Binary format
    - Fast
    - Java-specific
    - Security concerns

JSON:
    - Human-readable
    - Language-agnostic
    - Widely supported
    - Larger size
    - Slower than binary

XML:
    - Human-readable
    - Verbose
    - Standardized
    - Large size
    - Slower

Protocol Buffers:
    - Compact binary
    - Fast
    - Language-agnostic
    - Requires schema
    - Good for RPC
```

---

## Key Concepts Summary

### Serialization Summary
```
Java Serialization:
- implements Serializable
- serialVersionUID for versioning
- transient for non-serializable fields
- Custom readObject/writeObject

JSON:
- Human-readable
- Jackson or Gson
- @JsonProperty, @JsonIgnore

XML:
- JAXB (external library)
- @XmlRootElement, @XmlElement

Protocol Buffers:
- Compact binary
- Schema-based
- Cross-language support
- .proto files

Best Practices:
- Mark sensitive data as transient
- Define serialVersionUID
- Validate deserialized objects
- Consider format based on use case
```

---

**This completes all 10 parts of Java Language Fundamentals!**

**Summary:**
- Part 1: Java Versions & OOP
- Part 2: SOLID Principles
- Part 3: Generics & Type Safety
- Part 4: Collections Framework
- Part 5: Concurrency & Multithreading
- Part 6: Lambda & Functional Programming
- Part 7: Annotations & Reflection
- Part 8: Exception Handling
- Part 9: I/O & NIO
- Part 10: Serialization

All diagrams are in ASCII/text format with code examples! 🚀

