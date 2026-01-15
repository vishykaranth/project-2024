# Design Principles - In-Depth Diagrams (Part 5: Fail-Fast)

## ⚡ Fail-Fast: Early Validation & Error Detection

---

## 1. Core Concept

### What is Fail-Fast?
```
┌─────────────────────────────────────────────────────────────┐
│              Fail-Fast Principle                            │
└─────────────────────────────────────────────────────────────┘

    ❌ Fail Late (Silent Failures)
    ┌──────────────────────────────────────┐
    │  Input Validation                    │
    │  ────────────────────────────────── │
    │  1. Accept invalid input             │
    │  2. Process with invalid data        │
    │  3. Store corrupted data             │
    │  4. Continue processing               │
    │  5. Fail at end (hard to debug)      │
    └──────────────────────────────────────┘
         │
         ▼
    • Hard to find root cause
    • Data corruption
    • Wasted processing
    • Confusing errors

    ✅ Fail Fast (Early Detection)
    ┌──────────────────────────────────────┐
    │  Input Validation                    │
    │  ────────────────────────────────── │
    │  1. Validate input immediately       │
    │  2. Reject invalid input             │
    │  3. Clear error message               │
    │  4. Stop processing                   │
    └──────────────────────────────────────┘
         │
         ▼
    • Immediate feedback
    • Clear error location
    • No wasted processing
    • Easier debugging
```

### Fail-Fast Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              Benefits of Fail-Fast                          │
└─────────────────────────────────────────────────────────────┘

    Fail-Fast
         │
         ├───► Early Detection
         │         │
         │         └───► Catch errors at entry point
         │
         ├───► Clear Errors
         │         │
         │         └───► Know exactly what's wrong
         │
         ├───► No Wasted Work
         │         │
         │         └───► Stop before processing
         │
         ├───► Easier Debugging
         │         │
         │         └───► Error at known location
         │
         └───► Better UX
                   │
                   └───► Immediate feedback
```

---

## 2. Validation Layers

### Validation Strategy
```
┌─────────────────────────────────────────────────────────────┐
│              Validation Layers                              │
└─────────────────────────────────────────────────────────────┘

    Request Flow
         │
         ▼
    ┌─────────────────────┐
    │  Client Validation  │ ← First line of defense
    │  ───────────────── │
    │  • Form validation  │
    │  • Input checks     │
    │  • UX feedback      │
    └──────────┬──────────┘
               │
               ▼
    ┌─────────────────────┐
    │  API Validation      │ ← Fail-fast here
    │  ───────────────── │
    │  • Parameter check   │
    │  • Type validation   │
    │  • Format validation │
    │  • Business rules    │
    └──────────┬──────────┘
               │
               ▼
    ┌─────────────────────┐
    │  Business Logic      │
    │  ───────────────── │
    │  • Process data      │
    │  • Apply rules      │
    └──────────┬──────────┘
               │
               ▼
    ┌─────────────────────┐
    │  Data Layer          │
    │  ───────────────── │
    │  • Constraints       │
    │  • Final validation  │
    └─────────────────────┘

    Validate early, fail fast
```

### Code Example: Input Validation
```java
// ❌ BAD: Fail late
public class UserService {
    public void createUser(String email, String name) {
        // No validation, process anyway
        User user = new User();
        user.setEmail(email);  // Could be null
        user.setName(name);     // Could be null
        
        // Process...
        userRepository.save(user);
        
        // Send email...
        emailService.sendWelcomeEmail(email);  // Fails here if null
        
        // Generate report...
        reportService.generateReport(user);  // Fails here if name is null
    }
}

// ✅ GOOD: Fail fast
public class UserService {
    public void createUser(String email, String name) {
        // Validate immediately - fail fast
        validateEmail(email);
        validateName(name);
        
        // Only proceed if validation passes
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        
        userRepository.save(user);
        emailService.sendWelcomeEmail(email);
        reportService.generateReport(user);
    }
    
    private void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Name too long");
        }
    }
}
```

---

## 3. Preconditions and Postconditions

### Design by Contract
```
┌─────────────────────────────────────────────────────────────┐
│              Design by Contract                            │
└─────────────────────────────────────────────────────────────┘

    Method Contract
    ┌──────────────────────────────┐
    │  Preconditions                │
    │  ────────────────────────── │
    │  • Input must be valid       │
    │  • State must be correct     │
    │  • Dependencies available    │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Method Execution             │
    │  ────────────────────────── │
    │  • Process data               │
    │  • Apply business logic       │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Postconditions               │
    │  ────────────────────────── │
    │  • Output is valid            │
    │  • State is consistent        │
    │  • Side effects completed     │
    └──────────────────────────────┘

    Fail fast on preconditions
```

### Code Example
```java
// ✅ GOOD: Explicit preconditions
public class OrderService {
    public void processOrder(Order order, Payment payment) {
        // Preconditions - fail fast
        requireNonNull(order, "Order cannot be null");
        requireNonNull(payment, "Payment cannot be null");
        requireNonEmpty(order.getItems(), "Order must have items");
        requirePositive(order.getTotal(), "Order total must be positive");
        requireValidPayment(payment, order.getTotal());
        
        // Only proceed if all preconditions met
        validateInventory(order);
        processPayment(payment);
        updateInventory(order);
        sendConfirmation(order);
    }
    
    private void requireNonNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    private void requireNonEmpty(List<?> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
    
    private void requirePositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
```

---

## 4. Error Handling Strategy

### Fail-Fast Error Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Error Handling Flow                            │
└─────────────────────────────────────────────────────────────┘

    Input Received
         │
         ▼
    Validate Input
         │
         ├───► Invalid?
         │         │
         │         ├───► YES ──► Throw Exception Immediately
         │         │              │
         │         │              ▼
         │         │         Clear Error Message
         │         │              │
         │         │              ▼
         │         │         Stop Processing
         │         │
         │         └───► NO ──► Continue
         │
         ▼
    Process Data
         │
         ├───► Error?
         │         │
         │         ├───► YES ──► Throw Exception
         │         │              │
         │         │              ▼
         │         │         Stop Processing
         │         │
         │         └───► NO ──► Continue
         │
         ▼
    Return Result
```

### Exception Types
```java
// ✅ GOOD: Specific exceptions for fail-fast
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}

public class OrderService {
    public void createOrder(OrderRequest request) {
        // Fail fast with specific exceptions
        if (request == null) {
            throw new ValidationException("Order request is required");
        }
        
        if (request.getItems().isEmpty()) {
            throw new ValidationException("Order must contain items");
        }
        
        if (!isInventoryAvailable(request.getItems())) {
            throw new BusinessRuleException("Insufficient inventory");
        }
        
        // Process order...
    }
}
```

---

## 5. Configuration Validation

### Startup Validation
```
┌─────────────────────────────────────────────────────────────┐
│              Configuration Validation                       │
└─────────────────────────────────────────────────────────────┘

    Application Startup
         │
         ▼
    ┌─────────────────────┐
    │  Load Configuration │
    └──────────┬──────────┘
               │
               ▼
    ┌─────────────────────┐
    │  Validate Config     │ ← Fail fast here
    │  ───────────────── │
    │  • Required fields   │
    │  • Format checks     │
    │  • Range validation  │
    │  • Dependency check  │
    └──────────┬──────────┘
               │
         ┌─────┴─────┐
         │           │
         ▼           ▼
    ┌─────────┐  ┌─────────┐
    │ Valid   │  │ Invalid  │
    │ ─────── │  │ ──────── │
    │ Start   │  │ Fail     │
    │ App     │  │ Startup  │
    └─────────┘  └─────────┘

    Better to fail at startup
    than fail in production
```

### Code Example
```java
// ✅ GOOD: Validate configuration at startup
@Configuration
public class AppConfig {
    
    @PostConstruct
    public void validateConfiguration() {
        // Fail fast if configuration is invalid
        String dbUrl = System.getProperty("db.url");
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new IllegalStateException(
                "Database URL is required. Set db.url property."
            );
        }
        
        String apiKey = System.getProperty("api.key");
        if (apiKey == null || apiKey.length() < 32) {
            throw new IllegalStateException(
                "API key is required and must be at least 32 characters"
            );
        }
        
        // Validate database connection
        try {
            validateDatabaseConnection(dbUrl);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Cannot connect to database: " + dbUrl, e
            );
        }
    }
    
    private void validateDatabaseConnection(String url) {
        // Test connection
    }
}
```

---

## 6. Null Safety

### Null Handling Strategy
```
┌─────────────────────────────────────────────────────────────┐
│              Null Safety                                    │
└─────────────────────────────────────────────────────────────┘

    ❌ Null Propagation (Fail Late)
    ┌──────────────────────────────┐
    │  user.getName().length()     │
    │       │                       │
    │       ▼                       │
    │  NullPointerException         │
    │  (somewhere deep)             │
    └──────────────────────────────┘

    ✅ Null Checks (Fail Fast)
    ┌──────────────────────────────┐
    │  if (user == null) {           │
    │      throw new                 │
    │        IllegalArgumentException │
    │          ("User required");    │
    │  }                             │
    │  user.getName().length()       │
    └──────────────────────────────┘

    Or use Optional:
    ┌──────────────────────────────┐
    │  Optional<User> user = ...    │
    │  user.orElseThrow(() ->        │
    │      new IllegalArgumentException │
    │        ("User required"));    │
    └──────────────────────────────┘
```

### Code Example
```java
// ❌ BAD: Null propagation
public class OrderService {
    public BigDecimal calculateTotal(Order order) {
        // No null check - fails later
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            total = total.add(
                item.getPrice().multiply(
                    BigDecimal.valueOf(item.getQuantity())
                )
            );
        }
        return total;
    }
}

// ✅ GOOD: Fail fast on null
public class OrderService {
    public BigDecimal calculateTotal(Order order) {
        // Fail fast if null
        requireNonNull(order, "Order cannot be null");
        requireNonEmpty(order.getItems(), "Order must have items");
        
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            requireNonNull(item, "Order item cannot be null");
            requireNonNull(item.getPrice(), "Item price required");
            
            total = total.add(
                item.getPrice().multiply(
                    BigDecimal.valueOf(item.getQuantity())
                )
            );
        }
        return total;
    }
}
```

---

## 7. State Validation

### State Checks
```
┌─────────────────────────────────────────────────────────────┐
│              State Validation                               │
└─────────────────────────────────────────────────────────────┘

    Method Call
         │
         ▼
    ┌─────────────────────┐
    │  Check State        │ ← Fail fast
    │  ───────────────── │
    │  • Is initialized?   │
    │  • Is ready?        │
    │  • Is valid state?  │
    └──────────┬──────────┘
               │
         ┌─────┴─────┐
         │           │
         ▼           ▼
    ┌─────────┐  ┌─────────┐
    │ Valid   │  │ Invalid │
    │ ─────── │  │ ──────── │
    │ Execute │  │ Throw    │
    │         │  │ Exception│
    └─────────┘  └─────────┘
```

### Code Example
```java
// ✅ GOOD: State validation
public class PaymentProcessor {
    private boolean initialized = false;
    private PaymentGateway gateway;
    
    public void initialize(PaymentGateway gateway) {
        requireNonNull(gateway, "Gateway cannot be null");
        this.gateway = gateway;
        this.initialized = true;
    }
    
    public void processPayment(Payment payment) {
        // Fail fast if not initialized
        if (!initialized) {
            throw new IllegalStateException(
                "PaymentProcessor must be initialized before use"
            );
        }
        
        requireNonNull(payment, "Payment cannot be null");
        requireNonNull(payment.getAmount(), "Payment amount required");
        
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Payment amount must be positive"
            );
        }
        
        // Process payment...
        gateway.process(payment);
    }
}
```

---

## Key Takeaways

### Fail-Fast Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Fail-Fast Checklist                           │
└─────────────────────────────────────────────────────────────┘

✅ Validate input at entry points
✅ Check preconditions before processing
✅ Use specific exception types
✅ Provide clear error messages
✅ Fail at startup if configuration invalid
✅ Check for null values early
✅ Validate state before operations
✅ Stop processing on first error

❌ Don't silently ignore errors
❌ Don't continue with invalid data
❌ Don't defer validation
❌ Don't use generic exceptions
```

---

**Next: Part 6 will cover Idempotency principle.**

