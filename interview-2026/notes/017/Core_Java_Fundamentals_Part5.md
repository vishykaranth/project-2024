# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 5: Classes and Objects

---

## Table of Contents

1. [Introduction to OOP](#1-introduction-to-oop)
2. [Classes in Java](#2-classes-in-java)
3. [Objects in Java](#3-objects-in-java)
4. [Constructors](#4-constructors)
5. [this Keyword](#5-this-keyword)
6. [Instance vs Class Members](#6-instance-vs-class-members)
7. [Access Modifiers](#7-access-modifiers)
8. [Object Lifecycle](#8-object-lifecycle)

---

## 1. Introduction to OOP

### 1.1 What is Object-Oriented Programming?

**Object-Oriented Programming (OOP)** is a programming paradigm based on the concept of "objects" that contain data (attributes) and code (methods).

### 1.2 Core OOP Concepts

1. **Class**: Blueprint/template for objects
2. **Object**: Instance of a class
3. **Encapsulation**: Bundling data and methods
4. **Inheritance**: Reusing code from parent classes
5. **Polymorphism**: One interface, multiple implementations
6. **Abstraction**: Hiding implementation details

### 1.3 Benefits of OOP

- **Modularity**: Code organized into classes
- **Reusability**: Classes can be reused
- **Maintainability**: Easier to update and debug
- **Scalability**: Easy to extend functionality

---

## 2. Classes in Java

### 2.1 Class Definition

```java
// Basic class structure
[access-modifier] class ClassName {
    // Fields (variables)
    // Constructors
    // Methods
}
```

### 2.2 Simple Class Example

```java
public class Student {
    // Fields (attributes)
    String name;
    int age;
    String studentId;
    
    // Methods (behaviors)
    public void study() {
        System.out.println(name + " is studying");
    }
    
    public void displayInfo() {
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Student ID: " + studentId);
    }
}
```

### 2.3 Class Components

**1. Fields (Instance Variables)**
```java
public class Car {
    String brand;      // Instance variable
    String model;       // Instance variable
    int year;          // Instance variable
    double price;      // Instance variable
}
```

**2. Methods**
```java
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
    
    public int subtract(int a, int b) {
        return a - b;
    }
}
```

**3. Constructors**
```java
public class Person {
    String name;
    
    // Constructor
    public Person(String name) {
        this.name = name;
    }
}
```

---

## 3. Objects in Java

### 3.1 Creating Objects

```java
// Class definition
public class Student {
    String name;
    int age;
}

// Creating objects
Student student1 = new Student();
Student student2 = new Student();
Student student3 = new Student();
```

### 3.2 Object Creation Process

```java
// Step 1: Declaration
Student student;

// Step 2: Instantiation (new keyword)
student = new Student();

// Combined: Declaration and instantiation
Student student = new Student();
```

### 3.3 Accessing Object Members

```java
public class Student {
    String name;
    int age;
    
    public void displayInfo() {
        System.out.println("Name: " + name + ", Age: " + age);
    }
}

// Creating and using object
Student student = new Student();
student.name = "John";        // Access field
student.age = 20;             // Access field
student.displayInfo();        // Call method
```

### 3.4 Multiple Objects

```java
// Each object has its own copy of instance variables
Student student1 = new Student();
student1.name = "Alice";
student1.age = 20;

Student student2 = new Student();
student2.name = "Bob";
student2.age = 21;

// student1 and student2 are independent objects
System.out.println(student1.name);  // Alice
System.out.println(student2.name);  // Bob
```

### 3.5 Object Reference

```java
// Objects are accessed by reference
Student student1 = new Student();
Student student2 = student1;  // Both reference same object

student1.name = "John";
System.out.println(student2.name);  // John (same object)

// Creating new object
student2 = new Student();  // Now student2 references different object
student2.name = "Jane";
System.out.println(student1.name);  // Still John
```

---

## 4. Constructors

### 4.1 What is a Constructor?

A **constructor** is a special method that initializes objects. It has the same name as the class and no return type.

### 4.2 Default Constructor

```java
public class Student {
    String name;
    int age;
    
    // Default constructor (provided by Java if none defined)
    // public Student() { }
}

// Usage
Student student = new Student();  // Calls default constructor
```

### 4.3 Parameterized Constructor

```java
public class Student {
    String name;
    int age;
    
    // Parameterized constructor
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

// Usage
Student student = new Student("John", 20);
```

### 4.4 Constructor Overloading

```java
public class Student {
    String name;
    int age;
    String studentId;
    
    // Constructor 1: No parameters
    public Student() {
        this.name = "Unknown";
        this.age = 0;
    }
    
    // Constructor 2: Name only
    public Student(String name) {
        this.name = name;
        this.age = 0;
    }
    
    // Constructor 3: Name and age
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    // Constructor 4: All fields
    public Student(String name, int age, String studentId) {
        this.name = name;
        this.age = age;
        this.studentId = studentId;
    }
}

// Usage
Student s1 = new Student();                    // Constructor 1
Student s2 = new Student("John");               // Constructor 2
Student s3 = new Student("John", 20);          // Constructor 3
Student s4 = new Student("John", 20, "S001");  // Constructor 4
```

### 4.5 Constructor Chaining

```java
public class Student {
    String name;
    int age;
    String studentId;
    
    // Constructor 1: Calls Constructor 2
    public Student() {
        this("Unknown", 0);  // Calls Constructor 2
    }
    
    // Constructor 2: Calls Constructor 3
    public Student(String name) {
        this(name, 0);  // Calls Constructor 3
    }
    
    // Constructor 3: Calls Constructor 4
    public Student(String name, int age) {
        this(name, age, null);  // Calls Constructor 4
    }
    
    // Constructor 4: Main constructor
    public Student(String name, int age, String studentId) {
        this.name = name;
        this.age = age;
        this.studentId = studentId;
    }
}
```

### 4.6 Copy Constructor

```java
public class Student {
    String name;
    int age;
    
    // Regular constructor
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    // Copy constructor
    public Student(Student other) {
        this.name = other.name;
        this.age = other.age;
    }
}

// Usage
Student original = new Student("John", 20);
Student copy = new Student(original);  // Creates copy
```

---

## 5. this Keyword

### 5.1 this for Instance Variables

```java
public class Student {
    String name;  // Instance variable
    int age;      // Instance variable
    
    public Student(String name, int age) {
        this.name = name;  // this.name refers to instance variable
        this.age = age;    // this.age refers to instance variable
        // name and age are parameters
    }
}
```

### 5.2 this to Call Other Constructors

```java
public class Student {
    String name;
    int age;
    
    public Student() {
        this("Unknown", 0);  // Calls parameterized constructor
    }
    
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```

### 5.3 this to Pass Current Object

```java
public class Student {
    String name;
    
    public void displayInfo() {
        System.out.println("Name: " + name);
    }
    
    public void callDisplay() {
        this.displayInfo();  // Passes current object
        // Same as: displayInfo();
    }
}
```

### 5.4 this in Methods

```java
public class Student {
    String name;
    
    public Student setName(String name) {
        this.name = name;
        return this;  // Return current object for method chaining
    }
    
    public Student setAge(int age) {
        this.age = age;
        return this;
    }
}

// Method chaining
Student student = new Student()
    .setName("John")
    .setAge(20);
```

---

## 6. Instance vs Class Members

### 6.1 Instance Variables

```java
public class Student {
    // Instance variables (each object has its own copy)
    String name;
    int age;
    
    // Each Student object has its own name and age
}

Student s1 = new Student();
s1.name = "Alice";
s1.age = 20;

Student s2 = new Student();
s2.name = "Bob";
s2.age = 21;
// s1 and s2 have independent copies
```

### 6.2 Class Variables (Static)

```java
public class Student {
    // Instance variable
    String name;
    
    // Class variable (shared by all objects)
    static int studentCount = 0;
    
    public Student(String name) {
        this.name = name;
        studentCount++;  // Increment shared counter
    }
}

Student s1 = new Student("Alice");
System.out.println(Student.studentCount);  // 1

Student s2 = new Student("Bob");
System.out.println(Student.studentCount);  // 2 (shared)
```

### 6.3 Instance Methods

```java
public class Calculator {
    // Instance method (operates on instance data)
    public int add(int a, int b) {
        return a + b;
    }
}

Calculator calc = new Calculator();
int result = calc.add(10, 20);  // Called on object
```

### 6.4 Class Methods (Static)

```java
public class MathUtils {
    // Class method (doesn't need object)
    public static int add(int a, int b) {
        return a + b;
    }
    
    public static double calculateArea(double radius) {
        return Math.PI * radius * radius;
    }
}

// Called without creating object
int sum = MathUtils.add(10, 20);
double area = MathUtils.calculateArea(5.0);
```

### 6.5 When to Use Static

**Use Static For**:
- Utility methods (Math operations)
- Constants
- Factory methods
- Counters/trackers shared across instances

**Don't Use Static For**:
- Methods that need instance data
- Methods that should be overridden
- State that's unique to each object

---

## 7. Access Modifiers

### 7.1 Access Modifier Types

| Modifier | Class | Package | Subclass | World |
|----------|-------|---------|----------|-------|
| `public` | ✅ | ✅ | ✅ | ✅ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `package-private` (default) | ✅ | ✅ | ❌ | ❌ |
| `private` | ✅ | ❌ | ❌ | ❌ |

### 7.2 Public Access

```java
public class Student {
    public String name;      // Accessible everywhere
    public int age;          // Accessible everywhere
    
    public void displayInfo() {  // Accessible everywhere
        System.out.println("Name: " + name);
    }
}

// Accessible from anywhere
Student student = new Student();
student.name = "John";  // OK
student.displayInfo();  // OK
```

### 7.3 Private Access

```java
public class Student {
    private String name;     // Only accessible in this class
    private int age;          // Only accessible in this class
    
    // Public methods to access private fields
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}

// Cannot access directly
Student student = new Student();
// student.name = "John";  // ERROR: name is private
student.setName("John");    // OK: using public method
```

### 7.4 Protected Access

```java
public class Animal {
    protected String name;  // Accessible in package and subclasses
    
    protected void makeSound() {
        System.out.println("Animal makes sound");
    }
}

// In same package or subclass
public class Dog extends Animal {
    public void display() {
        name = "Buddy";  // OK: protected accessible in subclass
        makeSound();     // OK: protected accessible in subclass
    }
}
```

### 7.5 Package-Private (Default)

```java
class Student {  // No modifier = package-private
    String name;  // Package-private field
    
    void displayInfo() {  // Package-private method
        System.out.println(name);
    }
}

// Accessible only within same package
```

---

## 8. Object Lifecycle

### 8.1 Object Creation

```java
// 1. Declaration
Student student;

// 2. Instantiation
student = new Student();

// 3. Initialization (via constructor)
student = new Student("John", 20);
```

### 8.2 Object Usage

```java
Student student = new Student("John", 20);
student.displayInfo();  // Using object
student.study();        // Calling methods
```

### 8.3 Object Destruction (Garbage Collection)

```java
Student student = new Student("John", 20);
student = null;  // Object becomes eligible for garbage collection

// Garbage collector automatically frees memory
// No manual memory management needed
```

### 8.4 Finalize Method (Deprecated)

```java
public class Student {
    // Deprecated: Use try-with-resources or Cleaner instead
    @Deprecated
    protected void finalize() throws Throwable {
        // Cleanup code (not recommended)
        super.finalize();
    }
}
```

---

## Summary: Part 5

### Key Concepts Covered

1. **Classes**: Blueprints for objects
2. **Objects**: Instances of classes
3. **Constructors**: Initialize objects
4. **this Keyword**: Reference to current object
5. **Instance vs Static**: Object-specific vs class-wide
6. **Access Modifiers**: Control visibility
7. **Object Lifecycle**: Creation, usage, destruction

### Important Points

- Classes define structure, objects are instances
- Constructors initialize objects
- `this` refers to current object
- Static members belong to class, not instances
- Access modifiers control visibility
- Java handles memory management automatically

### Next Steps

**Part 6** will cover:
- Inheritance Concept
- extends Keyword
- super Keyword
- Method Overriding
- final Keyword
- Object Class

---

**Master classes and objects to understand the foundation of OOP!**

