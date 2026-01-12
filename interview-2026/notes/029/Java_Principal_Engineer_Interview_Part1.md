# Java Principal Engineer Interview Questions - Part 1

## Core Java & JVM Deep Dive

This part covers fundamental Java concepts, JVM internals, memory management, and advanced language features that Principal Engineers must master.

---

## 1. JVM Architecture & Memory Management

### Q1: Explain the JVM architecture in detail. What are the key components and how do they interact?

**Answer:**

The JVM (Java Virtual Machine) consists of several key components:

```java
┌─────────────────────────────────────────┐
│         Class Loader Subsystem          │
│  (Loading, Linking, Initialization)     │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Runtime Data Areas              │
│  ┌──────────┐  ┌──────────┐           │
│  │ Method   │  │  Heap    │           │
│  │  Area    │  │  (Young  │           │
│  │          │  │   & Old) │           │
│  └──────────┘  └──────────┘           │
│  ┌──────────┐  ┌──────────┐           │
│  │  Stack   │  │   PC     │           │
│  │  (per    │  │ Register│           │
│  │ thread)  │  │          │           │
│  └──────────┘  └──────────┘           │
│  ┌──────────┐                        │
│  │ Native   │                        │
│  │ Method   │                        │
│  │  Stack   │                        │
│  └──────────┘                        │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│      Execution Engine                   │
│  ┌──────────┐  ┌──────────┐           │
│  │Interpreter│  │   JIT   │           │
│  │          │  │Compiler  │           │
│  └──────────┘  └──────────┘           │
│  ┌──────────┐                        │
│  │ Garbage  │                        │
│  │Collector │                        │
│  └──────────┘                        │
└─────────────────────────────────────────┘
```

**Key Components:**

1. **Class Loader Subsystem**
   - **Loading**: Reads .class files and creates Class objects
   - **Linking**: Verification, preparation, resolution
   - **Initialization**: Executes static initializers

2. **Runtime Data Areas**
   - **Method Area**: Stores class metadata, static variables, constants
   - **Heap**: Object storage (Young: Eden, Survivor; Old: Tenured)
   - **Stack**: Per-thread stack frames (local variables, method calls)
   - **PC Register**: Current instruction pointer per thread
   - **Native Method Stack**: For native method calls

3. **Execution Engine**
   - **Interpreter**: Executes bytecode line by line
   - **JIT Compiler**: Compiles hot methods to native code
   - **Garbage Collector**: Manages memory automatically

**Example:**
```java
public class JVMExample {
    private static int staticVar = 10;  // Method Area
    private int instanceVar = 20;       // Heap (with object)
    
    public void method() {
        int localVar = 30;              // Stack
        // Method execution uses Execution Engine
    }
}
```

---

### Q2: Explain Java memory model (JMM) and how it ensures thread safety. What are happens-before relationships?

**Answer:**

The Java Memory Model (JMM) defines how threads interact through memory and what behaviors are guaranteed.

**Key Concepts:**

1. **Happens-Before Relationships**
   - Establishes ordering guarantees between operations
   - If A happens-before B, then A's effects are visible to B

2. **Memory Visibility**
   - Changes made by one thread may not be immediately visible to others
   - `volatile`, `synchronized`, `final` provide visibility guarantees

**Happens-Before Rules:**

```java
public class MemoryModelExample {
    private int x = 0;
    private volatile boolean flag = false;
    
    // Thread 1
    public void writer() {
        x = 42;              // 1
        flag = true;         // 2 - volatile write
    }
    
    // Thread 2
    public void reader() {
        if (flag) {          // 3 - volatile read
            // Guaranteed to see x = 42
            // Because: 1 happens-before 2 (program order)
            //          2 happens-before 3 (volatile rule)
            //          Therefore: 1 happens-before 3
            System.out.println(x);  // Always prints 42
        }
    }
}
```

**Happens-Before Relationships:**
- **Program Order**: Actions in same thread are ordered
- **Volatile**: Volatile write happens-before subsequent volatile read
- **Synchronized**: Unlock happens-before subsequent lock
- **Thread Start**: `Thread.start()` happens-before thread's actions
- **Thread Join**: Thread's actions happen-before `join()` returns
- **Final Fields**: Final field initialization happens-before object access

---

### Q3: Explain different garbage collection algorithms (G1, ZGC, Shenandoah). When would you use each?

**Answer:**

**1. G1 Garbage Collector (Garbage First)**

**How it works:**
- Divides heap into regions (typically 1-32MB each)
- Collects regions with most garbage first (garbage-first)
- Uses concurrent marking and incremental collection

**Characteristics:**
```java
// G1 GC Configuration
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200  // Target pause time
-XX:G1HeapRegionSize=16m  // Region size
```

**Use Cases:**
- Large heaps (>4GB)
- Low latency requirements (<200ms pauses)
- Applications with varying allocation rates

**2. ZGC (Z Garbage Collector)**

**How it works:**
- Concurrent collection (most work done while application runs)
- Uses colored pointers for marking
- Extremely low pause times (<10ms even for large heaps)

**Characteristics:**
```java
// ZGC Configuration
-XX:+UseZGC
-XX:+UnlockExperimentalVMOptions  // Java 11-14
// Java 15+: No experimental flag needed
```

**Use Cases:**
- Very large heaps (8GB+)
- Ultra-low latency requirements (<10ms)
- Real-time applications

**3. Shenandoah GC**

**How it works:**
- Concurrent evacuation (moves objects while application runs)
- Uses forwarding pointers
- Low pause times independent of heap size

**Characteristics:**
```java
// Shenandoah Configuration
-XX:+UseShenandoahGC
-XX:ShenandoahGCHeuristics=adaptive
```

**Use Cases:**
- Large heaps with strict latency requirements
- Applications sensitive to GC pauses
- When consistent pause times are critical

**Comparison:**

| GC | Pause Time | Throughput | Heap Size | Use Case |
|----|-----------|------------|-----------|----------|
| **G1** | <200ms | High | 4GB+ | Balanced workloads |
| **ZGC** | <10ms | Medium-High | 8GB+ | Ultra-low latency |
| **Shenandoah** | <10ms | Medium | Any | Consistent pauses |

---

### Q4: Explain class loading mechanism. What is the delegation model? How do you create a custom class loader?

**Answer:**

**Class Loading Process:**

1. **Loading**: Find and load .class file
2. **Linking**: 
   - Verification: Check bytecode validity
   - Preparation: Allocate memory for static variables
   - Resolution: Convert symbolic references to direct references
3. **Initialization**: Execute static initializers

**Delegation Model:**

```java
┌─────────────────┐
│  Application    │
│  ClassLoader    │
└────────┬────────┘
         │ Delegates to parent
┌────────▼────────┐
│  Extension      │
│  ClassLoader    │
└────────┬────────┘
         │ Delegates to parent
┌────────▼────────┐
│  Bootstrap      │
│  ClassLoader    │
└─────────────────┘
```

**Custom Class Loader Example:**

```java
public class CustomClassLoader extends ClassLoader {
    private String classPath;
    
    public CustomClassLoader(String classPath) {
        this.classPath = classPath;
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            // Load class file as byte array
            byte[] classBytes = loadClassFromFile(name);
            
            // Define class from byte array
            return defineClass(name, classBytes, 0, classBytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Class not found: " + name, e);
        }
    }
    
    private byte[] loadClassFromFile(String className) throws IOException {
        String fileName = className.replace('.', File.separatorChar) + ".class";
        File file = new File(classPath, fileName);
        
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }
}

// Usage
CustomClassLoader loader = new CustomClassLoader("/custom/classes");
Class<?> clazz = loader.loadClass("com.example.MyClass");
```

**Use Cases for Custom Class Loaders:**
- Loading classes from network
- Hot deployment/reloading
- Isolating application modules
- Security sandboxing

---

### Q5: Explain Java 8+ features (Streams, Lambda, Optional, CompletableFuture). Provide examples.

**Answer:**

**1. Streams API**

```java
// Before Java 8
List<String> filtered = new ArrayList<>();
for (String s : list) {
    if (s.startsWith("A")) {
        filtered.add(s.toUpperCase());
    }
}

// Java 8 Streams
List<String> filtered = list.stream()
    .filter(s -> s.startsWith("A"))
    .map(String::toUpperCase)
    .collect(Collectors.toList());

// Advanced Stream Operations
Map<String, Long> countByCategory = products.stream()
    .collect(Collectors.groupingBy(
        Product::getCategory,
        Collectors.counting()
    ));

// Parallel Streams
List<String> processed = largeList.parallelStream()
    .filter(this::isValid)
    .map(this::transform)
    .collect(Collectors.toList());
```

**2. Lambda Expressions**

```java
// Functional Interfaces
@FunctionalInterface
public interface Calculator {
    int calculate(int a, int b);
}

// Lambda usage
Calculator add = (a, b) -> a + b;
Calculator multiply = (a, b) -> a * b;

// Method References
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
names.forEach(System.out::println);  // Method reference
names.forEach(name -> System.out.println(name));  // Lambda

// Built-in Functional Interfaces
Predicate<String> isLong = s -> s.length() > 5;
Function<String, Integer> length = String::length;
Consumer<String> printer = System.out::println;
Supplier<String> supplier = () -> "Default";
```

**3. Optional**

```java
// Avoid NullPointerException
public Optional<String> findUser(Long id) {
    User user = userRepository.findById(id);
    return user != null ? Optional.of(user.getName()) : Optional.empty();
}

// Usage
Optional<String> name = findUser(123L);
name.ifPresent(System.out::println);  // Safe operation
String result = name.orElse("Unknown");  // Default value
String result2 = name.orElseGet(() -> getDefaultName());  // Lazy default

// Chaining
Optional<String> result = findUser(123L)
    .map(String::toUpperCase)
    .filter(s -> s.length() > 5)
    .orElse("Default");
```

**4. CompletableFuture (Asynchronous Programming)**

```java
// Basic Usage
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // Long-running operation
    return fetchDataFromDatabase();
});

// Chaining
CompletableFuture<String> result = CompletableFuture
    .supplyAsync(() -> fetchUser(123L))
    .thenApply(user -> processUser(user))
    .thenApply(processed -> saveUser(processed))
    .exceptionally(ex -> handleError(ex));

// Combining Futures
CompletableFuture<String> future1 = fetchData1();
CompletableFuture<String> future2 = fetchData2();

CompletableFuture<String> combined = future1
    .thenCombine(future2, (result1, result2) -> result1 + result2);

// All of / Any of
CompletableFuture<Void> allOf = CompletableFuture.allOf(
    future1, future2, future3
);
allOf.thenRun(() -> {
    // All futures completed
});

CompletableFuture<Object> anyOf = CompletableFuture.anyOf(
    future1, future2, future3
);
```

---

### Q6: Explain reflection API. What are its use cases and limitations?

**Answer:**

**Reflection allows runtime inspection and modification of classes, methods, fields.**

**Basic Usage:**

```java
public class ReflectionExample {
    public static void main(String[] args) throws Exception {
        // Get Class object
        Class<?> clazz = Class.forName("com.example.User");
        
        // Create instance
        Constructor<?> constructor = clazz.getConstructor(String.class, int.class);
        Object user = constructor.newInstance("John", 30);
        
        // Access fields
        Field nameField = clazz.getDeclaredField("name");
        nameField.setAccessible(true);  // Access private field
        String name = (String) nameField.get(user);
        nameField.set(user, "Jane");
        
        // Invoke methods
        Method getName = clazz.getMethod("getName");
        String result = (String) getName.invoke(user);
        
        // Get annotations
        Annotation[] annotations = clazz.getAnnotations();
    }
}
```

**Use Cases:**

1. **Frameworks**: Spring, Hibernate use reflection for dependency injection
2. **Testing**: Mock frameworks, test frameworks
3. **Serialization**: JSON/XML libraries
4. **Code Generation**: Build tools, annotation processors

**Limitations:**

```java
// 1. Performance Overhead
// Reflection is slower than direct calls
Method method = clazz.getMethod("getName");
method.invoke(obj);  // Slower than obj.getName()

// 2. Security Restrictions
// May not work in restricted environments
// Requires security permissions

// 3. Type Safety Lost
// Compile-time checks bypassed
Object result = method.invoke(obj);  // No compile-time type checking

// 4. Code Complexity
// Harder to understand and maintain
```

**Best Practices:**

```java
// Cache reflection results
private static final Method GET_NAME_METHOD;
static {
    try {
        GET_NAME_METHOD = User.class.getMethod("getName");
    } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
    }
}

// Use MethodHandle for better performance (Java 7+)
MethodHandle handle = MethodHandles.lookup()
    .findVirtual(User.class, "getName", MethodType.methodType(String.class));
String name = (String) handle.invoke(user);
```

---

### Q7: Explain Java generics, type erasure, and wildcards. Provide examples.

**Answer:**

**Generics provide type safety and eliminate need for casting.**

**Basic Generics:**

```java
// Generic Class
public class Box<T> {
    private T value;
    
    public void setValue(T value) {
        this.value = value;
    }
    
    public T getValue() {
        return value;
    }
}

// Usage
Box<String> stringBox = new Box<>();
stringBox.setValue("Hello");
String value = stringBox.getValue();  // No casting needed
```

**Type Erasure:**

```java
// At compile time
List<String> stringList = new ArrayList<>();
List<Integer> intList = new ArrayList<>();

// At runtime (after type erasure)
// Both become: List (raw type)
// Type information is erased

// This is why you can't do:
if (stringList instanceof List<String>) {  // Compile error!
    // ...
}

// But you can use:
if (stringList instanceof List) {  // OK
    // ...
}
```

**Wildcards:**

```java
// Upper Bounded Wildcard (? extends T)
public void processNumbers(List<? extends Number> numbers) {
    // Can read, but can't write (except null)
    Number first = numbers.get(0);  // OK
    // numbers.add(new Integer(1));  // Compile error!
}

// Lower Bounded Wildcard (? super T)
public void addNumbers(List<? super Integer> numbers) {
    // Can write, but read returns Object
    numbers.add(new Integer(1));  // OK
    Object obj = numbers.get(0);  // Returns Object
}

// Unbounded Wildcard (?)
public void processList(List<?> list) {
    // Can only read as Object, can't write
    Object obj = list.get(0);  // OK
    // list.add(new Object());  // Compile error!
}

// PECS: Producer Extends, Consumer Super
public <T> void copy(List<? extends T> src, List<? super T> dest) {
    for (T item : src) {
        dest.add(item);
    }
}
```

**Advanced Generics:**

```java
// Multiple Type Parameters
public class Pair<K, V> {
    private K key;
    private V value;
    // ...
}

// Generic Methods
public static <T> T getFirst(List<T> list) {
    return list.get(0);
}

// Bounded Type Parameters
public class NumberBox<T extends Number> {
    private T value;
    
    public double getDoubleValue() {
        return value.doubleValue();  // Can call Number methods
    }
}

// Recursive Type Bounds
public static <T extends Comparable<T>> T max(List<T> list) {
    T max = list.get(0);
    for (T item : list) {
        if (item.compareTo(max) > 0) {
            max = item;
        }
    }
    return max;
}
```

---

## Summary: Part 1

### Key Topics Covered:
1. JVM Architecture & Memory Management
2. Java Memory Model (JMM)
3. Garbage Collection Algorithms
4. Class Loading Mechanism
5. Java 8+ Features
6. Reflection API
7. Generics & Type Erasure

### Principal Engineer Focus:
- Deep understanding of JVM internals
- Performance optimization knowledge
- Memory management expertise
- Advanced language features mastery

---

**Next**: Part 2 will cover Concurrency & Multithreading in depth.

