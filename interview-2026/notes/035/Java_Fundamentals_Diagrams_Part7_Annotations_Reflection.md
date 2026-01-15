# Java Language Fundamentals - Complete Diagrams Guide (Part 7: Annotations & Reflection)

## 🏷️ Annotations & Reflection

---

## 1. Annotations Overview

### Annotation Types
```
┌─────────────────────────────────────────────────────────────┐
│              Annotation Categories                          │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │  Built-in Annotations │
    │  - @Override          │
    │  - @Deprecated        │
    │  - @SuppressWarnings  │
    └──────────────────────┘
           │
    ┌──────┴───────┐
    │              │
┌───┴────┐    ┌────┴────┐
│Meta    │    │Custom   │
│Annotations│  │Annotations│
│@Target │    │@MyAnnotation│
│@Retention│  │         │
│@Documented│ │         │
└────────┘    └─────────┘
```

### Built-in Annotations
```java
// @Override - Indicates method overrides parent
class Parent {
    public void method() {}
}

class Child extends Parent {
    @Override
    public void method() {  // Compiler checks override
        super.method();
    }
}

// @Deprecated - Marks as deprecated
@Deprecated
class OldClass {
    @Deprecated
    public void oldMethod() {}
}

// @SuppressWarnings - Suppress compiler warnings
@SuppressWarnings("unchecked")
List list = new ArrayList();
```

---

## 2. Custom Annotations

### Creating Custom Annotations
```
┌─────────────────────────────────────────────────────────────┐
│              Custom Annotation Structure                     │
└─────────────────────────────────────────────────────────────┘

    @interface MyAnnotation {
        String value();
        int count() default 1;
        String[] tags() default {};
    }

    Usage:
    @MyAnnotation(value = "test", count = 5)
    class MyClass {}
```

### Custom Annotation Example
```java
// Define annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface Author {
    String name();
    String date();
    int version() default 1;
}

// Use annotation
class MyClass {
    @Author(name = "John", date = "2024-01-01", version = 2)
    public void myMethod() {
        // Method implementation
    }
}
```

---

## 3. Meta-Annotations

### Meta-Annotations
```
┌─────────────────────────────────────────────────────────────┐
│              Meta-Annotations                               │
└─────────────────────────────────────────────────────────────┘

@Target:
    ┌──────────────────────┐
    │  Where annotation     │
    │  can be applied       │
    │  - TYPE              │
    │  - METHOD            │
    │  - FIELD             │
    │  - PARAMETER         │
    └──────────────────────┘

@Retention:
    ┌──────────────────────┐
    │  How long annotation  │
    │  is retained          │
    │  - SOURCE            │
    │  - CLASS             │
    │  - RUNTIME           │
    └──────────────────────┘

@Documented:
    ┌──────────────────────┐
    │  Include in Javadoc   │
    └──────────────────────┘

@Inherited:
    ┌──────────────────────┐
    │  Inherit by subclasses│
    └──────────────────────┘
```

### Meta-Annotation Examples
```java
// @Target - Specify where annotation can be used
@Target({ElementType.METHOD, ElementType.TYPE})
@interface MyAnnotation {}

// @Retention - Specify retention policy
@Retention(RetentionPolicy.RUNTIME)  // Available at runtime
@interface RuntimeAnnotation {}

@Retention(RetentionPolicy.SOURCE)  // Discarded at compile time
@interface SourceAnnotation {}

// @Documented - Include in Javadoc
@Documented
@interface DocumentedAnnotation {}

// @Inherited - Inherited by subclasses
@Inherited
@interface InheritedAnnotation {}
```

---

## 4. Annotation Processing

### Annotation Processing Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Annotation Processing                           │
└─────────────────────────────────────────────────────────────┘

    Source Code
    ┌──────────┐
    │@MyAnnotation│
    │class A {} │
    └────┬──────┘
         │
         │ Compile
         ▼
    Annotation Processor
    ┌──────────────┐
    │  Process     │
    │  annotations │
    └────┬─────────┘
         │
         │ Generate code
         ▼
    Generated Code
    ┌──────────┐
    │class B {}│
    └──────────┘
```

### Annotation Processor Example
```java
// Annotation
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@interface Builder {}

// Processor
@SupportedAnnotationTypes("Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class BuilderProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                          RoundEnvironment roundEnv) {
        // Process annotations and generate code
        return true;
    }
}
```

---

## 5. Reflection Overview

### Reflection Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Reflection                                     │
└─────────────────────────────────────────────────────────────┘

    Class Information
    ┌──────────────┐
    │  Class       │
    │  - Fields    │
    │  - Methods   │
    │  - Constructors│
    │  - Annotations│
    └──────┬───────┘
           │
           │ Reflection API
           │
    ┌──────┴───────┐
    │              │
Inspect        Modify
    │              │
    │              │
Get info    Set values
```

### Reflection Classes
```
┌─────────────────────────────────────────────────────────────┐
│              Reflection Classes                             │
└─────────────────────────────────────────────────────────────┘

    Class<T>
    ┌──────────────┐
    │  - getName() │
    │  - getFields()│
    │  - getMethods()│
    │  - newInstance()│
    └──────┬───────┘
           │
    ┌──────┴───────┐
    │              │
Field          Method
┌──────┐      ┌──────┐
│get() │      │invoke()│
│set() │      │      │
└──────┘      └──────┘
```

---

## 6. Reflection Examples

### Getting Class Information
```java
// Get Class object
Class<?> clazz = String.class;
Class<?> clazz2 = "Hello".getClass();
Class<?> clazz3 = Class.forName("java.lang.String");

// Get class name
String name = clazz.getName();  // "java.lang.String"
String simpleName = clazz.getSimpleName();  // "String"

// Get superclass
Class<?> superclass = clazz.getSuperclass();

// Get interfaces
Class<?>[] interfaces = clazz.getInterfaces();

// Check modifiers
int modifiers = clazz.getModifiers();
boolean isPublic = Modifier.isPublic(modifiers);
```

### Accessing Fields
```java
class Person {
    private String name;
    public int age;
}

// Get fields
Class<Person> clazz = Person.class;
Field[] allFields = clazz.getDeclaredFields();  // All fields
Field[] publicFields = clazz.getFields();  // Only public

// Get specific field
Field nameField = clazz.getDeclaredField("name");
nameField.setAccessible(true);  // Access private field

// Get/set field values
Person person = new Person();
nameField.set(person, "John");
String name = (String) nameField.get(person);
```

### Invoking Methods
```java
class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
    
    private int multiply(int a, int b) {
        return a * b;
    }
}

// Get method
Class<Calculator> clazz = Calculator.class;
Method addMethod = clazz.getMethod("add", int.class, int.class);

// Invoke method
Calculator calc = new Calculator();
Object result = addMethod.invoke(calc, 5, 3);  // 8

// Access private method
Method multiplyMethod = clazz.getDeclaredMethod("multiply", int.class, int.class);
multiplyMethod.setAccessible(true);
Object result2 = multiplyMethod.invoke(calc, 5, 3);  // 15
```

### Creating Instances
```java
class Person {
    private String name;
    
    public Person() {}
    
    public Person(String name) {
        this.name = name;
    }
}

// Create instance using default constructor
Class<Person> clazz = Person.class;
Person person1 = clazz.newInstance();  // Deprecated
Person person2 = clazz.getDeclaredConstructor().newInstance();  // Java 9+

// Create instance using parameterized constructor
Constructor<Person> constructor = clazz.getConstructor(String.class);
Person person3 = constructor.newInstance("John");
```

---

## 7. Reflection with Annotations

### Reading Annotations via Reflection
```java
// Annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface Author {
    String name();
    String date();
}

// Class with annotation
class MyClass {
    @Author(name = "John", date = "2024-01-01")
    public void myMethod() {}
}

// Read annotation via reflection
Method method = MyClass.class.getMethod("myMethod");
Author author = method.getAnnotation(Author.class);
if (author != null) {
    String name = author.name();
    String date = author.date();
}

// Get all annotations
Annotation[] annotations = method.getAnnotations();
```

---

## 8. Reflection Use Cases

### Common Use Cases
```
┌─────────────────────────────────────────────────────────────┐
│              Reflection Use Cases                           │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │  Frameworks          │
    │  - Spring            │
    │  - Hibernate         │
    │  - JUnit             │
    └──────────────────────┘
           │
    ┌──────┴───────┐
    │              │
┌───┴────┐    ┌────┴────┐
│Serialization│  │Code    │
│            │  │Generation│
└──────────┘    └─────────┘
```

### Framework Example (Spring-like)
```java
// Annotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Autowired {}

// Service
class UserService {
    public void process() {
        System.out.println("Processing user");
    }
}

// Controller
class UserController {
    @Autowired
    private UserService userService;
    
    public void handleRequest() {
        // Use reflection to inject dependencies
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                Object service = field.getType().newInstance();
                field.set(this, service);
            }
        }
        
        userService.process();
    }
}
```

---

## Key Concepts Summary

### Annotations Summary
```
Built-in:
- @Override, @Deprecated, @SuppressWarnings

Meta-Annotations:
- @Target: Where can be applied
- @Retention: How long retained
- @Documented: Include in Javadoc
- @Inherited: Inherited by subclasses

Custom Annotations:
- @interface keyword
- Can have methods (attributes)
- Default values supported

Processing:
- Compile-time processing
- Runtime reflection
- Code generation
```

### Reflection Summary
```
Class Information:
- Class object
- Fields, methods, constructors
- Annotations

Access:
- getDeclaredFields/Methods
- setAccessible(true) for private
- invoke() for methods

Use Cases:
- Frameworks (DI, ORM)
- Serialization
- Testing frameworks
- Code analysis
```

---

**Next: Part 8 will cover Exception Handling.**

