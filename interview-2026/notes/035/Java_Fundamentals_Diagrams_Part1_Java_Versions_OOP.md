# Java Language Fundamentals - Complete Diagrams Guide (Part 1: Java Versions & OOP)

## ☕ Java Versions & Object-Oriented Programming

---

## 1. Java Version Evolution (LTS Versions)

### Java Version Timeline
```
┌─────────────────────────────────────────────────────────────┐
│              Java LTS Versions Timeline                      │
└─────────────────────────────────────────────────────────────┘

1996 ──► Java 1.0 (Initial Release)
2004 ──► Java 5 (Generics, Annotations, Enums)
2014 ──► Java 8 (LTS) ────────────────┐
2018 ──► Java 11 (LTS) ────────────────┤
2021 ──► Java 17 (LTS) ────────────────┼──► Current LTS
2023 ──► Java 21 (LTS) ────────────────┘

LTS = Long Term Support (3+ years)
```

### Java 8 Key Features
```
┌─────────────────────────────────────────────────────────────┐
│              Java 8 Features                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Lambda Expressions                 │
│  (x, y) -> x + y                    │
└─────────────────────────────────────┘
           │
           ├──► Stream API
           │    list.stream()
           │        .filter(x -> x > 10)
           │        .collect(toList())
           │
           ├──► Method References
           │    String::toUpperCase
           │
           ├──► Optional<T>
           │    Optional.ofNullable(value)
           │
           ├──► Default Methods
           │    interface MyInterface {
           │        default void method() {}
           │    }
           │
           ├──► Date/Time API
           │    LocalDate, LocalTime, LocalDateTime
           │
           └──► Nashorn JavaScript Engine
```

### Java 11 Key Features
```
┌─────────────────────────────────────────────────────────────┐
│              Java 11 Features                               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│  String Methods                     │
│  - isBlank()                        │
│  - lines()                          │
│  - strip(), stripLeading(),         │
│    stripTrailing()                  │
└─────────────────────────────────────┘
           │
           ├──► Files.readString()
           │    Files.writeString()
           │
           ├──► var (Local Variable)
           │    var list = new ArrayList<>();
           │
           ├──► HTTP Client (java.net.http)
           │    HttpClient.newHttpClient()
           │
           ├──► Epsilon GC
           │    (No-op garbage collector)
           │
           └──► Nest-Based Access Control
```

### Java 17 Key Features
```
┌─────────────────────────────────────────────────────────────┐
│              Java 17 Features                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Sealed Classes                     │
│  sealed class Shape                 │
│      permits Circle, Rectangle {}    │
└─────────────────────────────────────┘
           │
           ├──► Pattern Matching
           │    if (obj instanceof String s) {
           │        // s is String
           │    }
           │
           ├──► Records
           │    record Point(int x, int y) {}
           │
           ├──► Text Blocks
           │    """
           │    Multi-line
           │    String
           │    """
           │
           ├──► Switch Expressions
           │    int result = switch (x) {
           │        case 1 -> 10;
           │        case 2 -> 20;
           │        default -> 0;
           │    };
           │
           └──► ZGC & Shenandoah GC
```

### Java 21 Key Features
```
┌─────────────────────────────────────────────────────────────┐
│              Java 21 Features                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│  Virtual Threads                   │
│  Thread.ofVirtual()                 │
│    .start(() -> {...})              │
└─────────────────────────────────────┘
           │
           ├──► Pattern Matching
           │    switch (obj) {
           │        case String s -> ...
           │        case Integer i -> ...
           │    }
           │
           ├──► Record Patterns
           │    if (obj instanceof Point(int x, int y)) {
           │        // use x, y
           │    }
           │
           ├──► String Templates
           │    STR."Hello \{name}!"
           │
           └──► Sequenced Collections
               list.getFirst()
               list.getLast()
```

---

## 2. Object-Oriented Programming (OOP)

### OOP Pillars
```
┌─────────────────────────────────────────────────────────────┐
│              OOP Four Pillars                               │
└─────────────────────────────────────────────────────────────┘

        ┌──────────────┐
        │  Encapsulation │
        │  (Data Hiding) │
        └──────┬─────────┘
               │
    ┌──────────┴──────────┐
    │                     │
┌───┴────┐          ┌────┴────┐
│Inheritance│      │Polymorphism│
│(Reusability)│    │(Flexibility)│
└───┬────┘          └────┬────┘
    │                     │
    └──────────┬──────────┘
               │
        ┌──────┴─────────┐
        │  Abstraction   │
        │  (Simplification)│
        └──────────────┘
```

---

## 3. Encapsulation

### Encapsulation Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Encapsulation                                  │
└─────────────────────────────────────────────────────────────┘

    ┌─────────────────────────┐
    │      Class (Capsule)     │
    │  ┌───────────────────┐  │
    │  │  Private Fields    │  │
    │  │  - name            │  │
    │  │  - age             │  │
    │  └───────────────────┘  │
    │                          │
    │  ┌───────────────────┐  │
    │  │  Public Methods   │  │
    │  │  + getName()      │  │
    │  │  + setName()     │  │
    │  │  + getAge()      │  │
    │  └───────────────────┘  │
    └─────────────────────────┘
           │
           │ Access only through
           │ public methods
           ▼
    External Code
```

### Encapsulation Example
```java
public class BankAccount {
    // Private fields (data hiding)
    private double balance;
    private String accountNumber;
    
    // Public methods (controlled access)
    public double getBalance() {
        return balance;
    }
    
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }
    
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        }
    }
}
```

---

## 4. Inheritance

### Inheritance Hierarchy
```
┌─────────────────────────────────────────────────────────────┐
│              Inheritance Hierarchy                           │
└─────────────────────────────────────────────────────────────┘

        ┌──────────────┐
        │   Animal     │  (Parent/Superclass)
        │  + name      │
        │  + eat()     │
        │  + sleep()   │
        └──────┬───────┘
               │
        ┌──────┴───────┐
        │              │
┌───────┴──────┐  ┌────┴──────┐
│    Dog      │  │    Cat     │  (Child/Subclass)
│  + bark()   │  │  + meow()  │
└─────────────┘  └────────────┘
        │              │
        └──────┬───────┘
               │
        ┌──────┴───────┐
        │  Puppy       │  (Grandchild)
        │  + play()    │
        └──────────────┘
```

### Inheritance Types
```
┌─────────────────────────────────────────────────────────────┐
│              Types of Inheritance                            │
└─────────────────────────────────────────────────────────────┘

Single Inheritance:
    A
    │
    └──► B

Multilevel Inheritance:
    A
    │
    └──► B
         │
         └──► C

Hierarchical Inheritance:
         A
         │
    ┌────┴────┐
    B         C

Multiple Inheritance (via Interfaces):
    I1    I2
     ╲    ╱
      ╲  ╱
       ╲╱
        C
```

### Inheritance Example
```java
// Parent class
class Animal {
    protected String name;
    
    public Animal(String name) {
        this.name = name;
    }
    
    public void eat() {
        System.out.println(name + " is eating");
    }
}

// Child class
class Dog extends Animal {
    public Dog(String name) {
        super(name);  // Call parent constructor
    }
    
    public void bark() {
        System.out.println(name + " is barking");
    }
    
    @Override
    public void eat() {
        super.eat();  // Call parent method
        System.out.println("Dog is eating dog food");
    }
}
```

---

## 5. Polymorphism

### Polymorphism Types
```
┌─────────────────────────────────────────────────────────────┐
│              Polymorphism Types                             │
└─────────────────────────────────────────────────────────────┘

        Polymorphism
             │
    ┌────────┴────────┐
    │                  │
Compile-time      Runtime
(Static)          (Dynamic)
    │                  │
    │                  │
Method          Method
Overloading     Overriding
```

### Method Overloading (Compile-time Polymorphism)
```
┌─────────────────────────────────────────────────────────────┐
│              Method Overloading                             │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────┐
    │  calculate()     │
    └────────┬─────────┘
             │
    ┌────────┼────────┐
    │        │        │
calculate(int)    calculate(int, int)    calculate(double)
    │        │        │
    └────────┴────────┘
         Same method name,
         Different parameters
```

### Method Overriding (Runtime Polymorphism)
```
┌─────────────────────────────────────────────────────────────┐
│              Method Overriding                              │
└─────────────────────────────────────────────────────────────┘

    Animal
    ┌──────┐
    │speak()│
    └──┬───┘
       │
   ┌───┴───┐
   │       │
  Dog     Cat
┌─────┐ ┌─────┐
│speak()│ │speak()│
└─────┘ └─────┘
 │       │
 │       │
"Woof"  "Meow"

Runtime decision based on
actual object type
```

### Polymorphism Example
```java
// Parent class
class Animal {
    public void makeSound() {
        System.out.println("Animal makes a sound");
    }
}

// Child classes
class Dog extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Dog barks");
    }
}

class Cat extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Cat meows");
    }
}

// Polymorphism in action
Animal animal1 = new Dog();  // Reference type: Animal
Animal animal2 = new Cat();  // Actual type: Cat

animal1.makeSound();  // Output: "Dog barks" (Runtime decision)
animal2.makeSound();  // Output: "Cat meows" (Runtime decision)
```

---

## 6. Abstraction

### Abstraction Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Abstraction                                    │
└─────────────────────────────────────────────────────────────┘

    Complex System
    ┌─────────────────────┐
    │  Implementation     │
    │  Details Hidden     │
    │  ┌───────────────┐  │
    │  │ Internal      │  │
    │  │ Complexity     │  │
    │  │ - Algorithms   │  │
    │  │ - Data         │  │
    │  │ - Logic        │  │
    │  └───────────────┘  │
    └─────────────────────┘
           │
           │ Simplified
           │ Interface
           ▼
    ┌──────────────┐
    │  Simple API  │
    │  - start()   │
    │  - stop()    │
    │  - pause()   │
    └──────────────┘
```

### Abstraction Implementation

#### Abstract Class
```java
// Abstract class (cannot be instantiated)
abstract class Vehicle {
    protected String brand;
    
    // Concrete method
    public void start() {
        System.out.println(brand + " is starting");
    }
    
    // Abstract method (must be implemented by subclasses)
    public abstract void accelerate();
    public abstract void brake();
}

// Concrete implementation
class Car extends Vehicle {
    public Car(String brand) {
        this.brand = brand;
    }
    
    @Override
    public void accelerate() {
        System.out.println(brand + " car is accelerating");
    }
    
    @Override
    public void brake() {
        System.out.println(brand + " car is braking");
    }
}
```

#### Interface
```java
// Interface (pure abstraction in Java 8+)
interface Drawable {
    // Abstract method
    void draw();
    
    // Default method (Java 8+)
    default void resize() {
        System.out.println("Resizing...");
    }
    
    // Static method (Java 8+)
    static void printInfo() {
        System.out.println("This is a drawable object");
    }
}

// Implementation
class Circle implements Drawable {
    @Override
    public void draw() {
        System.out.println("Drawing a circle");
    }
}
```

### Abstract Class vs Interface
```
┌─────────────────────────────────────────────────────────────┐
│              Abstract Class vs Interface                    │
└─────────────────────────────────────────────────────────────┘

Abstract Class:              Interface:
┌──────────────┐            ┌──────────────┐
│ Can have     │            │ Only abstract │
│ - Fields     │            │ methods       │
│ - Constructors│           │ (before Java 8)│
│ - Concrete   │            │               │
│   methods    │            │ Java 8+:      │
│ - Abstract   │            │ - Default     │
│   methods    │            │ - Static      │
└──────────────┘            └──────────────┘
     │                          │
     │                          │
     └──────────┬───────────────┘
                │
         Single class inheritance
         Multiple interface implementation
```

---

## 7. OOP Relationships

### Class Relationships
```
┌─────────────────────────────────────────────────────────────┐
│              OOP Relationships                              │
└─────────────────────────────────────────────────────────────┘

Inheritance (is-a):
    Animal
        │
        └──► Dog (is-a Animal)

Composition (has-a):
    Car ────► Engine
    Car ────► Wheels
    (Car has Engine and Wheels)

Aggregation (has-a, weak):
    University ────► Students
    (University has Students, but Students can exist independently)

Association (uses-a):
    Student ────► Course
    (Student takes Course)

Dependency (uses):
    ClassA ────► ClassB
    (ClassA uses ClassB)
```

---

## Key Concepts Summary

### OOP Principles
```
Encapsulation:
- Data hiding with private fields
- Controlled access via public methods
- Maintains data integrity

Inheritance:
- Code reusability
- extends keyword
- super keyword for parent access

Polymorphism:
- One interface, multiple implementations
- Method overriding (runtime)
- Method overloading (compile-time)

Abstraction:
- Hide implementation details
- Show only essential features
- Abstract classes and interfaces
```

### Java Version Highlights
```
Java 8:  Lambda, Streams, Optional, Default methods
Java 11: String methods, var, HTTP Client
Java 17: Sealed classes, Records, Pattern matching
Java 21: Virtual threads, String templates, Sequenced collections
```

---

**Next: Part 2 will cover SOLID Principles in depth.**

