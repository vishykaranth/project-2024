# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 6: Inheritance

---

## Table of Contents

1. [Introduction to Inheritance](#1-introduction-to-inheritance)
2. [extends Keyword](#2-extends-keyword)
3. [super Keyword](#3-super-keyword)
4. [Method Overriding](#4-method-overriding)
5. [final Keyword](#5-final-keyword)
6. [Object Class](#6-object-class)
7. [Inheritance Types](#7-inheritance-types)
8. [Best Practices](#8-best-practices)

---

## 1. Introduction to Inheritance

### 1.1 What is Inheritance?

**Inheritance** is a mechanism where a new class (child/subclass) acquires the properties and behaviors of an existing class (parent/superclass).

**Benefits**:
- **Code Reusability**: Reuse existing code
- **Code Organization**: Hierarchical structure
- **Polymorphism**: Enable runtime polymorphism
- **Maintainability**: Easier to maintain and extend

### 1.2 Inheritance Terminology

- **Superclass/Parent Class/Base Class**: The class being inherited from
- **Subclass/Child Class/Derived Class**: The class that inherits
- **IS-A Relationship**: Subclass IS-A type of superclass

### 1.3 Inheritance Example

```java
// Parent class
class Animal {
    String name;
    
    public void eat() {
        System.out.println(name + " is eating");
    }
    
    public void sleep() {
        System.out.println(name + " is sleeping");
    }
}

// Child class
class Dog extends Animal {
    public void bark() {
        System.out.println(name + " is barking");
    }
}

// Dog IS-A Animal
Dog dog = new Dog();
dog.name = "Buddy";
dog.eat();    // Inherited from Animal
dog.sleep();  // Inherited from Animal
dog.bark();   // Own method
```

---

## 2. extends Keyword

### 2.1 Basic Inheritance Syntax

```java
class ParentClass {
    // Parent class members
}

class ChildClass extends ParentClass {
    // Child class members
    // Inherits all non-private members from ParentClass
}
```

### 2.2 Simple Inheritance Example

```java
// Parent class
class Vehicle {
    String brand;
    int year;
    
    public void start() {
        System.out.println("Vehicle is starting");
    }
    
    public void stop() {
        System.out.println("Vehicle is stopping");
    }
}

// Child class
class Car extends Vehicle {
    int numberOfDoors;
    
    public void honk() {
        System.out.println("Car is honking");
    }
}

// Usage
Car car = new Car();
car.brand = "Toyota";        // Inherited from Vehicle
car.year = 2024;             // Inherited from Vehicle
car.numberOfDoors = 4;       // Own property
car.start();                 // Inherited method
car.stop();                  // Inherited method
car.honk();                  // Own method
```

### 2.3 Multi-Level Inheritance

```java
// Grandparent class
class Animal {
    String name;
    
    public void eat() {
        System.out.println(name + " is eating");
    }
}

// Parent class
class Mammal extends Animal {
    public void breathe() {
        System.out.println(name + " is breathing");
    }
}

// Child class
class Dog extends Mammal {
    public void bark() {
        System.out.println(name + " is barking");
    }
}

// Dog inherits from Mammal, which inherits from Animal
Dog dog = new Dog();
dog.name = "Buddy";
dog.eat();      // From Animal
dog.breathe();  // From Mammal
dog.bark();     // Own method
```

### 2.4 What is Inherited?

**Inherited**:
- Public and protected fields
- Public and protected methods
- Package-private members (if in same package)

**Not Inherited**:
- Private members
- Constructors (but can be called)
- Static members (belong to class, not instance)

### 2.5 Inheritance Chain

```java
class A {
    public void methodA() {
        System.out.println("Method A");
    }
}

class B extends A {
    public void methodB() {
        System.out.println("Method B");
    }
}

class C extends B {
    public void methodC() {
        System.out.println("Method C");
    }
}

// C has access to all methods
C obj = new C();
obj.methodA();  // From A
obj.methodB();  // From B
obj.methodC();  // Own method
```

---

## 3. super Keyword

### 3.1 super for Parent Class Members

```java
class Animal {
    String name = "Animal";
    
    public void display() {
        System.out.println("Animal: " + name);
    }
}

class Dog extends Animal {
    String name = "Dog";
    
    public void display() {
        System.out.println("Dog: " + name);
        System.out.println("Animal: " + super.name);  // Access parent's name
        super.display();  // Call parent's method
    }
}

Dog dog = new Dog();
dog.display();
// Output:
// Dog: Dog
// Animal: Animal
// Animal: Animal
```

### 3.2 super to Call Parent Constructor

```java
class Animal {
    String name;
    
    public Animal(String name) {
        this.name = name;
        System.out.println("Animal constructor");
    }
}

class Dog extends Animal {
    String breed;
    
    public Dog(String name, String breed) {
        super(name);  // Must be first statement
        this.breed = breed;
        System.out.println("Dog constructor");
    }
}

Dog dog = new Dog("Buddy", "Golden Retriever");
// Output:
// Animal constructor
// Dog constructor
```

### 3.3 super Constructor Rules

```java
// Rule 1: super() must be first statement
class Child extends Parent {
    public Child() {
        super();  // Must be first
        // Other code
    }
}

// Rule 2: If no super() call, default super() is called
class Child extends Parent {
    public Child() {
        // super() is called implicitly
    }
}

// Rule 3: If parent has no default constructor, must call super()
class Parent {
    public Parent(int x) { }  // No default constructor
}

class Child extends Parent {
    public Child() {
        super(10);  // Must call super with parameter
    }
}
```

### 3.4 super vs this

```java
class Parent {
    String name = "Parent";
    
    public void display() {
        System.out.println("Parent display");
    }
}

class Child extends Parent {
    String name = "Child";
    
    public void display() {
        System.out.println("Child display");
    }
    
    public void show() {
        System.out.println(this.name);   // Child
        System.out.println(super.name);  // Parent
        this.display();   // Child's display
        super.display();  // Parent's display
    }
}
```

---

## 4. Method Overriding

### 4.1 What is Method Overriding?

**Method Overriding** allows a subclass to provide a specific implementation of a method that is already defined in its superclass.

**Rules**:
- Method name must be same
- Parameters must be same
- Return type must be same (or covariant)
- Access modifier cannot be more restrictive
- Cannot override static, final, or private methods

### 4.2 Basic Overriding

```java
class Animal {
    public void makeSound() {
        System.out.println("Animal makes sound");
    }
}

class Dog extends Animal {
    @Override  // Annotation (optional but recommended)
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

Animal animal = new Animal();
animal.makeSound();  // Animal makes sound

Dog dog = new Dog();
dog.makeSound();     // Dog barks (overridden)

Cat cat = new Cat();
cat.makeSound();     // Cat meows (overridden)
```

### 4.3 @Override Annotation

```java
class Parent {
    public void method() {
        System.out.println("Parent method");
    }
}

class Child extends Parent {
    @Override  // Compiler checks if method is actually overriding
    public void method() {
        System.out.println("Child method");
    }
    
    // @Override
    // public void method2() { }  // ERROR: Not overriding anything
}
```

### 4.4 Access Modifier in Overriding

```java
class Parent {
    protected void method() { }  // Protected
}

class Child extends Parent {
    @Override
    public void method() { }  // OK: Can be more accessible (public)
    
    // @Override
    // private void method() { }  // ERROR: Cannot be more restrictive
}
```

### 4.5 Covariant Return Types

```java
class Parent {
    public Animal getAnimal() {
        return new Animal();
    }
}

class Child extends Parent {
    @Override
    public Dog getAnimal() {  // OK: Dog is subclass of Animal (covariant)
        return new Dog();
    }
}
```

### 4.6 Overriding vs Overloading

```java
class Parent {
    public void method(int x) {
        System.out.println("Parent: " + x);
    }
}

class Child extends Parent {
    // Overriding: Same signature
    @Override
    public void method(int x) {
        System.out.println("Child: " + x);
    }
    
    // Overloading: Different signature
    public void method(String s) {
        System.out.println("Child: " + s);
    }
}
```

---

## 5. final Keyword

### 5.1 final Variable

```java
class Example {
    final int VALUE = 10;  // Must be initialized
    
    public void method() {
        // VALUE = 20;  // ERROR: Cannot modify final variable
        final int local = 5;  // Final local variable
    }
}
```

### 5.2 final Method

```java
class Parent {
    public final void method() {  // Cannot be overridden
        System.out.println("Parent method");
    }
}

class Child extends Parent {
    // @Override
    // public void method() { }  // ERROR: Cannot override final method
}
```

### 5.3 final Class

```java
final class Parent {  // Cannot be extended
    public void method() {
        System.out.println("Parent method");
    }
}

// class Child extends Parent { }  // ERROR: Cannot extend final class
```

### 5.4 final Parameters

```java
public void method(final int x) {
    // x = 10;  // ERROR: Cannot modify final parameter
    System.out.println(x);
}
```

---

## 6. Object Class

### 6.1 Object Class Overview

**Object** is the root class of all classes in Java. Every class implicitly extends Object.

```java
// These are equivalent:
class MyClass { }
class MyClass extends Object { }  // Implicit
```

### 6.2 Important Object Methods

**1. toString()**
```java
class Student {
    String name;
    int age;
    
    @Override
    public String toString() {
        return "Student{name='" + name + "', age=" + age + "}";
    }
}

Student student = new Student();
student.name = "John";
student.age = 20;
System.out.println(student);  // Calls toString()
// Output: Student{name='John', age=20}
```

**2. equals()**
```java
class Student {
    String name;
    int age;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return age == student.age && Objects.equals(name, student.name);
    }
}

Student s1 = new Student("John", 20);
Student s2 = new Student("John", 20);
System.out.println(s1.equals(s2));  // true
```

**3. hashCode()**
```java
class Student {
    String name;
    int age;
    
    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}

// If equals() is overridden, hashCode() should also be overridden
```

**4. getClass()**
```java
Student student = new Student();
Class<?> clazz = student.getClass();
System.out.println(clazz.getName());  // Student
```

**5. clone()**
```java
class Student implements Cloneable {
    String name;
    int age;
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
```

---

## 7. Inheritance Types

### 7.1 Single Inheritance

```java
// Java supports single inheritance (one parent)
class Animal { }
class Dog extends Animal { }  // Single inheritance
```

### 7.2 Multi-Level Inheritance

```java
class Animal { }
class Mammal extends Animal { }
class Dog extends Mammal { }  // Multi-level
```

### 7.3 Hierarchical Inheritance

```java
class Animal { }
class Dog extends Animal { }
class Cat extends Animal { }
class Bird extends Animal { }  // Multiple children, one parent
```

### 7.4 Multiple Inheritance (Not Supported)

```java
// Java does NOT support multiple inheritance for classes
// class Child extends Parent1, Parent2 { }  // ERROR

// But interfaces support multiple inheritance
interface Interface1 { }
interface Interface2 { }
interface Interface3 extends Interface1, Interface2 { }  // OK
```

---

## 8. Best Practices

### 8.1 Use Inheritance for IS-A Relationship

```java
// Good: Dog IS-A Animal
class Animal { }
class Dog extends Animal { }  // Correct

// Bad: Car IS-A Engine (wrong relationship)
class Engine { }
// class Car extends Engine { }  // Wrong: Use composition instead
```

### 8.2 Favor Composition Over Inheritance

```java
// Composition: Has-A relationship
class Car {
    private Engine engine;  // Car HAS-A Engine
    // Better than Car extends Engine
}

// Inheritance: IS-A relationship
class Dog extends Animal { }  // Dog IS-A Animal
```

### 8.3 Don't Override for the Sake of Overriding

```java
// Only override when you need different behavior
class Parent {
    public void method() {
        // Implementation
    }
}

class Child extends Parent {
    // Don't override if behavior is same
    // Only override if you need different implementation
}
```

### 8.4 Use @Override Annotation

```java
class Child extends Parent {
    @Override  // Always use this annotation
    public void method() {
        // Implementation
    }
}
```

### 8.5 Keep Inheritance Hierarchy Shallow

```java
// Avoid deep inheritance hierarchies
// A -> B -> C -> D -> E  // Too deep, hard to maintain

// Prefer shallow hierarchies
// A -> B -> C  // Better
```

---

## Summary: Part 6

### Key Concepts Covered

1. **Inheritance**: Mechanism to reuse code
2. **extends Keyword**: Create inheritance relationship
3. **super Keyword**: Access parent class members
4. **Method Overriding**: Provide specific implementation
5. **final Keyword**: Prevent modification/inheritance
6. **Object Class**: Root of all classes
7. **Inheritance Types**: Single, multi-level, hierarchical

### Important Points

- Java supports single inheritance for classes
- Use inheritance for IS-A relationships
- super() must be first statement in constructor
- Override methods when behavior needs to change
- Always use @Override annotation
- Object class is parent of all classes

### Next Steps

**Part 7** will cover:
- Polymorphism Concept
- Runtime Polymorphism
- Method Overriding and Polymorphism
- Dynamic Method Dispatch
- Upcasting and Downcasting
- instanceof Operator

---

**Master inheritance to build reusable and maintainable code!**

