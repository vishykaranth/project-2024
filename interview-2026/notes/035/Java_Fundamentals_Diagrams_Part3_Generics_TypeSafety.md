# Java Language Fundamentals - Complete Diagrams Guide (Part 3: Generics & Type Safety)

## 🔒 Generics & Type Safety

---

## 1. Generics Overview

### Why Generics?
```
┌─────────────────────────────────────────────────────────────┐
│              Problem Before Generics                        │
└─────────────────────────────────────────────────────────────┘

❌ Without Generics:
    List list = new ArrayList();
    list.add("Hello");
    list.add(123);  // No type checking
    String str = (String) list.get(0);  // Cast required
    Integer num = (Integer) list.get(1);  // Runtime error possible

✅ With Generics:
    List<String> list = new ArrayList<>();
    list.add("Hello");
    list.add(123);  // Compile-time error!
    String str = list.get(0);  // No cast needed
```

### Generics Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              Generics Benefits                              │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │   Type Safety │
    │   (Compile-time)│
    └──────┬───────┘
           │
    ┌──────┴───────┐
    │              │
┌───┴────┐    ┌────┴────┐
│No Casts│    │Code     │
│Needed  │    │Reuse    │
└────────┘    └─────────┘
```

---

## 2. Generic Classes

### Generic Class Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Generic Class                                  │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │  class Box<T>        │
    │  {                   │
    │    private T item;   │
    │                      │
    │    public void       │
    │    setItem(T item)   │
    │    {                 │
    │      this.item = item;│
    │    }                 │
    │                      │
    │    public T          │
    │    getItem()         │
    │    {                 │
    │      return item;    │
    │    }                 │
    │  }                   │
    └──────────────────────┘
           │
           │ T = Type Parameter
           │
    ┌──────┴───────┐
    │              │
Box<String>    Box<Integer>
```

### Generic Class Example
```java
// Generic class definition
class Box<T> {
    private T item;
    
    public void setItem(T item) {
        this.item = item;
    }
    
    public T getItem() {
        return item;
    }
}

// Usage
Box<String> stringBox = new Box<>();
stringBox.setItem("Hello");
String value = stringBox.getItem();  // No cast needed

Box<Integer> intBox = new Box<>();
intBox.setItem(123);
Integer number = intBox.getItem();  // Type safe
```

### Multiple Type Parameters
```java
// Multiple type parameters
class Pair<K, V> {
    private K key;
    private V value;
    
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    public K getKey() { return key; }
    public V getValue() { return value; }
}

// Usage
Pair<String, Integer> pair = new Pair<>("Age", 25);
String key = pair.getKey();
Integer value = pair.getValue();
```

---

## 3. Generic Methods

### Generic Method Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Generic Method                                 │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────────────┐
    │  public <T> T                │
    │  methodName(T param)          │
    │  {                            │
    │    return param;              │
    │  }                            │
    └──────────────────────────────┘
           │
           │ <T> = Type parameter
           │      (before return type)
           │
    ┌──────┴───────┐
    │              │
methodName("str")  methodName(123)
(String)          (Integer)
```

### Generic Method Example
```java
class Utils {
    // Generic method
    public static <T> T getFirst(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
    
    // Generic method with multiple types
    public static <T, U> U convert(T input, Function<T, U> converter) {
        return converter.apply(input);
    }
}

// Usage
List<String> strings = Arrays.asList("a", "b", "c");
String first = Utils.getFirst(strings);  // Type inferred

List<Integer> numbers = Arrays.asList(1, 2, 3);
Integer firstNum = Utils.getFirst(numbers);  // Type inferred
```

---

## 4. Bounded Type Parameters

### Bounded Types
```
┌─────────────────────────────────────────────────────────────┐
│              Bounded Type Parameters                        │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │  <T extends Number>  │
    │                      │
    │  Upper Bound         │
    │  (T must be Number    │
    │   or its subclass)    │
    └──────────────────────┘
           │
           │
    ┌──────┴───────┐
    │              │
Integer        Double
(Valid)        (Valid)

String
(Invalid - not Number)
```

### Upper Bound Example
```java
// Upper bound: T must be Number or its subclass
class NumberBox<T extends Number> {
    private T number;
    
    public NumberBox(T number) {
        this.number = number;
    }
    
    public double getDoubleValue() {
        return number.doubleValue();  // Can call Number methods
    }
}

// Valid usage
NumberBox<Integer> intBox = new NumberBox<>(10);
NumberBox<Double> doubleBox = new NumberBox<>(10.5);
// NumberBox<String> stringBox = new NumberBox<>("test");  // Compile error!
```

### Multiple Bounds
```java
// Multiple bounds
class MultiBound<T extends Number & Comparable<T>> {
    private T value;
    
    public int compareTo(T other) {
        return value.compareTo(other);  // Can use both Number and Comparable
    }
}

// T must be Number AND Comparable
```

---

## 5. Wildcards

### Wildcard Types
```
┌─────────────────────────────────────────────────────────────┐
│              Wildcard Types                                 │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │  List<?>             │
    │  (Unbounded)         │
    └──────────────────────┘
           │
    ┌──────┴───────┐
    │              │
┌───┴────┐    ┌────┴────┐
│List<?  │    │List<?   │
│extends │    │super    │
│Number> │    │Number>  │
│(Upper) │    │(Lower)  │
└────────┘    └─────────┘
```

### Unbounded Wildcard
```java
// Unbounded wildcard: List<?>
void printList(List<?> list) {
    for (Object item : list) {
        System.out.println(item);
    }
    // Can read, but cannot add (except null)
    // list.add("test");  // Compile error!
    list.add(null);  // OK
}

// Usage
List<String> strings = Arrays.asList("a", "b");
List<Integer> numbers = Arrays.asList(1, 2);
printList(strings);  // Works
printList(numbers);  // Works
```

### Upper Bounded Wildcard
```java
// Upper bounded: ? extends Number
void processNumbers(List<? extends Number> numbers) {
    for (Number num : numbers) {
        System.out.println(num.doubleValue());
    }
    // Can read as Number
    // Cannot add (except null)
    // numbers.add(10);  // Compile error!
}

// Usage
List<Integer> integers = Arrays.asList(1, 2, 3);
List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);
processNumbers(integers);  // Works
processNumbers(doubles);    // Works
```

### Lower Bounded Wildcard
```java
// Lower bounded: ? super Integer
void addNumbers(List<? super Integer> numbers) {
    numbers.add(10);  // Can add Integer or its supertypes
    numbers.add(20);
    // Can read as Object
    Object obj = numbers.get(0);
}

// Usage
List<Number> numbers = new ArrayList<>();
List<Object> objects = new ArrayList<>();
addNumbers(numbers);  // Works
addNumbers(objects);  // Works
```

### PECS Principle
```
┌─────────────────────────────────────────────────────────────┐
│              PECS Principle                                 │
└─────────────────────────────────────────────────────────────┘

Producer Extends:
    ┌──────────────────────┐
    │  List<? extends T>   │
    │                      │
    │  - Read only         │
    │  - Produces T        │
    └──────────────────────┘

Consumer Super:
    ┌──────────────────────┐
    │  List<? super T>     │
    │                      │
    │  - Write only        │
    │  - Consumes T        │
    └──────────────────────┘
```

---

## 6. Type Erasure

### Type Erasure Process
```
┌─────────────────────────────────────────────────────────────┐
│              Type Erasure                                   │
└─────────────────────────────────────────────────────────────┘

Compile Time:              Runtime:
┌──────────────┐          ┌──────────────┐
│List<String>  │          │List          │
│List<Integer>│   ────►  │List          │
└──────────────┘          └──────────────┘
    │                          │
    │ Type information          │ Raw types
    │ available                 │ only
    │                          │
    ▼                          ▼
Type checking            No type info
at compile time          at runtime
```

### Type Erasure Example
```java
// Source code
class Box<T> {
    private T item;
    
    public void setItem(T item) {
        this.item = item;
    }
    
    public T getItem() {
        return item;
    }
}

// After type erasure (conceptually)
class Box {
    private Object item;  // T replaced with Object
    
    public void setItem(Object item) {
        this.item = item;
    }
    
    public Object getItem() {
        return item;
    }
}
```

### Bridge Methods
```java
// Source
interface Comparable<T> {
    int compareTo(T other);
}

class String implements Comparable<String> {
    public int compareTo(String other) { ... }
}

// After erasure, compiler adds bridge method
class String implements Comparable {
    public int compareTo(String other) { ... }
    
    // Bridge method (synthetic)
    public int compareTo(Object other) {
        return compareTo((String) other);
    }
}
```

---

## 7. Generic Constraints and Limitations

### Limitations
```
┌─────────────────────────────────────────────────────────────┐
│              Generic Limitations                            │
└─────────────────────────────────────────────────────────────┘

❌ Cannot instantiate generic types with primitives
    List<int> list;  // Error
    ✅ Use: List<Integer>

❌ Cannot create arrays of parameterized types
    List<String>[] array;  // Error
    ✅ Use: List<List<String>>

❌ Cannot use instanceof with parameterized types
    if (obj instanceof List<String>)  // Error
    ✅ Use: if (obj instanceof List)

❌ Cannot create static fields of type parameter
    class Box<T> {
        static T item;  // Error
    }

❌ Cannot catch or throw parameterized types
    catch (Exception<T> e)  // Error
```

---

## 8. Advanced Generics

### Recursive Type Bounds
```java
// Recursive type bound
class ComparableBox<T extends Comparable<T>> {
    private T item;
    
    public boolean isGreaterThan(T other) {
        return item.compareTo(other) > 0;
    }
}

// T must be comparable to itself
```

### Generic Constructors
```java
class Box<T> {
    private T item;
    
    // Generic constructor
    public <U extends T> Box(U item) {
        this.item = item;
    }
}
```

### Generic Interfaces
```java
// Generic interface
interface Container<T> {
    void add(T item);
    T get(int index);
}

// Implementation
class ListContainer<T> implements Container<T> {
    private List<T> items = new ArrayList<>();
    
    @Override
    public void add(T item) {
        items.add(item);
    }
    
    @Override
    public T get(int index) {
        return items.get(index);
    }
}
```

---

## Key Concepts Summary

### Generics Summary
```
Type Parameters:
- <T> - Single type parameter
- <K, V> - Multiple type parameters
- <T extends Number> - Bounded type

Wildcards:
- ? - Unbounded wildcard
- ? extends T - Upper bounded
- ? super T - Lower bounded

PECS:
- Producer: ? extends T
- Consumer: ? super T

Type Erasure:
- Generics removed at runtime
- Type information only at compile time
- Bridge methods added by compiler
```

---

**Next: Part 4 will cover Collections Framework.**

