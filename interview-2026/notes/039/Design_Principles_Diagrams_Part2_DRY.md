# Design Principles - In-Depth Diagrams (Part 2: DRY - Don't Repeat Yourself)

## 🔄 DRY: Code Reuse & Abstraction

---

## 1. Core Concept

### What is DRY?
```
┌─────────────────────────────────────────────────────────────┐
│              DRY Principle                                  │
└─────────────────────────────────────────────────────────────┘

    ❌ WET (Write Everything Twice)
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │ Method A │    │ Method B │    │ Method C │
    │ ──────── │    │ ──────── │    │ ──────── │
    │ Code 1   │    │ Code 1   │    │ Code 1   │
    │ Code 2   │    │ Code 2   │    │ Code 2   │
    │ Code 3   │    │ Code 3   │    │ Code 3   │
    │ Unique A │    │ Unique B │    │ Unique C │
    └──────────┘    └──────────┘    └──────────┘
         │               │               │
         └───────────────┴───────────────┘
              (Duplicated Code)

    ✅ DRY (Don't Repeat Yourself)
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │ Method A │    │ Method B │    │ Method C │
    │ ──────── │    │ ──────── │    │ ──────── │
    │ Unique A │    │ Unique B │    │ Unique C │
    └────┬─────┘    └────┬─────┘    └────┬─────┘
         │               │               │
         └───────────────┴───────────────┘
                    │
                    ▼
            ┌───────────────┐
            │ Common Helper │
            │ ──────────── │
            │ Code 1        │
            │ Code 2        │
            │ Code 3        │
            └───────────────┘
         (Single Source of Truth)
```

### DRY Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              Benefits of DRY                                │
└─────────────────────────────────────────────────────────────┘

    DRY Principle
         │
         ├───► Maintainability
         │         │
         │         └───► Fix once, works everywhere
         │
         ├───► Consistency
         │         │
         │         └───► Same behavior everywhere
         │
         ├───► Reduced Bugs
         │         │
         │         └───► Less code = fewer bugs
         │
         ├───► Easier Testing
         │         │
         │         └───► Test once, reuse everywhere
         │
         └───► Code Size
                   │
                   └───► Smaller codebase
```

---

## 2. Code Reuse Patterns

### Function/Method Extraction
```
┌─────────────────────────────────────────────────────────────┐
│              Function Extraction                            │
└─────────────────────────────────────────────────────────────┘

    ❌ Before: Duplicated Code
    ┌──────────────────────────────┐
    │  calculateTotalPrice()       │
    │  ────────────────────────── │
    │  price = basePrice;           │
    │  if (isVip) {                 │
    │      price *= 0.9;            │
    │  }                            │
    │  if (hasCoupon) {             │
    │      price *= 0.95;          │
    │  }                            │
    │  price += tax;                │
    │  return price;                │
    └──────────────────────────────┘

    ┌──────────────────────────────┐
    │  calculateShippingCost()     │
    │  ────────────────────────── │
    │  cost = baseCost;             │
    │  if (isVip) {                 │  ← Duplicated
    │      cost *= 0.9;            │  ← Duplicated
    │  }                            │  ← Duplicated
    │  if (hasCoupon) {             │  ← Duplicated
    │      cost *= 0.95;           │  ← Duplicated
    │  }                            │  ← Duplicated
    │  cost += tax;                 │  ← Duplicated
    │  return cost;                 │
    └──────────────────────────────┘

    ✅ After: Extracted Common Logic
    ┌──────────────────────────────┐
    │  applyDiscounts(amount)      │
    │  ────────────────────────── │
    │  if (isVip) {                 │
    │      amount *= 0.9;          │
    │  }                            │
    │  if (hasCoupon) {             │
    │      amount *= 0.95;         │
    │  }                            │
    │  return amount;                │
    └──────────────────────────────┘
              ▲              ▲
              │              │
    ┌─────────┘              └─────────┐
    │                                  │
    ┌──────────────────┐    ┌──────────────────┐
    │ calculateTotal   │    │ calculateShipping │
    │ Price()          │    │ Cost()            │
    │ ──────────────── │    │ ──────────────── │
    │ price = base;    │    │ cost = base;     │
    │ price = apply    │    │ cost = apply     │
    │   Discounts(     │    │   Discounts(     │
    │     price);      │    │     cost);       │
    │ price += tax;    │    │ cost += tax;     │
    │ return price;    │    │ return cost;     │
    └──────────────────┘    └──────────────────┘
```

### Code Example
```java
// ❌ BAD: Duplicated validation logic
public class UserService {
    public void createUser(String email, String password) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email required");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password too short");
        }
        // ... create user
    }
    
    public void updateUser(String email, String password) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email required");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password too short");
        }
        // ... update user
    }
}

// ✅ GOOD: Extracted validation
public class UserService {
    private EmailValidator emailValidator;
    private PasswordValidator passwordValidator;
    
    public void createUser(String email, String password) {
        emailValidator.validate(email);
        passwordValidator.validate(password);
        // ... create user
    }
    
    public void updateUser(String email, String password) {
        emailValidator.validate(email);
        passwordValidator.validate(password);
        // ... update user
    }
}

// Reusable validators
public class EmailValidator {
    public void validate(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email required");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }
}
```

---

## 3. Abstraction Patterns

### Template Method Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Template Method Pattern                       │
└─────────────────────────────────────────────────────────────┘

    Abstract Base Class
    ┌──────────────────────────────┐
    │  processOrder()              │
    │  ────────────────────────── │
    │  1. validate()              │ ← Common
    │  2. calculatePrice()        │ ← Common
    │  3. applyDiscount()          │ ← Varies
    │  4. addTax()                 │ ← Common
    │  5. processPayment()         │ ← Varies
    │  6. sendConfirmation()        │ ← Common
    └──────────────────────────────┘
              ▲
              │
      ┌───────┴───────┐
      │               │
      ▼               ▼
    ┌─────────┐    ┌─────────┐
    │ Online  │    │ InStore  │
    │ Order   │    │ Order    │
    │ ─────── │    │ ──────── │
    │ • apply │    │ • apply  │
    │   coupon│    │   member │
    │ • credit│    │ • cash   │
    │   card  │    │   only   │
    └─────────┘    └─────────┘

Common flow defined once,
specific steps vary by type
```

### Strategy Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Strategy Pattern                               │
└─────────────────────────────────────────────────────────────┘

    Context
    ┌──────────────────────────────┐
    │  PaymentProcessor            │
    │  ────────────────────────── │
    │  - paymentStrategy           │
    │  ────────────────────────── │
    │  processPayment(amount) {     │
    │      strategy.process(       │
    │        amount)               │
    │  }                           │
    └──────────────┬───────────────┘
                   │
        ┌──────────┴──────────┐
        │                      │
        ▼                      ▼
    ┌─────────┐          ┌─────────┐
    │ Credit  │          │ PayPal  │
    │ Card    │          │         │
    │ ─────── │          │ ─────── │
    │ process │          │ process │
    └─────────┘          └─────────┘

Same interface, different implementations
```

### Code Example
```java
// ✅ GOOD: Strategy pattern for payment processing
public interface PaymentStrategy {
    void processPayment(BigDecimal amount);
}

public class CreditCardPayment implements PaymentStrategy {
    public void processPayment(BigDecimal amount) {
        // Credit card specific logic
    }
}

public class PayPalPayment implements PaymentStrategy {
    public void processPayment(BigDecimal amount) {
        // PayPal specific logic
    }
}

public class PaymentProcessor {
    private PaymentStrategy strategy;
    
    public PaymentProcessor(PaymentStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void process(BigDecimal amount) {
        // Common validation
        validateAmount(amount);
        // Delegate to strategy
        strategy.processPayment(amount);
        // Common logging
        logPayment(amount);
    }
    
    // Reusable validation
    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }
}
```

---

## 4. Inheritance and Composition

### Inheritance for Code Reuse
```
┌─────────────────────────────────────────────────────────────┐
│              Inheritance                                    │
└─────────────────────────────────────────────────────────────┘

    Base Class
    ┌──────────────────────────────┐
    │  Animal                      │
    │  ────────────────────────── │
    │  + name                      │
    │  + age                       │
    │  ────────────────────────── │
    │  + eat()                     │ ← Common
    │  + sleep()                   │ ← Common
    │  + makeSound()               │ ← Abstract
    └──────────────┬───────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
    ┌─────────┐          ┌─────────┐
    │   Dog   │          │   Cat   │
    │ ─────── │          │ ─────── │
    │ + bark()│          │ + meow()│
    └─────────┘          └─────────┘

Common behavior in base class
Specific behavior in subclasses
```

### Composition for Code Reuse
```
┌─────────────────────────────────────────────────────────────┐
│              Composition                                     │
└─────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │  Logger  │ ← Reusable component
    │ ──────── │
    │ • log()  │
    │ • error()│
    └────┬─────┘
         │
         │ (used by)
         │
    ┌────┴─────┐
    │          │
    ▼          ▼
    ┌──────────┐  ┌──────────┐
    │  User    │  │  Order   │
    │ Service  │  │ Service  │
    │ ──────── │  │ ──────── │
    │ - logger │  │ - logger │
    └──────────┘  └──────────┘

Composition over inheritance
More flexible, less coupling
```

---

## 5. Utility Classes and Helpers

### Common Utilities
```
┌─────────────────────────────────────────────────────────────┐
│              Utility Classes                                │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────────────┐
    │  StringUtils                 │
    │  ────────────────────────── │
    │  • isEmpty()                 │
    │  • isBlank()                 │
    │  • capitalize()              │
    │  • truncate()                │
    └──────────────┬───────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
    ┌─────────┐          ┌─────────┐
    │  User   │          │ Product │
    │ Service │          │ Service │
    └─────────┘          └─────────┘

    ┌──────────────────────────────┐
    │  DateUtils                    │
    │  ────────────────────────── │
    │  • format()                  │
    │  • parse()                    │
    │  • addDays()                  │
    │  • isBefore()                 │
    └──────────────┬───────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
    ┌─────────┐          ┌─────────┐
    │  Order  │          │ Invoice  │
    │ Service │          │ Service  │
    └─────────┘          └─────────┘
```

### Code Example
```java
// ✅ GOOD: Reusable utility class
public class ValidationUtils {
    private ValidationUtils() {} // Prevent instantiation
    
    public static void requireNonNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    public static void requireNonEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
    
    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}

// Used everywhere
public class UserService {
    public void createUser(String email, String name) {
        ValidationUtils.requireNonEmpty(email, "Email required");
        ValidationUtils.requireNonEmpty(name, "Name required");
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email");
        }
        // ... create user
    }
}

public class OrderService {
    public void createOrder(String customerEmail) {
        ValidationUtils.requireNonEmpty(customerEmail, "Email required");
        if (!ValidationUtils.isValidEmail(customerEmail)) {
            throw new IllegalArgumentException("Invalid email");
        }
        // ... create order
    }
}
```

---

## 6. Configuration and Constants

### Centralized Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Configuration Management                       │
└─────────────────────────────────────────────────────────────┘

    ❌ Scattered Constants
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │  User    │    │  Order   │    │ Payment  │
    │ Service  │    │ Service  │    │ Service  │
    │ ──────── │    │ ──────── │    │ ──────── │
    │ MAX_AGE  │    │ MAX_AGE  │    │ MAX_AGE  │
    │ = 120    │    │ = 120    │    │ = 120    │
    └──────────┘    └──────────┘    └──────────┘
         │               │               │
         └───────────────┴───────────────┘
              (Same value, 3 places)

    ✅ Centralized Configuration
    ┌──────────────────────────────┐
    │  ApplicationConstants         │
    │  ────────────────────────── │
    │  MAX_USER_AGE = 120          │
    │  MIN_PASSWORD_LENGTH = 8    │
    │  SESSION_TIMEOUT = 3600     │
    └──────────────┬───────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
    ┌─────────┐          ┌─────────┐
    │  User   │          │  Order   │
    │ Service │          │ Service  │
    └─────────┘          └─────────┘
```

---

## 7. When NOT to Apply DRY

### Over-Abstraction Warning
```
┌─────────────────────────────────────────────────────────────┐
│              When NOT to Apply DRY                          │
└─────────────────────────────────────────────────────────────┘

    ❌ Premature Abstraction
    ┌──────────────────────────────┐
    │  GenericProcessor            │
    │  ────────────────────────── │
    │  process(type, data,         │
    │    format, output,           │
    │    options...)               │
    └──────────────────────────────┘
    
    Problems:
    • Too generic
    • Hard to understand
    • Hard to maintain
    • Over-engineered

    ✅ Wait for Pattern
    ┌──────────┐    ┌──────────┐
    │  User    │    │  Order   │
    │ Service  │    │ Service  │
    │ ──────── │    │ ──────── │
    │ Similar  │    │ Similar  │
    │ but      │    │ but      │
    │ different│    │ different│
    └──────────┘    └──────────┘
    
    Rule of Three:
    • First time: Write it
    • Second time: Notice duplication
    • Third time: Refactor to DRY
```

---

## Key Takeaways

### DRY Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              DRY Checklist                                 │
└─────────────────────────────────────────────────────────────┘

✅ Extract common logic into functions
✅ Use inheritance for shared behavior
✅ Use composition for reusable components
✅ Create utility classes for common operations
✅ Centralize configuration and constants
✅ Use design patterns (Strategy, Template Method)
✅ Create abstractions for similar operations

❌ Don't abstract too early (Rule of Three)
❌ Don't create artificial abstractions
❌ Don't sacrifice readability for DRY
❌ Don't over-engineer simple code
```

---

**Next: Part 3 will cover KISS (Keep It Simple, Stupid) principle.**

