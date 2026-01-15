# Java Language Fundamentals - Complete Diagrams Guide (Part 6: Lambda & Functional Programming)

## 🎯 Lambda Expressions & Functional Programming

---

## 1. Lambda Expressions

### Lambda Syntax
```
┌─────────────────────────────────────────────────────────────┐
│              Lambda Expression Structure                    │
└─────────────────────────────────────────────────────────────┘

    (parameters) -> expression
    
    (parameters) -> {
        statements;
        return value;
    }

Examples:
    () -> System.out.println("Hello")
    (x) -> x * 2
    (x, y) -> x + y
    (String s) -> s.length()
    (x) -> {
        System.out.println(x);
        return x * 2;
    }
```

### Lambda vs Anonymous Class
```
┌─────────────────────────────────────────────────────────────┐
│              Lambda vs Anonymous Class                      │
└─────────────────────────────────────────────────────────────┘

❌ Anonymous Class:
    Runnable r = new Runnable() {
        @Override
        public void run() {
            System.out.println("Hello");
        }
    };

✅ Lambda:
    Runnable r = () -> System.out.println("Hello");

Benefits:
- Concise
- More readable
- Functional style
```

---

## 2. Functional Interfaces

### Built-in Functional Interfaces
```
┌─────────────────────────────────────────────────────────────┐
│              Functional Interfaces                           │
└─────────────────────────────────────────────────────────────┘

Function<T, R>:
    T ────► R
    R apply(T t)

Predicate<T>:
    T ────► boolean
    boolean test(T t)

Consumer<T>:
    T ────► void
    void accept(T t)

Supplier<T>:
    () ────► T
    T get()

BiFunction<T, U, R>:
    (T, U) ────► R
    R apply(T t, U u)
```

### Functional Interface Examples
```java
// Function
Function<String, Integer> length = s -> s.length();
Integer len = length.apply("Hello");  // 5

// Predicate
Predicate<Integer> isEven = x -> x % 2 == 0;
boolean result = isEven.test(4);  // true

// Consumer
Consumer<String> printer = s -> System.out.println(s);
printer.accept("Hello");  // Prints "Hello"

// Supplier
Supplier<String> supplier = () -> "Hello";
String value = supplier.get();  // "Hello"

// BiFunction
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
Integer sum = add.apply(3, 4);  // 7
```

---

## 3. Method References

### Method Reference Types
```
┌─────────────────────────────────────────────────────────────┐
│              Method Reference Types                         │
└─────────────────────────────────────────────────────────────┘

Static Method:
    ClassName::staticMethod
    Example: String::valueOf

Instance Method (specific):
    instance::instanceMethod
    Example: str::toUpperCase

Instance Method (arbitrary):
    ClassName::instanceMethod
    Example: String::length

Constructor:
    ClassName::new
    Example: ArrayList::new
```

### Method Reference Examples
```java
// Static method reference
Function<Integer, String> converter = String::valueOf;
String str = converter.apply(123);  // "123"

// Instance method (specific)
String s = "hello";
Supplier<String> upper = s::toUpperCase;
String result = upper.get();  // "HELLO"

// Instance method (arbitrary)
Function<String, Integer> length = String::length;
Integer len = length.apply("Hello");  // 5

// Constructor reference
Supplier<List<String>> listSupplier = ArrayList::new;
List<String> list = listSupplier.get();

// BiFunction with method reference
BiFunction<String, String, String> concat = String::concat;
String result = concat.apply("Hello", "World");  // "HelloWorld"
```

---

## 4. Stream API (Functional Style)

### Stream Operations
```
┌─────────────────────────────────────────────────────────────┐
│              Stream Operations                              │
└─────────────────────────────────────────────────────────────┘

Intermediate (Lazy):
    filter() ────► Predicate
    map() ────► Function
    flatMap() ────► Function<Stream>
    sorted() ────► Comparator
    distinct()
    limit()
    skip()

Terminal (Eager):
    forEach() ────► Consumer
    collect() ────► Collector
    reduce() ────► BinaryOperator
    findFirst()
    anyMatch() ────► Predicate
    count()
```

### Stream Examples
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");

// Filter and map
List<String> result = names.stream()
    .filter(name -> name.length() > 4)
    .map(String::toUpperCase)
    .collect(Collectors.toList());

// FlatMap
List<List<Integer>> lists = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4)
);
List<Integer> flattened = lists.stream()
    .flatMap(List::stream)
    .collect(Collectors.toList());

// Reduce
Optional<Integer> sum = Stream.of(1, 2, 3, 4, 5)
    .reduce((a, b) -> a + b);
```

---

## 5. Optional

### Optional Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Optional<T>                                    │
└─────────────────────────────────────────────────────────────┘

    Optional<String>
    ┌──────────────┐
    │              │
    │  empty()     │  ────► No value
    │  of(value)   │  ────► Has value
    │  ofNullable()│  ────► May have value
    └──────────────┘

Methods:
    isPresent()
    isEmpty()
    get()
    orElse(default)
    orElseGet(supplier)
    orElseThrow()
    ifPresent(consumer)
    map(function)
    flatMap(function)
```

### Optional Examples
```java
// Creating Optional
Optional<String> empty = Optional.empty();
Optional<String> present = Optional.of("Hello");
Optional<String> nullable = Optional.ofNullable(null);

// Checking
if (present.isPresent()) {
    String value = present.get();
}

// Safe operations
String result = nullable
    .orElse("Default");
    
String result2 = nullable
    .orElseGet(() -> "Default from supplier");
    
String result3 = nullable
    .orElseThrow(() -> new RuntimeException("No value"));

// Functional style
present.ifPresent(System.out::println);

// Transformations
Optional<Integer> length = present.map(String::length);
Optional<String> upper = present.map(String::toUpperCase);
```

---

## 6. Functional Composition

### Function Composition
```
┌─────────────────────────────────────────────────────────────┐
│              Function Composition                           │
└─────────────────────────────────────────────────────────────┘

    f: A ────► B
    g: B ────► C
    
    g.compose(f): A ────► C
    (g after f)
    
    f.andThen(g): A ────► C
    (f then g)
```

### Composition Examples
```java
// Function composition
Function<Integer, Integer> multiply = x -> x * 2;
Function<Integer, Integer> add = x -> x + 3;

// andThen: multiply then add
Function<Integer, Integer> multiplyThenAdd = multiply.andThen(add);
Integer result1 = multiplyThenAdd.apply(5);  // (5 * 2) + 3 = 13

// compose: add then multiply
Function<Integer, Integer> addThenMultiply = multiply.compose(add);
Integer result2 = addThenMultiply.apply(5);  // (5 + 3) * 2 = 16

// Predicate composition
Predicate<Integer> isEven = x -> x % 2 == 0;
Predicate<Integer> isGreaterThan10 = x -> x > 10;

Predicate<Integer> isEvenAndGreaterThan10 = isEven.and(isGreaterThan10);
boolean result = isEvenAndGreaterThan10.test(12);  // true
```

---

## 7. Higher-Order Functions

### Higher-Order Functions
```
┌─────────────────────────────────────────────────────────────┐
│              Higher-Order Functions                         │
└─────────────────────────────────────────────────────────────┘

Functions that:
- Take functions as parameters
- Return functions as results

Examples:
    map(function) ────► Applies function to each element
    filter(predicate) ────► Filters based on predicate
    reduce(operator) ────► Combines elements
```

### Higher-Order Function Examples
```java
// Function that returns a function
Function<Integer, Function<Integer, Integer>> add = a -> b -> a + b;
Function<Integer, Integer> add5 = add.apply(5);
Integer result = add5.apply(3);  // 8

// Function that takes a function
Function<Function<Integer, Integer>, Integer> applyTwice = f -> f.apply(f.apply(2));
Function<Integer, Integer> square = x -> x * x;
Integer result2 = applyTwice.apply(square);  // 16

// Currying
BiFunction<Integer, Integer, Integer> addBi = (a, b) -> a + b;
Function<Integer, Function<Integer, Integer>> addCurried = a -> b -> a + b;
```

---

## 8. Parallel Streams

### Parallel Streams
```
┌─────────────────────────────────────────────────────────────┐
│              Parallel Streams                                │
└─────────────────────────────────────────────────────────────┘

    Sequential Stream:
    [1][2][3][4][5]
    └──► Process one by one

    Parallel Stream:
    [1][2][3][4][5]
    ├──► Thread 1: [1][2]
    ├──► Thread 2: [3][4]
    └──► Thread 3: [5]
    
    Combine results
```

### Parallel Stream Examples
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// Parallel stream
long sum = numbers.parallelStream()
    .mapToInt(Integer::intValue)
    .sum();

// Sequential vs Parallel
List<String> result1 = names.stream()  // Sequential
    .map(String::toUpperCase)
    .collect(Collectors.toList());

List<String> result2 = names.parallelStream()  // Parallel
    .map(String::toUpperCase)
    .collect(Collectors.toList());

// Note: Use parallel only for large datasets and stateless operations
```

---

## Key Concepts Summary

### Functional Programming Summary
```
Lambda Expressions:
- Concise function syntax
- (params) -> expression
- Replaces anonymous classes

Functional Interfaces:
- Single abstract method
- @FunctionalInterface
- Function, Predicate, Consumer, Supplier

Method References:
- ClassName::method
- Shorthand for lambdas
- Static, instance, constructor

Stream API:
- Functional operations
- Lazy evaluation
- Pipeline pattern
- Intermediate and terminal operations

Optional:
- Avoid null pointer exceptions
- Functional style null handling
- map, flatMap, orElse

Composition:
- Combine functions
- andThen, compose
- Build complex operations
```

---

**Next: Part 7 will cover Annotations & Reflection.**

