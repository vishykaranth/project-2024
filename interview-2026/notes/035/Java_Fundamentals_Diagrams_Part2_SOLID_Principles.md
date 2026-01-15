# Java Language Fundamentals - Complete Diagrams Guide (Part 2: SOLID Principles)

## 🏗️ SOLID Principles

---

## 1. SOLID Overview

### SOLID Principles
```
┌─────────────────────────────────────────────────────────────┐
│              SOLID Principles                               │
└─────────────────────────────────────────────────────────────┘

    S ────► Single Responsibility Principle
    │
    O ────► Open/Closed Principle
    │
    L ────► Liskov Substitution Principle
    │
    I ────► Interface Segregation Principle
    │
    D ────► Dependency Inversion Principle
```

---

## 2. Single Responsibility Principle (SRP)

### SRP Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Single Responsibility Principle                │
└─────────────────────────────────────────────────────────────┘

❌ Bad Design:
    ┌──────────────────────┐
    │   User Class         │
    │  - save()            │
    │  - sendEmail()       │
    │  - generateReport() │
    │  - validate()        │
    └──────────────────────┘
    (Too many responsibilities)

✅ Good Design:
    ┌──────────────┐
    │  User        │
    │  - validate() │
    └──────┬───────┘
           │
    ┌──────┴───────┐
    │              │
┌───┴────┐    ┌────┴────┐
│UserRepo│    │EmailService│
│-save() │    │-sendEmail()│
└────────┘    └───────────┘
```

### SRP Example

#### ❌ Violation
```java
// Bad: Multiple responsibilities
class User {
    private String name;
    private String email;
    
    // Responsibility 1: User data management
    public void setName(String name) { this.name = name; }
    
    // Responsibility 2: Database operations
    public void saveToDatabase() {
        // Database save logic
    }
    
    // Responsibility 3: Email operations
    public void sendEmail(String message) {
        // Email sending logic
    }
    
    // Responsibility 4: Report generation
    public void generateReport() {
        // Report generation logic
    }
}
```

#### ✅ Correct Implementation
```java
// Good: Single responsibility per class
class User {
    private String name;
    private String email;
    
    // Only user data management
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
}

class UserRepository {
    // Only database operations
    public void save(User user) {
        // Database save logic
    }
}

class EmailService {
    // Only email operations
    public void sendEmail(User user, String message) {
        // Email sending logic
    }
}

class ReportGenerator {
    // Only report generation
    public void generateReport(User user) {
        // Report generation logic
    }
}
```

---

## 3. Open/Closed Principle (OCP)

### OCP Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Open/Closed Principle                          │
└─────────────────────────────────────────────────────────────┘

    Open for Extension
    ┌─────────────────┐
    │   Base Class    │
    │   (Closed for   │
    │   modification) │
    └────────┬────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───┴────┐      ┌────┴────┐
│Extended│      │Extended │
│Class 1 │      │Class 2  │
└────────┘      └─────────┘

✅ Extend functionality without modifying existing code
❌ Don't modify existing classes for new features
```

### OCP Example

#### ❌ Violation
```java
// Bad: Modifying existing class for new features
class Shape {
    private String type;
    
    public double calculateArea() {
        if (type.equals("circle")) {
            // Circle area calculation
        } else if (type.equals("rectangle")) {
            // Rectangle area calculation
        } else if (type.equals("triangle")) {
            // Triangle area calculation
        }
        // Adding new shape requires modifying this method
    }
}
```

#### ✅ Correct Implementation
```java
// Good: Open for extension, closed for modification
abstract class Shape {
    public abstract double calculateArea();
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
}

// New shape can be added without modifying existing code
class Triangle extends Shape {
    private double base;
    private double height;
    
    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }
    
    @Override
    public double calculateArea() {
        return 0.5 * base * height;
    }
}
```

---

## 4. Liskov Substitution Principle (LSP)

### LSP Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Liskov Substitution Principle                  │
└─────────────────────────────────────────────────────────────┘

    Parent Class
    ┌──────────┐
    │  Bird    │
    │  fly()   │
    └────┬─────┘
         │
    ┌────┴────┐
    │         │
  Sparrow   Penguin
  ┌────┐    ┌──────┐
  │fly()│    │fly() │ ❌
  └────┘    └──────┘
            (Violates LSP)

✅ Subtypes must be substitutable for their base types
❌ Subclass shouldn't break parent's contract
```

### LSP Example

#### ❌ Violation
```java
// Bad: Violates LSP
class Bird {
    public void fly() {
        System.out.println("Flying");
    }
}

class Sparrow extends Bird {
    @Override
    public void fly() {
        System.out.println("Sparrow flying");
    }
}

class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Penguins can't fly!");
        // This breaks the contract - violates LSP
    }
}

// Usage
void makeBirdFly(Bird bird) {
    bird.fly();  // Might throw exception if Penguin!
}
```

#### ✅ Correct Implementation
```java
// Good: Proper inheritance hierarchy
class Bird {
    public void eat() {
        System.out.println("Eating");
    }
}

class FlyingBird extends Bird {
    public void fly() {
        System.out.println("Flying");
    }
}

class Sparrow extends FlyingBird {
    @Override
    public void fly() {
        System.out.println("Sparrow flying");
    }
}

class Penguin extends Bird {
    public void swim() {
        System.out.println("Swimming");
    }
    // No fly() method - correct design
}

// Usage
void makeBirdFly(FlyingBird bird) {
    bird.fly();  // Safe - all FlyingBird can fly
}
```

### LSP Rectangle-Square Problem
```
┌─────────────────────────────────────────────────────────────┐
│              Rectangle-Square Problem                       │
└─────────────────────────────────────────────────────────────┘

❌ Common Mistake:
    Rectangle
        │
        └──► Square (is-a Rectangle?)
        
Problem:
- Rectangle: setWidth() and setHeight() independent
- Square: setWidth() must also set height
- Violates LSP: Square cannot substitute Rectangle

✅ Solution:
    Shape
    │
    ├──► Rectangle
    └──► Square
    
    Or use composition instead of inheritance
```

---

## 5. Interface Segregation Principle (ISP)

### ISP Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Interface Segregation Principle                │
└─────────────────────────────────────────────────────────────┘

❌ Bad Design:
    ┌──────────────────────┐
    │  Worker Interface     │
    │  - work()            │
    │  - eat()             │
    │  - sleep()           │
    └──────────────────────┘
           │
    ┌──────┴──────┐
    │             │
  Human         Robot
  (implements   (implements
   all methods)  all methods)
                 │
                 │ Robot forced to
                 │ implement eat()
                 │ and sleep() ❌

✅ Good Design:
    ┌──────────┐    ┌──────────┐
    │  Workable│    │  Eatable │
    │  -work() │    │  -eat()  │
    └────┬─────┘    └────┬─────┘
         │               │
    ┌────┴────┐      ┌───┴────┐
    │         │      │        │
  Human     Robot   Human   (Robot
  (implements)      (implements)  doesn't
                            implement)
```

### ISP Example

#### ❌ Violation
```java
// Bad: Fat interface
interface Worker {
    void work();
    void eat();
    void sleep();
}

class Human implements Worker {
    @Override
    public void work() { /* ... */ }
    
    @Override
    public void eat() { /* ... */ }
    
    @Override
    public void sleep() { /* ... */ }
}

class Robot implements Worker {
    @Override
    public void work() { /* ... */ }
    
    @Override
    public void eat() {
        // Robot doesn't eat - forced to implement
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void sleep() {
        // Robot doesn't sleep - forced to implement
        throw new UnsupportedOperationException();
    }
}
```

#### ✅ Correct Implementation
```java
// Good: Segregated interfaces
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

interface Sleepable {
    void sleep();
}

class Human implements Workable, Eatable, Sleepable {
    @Override
    public void work() { /* ... */ }
    
    @Override
    public void eat() { /* ... */ }
    
    @Override
    public void sleep() { /* ... */ }
}

class Robot implements Workable {
    @Override
    public void work() { /* ... */ }
    // No need to implement eat() or sleep()
}
```

---

## 6. Dependency Inversion Principle (DIP)

### DIP Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Dependency Inversion Principle                 │
└─────────────────────────────────────────────────────────────┘

❌ Bad Design:
    High-Level Module
    ┌──────────────┐
    │  UserService │
    └──────┬───────┘
           │ depends on
           ▼
    Low-Level Module
    ┌──────────────┐
    │  MySQLDB     │
    └──────────────┘
    
    (Tight coupling)

✅ Good Design:
    High-Level Module
    ┌──────────────┐
    │  UserService │
    └──────┬───────┘
           │ depends on
           ▼
    ┌──────────────┐
    │  Database    │  (Abstraction)
    │  Interface   │
    └──────┬───────┘
           │
    ┌──────┴──────┐
    │             │
┌───┴────┐    ┌───┴────┐
│ MySQLDB│    │PostgreSQL│
└────────┘    └─────────┘
    
    (Loose coupling via abstraction)
```

### DIP Example

#### ❌ Violation
```java
// Bad: High-level depends on low-level
class UserService {
    private MySQLDatabase database;  // Direct dependency
    
    public UserService() {
        this.database = new MySQLDatabase();
    }
    
    public void saveUser(User user) {
        database.save(user);
    }
}
```

#### ✅ Correct Implementation
```java
// Good: Depend on abstraction
interface Database {
    void save(User user);
    User findById(String id);
}

class MySQLDatabase implements Database {
    @Override
    public void save(User user) {
        // MySQL implementation
    }
    
    @Override
    public User findById(String id) {
        // MySQL implementation
        return null;
    }
}

class PostgreSQLDatabase implements Database {
    @Override
    public void save(User user) {
        // PostgreSQL implementation
    }
    
    @Override
    public User findById(String id) {
        // PostgreSQL implementation
        return null;
    }
}

// High-level depends on abstraction
class UserService {
    private Database database;  // Dependency on abstraction
    
    public UserService(Database database) {  // Dependency Injection
        this.database = database;
    }
    
    public void saveUser(User user) {
        database.save(user);
    }
}

// Usage
Database db = new MySQLDatabase();  // or new PostgreSQLDatabase()
UserService service = new UserService(db);
```

---

## 7. SOLID Principles Summary

### SOLID Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              SOLID Benefits                                │
└─────────────────────────────────────────────────────────────┘

S - Single Responsibility
    ✅ Easier to understand
    ✅ Easier to test
    ✅ Easier to maintain

O - Open/Closed
    ✅ Extensible without modification
    ✅ Reduces risk of bugs
    ✅ Stable codebase

L - Liskov Substitution
    ✅ Correct inheritance
    ✅ Polymorphism works correctly
    ✅ Contract compliance

I - Interface Segregation
    ✅ No forced implementations
    ✅ Focused interfaces
    ✅ Better design

D - Dependency Inversion
    ✅ Loose coupling
    ✅ Testable code
    ✅ Flexible architecture
```

### SOLID in Practice
```
┌─────────────────────────────────────────────────────────────┐
│              Applying SOLID                                │
└─────────────────────────────────────────────────────────────┘

1. Start with SRP
   - One class, one reason to change

2. Apply OCP
   - Use interfaces/abstract classes
   - Extend, don't modify

3. Ensure LSP
   - Subtypes must be substitutable
   - Don't break contracts

4. Segregate Interfaces
   - Small, focused interfaces
   - Clients only depend on what they need

5. Invert Dependencies
   - Depend on abstractions
   - Use Dependency Injection
```

---

## Key Takeaways

### SOLID Principles
```
Single Responsibility:
- One class should have one reason to change
- Separate concerns

Open/Closed:
- Open for extension
- Closed for modification
- Use inheritance/polymorphism

Liskov Substitution:
- Subtypes must be substitutable
- Don't break parent contracts

Interface Segregation:
- Many specific interfaces
- Not one general interface
- Clients shouldn't depend on unused methods

Dependency Inversion:
- Depend on abstractions
- Not on concrete classes
- Use Dependency Injection
```

---

**Next: Part 3 will cover Generics & Type Safety.**

