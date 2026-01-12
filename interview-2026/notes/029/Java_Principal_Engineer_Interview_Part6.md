# Java Principal Engineer Interview Questions - Part 6

## Design Patterns & Best Practices

This part covers advanced design patterns, SOLID principles, code quality, and architectural best practices.

---

## 1. SOLID Principles

### Q1: Explain SOLID principles with practical Java examples. How do they apply to real-world scenarios?

**Answer:**

**1. Single Responsibility Principle (SRP)**

```java
// BAD: Multiple responsibilities
public class User {
    private String name;
    private String email;
    
    // Responsibility 1: User data
    public void setName(String name) { this.name = name; }
    
    // Responsibility 2: Database operations
    public void save() {
        // Database code
    }
    
    // Responsibility 3: Email sending
    public void sendEmail() {
        // Email code
    }
    
    // Responsibility 4: Validation
    public boolean isValid() {
        // Validation code
    }
}

// GOOD: Single responsibility per class
public class User {
    private String name;
    private String email;
    
    // Only user data
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
}

public class UserRepository {
    // Only database operations
    public void save(User user) {
        // Database code
    }
}

public class EmailService {
    // Only email operations
    public void sendEmail(User user, String message) {
        // Email code
    }
}

public class UserValidator {
    // Only validation
    public boolean isValid(User user) {
        // Validation code
    }
}
```

**2. Open/Closed Principle (OCP)**

```java
// BAD: Modify existing code for new features
public class AreaCalculator {
    public double calculateArea(Object shape) {
        if (shape instanceof Rectangle) {
            Rectangle rect = (Rectangle) shape;
            return rect.width * rect.height;
        } else if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            return Math.PI * circle.radius * circle.radius;
        }
        // Adding new shape requires modifying this method
        throw new IllegalArgumentException();
    }
}

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

// New shapes can be added without modifying existing code
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

**3. Liskov Substitution Principle (LSP)**

```java
// BAD: Subclass violates parent contract
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
        this.height = width;  // Violates LSP: changes behavior
    }
    
    @Override
    public void setHeight(int height) {
        this.width = height;
        this.height = height;  // Violates LSP
    }
}

// GOOD: Subclass maintains parent contract
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
```

**4. Interface Segregation Principle (ISP)**

```java
// BAD: Fat interface
public interface Worker {
    void work();
    void eat();
    void sleep();
}

public class Human implements Worker {
    public void work() { }
    public void eat() { }
    public void sleep() { }
}

public class Robot implements Worker {
    public void work() { }
    public void eat() { }  // Robot doesn't eat - forced to implement
    public void sleep() { }  // Robot doesn't sleep - forced to implement
}

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

public class Human implements Workable, Eatable, Sleepable {
    public void work() { }
    public void eat() { }
    public void sleep() { }
}

public class Robot implements Workable {
    public void work() { }  // Only implements what it needs
}
```

**5. Dependency Inversion Principle (DIP)**

```java
// BAD: High-level depends on low-level
public class UserService {
    private MySQLUserRepository repository;  // Depends on concrete class
    
    public User getUser(Long id) {
        return repository.findById(id);
    }
}

// GOOD: Depend on abstractions
public class UserService {
    private UserRepository repository;  // Depends on interface
    
    public UserService(UserRepository repository) {
        this.repository = repository;  // Dependency injection
    }
    
    public User getUser(Long id) {
        return repository.findById(id);
    }
}

public interface UserRepository {
    User findById(Long id);
}

public class MySQLUserRepository implements UserRepository {
    public User findById(Long id) {
        // MySQL implementation
    }
}

public class PostgreSQLUserRepository implements UserRepository {
    public User findById(Long id) {
        // PostgreSQL implementation
    }
}
```

---

### Q2: Explain advanced design patterns used in enterprise applications (Strategy, Observer, Factory, Builder, Singleton variations).

**Answer:**

**1. Strategy Pattern (Runtime Algorithm Selection)**

```java
// Payment processing with different strategies
public interface PaymentStrategy {
    void pay(double amount);
}

public class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    
    @Override
    public void pay(double amount) {
        // Credit card processing
    }
}

public class PayPalPayment implements PaymentStrategy {
    private String email;
    
    @Override
    public void pay(double amount) {
        // PayPal processing
    }
}

public class PaymentProcessor {
    private PaymentStrategy strategy;
    
    public PaymentProcessor(PaymentStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;  // Can change at runtime
    }
    
    public void processPayment(double amount) {
        strategy.pay(amount);
    }
}

// Usage
PaymentProcessor processor = new PaymentProcessor(new CreditCardPayment());
processor.processPayment(100.0);

processor.setStrategy(new PayPalPayment());  // Switch strategy
processor.processPayment(200.0);
```

**2. Observer Pattern (Event-Driven Architecture)**

```java
public interface EventListener {
    void onEvent(Event event);
}

public class OrderService {
    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(EventListener listener) {
        listeners.add(listener);
    }
    
    public void createOrder(Order order) {
        // Create order
        orderRepository.save(order);
        
        // Notify listeners
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}

// Listeners
public class InventoryService implements EventListener {
    @Override
    public void onEvent(Event event) {
        if (event instanceof OrderCreatedEvent) {
            updateInventory((OrderCreatedEvent) event);
        }
    }
}

public class NotificationService implements EventListener {
    @Override
    public void onEvent(Event event) {
        if (event instanceof OrderCreatedEvent) {
            sendNotification((OrderCreatedEvent) event);
        }
    }
}
```

**3. Factory Pattern (Object Creation)**

```java
// Simple Factory
public class PaymentFactory {
    public static PaymentStrategy createPayment(String type) {
        switch (type) {
            case "credit":
                return new CreditCardPayment();
            case "paypal":
                return new PayPalPayment();
            default:
                throw new IllegalArgumentException("Unknown payment type");
        }
    }
}

// Factory Method
public abstract class PaymentProcessorFactory {
    public abstract PaymentStrategy createPayment();
    
    public void processPayment(double amount) {
        PaymentStrategy payment = createPayment();
        payment.pay(amount);
    }
}

public class CreditCardProcessorFactory extends PaymentProcessorFactory {
    @Override
    public PaymentStrategy createPayment() {
        return new CreditCardPayment();
    }
}

// Abstract Factory
public interface PaymentFactory {
    PaymentStrategy createPayment();
    RefundStrategy createRefund();
}

public class CreditCardFactory implements PaymentFactory {
    public PaymentStrategy createPayment() {
        return new CreditCardPayment();
    }
    
    public RefundStrategy createRefund() {
        return new CreditCardRefund();
    }
}
```

**4. Builder Pattern (Complex Object Construction)**

```java
public class User {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final int age;
    private final String phone;
    private final List<String> roles;
    
    private User(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.age = builder.age;
        this.phone = builder.phone;
        this.roles = builder.roles;
    }
    
    public static class Builder {
        private String firstName;
        private String lastName;
        private String email;
        private int age;
        private String phone;
        private List<String> roles = new ArrayList<>();
        
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder age(int age) {
            this.age = age;
            return this;
        }
        
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Builder addRole(String role) {
            this.roles.add(role);
            return this;
        }
        
        public User build() {
            // Validation
            if (firstName == null || lastName == null) {
                throw new IllegalArgumentException("Name required");
            }
            return new User(this);
        }
    }
}

// Usage
User user = new User.Builder()
    .firstName("John")
    .lastName("Doe")
    .email("john@example.com")
    .age(30)
    .addRole("USER")
    .addRole("ADMIN")
    .build();
```

**5. Singleton Variations**

```java
// 1. Eager Singleton
public class EagerSingleton {
    private static final EagerSingleton instance = new EagerSingleton();
    
    private EagerSingleton() { }
    
    public static EagerSingleton getInstance() {
        return instance;
    }
}

// 2. Lazy Singleton (Thread-safe)
public class LazySingleton {
    private static volatile LazySingleton instance;
    
    private LazySingleton() { }
    
    public static LazySingleton getInstance() {
        if (instance == null) {
            synchronized (LazySingleton.class) {
                if (instance == null) {
                    instance = new LazySingleton();
                }
            }
        }
        return instance;
    }
}

// 3. Enum Singleton (Best practice)
public enum EnumSingleton {
    INSTANCE;
    
    public void doSomething() {
        // Implementation
    }
}

// 4. Initialization-on-demand Holder (Thread-safe, lazy)
public class HolderSingleton {
    private HolderSingleton() { }
    
    private static class Holder {
        private static final HolderSingleton instance = new HolderSingleton();
    }
    
    public static HolderSingleton getInstance() {
        return Holder.instance;
    }
}
```

---

## Summary: Part 6

### Key Topics Covered:
1. SOLID Principles
2. Advanced Design Patterns

### Principal Engineer Focus:
- Code quality and maintainability
- Design pattern selection
- Architectural decisions
- Best practices enforcement

---

**Next**: Part 7 will cover Leadership & Mentoring.

