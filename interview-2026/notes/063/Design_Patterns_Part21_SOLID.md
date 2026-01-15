# SOLID Principles: Design Principles Application

## Overview

SOLID is an acronym for five object-oriented design principles that make software designs more understandable, flexible, and maintainable. These principles were introduced by Robert C. Martin and are fundamental to writing clean, maintainable code.

## SOLID Principles Overview

```
┌─────────────────────────────────────────────────────────┐
│         SOLID Principles                                │
└─────────────────────────────────────────────────────────┘

S - Single Responsibility Principle
O - Open/Closed Principle
L - Liskov Substitution Principle
I - Interface Segregation Principle
D - Dependency Inversion Principle
```

## S - Single Responsibility Principle (SRP)

**Definition**: A class should have only one reason to change.

### Violation Example

```java
// BAD: Multiple responsibilities
public class User {
    private String name;
    private String email;
    
    // Responsibility 1: User data
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // Responsibility 2: Database operations
    public void save() {
        // Save to database
    }
    
    public void delete() {
        // Delete from database
    }
    
    // Responsibility 3: Email sending
    public void sendEmail(String message) {
        // Send email
    }
    
    // Responsibility 4: Validation
    public boolean isValid() {
        // Validate user
    }
}
```

### Correct Implementation

```java
// GOOD: Single responsibility
public class User {
    private String name;
    private String email;
    
    // Only user data
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

// Separate class for persistence
public class UserRepository {
    public void save(User user) { }
    public void delete(User user) { }
}

// Separate class for email
public class EmailService {
    public void sendEmail(User user, String message) { }
}

// Separate class for validation
public class UserValidator {
    public boolean isValid(User user) { }
}
```

## O - Open/Closed Principle (OCP)

**Definition**: Software entities should be open for extension but closed for modification.

### Violation Example

```java
// BAD: Must modify class to add new type
public class AreaCalculator {
    public double calculateArea(Object shape) {
        if (shape instanceof Rectangle) {
            Rectangle rect = (Rectangle) shape;
            return rect.getWidth() * rect.getHeight();
        } else if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            return Math.PI * circle.getRadius() * circle.getRadius();
        }
        // Must modify this class to add Triangle
        throw new IllegalArgumentException("Unknown shape");
    }
}
```

### Correct Implementation

```java
// GOOD: Open for extension, closed for modification
public interface Shape {
    double calculateArea();
}

public class Rectangle implements Shape {
    private double width;
    private double height;
    
    @Override
    public double calculateArea() {
        return width * height;
    }
}

public class Circle implements Shape {
    private double radius;
    
    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}

// Can add new shapes without modifying existing code
public class Triangle implements Shape {
    private double base;
    private double height;
    
    @Override
    public double calculateArea() {
        return 0.5 * base * height;
    }
}

public class AreaCalculator {
    public double calculateArea(Shape shape) {
        return shape.calculateArea();  // No modification needed
    }
}
```

## L - Liskov Substitution Principle (LSP)

**Definition**: Objects of a superclass should be replaceable with objects of its subclasses without breaking the application.

### Violation Example

```java
// BAD: Violates LSP
public class Rectangle {
    protected int width;
    protected int height;
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getArea() {
        return width * height;
    }
}

public class Square extends Rectangle {
    @Override
    public void setWidth(int width) {
        this.width = width;
        this.height = width;  // Violates LSP - unexpected behavior
    }
    
    @Override
    public void setHeight(int height) {
        this.width = height;
        this.height = height;  // Violates LSP
    }
}

// This breaks LSP
Rectangle rect = new Square();
rect.setWidth(5);
rect.setHeight(10);
// Expects 50, but gets 100 (square behavior)
```

### Correct Implementation

```java
// GOOD: Proper inheritance hierarchy
public abstract class Shape {
    public abstract int getArea();
}

public class Rectangle extends Shape {
    private int width;
    private int height;
    
    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public int getArea() {
        return width * height;
    }
}

public class Square extends Shape {
    private int side;
    
    public Square(int side) {
        this.side = side;
    }
    
    @Override
    public int getArea() {
        return side * side;
    }
}

// Now substitution works correctly
Shape shape = new Rectangle(5, 10);
int area = shape.getArea();  // 50

Shape shape2 = new Square(5);
int area2 = shape2.getArea();  // 25
```

## I - Interface Segregation Principle (ISP)

**Definition**: Clients should not be forced to depend on interfaces they don't use.

### Violation Example

```java
// BAD: Fat interface
public interface Worker {
    void work();
    void eat();
    void sleep();
}

public class HumanWorker implements Worker {
    public void work() { }
    public void eat() { }
    public void sleep() { }
}

// Robot doesn't need eat() or sleep()
public class RobotWorker implements Worker {
    public void work() { }
    public void eat() {
        throw new UnsupportedOperationException("Robots don't eat");
    }
    public void sleep() {
        throw new UnsupportedOperationException("Robots don't sleep");
    }
}
```

### Correct Implementation

```java
// GOOD: Segregated interfaces
public interface Workable {
    void work();
}

public interface Eatable {
    void eat();
}

public interface Sleepable {
    void sleep();
}

public class HumanWorker implements Workable, Eatable, Sleepable {
    public void work() { }
    public void eat() { }
    public void sleep() { }
}

// Robot only implements what it needs
public class RobotWorker implements Workable {
    public void work() { }
    // No need to implement eat() or sleep()
}
```

## D - Dependency Inversion Principle (DIP)

**Definition**: High-level modules should not depend on low-level modules. Both should depend on abstractions.

### Violation Example

```java
// BAD: High-level depends on low-level
public class UserService {
    private MySQLDatabase database;  // Depends on concrete class
    
    public UserService() {
        this.database = new MySQLDatabase();  // Tight coupling
    }
    
    public void saveUser(User user) {
        database.save(user);
    }
}

public class MySQLDatabase {
    public void save(User user) {
        // MySQL specific code
    }
}
```

### Correct Implementation

```java
// GOOD: Depend on abstraction
public interface UserRepository {
    void save(User user);
    User findById(Long id);
}

public class UserService {
    private UserRepository repository;  // Depends on abstraction
    
    public UserService(UserRepository repository) {
        this.repository = repository;  // Dependency injection
    }
    
    public void saveUser(User user) {
        repository.save(user);
    }
}

// Implementations
public class MySQLUserRepository implements UserRepository {
    public void save(User user) {
        // MySQL implementation
    }
    
    public User findById(Long id) {
        // MySQL implementation
    }
}

public class PostgreSQLUserRepository implements UserRepository {
    public void save(User user) {
        // PostgreSQL implementation
    }
    
    public User findById(Long id) {
        // PostgreSQL implementation
    }
}

// Can switch implementations easily
UserService service = new UserService(new MySQLUserRepository());
// or
UserService service = new UserService(new PostgreSQLUserRepository());
```

## SOLID Principles Summary

```
┌─────────────────────────────────────────────────────────┐
│         SOLID Principles Summary                        │
└─────────────────────────────────────────────────────────┘

S - Single Responsibility
   └─ One class, one responsibility
   └─ One reason to change

O - Open/Closed
   └─ Open for extension
   └─ Closed for modification

L - Liskov Substitution
   └─ Subtypes must be substitutable
   └─ No unexpected behavior

I - Interface Segregation
   └─ Small, focused interfaces
   └─ No unused methods

D - Dependency Inversion
   └─ Depend on abstractions
   └─ Not concrete classes
```

## Benefits of SOLID Principles

```
┌─────────────────────────────────────────────────────────┐
│         SOLID Benefits                                  │
└─────────────────────────────────────────────────────────┘

1. Maintainability
   └─ Easy to understand and modify
   └─ Changes are localized

2. Testability
   └─ Easy to test in isolation
   └─ Mock dependencies easily

3. Flexibility
   └─ Easy to extend functionality
   └─ Switch implementations

4. Reusability
   └─ Components can be reused
   └─ Less duplication

5. Reduced Coupling
   └─ Loose coupling between components
   └─ High cohesion within components
```

## Applying SOLID in Practice

### Example: Payment Processing

```java
// Following SOLID principles

// 1. SRP: Separate concerns
public interface PaymentProcessor {
    PaymentResult process(PaymentRequest request);
}

public class CreditCardProcessor implements PaymentProcessor {
    public PaymentResult process(PaymentRequest request) {
        // Credit card processing
    }
}

public class PayPalProcessor implements PaymentProcessor {
    public PaymentResult process(PaymentRequest request) {
        // PayPal processing
    }
}

// 2. OCP: Open for extension (new processors), closed for modification
// 3. LSP: All processors are substitutable
// 4. ISP: Small, focused interface
// 5. DIP: Service depends on PaymentProcessor interface

public class PaymentService {
    private PaymentProcessor processor;  // DIP: depends on abstraction
    
    public PaymentService(PaymentProcessor processor) {
        this.processor = processor;
    }
    
    public PaymentResult processPayment(PaymentRequest request) {
        return processor.process(request);
    }
}
```

## Summary

SOLID Principles:
- **S - Single Responsibility**: One class, one responsibility
- **O - Open/Closed**: Open for extension, closed for modification
- **L - Liskov Substitution**: Subtypes must be substitutable
- **I - Interface Segregation**: Small, focused interfaces
- **D - Dependency Inversion**: Depend on abstractions

**Key Benefits:**
- ✅ Maintainable code
- ✅ Testable components
- ✅ Flexible design
- ✅ Reusable components
- ✅ Reduced coupling

**Remember**: SOLID principles guide you to write better object-oriented code. Apply them consistently for maintainable, extensible software!
