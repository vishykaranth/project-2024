# Code Smells: Identifying and Refactoring Bad Code

## Overview

Code smells are surface indications that usually correspond to deeper problems in the system. They are not bugs - the code works - but they indicate design problems that make the code harder to maintain, understand, and extend. Recognizing and addressing code smells improves code quality.

## Code Smell Categories

```
┌─────────────────────────────────────────────────────────┐
│         Code Smell Categories                           │
└─────────────────────────────────────────────────────────┘

1. Bloaters
   └─ Long methods, large classes, too many parameters

2. Object-Orientation Abusers
   └─ Switch statements, refused bequest, alternative classes

3. Change Preventers
   └─ Divergent change, shotgun surgery, parallel inheritance

4. Dispensables
   └─ Dead code, duplicate code, lazy class

5. Couplers
   └─ Feature envy, inappropriate intimacy, message chains
```

## Bloaters

### 1. Long Method

**Smell**: Method is too long (50+ lines)

```java
// BAD: Long method
public void processOrder(Order order) {
    // Validate order
    if (order == null) {
        throw new IllegalArgumentException("Order cannot be null");
    }
    if (order.getItems() == null || order.getItems().isEmpty()) {
        throw new IllegalArgumentException("Order must have items");
    }
    for (OrderItem item : order.getItems()) {
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid price");
        }
    }
    
    // Calculate total
    BigDecimal total = BigDecimal.ZERO;
    for (OrderItem item : order.getItems()) {
        BigDecimal itemTotal = item.getPrice().multiply(
            BigDecimal.valueOf(item.getQuantity()));
        total = total.add(itemTotal);
    }
    
    // Apply discount
    if (order.getDiscountCode() != null) {
        Discount discount = discountService.getDiscount(order.getDiscountCode());
        if (discount != null && discount.isValid()) {
            BigDecimal discountAmount = total.multiply(
                BigDecimal.valueOf(discount.getPercentage()));
            total = total.subtract(discountAmount);
        }
    }
    
    // Process payment
    PaymentResult result = paymentService.processPayment(
        order.getPaymentMethod(), total);
    if (!result.isSuccess()) {
        throw new PaymentException("Payment failed");
    }
    
    // Save order
    order.setTotal(total);
    order.setStatus(OrderStatus.CONFIRMED);
    orderRepository.save(order);
    
    // Send confirmation
    emailService.sendOrderConfirmation(order.getCustomerEmail(), order);
    
    // Update inventory
    for (OrderItem item : order.getItems()) {
        inventoryService.reserve(item.getProductId(), item.getQuantity());
    }
}

// GOOD: Broken into smaller methods
public void processOrder(Order order) {
    validateOrder(order);
    calculateTotal(order);
    applyDiscount(order);
    processPayment(order);
    saveOrder(order);
    sendConfirmation(order);
    updateInventory(order);
}

private void validateOrder(Order order) {
    if (order == null) {
        throw new IllegalArgumentException("Order cannot be null");
    }
    if (order.getItems() == null || order.getItems().isEmpty()) {
        throw new IllegalArgumentException("Order must have items");
    }
    validateOrderItems(order.getItems());
}

private void validateOrderItems(List<OrderItem> items) {
    for (OrderItem item : items) {
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Invalid quantity");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid price");
        }
    }
}
```

### 2. Large Class

**Smell**: Class has too many responsibilities

```java
// BAD: Large class doing everything
public class UserManager {
    // User creation
    public void createUser() { }
    
    // User validation
    public void validateUser() { }
    
    // Email sending
    public void sendEmail() { }
    
    // Database operations
    public void saveToDatabase() { }
    
    // File operations
    public void exportToFile() { }
    
    // Reporting
    public void generateReport() { }
    // ... 50+ methods
}

// GOOD: Split into focused classes
public class UserService {
    public void createUser() { }
}

public class UserValidator {
    public void validateUser() { }
}

public class EmailService {
    public void sendEmail() { }
}

public class UserRepository {
    public void save() { }
}
```

### 3. Long Parameter List

**Smell**: Method has too many parameters (5+)

```java
// BAD: Too many parameters
public void createUser(String firstName, String lastName, String email, 
                      String phone, String address, String city, 
                      String state, String zip, String country, 
                      String role, boolean active) {
    // ...
}

// GOOD: Use object
public void createUser(CreateUserRequest request) {
    // ...
}

public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Address address;
    private String role;
    private boolean active;
}
```

## Object-Orientation Abusers

### 1. Switch Statements

**Smell**: Large switch/if-else chains

```java
// BAD: Switch statement
public double calculateDiscount(String customerType, double amount) {
    switch (customerType) {
        case "REGULAR":
            return amount * 0.05;
        case "PREMIUM":
            return amount * 0.10;
        case "VIP":
            return amount * 0.15;
        default:
            return 0;
    }
}

// GOOD: Strategy pattern
public interface DiscountStrategy {
    double calculate(double amount);
}

public class RegularDiscount implements DiscountStrategy {
    public double calculate(double amount) {
        return amount * 0.05;
    }
}

public class PremiumDiscount implements DiscountStrategy {
    public double calculate(double amount) {
        return amount * 0.10;
    }
}

public class DiscountCalculator {
    private DiscountStrategy strategy;
    
    public double calculate(double amount) {
        return strategy.calculate(amount);
    }
}
```

### 2. Refused Bequest

**Smell**: Subclass doesn't use inherited methods

```java
// BAD: Subclass doesn't use parent methods
public class Vehicle {
    public void startEngine() { }
    public void drive() { }
}

public class Bicycle extends Vehicle {
    // Bicycle doesn't have an engine!
    @Override
    public void startEngine() {
        throw new UnsupportedOperationException("Bicycles don't have engines");
    }
}

// GOOD: Use composition or interface
public interface Drivable {
    void drive();
}

public class Car implements Drivable {
    public void startEngine() { }
    public void drive() { }
}

public class Bicycle implements Drivable {
    public void drive() { }
    // No engine method needed
}
```

## Change Preventers

### 1. Divergent Change

**Smell**: Class changes for multiple reasons

```java
// BAD: Class changes for multiple reasons
public class User {
    // Changes when user properties change
    private String name;
    private String email;
    
    // Changes when database schema changes
    public void save() { }
    
    // Changes when email format changes
    public void sendEmail() { }
    
    // Changes when report format changes
    public void generateReport() { }
}

// GOOD: Single responsibility
public class User {
    private String name;
    private String email;
}

public class UserRepository {
    public void save(User user) { }
}

public class EmailService {
    public void sendEmail(User user) { }
}

public class ReportGenerator {
    public void generateReport(User user) { }
}
```

### 2. Shotgun Surgery

**Smell**: One change requires modifications in many classes

```java
// BAD: Adding a field requires changes everywhere
// Need to update User, UserDTO, UserService, UserController, etc.

// GOOD: Use mapper to handle conversions
public class UserMapper {
    public UserDTO toDTO(User user) {
        // Centralized mapping logic
    }
}
```

## Dispensables

### 1. Duplicate Code

**Smell**: Same code in multiple places

```java
// BAD: Duplicate code
public void processOrder1(Order order) {
    if (order.getTotal().compareTo(BigDecimal.ZERO) > 0) {
        // Process order
    }
}

public void processOrder2(Order order) {
    if (order.getTotal().compareTo(BigDecimal.ZERO) > 0) {
        // Process order (duplicate)
    }
}

// GOOD: Extract common code
public void processOrder(Order order) {
    if (isValidOrder(order)) {
        // Process order
    }
}

private boolean isValidOrder(Order order) {
    return order.getTotal().compareTo(BigDecimal.ZERO) > 0;
}
```

### 2. Dead Code

**Smell**: Unused code

```java
// BAD: Dead code
public class UserService {
    public void createUser() {
        // Used
    }
    
    public void deleteUser() {
        // Used
    }
    
    public void updateUser() {
        // Never called - dead code
    }
    
    private void helperMethod() {
        // Never called - dead code
    }
}

// GOOD: Remove unused code
public class UserService {
    public void createUser() { }
    public void deleteUser() { }
    // Removed unused methods
}
```

## Couplers

### 1. Feature Envy

**Smell**: Method uses more features of another class than its own

```java
// BAD: Feature envy
public class Order {
    public double calculateTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getPrice() * item.getQuantity();  // Using item's features
        }
        return total;
    }
}

// GOOD: Move method to appropriate class
public class Order {
    public double calculateTotal() {
        return items.stream()
            .mapToDouble(OrderItem::getTotal)
            .sum();
    }
}

public class OrderItem {
    public double getTotal() {
        return price * quantity;  // Logic in OrderItem
    }
}
```

### 2. Inappropriate Intimacy

**Smell**: Classes know too much about each other's internals

```java
// BAD: Too much intimacy
public class Order {
    public void process() {
        User user = getUser();
        user.getAccount().getBalance().subtract(total);  // Too intimate
    }
}

// GOOD: Use proper encapsulation
public class Order {
    public void process() {
        User user = getUser();
        user.deductAmount(total);  // User handles its own logic
    }
}
```

## Refactoring Techniques

### Extract Method

```java
// Before
public void processOrder(Order order) {
    // 50 lines of code
}

// After
public void processOrder(Order order) {
    validateOrder(order);
    calculateTotal(order);
    processPayment(order);
}
```

### Extract Class

```java
// Before: Large class
public class UserManager {
    // 100+ methods
}

// After: Split classes
public class UserService { }
public class UserValidator { }
public class UserRepository { }
```

### Replace Parameter with Object

```java
// Before
public void createUser(String name, String email, String phone, ...) {
}

// After
public void createUser(CreateUserRequest request) {
}
```

## Summary

Code Smells:
- **Bloaters**: Long methods, large classes, too many parameters
- **OO Abusers**: Switch statements, refused bequest
- **Change Preventers**: Divergent change, shotgun surgery
- **Dispensables**: Duplicate code, dead code
- **Couplers**: Feature envy, inappropriate intimacy

**Key Actions:**
- ✅ Identify code smells regularly
- ✅ Refactor incrementally
- ✅ Use design patterns to eliminate smells
- ✅ Keep code clean and maintainable
- ✅ Review code for smells in code reviews

**Remember**: Code smells are indicators of deeper problems. Addressing them improves code quality and maintainability!
