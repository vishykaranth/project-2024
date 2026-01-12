# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 8: Abstraction

---

## Table of Contents

1. [Introduction to Abstraction](#1-introduction-to-abstraction)
2. [Abstract Classes](#2-abstract-classes)
3. [Abstract Methods](#3-abstract-methods)
4. [Interfaces](#4-interfaces)
5. [Interface Implementation](#5-interface-implementation)
6. [Abstract Class vs Interface](#6-abstract-class-vs-interface)
7. [Default Methods in Interfaces](#7-default-methods-in-interfaces)
8. [Static Methods in Interfaces](#8-static-methods-in-interfaces)

---

## 1. Introduction to Abstraction

### 1.1 What is Abstraction?

**Abstraction** is the process of hiding implementation details and showing only essential features. It focuses on "what" rather than "how".

**Benefits**:
- **Simplifies Complexity**: Hide unnecessary details
- **Security**: Prevents direct access to internal implementation
- **Flexibility**: Can change implementation without affecting users
- **Code Reusability**: Common interface for similar classes

### 1.2 Real-World Example

```java
// User doesn't need to know how car engine works
// User only needs to know: start(), stop(), accelerate()

class Car {
    // Implementation details hidden
    private Engine engine;
    private Transmission transmission;
    
    // Simple interface exposed
    public void start() {
        // Complex implementation hidden
    }
    
    public void stop() {
        // Complex implementation hidden
    }
}
```

---

## 2. Abstract Classes

### 2.1 What is an Abstract Class?

An **abstract class** is a class that cannot be instantiated. It may contain abstract methods (methods without implementation) and concrete methods.

**Syntax**:
```java
abstract class ClassName {
    // Abstract methods
    // Concrete methods
    // Fields
}
```

### 2.2 Basic Abstract Class

```java
abstract class Animal {
    String name;
    
    // Concrete method
    public void eat() {
        System.out.println(name + " is eating");
    }
    
    // Abstract method (no implementation)
    public abstract void makeSound();
}

// Cannot instantiate abstract class
// Animal animal = new Animal();  // ERROR

// Must extend and implement abstract methods
class Dog extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Dog barks");
    }
}

Dog dog = new Dog();
dog.name = "Buddy";
dog.eat();        // Concrete method
dog.makeSound();  // Implemented abstract method
```

### 2.3 Abstract Class with Multiple Abstract Methods

```java
abstract class Shape {
    // Abstract methods
    public abstract double calculateArea();
    public abstract double calculatePerimeter();
    public abstract void draw();
    
    // Concrete method
    public void displayInfo() {
        System.out.println("Area: " + calculateArea());
        System.out.println("Perimeter: " + calculatePerimeter());
    }
}

class Circle extends Shape {
    private double radius;
    
    public Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
    
    @Override
    public double calculatePerimeter() {
        return 2 * Math.PI * radius;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing circle");
    }
}
```

### 2.4 Abstract Class Rules

```java
// Rule 1: Cannot instantiate abstract class
abstract class Animal { }
// Animal animal = new Animal();  // ERROR

// Rule 2: Can have both abstract and concrete methods
abstract class Animal {
    public abstract void makeSound();  // Abstract
    public void eat() { }              // Concrete
}

// Rule 3: Can have constructors
abstract class Animal {
    String name;
    
    public Animal(String name) {
        this.name = name;
    }
}

// Rule 4: Can have fields
abstract class Animal {
    String name;  // Field
    int age;      // Field
}

// Rule 5: Subclass must implement all abstract methods
abstract class Animal {
    public abstract void makeSound();
}

class Dog extends Animal {
    @Override
    public void makeSound() {  // Must implement
        System.out.println("Bark");
    }
}
```

---

## 3. Abstract Methods

### 3.1 What is an Abstract Method?

An **abstract method** is a method declared without implementation. It must be implemented by subclasses.

**Syntax**:
```java
public abstract returnType methodName(parameters);
```

### 3.2 Abstract Method Example

```java
abstract class Vehicle {
    // Abstract method (no body)
    public abstract void start();
    public abstract void stop();
    public abstract double calculateFuelEfficiency();
    
    // Concrete method
    public void displayInfo() {
        System.out.println("Vehicle information");
    }
}

class Car extends Vehicle {
    @Override
    public void start() {
        System.out.println("Car engine started");
    }
    
    @Override
    public void stop() {
        System.out.println("Car engine stopped");
    }
    
    @Override
    public double calculateFuelEfficiency() {
        return 25.5;  // miles per gallon
    }
}
```

### 3.3 Abstract Method Rules

```java
// Rule 1: Abstract methods can only be in abstract classes
abstract class Animal {
    public abstract void makeSound();  // OK
}

// class Animal {
//     public abstract void makeSound();  // ERROR: Class must be abstract
// }

// Rule 2: Abstract methods cannot have body
abstract class Animal {
    // public abstract void makeSound() { }  // ERROR: Cannot have body
    public abstract void makeSound();  // OK
}

// Rule 3: Abstract methods cannot be static
abstract class Animal {
    // public static abstract void makeSound();  // ERROR
    public abstract void makeSound();  // OK
}

// Rule 4: Abstract methods cannot be final
abstract class Animal {
    // public final abstract void makeSound();  // ERROR
    public abstract void makeSound();  // OK
}

// Rule 5: Abstract methods cannot be private
abstract class Animal {
    // private abstract void makeSound();  // ERROR
    public abstract void makeSound();  // OK
}
```

---

## 4. Interfaces

### 4.1 What is an Interface?

An **interface** is a contract that defines what methods a class must implement. It contains only abstract methods (until Java 8).

**Syntax**:
```java
interface InterfaceName {
    // Constants (implicitly public static final)
    // Abstract methods (implicitly public abstract)
}
```

### 4.2 Basic Interface

```java
interface Drawable {
    // Implicitly: public abstract void draw();
    void draw();
}

class Circle implements Drawable {
    @Override
    public void draw() {
        System.out.println("Drawing circle");
    }
}

class Rectangle implements Drawable {
    @Override
    public void draw() {
        System.out.println("Drawing rectangle");
    }
}

// Polymorphic usage
Drawable[] shapes = {new Circle(), new Rectangle()};
for (Drawable shape : shapes) {
    shape.draw();
}
```

### 4.3 Interface with Multiple Methods

```java
interface Animal {
    void eat();
    void sleep();
    void makeSound();
}

class Dog implements Animal {
    @Override
    public void eat() {
        System.out.println("Dog is eating");
    }
    
    @Override
    public void sleep() {
        System.out.println("Dog is sleeping");
    }
    
    @Override
    public void makeSound() {
        System.out.println("Dog barks");
    }
}
```

### 4.4 Interface Constants

```java
interface Constants {
    // Implicitly: public static final
    int MAX_SIZE = 100;
    String DEFAULT_NAME = "Unknown";
    double PI = 3.14159;
}

// Usage
System.out.println(Constants.MAX_SIZE);
System.out.println(Constants.DEFAULT_NAME);
```

### 4.5 Interface Rules

```java
// Rule 1: All methods are implicitly public abstract
interface MyInterface {
    void method();  // Same as: public abstract void method();
}

// Rule 2: All fields are implicitly public static final
interface MyInterface {
    int VALUE = 10;  // Same as: public static final int VALUE = 10;
}

// Rule 3: Cannot instantiate interface
interface MyInterface { }
// MyInterface obj = new MyInterface();  // ERROR

// Rule 4: Class must implement all methods
interface MyInterface {
    void method1();
    void method2();
}

class MyClass implements MyInterface {
    @Override
    public void method1() { }  // Must implement
    
    @Override
    public void method2() { }  // Must implement
}
```

---

## 5. Interface Implementation

### 5.1 Single Interface Implementation

```java
interface Flyable {
    void fly();
}

class Bird implements Flyable {
    @Override
    public void fly() {
        System.out.println("Bird is flying");
    }
}

class Airplane implements Flyable {
    @Override
    public void fly() {
        System.out.println("Airplane is flying");
    }
}
```

### 5.2 Multiple Interface Implementation

```java
interface Flyable {
    void fly();
}

interface Swimmable {
    void swim();
}

// Class can implement multiple interfaces
class Duck implements Flyable, Swimmable {
    @Override
    public void fly() {
        System.out.println("Duck is flying");
    }
    
    @Override
    public void swim() {
        System.out.println("Duck is swimming");
    }
}
```

### 5.3 Interface Inheritance

```java
interface Animal {
    void eat();
}

interface Flyable {
    void fly();
}

// Interface can extend multiple interfaces
interface Bird extends Animal, Flyable {
    void chirp();
}

class Sparrow implements Bird {
    @Override
    public void eat() {
        System.out.println("Sparrow is eating");
    }
    
    @Override
    public void fly() {
        System.out.println("Sparrow is flying");
    }
    
    @Override
    public void chirp() {
        System.out.println("Sparrow is chirping");
    }
}
```

### 5.4 Interface as Type

```java
interface Drawable {
    void draw();
}

class Circle implements Drawable {
    @Override
    public void draw() {
        System.out.println("Drawing circle");
    }
}

// Interface can be used as reference type
Drawable drawable = new Circle();
drawable.draw();
```

---

## 6. Abstract Class vs Interface

### 6.1 Comparison Table

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| **Instantiation** | Cannot instantiate | Cannot instantiate |
| **Methods** | Abstract + Concrete | Abstract (until Java 8) |
| **Fields** | Any access modifier | Only public static final |
| **Constructors** | Can have | Cannot have |
| **Inheritance** | Single inheritance | Multiple inheritance |
| **Access Modifiers** | Any | Only public |
| **Default Methods** | No (Java 8+) | Yes (Java 8+) |
| **Static Methods** | Yes | Yes (Java 8+) |

### 6.2 When to Use Abstract Class

```java
// Use abstract class when:
// 1. Share code among related classes
abstract class Animal {
    String name;  // Shared field
    
    public void eat() {  // Shared method
        System.out.println(name + " is eating");
    }
    
    public abstract void makeSound();  // Different implementation
}

// 2. Need constructors
abstract class Animal {
    String name;
    
    public Animal(String name) {
        this.name = name;
    }
}

// 3. Need non-public fields/methods
abstract class Animal {
    private String name;  // Private field
    protected void method() { }  // Protected method
}
```

### 6.3 When to Use Interface

```java
// Use interface when:
// 1. Define contract for unrelated classes
interface Flyable {
    void fly();
}

class Bird implements Flyable { }
class Airplane implements Flyable { }

// 2. Need multiple inheritance
interface Flyable { }
interface Swimmable { }
class Duck implements Flyable, Swimmable { }

// 3. Define API contract
interface PaymentProcessor {
    void processPayment(double amount);
}

// 4. Want to achieve polymorphism without inheritance
```

---

## 7. Default Methods in Interfaces

### 7.1 Default Methods (Java 8+)

**Default methods** allow interfaces to have method implementations.

```java
interface Vehicle {
    void start();
    
    // Default method (has implementation)
    default void stop() {
        System.out.println("Vehicle stopped");
    }
}

class Car implements Vehicle {
    @Override
    public void start() {
        System.out.println("Car started");
    }
    // stop() method inherited from interface
}

Car car = new Car();
car.start();  // Car started
car.stop();   // Vehicle stopped (default implementation)
```

### 7.2 Overriding Default Methods

```java
interface Vehicle {
    default void stop() {
        System.out.println("Vehicle stopped");
    }
}

class Car implements Vehicle {
    @Override
    public void stop() {
        System.out.println("Car stopped");
    }
}

Car car = new Car();
car.stop();  // Car stopped (overridden)
```

### 7.3 Multiple Default Methods

```java
interface Flyable {
    default void fly() {
        System.out.println("Flying");
    }
}

interface Swimmable {
    default void swim() {
        System.out.println("Swimming");
    }
}

class Duck implements Flyable, Swimmable {
    // Inherits both default methods
}

Duck duck = new Duck();
duck.fly();   // Flying
duck.swim();  // Swimming
```

### 7.4 Default Method Conflict Resolution

```java
interface A {
    default void method() {
        System.out.println("A");
    }
}

interface B {
    default void method() {
        System.out.println("B");
    }
}

// Must override if both interfaces have same default method
class C implements A, B {
    @Override
    public void method() {
        A.super.method();  // Call A's default method
        B.super.method();  // Call B's default method
        System.out.println("C");
    }
}
```

---

## 8. Static Methods in Interfaces

### 8.1 Static Methods (Java 8+)

**Static methods** in interfaces can be called without implementing the interface.

```java
interface MathUtils {
    static int add(int a, int b) {
        return a + b;
    }
    
    static int multiply(int a, int b) {
        return a * b;
    }
}

// Call without implementation
int sum = MathUtils.add(10, 20);
int product = MathUtils.multiply(5, 6);
```

### 8.2 Static Method Use Cases

```java
// Utility methods
interface StringUtils {
    static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }
}

// Factory methods
interface Animal {
    void makeSound();
    
    static Animal createDog() {
        return new Dog();
    }
    
    static Animal createCat() {
        return new Cat();
    }
}
```

---

## Summary: Part 8

### Key Concepts Covered

1. **Abstraction**: Hiding implementation details
2. **Abstract Classes**: Cannot instantiate, can have abstract/concrete methods
3. **Abstract Methods**: Methods without implementation
4. **Interfaces**: Contracts defining required methods
5. **Interface Implementation**: Classes implementing interfaces
6. **Default Methods**: Interface methods with implementation (Java 8+)
7. **Static Methods**: Interface utility methods (Java 8+)

### Important Points

- Abstract classes share code, interfaces define contracts
- Abstract classes: single inheritance, interfaces: multiple inheritance
- Abstract methods must be implemented by subclasses
- Interfaces can have default and static methods (Java 8+)
- Use abstract class for IS-A relationship with shared code
- Use interface for CAN-DO relationship or multiple inheritance

### Next Steps

**Part 9** will cover:
- Encapsulation Concept
- Access Modifiers
- Getters and Setters
- Data Hiding
- Package Structure
- Best Practices

---

**Master abstraction to build flexible and maintainable Java applications!**

