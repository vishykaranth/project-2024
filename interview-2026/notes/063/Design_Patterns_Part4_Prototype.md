# Prototype Pattern: Object Cloning, Deep vs Shallow Copy

## Overview

The Prototype pattern creates new objects by cloning existing instances (prototypes) rather than creating them from scratch. This is useful when object creation is expensive or when you need to create objects that are similar to existing ones.

## Prototype Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Prototype Pattern Structure                 │
└─────────────────────────────────────────────────────────┘

        Prototype (Interface)
    ┌──────────────────────┐
    │ + clone(): Prototype  │
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │                       │
    ▼                       ▼
ConcretePrototype1    ConcretePrototype2
    │                       │
    ├─► clone()             ├─► clone()
    │   returns copy        │   returns copy
    │                       │
    ▼                       ▼
    New Instance        New Instance
```

## Basic Prototype Implementation

### Java Cloneable Interface

```java
// Prototype interface
public interface Prototype extends Cloneable {
    Prototype clone();
}

// Concrete Prototype
public class ConcretePrototype implements Prototype {
    private String field1;
    private int field2;
    
    public ConcretePrototype(String field1, int field2) {
        this.field1 = field1;
        this.field2 = field2;
    }
    
    // Copy constructor
    public ConcretePrototype(ConcretePrototype prototype) {
        this.field1 = prototype.field1;
        this.field2 = prototype.field2;
    }
    
    @Override
    public Prototype clone() {
        return new ConcretePrototype(this);
    }
    
    // Getters and setters
    public String getField1() { return field1; }
    public void setField1(String field1) { this.field1 = field1; }
    // ...
}

// Usage
ConcretePrototype original = new ConcretePrototype("value", 10);
ConcretePrototype copy = (ConcretePrototype) original.clone();
```

## Shallow Copy vs Deep Copy

### Shallow Copy

A shallow copy creates a new object but copies references to nested objects, not the objects themselves.

```
┌─────────────────────────────────────────────────────────┐
│              Shallow Copy Illustration                  │
└─────────────────────────────────────────────────────────┘

Original Object
┌─────────────────┐
│ field1: "A"     │
│ field2: 10      │
│ nested: ───────┼───┐
└─────────────────┘   │
                      │
                      ▼
              ┌───────────────┐
              │ nested object │
              │ data: "B"      │
              └───────────────┘
                      ▲
                      │
Copy Object          │
┌─────────────────┐   │
│ field1: "A"     │   │
│ field2: 10      │   │
│ nested: ───────┼───┘  (Same reference!)
└─────────────────┘
```

### Deep Copy

A deep copy creates a new object and recursively copies all nested objects.

```
┌─────────────────────────────────────────────────────────┐
│              Deep Copy Illustration                     │
└─────────────────────────────────────────────────────────┘

Original Object
┌─────────────────┐
│ field1: "A"     │
│ field2: 10      │
│ nested: ───────┼───┐
└─────────────────┘   │
                      │
                      ▼
              ┌───────────────┐
              │ nested object │
              │ data: "B"      │
              └───────────────┘

Copy Object
┌─────────────────┐
│ field1: "A"     │
│ field2: 10      │
│ nested: ───────┼───┐
└─────────────────┘   │
                      │
                      ▼
              ┌───────────────┐
              │ nested object │  (New object!)
              │ data: "B"      │
              └───────────────┘
```

## Shallow Copy Example

```java
public class ShallowCopyExample implements Cloneable {
    private String name;
    private List<String> items;  // Reference type
    
    public ShallowCopyExample(String name, List<String> items) {
        this.name = name;
        this.items = items;
    }
    
    @Override
    public ShallowCopyExample clone() {
        try {
            // Shallow copy - items list is shared!
            return (ShallowCopyExample) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    // Getters
    public String getName() { return name; }
    public List<String> getItems() { return items; }
}

// Usage
List<String> originalList = new ArrayList<>();
originalList.add("item1");
originalList.add("item2");

ShallowCopyExample original = new ShallowCopyExample("Original", originalList);
ShallowCopyExample copy = original.clone();

// Modify copy's list
copy.getItems().add("item3");

// Original is also affected! (Same reference)
System.out.println(original.getItems());  // [item1, item2, item3]
System.out.println(copy.getItems());      // [item1, item2, item3]
```

## Deep Copy Example

```java
public class DeepCopyExample implements Cloneable {
    private String name;
    private List<String> items;
    private NestedObject nested;
    
    public DeepCopyExample(String name, List<String> items, NestedObject nested) {
        this.name = name;
        this.items = new ArrayList<>(items);  // Copy list
        this.nested = nested;
    }
    
    @Override
    public DeepCopyExample clone() {
        // Deep copy - create new instances
        List<String> copiedItems = new ArrayList<>(this.items);
        NestedObject copiedNested = this.nested.clone();
        return new DeepCopyExample(this.name, copiedItems, copiedNested);
    }
    
    // Getters
    public String getName() { return name; }
    public List<String> getItems() { return items; }
    public NestedObject getNested() { return nested; }
}

class NestedObject implements Cloneable {
    private String data;
    
    public NestedObject(String data) {
        this.data = data;
    }
    
    @Override
    public NestedObject clone() {
        return new NestedObject(this.data);
    }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}

// Usage
List<String> originalList = new ArrayList<>();
originalList.add("item1");
NestedObject nested = new NestedObject("nested data");

DeepCopyExample original = new DeepCopyExample("Original", originalList, nested);
DeepCopyExample copy = original.clone();

// Modify copy
copy.getItems().add("item2");
copy.getNested().setData("modified");

// Original is NOT affected (Different objects)
System.out.println(original.getItems());  // [item1]
System.out.println(copy.getItems());     // [item1, item2]
```

## Prototype Registry

A registry that stores and manages prototypes.

```java
// Prototype Registry
public class PrototypeRegistry {
    private Map<String, Prototype> prototypes = new HashMap<>();
    
    public void register(String key, Prototype prototype) {
        prototypes.put(key, prototype);
    }
    
    public Prototype get(String key) {
        Prototype prototype = prototypes.get(key);
        if (prototype == null) {
            throw new IllegalArgumentException("Prototype not found: " + key);
        }
        return prototype.clone();
    }
}

// Usage
PrototypeRegistry registry = new PrototypeRegistry();

// Register prototypes
registry.register("user", new User("John", "john@example.com"));
registry.register("admin", new User("Admin", "admin@example.com"));

// Create instances from prototypes
User user1 = (User) registry.get("user");
User user2 = (User) registry.get("user");  // New instance
```

## Real-World Examples

### 1. Game Object Cloning

```java
public abstract class GameObject implements Cloneable {
    protected String name;
    protected int x, y;
    
    public GameObject(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }
    
    public abstract GameObject clone();
    public abstract void render();
}

public class Enemy extends GameObject {
    private int health;
    private String weapon;
    
    public Enemy(String name, int x, int y, int health, String weapon) {
        super(name, x, y);
        this.health = health;
        this.weapon = weapon;
    }
    
    @Override
    public Enemy clone() {
        return new Enemy(this.name, this.x, this.y, this.health, this.weapon);
    }
    
    @Override
    public void render() {
        System.out.println("Rendering enemy: " + name);
    }
}

// Usage - Spawn multiple enemies from prototype
Enemy prototype = new Enemy("Orc", 0, 0, 100, "Sword");
Enemy enemy1 = prototype.clone();
enemy1.setX(10);
Enemy enemy2 = prototype.clone();
enemy2.setX(20);
```

### 2. Document Template Cloning

```java
public class Document implements Cloneable {
    private String title;
    private String content;
    private List<String> tags;
    private Metadata metadata;
    
    public Document(String title, String content) {
        this.title = title;
        this.content = content;
        this.tags = new ArrayList<>();
        this.metadata = new Metadata();
    }
    
    @Override
    public Document clone() {
        // Deep copy
        Document clone = new Document(this.title, this.content);
        clone.tags = new ArrayList<>(this.tags);
        clone.metadata = this.metadata.clone();
        return clone;
    }
    
    // Getters and setters
    public void addTag(String tag) {
        this.tags.add(tag);
    }
}

// Usage - Create documents from template
Document template = new Document("Template", "Default content");
template.addTag("template");

Document doc1 = template.clone();
doc1.setTitle("Document 1");
doc1.setContent("Custom content 1");

Document doc2 = template.clone();
doc2.setTitle("Document 2");
doc2.setContent("Custom content 2");
```

## When to Use Shallow vs Deep Copy

### Use Shallow Copy When:
- Objects contain only primitive types
- Nested objects are immutable
- Performance is critical
- You want to share references

### Use Deep Copy When:
- Objects contain mutable nested objects
- You need independent copies
- Modifications to copy shouldn't affect original
- Data integrity is important

## Best Practices

### 1. Implement Cloneable Carefully

```java
public class GoodClone implements Cloneable {
    private String field;
    private List<String> items;
    
    @Override
    public GoodClone clone() {
        try {
            GoodClone clone = (GoodClone) super.clone();
            // Deep copy mutable fields
            clone.items = new ArrayList<>(this.items);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 2. Use Copy Constructor as Alternative

```java
public class UsingCopyConstructor {
    private String field;
    private List<String> items;
    
    // Copy constructor
    public UsingCopyConstructor(UsingCopyConstructor other) {
        this.field = other.field;
        this.items = new ArrayList<>(other.items);  // Deep copy
    }
}

// Usage
UsingCopyConstructor original = new UsingCopyConstructor(...);
UsingCopyConstructor copy = new UsingCopyConstructor(original);
```

### 3. Use Serialization for Deep Copy

```java
public class DeepCopyViaSerialization {
    public static <T extends Serializable> T deepCopy(T object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }
}
```

## Summary

Prototype Pattern:
- **Purpose**: Create objects by cloning existing instances
- **Types**: Shallow copy (shared references) vs Deep copy (independent objects)
- **Use Cases**: Expensive object creation, similar objects, templates
- **Benefits**: Performance, flexibility, object creation abstraction

**Key Takeaways:**
- ✅ Use when object creation is expensive
- ✅ Shallow copy shares references (faster)
- ✅ Deep copy creates independent objects (safer)
- ✅ Consider prototype registry for managing prototypes
- ✅ Be careful with mutable nested objects

**Remember**: Choose shallow or deep copy based on your needs - shallow is faster but deep is safer for mutable objects!
