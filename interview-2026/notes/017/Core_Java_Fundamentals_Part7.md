# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 7: Polymorphism

---

## Table of Contents

1. [Introduction to Polymorphism](#1-introduction-to-polymorphism)
2. [Runtime Polymorphism](#2-runtime-polymorphism)
3. [Method Overriding and Polymorphism](#3-method-overriding-and-polymorphism)
4. [Dynamic Method Dispatch](#4-dynamic-method-dispatch)
5. [Upcasting and Downcasting](#5-upcasting-and-downcasting)
6. [instanceof Operator](#6-instanceof-operator)
7. [Polymorphism Examples](#7-polymorphism-examples)
8. [Benefits of Polymorphism](#8-benefits-of-polymorphism)

---

## 1. Introduction to Polymorphism

### 1.1 What is Polymorphism?

**Polymorphism** means "many forms". It allows objects of different types to be treated as objects of a common type.

**Types of Polymorphism**:
1. **Compile-time Polymorphism**: Method overloading
2. **Runtime Polymorphism**: Method overriding

### 1.2 Polymorphism Example

```java
// One interface, multiple implementations
Animal animal;

animal = new Dog();
animal.makeSound();  // Dog barks

animal = new Cat();
animal.makeSound();  // Cat meows

animal = new Bird();
animal.makeSound();  // Bird chirps
```

---

## 2. Runtime Polymorphism

### 2.1 What is Runtime Polymorphism?

**Runtime Polymorphism** (Dynamic Polymorphism) is achieved through method overriding. The method to be called is determined at runtime based on the actual object type.

### 2.2 Basic Example

```java
class Animal {
    public void makeSound() {
        System.out.println("Animal makes sound");
    }
}

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

// Runtime polymorphism
Animal animal1 = new Dog();
animal1.makeSound();  // Dog barks (determined at runtime)

Animal animal2 = new Cat();
animal2.makeSound();  // Cat meows (determined at runtime)
```

### 2.3 How Runtime Polymorphism Works

```java
// Reference type: Animal
// Object type: Dog
Animal animal = new Dog();

// At compile time: Compiler checks if makeSound() exists in Animal
// At runtime: JVM calls makeSound() from Dog class (actual object type)
animal.makeSound();  // Calls Dog's makeSound()
```

---

## 3. Method Overriding and Polymorphism

### 3.1 Overriding Enables Polymorphism

```java
class Shape {
    public void draw() {
        System.out.println("Drawing shape");
    }
    
    public double calculateArea() {
        return 0.0;
    }
}

class Circle extends Shape {
    private double radius;
    
    public Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing circle");
    }
    
    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}

class Rectangle extends Shape {
    private double width;
    private double height;
    
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing rectangle");
    }
    
    @Override
    public double calculateArea() {
        return width * height;
    }
}

// Polymorphic behavior
Shape shape1 = new Circle(5.0);
shape1.draw();  // Drawing circle
System.out.println("Area: " + shape1.calculateArea());  // 78.54

Shape shape2 = new Rectangle(4.0, 6.0);
shape2.draw();  // Drawing rectangle
System.out.println("Area: " + shape2.calculateArea());  // 24.0
```

### 3.2 Virtual Method Invocation

```java
// In Java, all non-static methods are virtual by default
// Method calls are resolved at runtime based on actual object type

class Parent {
    public void method() {
        System.out.println("Parent method");
    }
}

class Child extends Parent {
    @Override
    public void method() {
        System.out.println("Child method");
    }
}

Parent parent = new Child();
parent.method();  // Child method (virtual method invocation)
```

---

## 4. Dynamic Method Dispatch

### 4.1 What is Dynamic Method Dispatch?

**Dynamic Method Dispatch** is the mechanism by which a call to an overridden method is resolved at runtime rather than compile time.

### 4.2 Example

```java
class Animal {
    public void eat() {
        System.out.println("Animal is eating");
    }
}

class Dog extends Animal {
    @Override
    public void eat() {
        System.out.println("Dog is eating");
    }
    
    public void bark() {
        System.out.println("Dog is barking");
    }
}

class Cat extends Animal {
    @Override
    public void eat() {
        System.out.println("Cat is eating");
    }
    
    public void meow() {
        System.out.println("Cat is meowing");
    }
}

// Dynamic method dispatch
Animal[] animals = {new Dog(), new Cat(), new Dog()};

for (Animal animal : animals) {
    animal.eat();  // Calls appropriate method based on actual object type
}
// Output:
// Dog is eating
// Cat is eating
// Dog is eating
```

### 4.3 Method Resolution Process

```java
// Step 1: Compile time
Animal animal = new Dog();
animal.eat();  // Compiler checks: Does Animal have eat()? Yes ✓

// Step 2: Runtime
// JVM checks actual object type (Dog)
// JVM looks for eat() in Dog class
// If found, calls Dog's eat()
// If not found, looks in parent class
```

---

## 5. Upcasting and Downcasting

### 5.1 Upcasting

**Upcasting**: Converting subclass reference to superclass reference (automatic, safe)

```java
class Animal { }
class Dog extends Animal { }

// Upcasting: Dog reference to Animal reference
Dog dog = new Dog();
Animal animal = dog;  // Upcasting (automatic)
// OR
Animal animal = new Dog();  // Direct upcasting

// Upcasted reference can only access superclass members
animal.eat();  // OK: eat() is in Animal
// animal.bark();  // ERROR: bark() is not in Animal
```

### 5.2 Downcasting

**Downcasting**: Converting superclass reference to subclass reference (explicit, risky)

```java
class Animal { }
class Dog extends Animal {
    public void bark() { }
}

// Downcasting: Animal reference to Dog reference
Animal animal = new Dog();
Dog dog = (Dog) animal;  // Downcasting (explicit cast)
dog.bark();  // OK: Now can access Dog's methods

// Risky: If actual object is not Dog
Animal animal2 = new Animal();
// Dog dog2 = (Dog) animal2;  // ClassCastException at runtime
```

### 5.3 Safe Downcasting with instanceof

```java
Animal animal = new Dog();

if (animal instanceof Dog) {
    Dog dog = (Dog) animal;  // Safe downcast
    dog.bark();
}

// Or using pattern matching (Java 16+)
if (animal instanceof Dog dog) {
    dog.bark();  // No explicit cast needed
}
```

### 5.4 Upcasting and Polymorphism

```java
class Animal {
    public void makeSound() {
        System.out.println("Animal sound");
    }
}

class Dog extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Dog barks");
    }
}

// Upcasting enables polymorphism
Animal animal = new Dog();  // Upcasting
animal.makeSound();  // Dog barks (polymorphic call)
```

---

## 6. instanceof Operator

### 6.1 Basic Usage

```java
class Animal { }
class Dog extends Animal { }
class Cat extends Animal { }

Animal animal = new Dog();

System.out.println(animal instanceof Animal);  // true
System.out.println(animal instanceof Dog);     // true
System.out.println(animal instanceof Cat);     // false
```

### 6.2 Using instanceof for Safe Casting

```java
Animal[] animals = {new Dog(), new Cat(), new Dog()};

for (Animal animal : animals) {
    if (animal instanceof Dog) {
        Dog dog = (Dog) animal;
        dog.bark();
    } else if (animal instanceof Cat) {
        Cat cat = (Cat) animal;
        cat.meow();
    }
}
```

### 6.3 Pattern Matching with instanceof (Java 16+)

```java
// Traditional way
if (animal instanceof Dog) {
    Dog dog = (Dog) animal;
    dog.bark();
}

// Pattern matching (Java 16+)
if (animal instanceof Dog dog) {
    dog.bark();  // dog is automatically cast
}

// Pattern matching in switch (Java 17+)
switch (animal) {
    case Dog dog -> dog.bark();
    case Cat cat -> cat.meow();
    default -> System.out.println("Unknown animal");
}
```

---

## 7. Polymorphism Examples

### 7.1 Shape Example

```java
abstract class Shape {
    public abstract double calculateArea();
    public abstract void draw();
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
    public void draw() {
        System.out.println("Drawing circle with radius " + radius);
    }
}

class Rectangle extends Shape {
    private double width;
    private double height;
    
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return width * height;
    }
    
    @Override
    public void draw() {
        System.out.println("Drawing rectangle " + width + "x" + height);
    }
}

// Polymorphic usage
Shape[] shapes = {
    new Circle(5.0),
    new Rectangle(4.0, 6.0),
    new Circle(3.0)
};

for (Shape shape : shapes) {
    shape.draw();
    System.out.println("Area: " + shape.calculateArea());
}
```

### 7.2 Payment System Example

```java
abstract class PaymentMethod {
    public abstract void processPayment(double amount);
}

class CreditCard extends PaymentMethod {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing credit card payment: $" + amount);
    }
}

class PayPal extends PaymentMethod {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing PayPal payment: $" + amount);
    }
}

class BankTransfer extends PaymentMethod {
    @Override
    public void processPayment(double amount) {
        System.out.println("Processing bank transfer: $" + amount);
    }
}

// Polymorphic payment processing
void processOrder(PaymentMethod payment, double amount) {
    payment.processPayment(amount);  // Calls appropriate method
}

// Usage
processOrder(new CreditCard(), 100.0);
processOrder(new PayPal(), 200.0);
processOrder(new BankTransfer(), 300.0);
```

### 7.3 Employee Management Example

```java
class Employee {
    String name;
    double salary;
    
    public Employee(String name, double salary) {
        this.name = name;
        this.salary = salary;
    }
    
    public double calculateBonus() {
        return salary * 0.1;  // 10% bonus
    }
}

class Manager extends Employee {
    public Manager(String name, double salary) {
        super(name, salary);
    }
    
    @Override
    public double calculateBonus() {
        return salary * 0.2;  // 20% bonus for managers
    }
}

class Developer extends Employee {
    public Developer(String name, double salary) {
        super(name, salary);
    }
    
    @Override
    public double calculateBonus() {
        return salary * 0.15;  // 15% bonus for developers
    }
}

// Polymorphic bonus calculation
Employee[] employees = {
    new Manager("Alice", 100000),
    new Developer("Bob", 80000),
    new Employee("Charlie", 60000)
};

for (Employee emp : employees) {
    System.out.println(emp.name + ": $" + emp.calculateBonus());
}
```

---

## 8. Benefits of Polymorphism

### 8.1 Code Flexibility

```java
// Can easily add new types without changing existing code
class NewShape extends Shape {
    @Override
    public void draw() {
        System.out.println("Drawing new shape");
    }
}

// Existing code works with new type
Shape shape = new NewShape();
shape.draw();  // Works without modifying existing code
```

### 8.2 Code Reusability

```java
// One method works with multiple types
void processShape(Shape shape) {
    shape.draw();
    System.out.println("Area: " + shape.calculateArea());
}

// Works with any Shape subclass
processShape(new Circle(5.0));
processShape(new Rectangle(4.0, 6.0));
processShape(new Triangle(3.0, 4.0, 5.0));
```

### 8.3 Maintainability

```java
// Changes to implementation don't affect interface
// Can modify Circle's draw() without affecting other code
class Circle extends Shape {
    @Override
    public void draw() {
        // New implementation
        // Other code using Shape interface doesn't need changes
    }
}
```

### 8.4 Extensibility

```java
// Easy to extend functionality
interface Drawable {
    void draw();
}

class Circle implements Drawable { }
class Rectangle implements Drawable { }
class Triangle implements Drawable { }

// Can add new Drawable types easily
class Star implements Drawable { }
```

---

## Summary: Part 7

### Key Concepts Covered

1. **Polymorphism**: One interface, multiple implementations
2. **Runtime Polymorphism**: Method overriding, dynamic dispatch
3. **Upcasting**: Subclass to superclass (automatic)
4. **Downcasting**: Superclass to subclass (explicit, risky)
5. **instanceof**: Type checking operator
6. **Dynamic Method Dispatch**: Runtime method resolution

### Important Points

- Polymorphism enables flexible, extensible code
- Method overriding enables runtime polymorphism
- Upcasting is automatic and safe
- Downcasting requires explicit cast and instanceof check
- All non-static methods are virtual in Java
- Polymorphism is key to OOP design

### Next Steps

**Part 8** will cover:
- Abstraction Concept
- Abstract Classes
- Abstract Methods
- Interfaces
- Interface Implementation
- Multiple Interface Implementation

---

**Master polymorphism to build flexible and extensible Java applications!**

