# Clean Code: Readable, Maintainable, Self-Documenting

## Overview

Clean Code is code that is easy to read, understand, and maintain. It follows best practices, uses meaningful names, has clear structure, and is self-documenting. Clean code reduces technical debt, makes debugging easier, and enables teams to work more efficiently.

## Clean Code Principles

```
┌─────────────────────────────────────────────────────────┐
│         Clean Code Principles                           │
└─────────────────────────────────────────────────────────┘

1. Readability
   └─ Code should read like well-written prose
   └─ Easy to understand at first glance

2. Simplicity
   └─ Simple solutions over complex ones
   └─ Avoid unnecessary complexity

3. Maintainability
   └─ Easy to modify and extend
   └─ Changes don't break existing functionality

4. Self-Documenting
   └─ Code explains itself
   └─ Minimal comments needed

5. Testability
   └─ Easy to test
   └─ Well-structured for unit tests
```

## Meaningful Names

### Good Naming Examples

```java
// GOOD: Descriptive names
public class UserAccountService {
    public void sendPasswordResetEmail(String userEmail) {
        // Clear what it does
    }
    
    public boolean isUserAccountActive(Long userId) {
        // Returns boolean, clear purpose
    }
    
    public List<Order> getPendingOrdersForUser(Long userId) {
        // Clear what it returns
    }
}

// BAD: Unclear names
public class UAS {
    public void send(String e) {  // What does it send? What is e?
    }
    
    public boolean check(Long id) {  // Check what?
    }
    
    public List<Order> get(Long id) {  // Get what?
    }
}
```

### Naming Conventions

```java
// Classes: PascalCase, nouns
public class UserAccount { }
public class OrderProcessor { }
public class PaymentGateway { }

// Methods: camelCase, verbs
public void calculateTotal() { }
public boolean isValid() { }
public User findById(Long id) { }

// Variables: camelCase, descriptive
String userEmailAddress;
List<OrderItem> orderItems;
boolean isAccountActive;

// Constants: UPPER_SNAKE_CASE
public static final int MAX_RETRY_ATTEMPTS = 3;
public static final String DEFAULT_CURRENCY = "USD";

// Booleans: is/has/should prefix
boolean isActive;
boolean hasPermission;
boolean shouldValidate;
```

## Functions

### Small Functions

```java
// GOOD: Small, focused function
public void processOrder(Order order) {
    validateOrder(order);
    calculateTotal(order);
    applyDiscount(order);
    processPayment(order);
    sendConfirmation(order);
}

private void validateOrder(Order order) {
    if (order.getItems().isEmpty()) {
        throw new InvalidOrderException("Order must have items");
    }
    // Single responsibility
}

// BAD: Large function doing everything
public void processOrder(Order order) {
    // 100 lines of code doing everything
    // Validation
    // Calculation
    // Payment
    // Email
    // Database
    // Logging
    // ...
}
```

### Single Responsibility

```java
// GOOD: Each function does one thing
public double calculateTotal(List<OrderItem> items) {
    return items.stream()
        .mapToDouble(item -> item.getPrice() * item.getQuantity())
        .sum();
}

public double applyDiscount(double total, String discountCode) {
    Discount discount = discountService.getDiscount(discountCode);
    return total * (1 - discount.getPercentage());
}

// BAD: Function does multiple things
public double calculateAndApplyDiscount(List<OrderItem> items, String code) {
    // Calculates total AND applies discount
    // Two responsibilities!
}
```

### Function Parameters

```java
// GOOD: Few parameters (0-3)
public User createUser(String name, String email) {
    // Clear and simple
}

// ACCEPTABLE: 3 parameters
public void sendEmail(String to, String subject, String body) {
    // Still manageable
}

// BAD: Too many parameters
public void createUser(String name, String email, String phone, 
                      String address, String city, String state, 
                      String zip, String country, String role) {
    // Too many! Use an object instead
}

// BETTER: Use object
public void createUser(CreateUserRequest request) {
    // Cleaner
}
```

## Comments

### When to Comment

```java
// GOOD: Explain WHY, not WHAT
// Using this algorithm because it handles edge cases better
// than the standard approach
public void processData(List<Data> data) {
    // Complex algorithm with good reason
}

// BAD: Comment explains what code does (code should be self-explanatory)
// Loop through users and send email
for (User user : users) {
    emailService.send(user);
}

// GOOD: No comment needed - code is self-explanatory
for (User user : users) {
    emailService.sendWelcomeEmail(user);
}
```

### Good Comments

```java
// GOOD: Legal requirement
// Required by GDPR: must log all data access
logger.logDataAccess(userId, resourceId);

// GOOD: Warning about consequences
// WARNING: This method modifies shared state. 
// Must be called within transaction.
public void updateBalance(Account account, BigDecimal amount) {
    // ...
}

// GOOD: TODO with context
// TODO: Optimize this query when user count exceeds 10,000
public List<User> getAllUsers() {
    // ...
}
```

## Error Handling

### Meaningful Error Messages

```java
// GOOD: Clear error message
public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(
            "User with ID " + id + " not found"));
}

// BAD: Generic error
public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Error"));
}

// GOOD: Specific exception types
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
```

### Don't Return Null

```java
// BAD: Returns null
public User getUserById(Long id) {
    // Returns null if not found
    return userRepository.findById(id);
}

// GOOD: Returns Optional
public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
}

// GOOD: Throws exception
public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
}
```

## Code Organization

### Class Organization

```java
public class UserService {
    // 1. Constants
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    
    // 2. Fields
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // 3. Constructors
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    
    // 4. Public methods
    public User createUser(CreateUserRequest request) {
        // ...
    }
    
    // 5. Private helper methods
    private void validateRequest(CreateUserRequest request) {
        // ...
    }
}
```

### Package Organization

```
com.example.project
├── domain
│   ├── User.java
│   └── Order.java
├── repository
│   ├── UserRepository.java
│   └── OrderRepository.java
├── service
│   ├── UserService.java
│   └── OrderService.java
├── controller
│   ├── UserController.java
│   └── OrderController.java
└── dto
    ├── UserDTO.java
    └── OrderDTO.java
```

## Code Smells to Avoid

### 1. Long Methods

```java
// BAD: 50+ lines
public void processOrder(Order order) {
    // 50 lines of code
}

// GOOD: Break into smaller methods
public void processOrder(Order order) {
    validateOrder(order);
    calculateTotal(order);
    processPayment(order);
    sendConfirmation(order);
}
```

### 2. Large Classes

```java
// BAD: 1000+ lines, too many responsibilities
public class UserManager {
    // Everything related to users
}

// GOOD: Split into focused classes
public class UserService { }
public class UserValidator { }
public class UserRepository { }
```

### 3. Duplicate Code

```java
// BAD: Duplicate code
public void processOrder1(Order order) {
    validate(order);
    calculate(order);
    // ...
}

public void processOrder2(Order order) {
    validate(order);  // Duplicate
    calculate(order);  // Duplicate
    // ...
}

// GOOD: Extract common code
public void processOrder(Order order) {
    validate(order);
    calculate(order);
    // ...
}
```

### 4. Magic Numbers

```java
// BAD: Magic numbers
if (user.getAge() > 18) { }
if (retryCount < 3) { }

// GOOD: Named constants
private static final int MINIMUM_AGE = 18;
private static final int MAX_RETRY_ATTEMPTS = 3;

if (user.getAge() > MINIMUM_AGE) { }
if (retryCount < MAX_RETRY_ATTEMPTS) { }
```

## Best Practices Summary

### 1. Write Self-Documenting Code

```java
// Code should explain itself
boolean isUserAccountActive = user.isActive() && !user.isLocked();
if (isUserAccountActive) {
    // Clear what this means
}
```

### 2. Keep Functions Small

- Functions should do one thing
- Prefer 5-20 lines per function
- Extract complex logic into separate functions

### 3. Use Meaningful Names

- Names should reveal intent
- Avoid abbreviations
- Use searchable names

### 4. Avoid Deep Nesting

```java
// BAD: Deep nesting
if (condition1) {
    if (condition2) {
        if (condition3) {
            // Too nested!
        }
    }
}

// GOOD: Early returns
if (!condition1) return;
if (!condition2) return;
if (!condition3) return;
// Continue with logic
```

### 5. Follow DRY (Don't Repeat Yourself)

- Extract duplicate code into methods
- Reuse common functionality
- Avoid copy-paste programming

## Summary

Clean Code Principles:
- **Readability**: Code should read like prose
- **Simplicity**: Simple solutions over complex
- **Maintainability**: Easy to modify and extend
- **Self-Documenting**: Code explains itself
- **Testability**: Easy to test

**Key Practices:**
- ✅ Use meaningful names
- ✅ Keep functions small and focused
- ✅ Write self-documenting code
- ✅ Avoid code smells
- ✅ Follow consistent style

**Remember**: Clean code is not just about making it work - it's about making it maintainable for the next developer (which might be you in 6 months)!
