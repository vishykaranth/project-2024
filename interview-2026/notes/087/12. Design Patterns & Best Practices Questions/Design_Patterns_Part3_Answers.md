# Design Patterns - Part 3: Remaining Patterns & Summary

## Question 248: How did you use the Facade pattern?

### Answer

### Facade Pattern

#### 1. **Facade Pattern Purpose**

```
┌─────────────────────────────────────────────────────────┐
│         Facade Pattern Purpose                          │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Complex subsystem
├─ Multiple interfaces
├─ Difficult to use
└─ Tight coupling

Solution:
├─ Facade pattern
├─ Simple unified interface
├─ Hide complexity
└─ Easy to use
```

#### 2. **NLU Facade Service**

```java
// Complex Subsystem: Multiple NLU Providers
public interface NLUProvider {
    NLUResponse processMessage(String message, String conversationId);
}

// Facade: Simple Interface
@Service
public class NLUFacadeService {
    private final List<NLUProvider> providers;
    private final CircuitBreaker circuitBreaker;
    private final NLUCacheService cacheService;
    private final RetryManager retryManager;
    
    // Simple interface hiding complexity
    public NLUResponse processMessage(String message, String conversationId) {
        // Check cache
        NLUResponse cached = cacheService.getCachedResponse(message);
        if (cached != null) {
            return cached;
        }
        
        // Select provider
        NLUProvider provider = selectProvider(conversationId);
        
        // Process with circuit breaker and retry
        NLUResponse response = retryManager.executeWithRetry(() -> {
            if (circuitBreaker.isOpen(provider)) {
                return fallbackToSecondary(provider, message, conversationId);
            }
            return provider.processMessage(message, conversationId);
        });
        
        // Cache response
        cacheService.cacheResponse(message, response);
        
        return response;
    }
    
    // Hides complexity of:
    // - Provider selection
    // - Circuit breaker logic
    // - Retry logic
    // - Caching
    // - Fallback mechanism
}
```

#### 3. **Payment Facade**

```java
// Complex Payment Subsystem
public interface PaymentProvider {
    PaymentResult processPayment(PaymentRequest request);
}

public interface FraudDetectionService {
    FraudCheckResult checkFraud(PaymentRequest request);
}

public interface PaymentGateway {
    GatewayResponse charge(PaymentRequest request);
}

// Facade: Simple Interface
@Service
public class PaymentFacadeService {
    private final FraudDetectionService fraudService;
    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    
    // Simple interface hiding complexity
    public PaymentResult processPayment(PaymentRequest request) {
        // Step 1: Fraud check
        FraudCheckResult fraudCheck = fraudService.checkFraud(request);
        if (!fraudCheck.isApproved()) {
            return PaymentResult.failed("Fraud check failed");
        }
        
        // Step 2: Process payment
        GatewayResponse gatewayResponse = paymentGateway.charge(request);
        if (!gatewayResponse.isSuccess()) {
            return PaymentResult.failed(gatewayResponse.getErrorMessage());
        }
        
        // Step 3: Save payment record
        Payment payment = createPayment(request, gatewayResponse);
        paymentRepository.save(payment);
        
        // Step 4: Send notification
        notificationService.sendPaymentConfirmation(payment);
        
        return PaymentResult.success(payment);
    }
    
    // Client code doesn't need to know about:
    // - Fraud detection
    // - Payment gateway
    // - Database operations
    // - Notifications
}
```

---

## Question 249: Explain the Builder pattern usage.

### Answer

### Builder Pattern

#### 1. **Builder Pattern Purpose**

```
┌─────────────────────────────────────────────────────────┐
│         Builder Pattern Purpose                        │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Complex object construction
├─ Many constructor parameters
├─ Optional parameters
└─ Immutability

Solution:
├─ Builder pattern
├─ Step-by-step construction
├─ Fluent interface
└─ Immutable objects
```

#### 2. **Event Builder**

```java
// Complex Event Object
public class TradeCreatedEvent {
    private final String tradeId;
    private final String accountId;
    private final String instrumentId;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final String currency;
    private final TradeType type;
    private final Instant timestamp;
    private final String idempotencyKey;
    
    // Private constructor
    private TradeCreatedEvent(Builder builder) {
        this.tradeId = builder.tradeId;
        this.accountId = builder.accountId;
        this.instrumentId = builder.instrumentId;
        this.quantity = builder.quantity;
        this.price = builder.price;
        this.currency = builder.currency;
        this.type = builder.type;
        this.timestamp = builder.timestamp;
        this.idempotencyKey = builder.idempotencyKey;
    }
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String tradeId;
        private String accountId;
        private String instrumentId;
        private BigDecimal quantity;
        private BigDecimal price;
        private String currency;
        private TradeType type;
        private Instant timestamp;
        private String idempotencyKey;
        
        public Builder tradeId(String tradeId) {
            this.tradeId = tradeId;
            return this;
        }
        
        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }
        
        public Builder instrumentId(String instrumentId) {
            this.instrumentId = instrumentId;
            return this;
        }
        
        public Builder quantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }
        
        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
        
        public Builder type(TradeType type) {
            this.type = type;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }
        
        public TradeCreatedEvent build() {
            // Validation
            if (tradeId == null) {
                throw new IllegalArgumentException("Trade ID is required");
            }
            if (timestamp == null) {
                timestamp = Instant.now();
            }
            
            return new TradeCreatedEvent(this);
        }
    }
}

// Usage
TradeCreatedEvent event = TradeCreatedEvent.builder()
    .tradeId("trade-123")
    .accountId("account-456")
    .instrumentId("instrument-789")
    .quantity(new BigDecimal("100"))
    .price(new BigDecimal("50.25"))
    .currency("USD")
    .type(TradeType.BUY)
    .timestamp(Instant.now())
    .idempotencyKey("idempotency-key")
    .build();
```

#### 3. **ActivityOptions Builder**

```java
// Complex Configuration Object
public class ActivityOptions {
    private final Duration startToCloseTimeout;
    private final Duration scheduleToStartTimeout;
    private final Duration scheduleToCloseTimeout;
    private final Duration heartbeatTimeout;
    private final RetryOptions retryOptions;
    
    private ActivityOptions(Builder builder) {
        this.startToCloseTimeout = builder.startToCloseTimeout;
        this.scheduleToStartTimeout = builder.scheduleToStartTimeout;
        this.scheduleToCloseTimeout = builder.scheduleToCloseTimeout;
        this.heartbeatTimeout = builder.heartbeatTimeout;
        this.retryOptions = builder.retryOptions;
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }
    
    public static class Builder {
        private Duration startToCloseTimeout;
        private Duration scheduleToStartTimeout;
        private Duration scheduleToCloseTimeout;
        private Duration heartbeatTimeout;
        private RetryOptions retryOptions;
        
        public Builder setStartToCloseTimeout(Duration timeout) {
            this.startToCloseTimeout = timeout;
            return this;
        }
        
        public Builder setScheduleToStartTimeout(Duration timeout) {
            this.scheduleToStartTimeout = timeout;
            return this;
        }
        
        public Builder setHeartbeatTimeout(Duration timeout) {
            this.heartbeatTimeout = timeout;
            return this;
        }
        
        public Builder setRetryOptions(RetryOptions retryOptions) {
            this.retryOptions = retryOptions;
            return this;
        }
        
        public ActivityOptions build() {
            return new ActivityOptions(this);
        }
    }
}

// Usage
ActivityOptions options = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(30))
    .setScheduleToStartTimeout(Duration.ofSeconds(10))
    .setHeartbeatTimeout(Duration.ofSeconds(5))
    .setRetryOptions(retryOptions)
    .build();
```

---

## Question 250: What's the Singleton pattern usage (if any)?

### Answer

### Singleton Pattern

#### 1. **Singleton Pattern - When to Use**

```
┌─────────────────────────────────────────────────────────┐
│         Singleton Pattern Considerations               │
└─────────────────────────────────────────────────────────┘

Use Cases:
├─ Configuration managers
├─ Connection pools
├─ Logging services
└─ Cache managers

Avoid:
├─ In stateless services
├─ For business logic
├─ In distributed systems
└─ For testability
```

#### 2. **Spring Singleton (Recommended)**

```java
// Spring manages singleton lifecycle
@Service // Singleton by default in Spring
public class ConfigurationManager {
    private final Map<String, String> configuration = new ConcurrentHashMap<>();
    
    public String getConfiguration(String key) {
        return configuration.get(key);
    }
    
    public void setConfiguration(String key, String value) {
        configuration.put(key, value);
    }
}
```

#### 3. **Manual Singleton (Not Recommended)**

```java
// Manual singleton - avoid in most cases
public class ManualSingleton {
    private static ManualSingleton instance;
    private final Map<String, String> data = new ConcurrentHashMap<>();
    
    private ManualSingleton() {
        // Private constructor
    }
    
    public static synchronized ManualSingleton getInstance() {
        if (instance == null) {
            instance = new ManualSingleton();
        }
        return instance;
    }
    
    // Problems:
    // - Hard to test
    // - Thread safety issues
    // - Not suitable for distributed systems
    // - Tight coupling
}
```

#### 4. **Better Alternatives**

```java
// Use dependency injection instead
@Configuration
public class AppConfiguration {
    @Bean
    @Scope("singleton") // Explicit singleton
    public ConfigurationManager configurationManager() {
        return new ConfigurationManager();
    }
}

// Or use Spring's @Service (singleton by default)
@Service
public class ConfigurationManager {
    // Spring manages singleton lifecycle
    // Easy to test with @MockBean
    // Thread-safe
    // Works in distributed systems
}
```

---

## Design Patterns Summary

### Pattern Usage Matrix

```
┌─────────────────────────────────────────────────────────┐
│         Pattern Usage Summary                          │
└─────────────────────────────────────────────────────────┘

Pattern              | Use Case                    | Example
--------------------|-----------------------------|------------------
Adapter             | Multiple provider APIs      | NLU Facade
Circuit Breaker     | External service calls      | NLU providers
Saga                | Distributed transactions    | Settlement
Factory             | Object creation             | NLU providers
Strategy            | Interchangeable algorithms  | Routing, retry
Observer            | Event-driven architecture   | Kafka events
Repository          | Data access abstraction     | Database access
Facade              | Complex subsystem           | NLU Facade
Builder             | Complex object construction | Events, configs
Singleton           | Shared resources            | Configuration
```

### Best Practices

1. **Use Spring's Dependency Injection**: Avoid manual singletons
2. **Prefer Composition**: Use patterns to compose behavior
3. **Keep Patterns Simple**: Don't over-engineer
4. **Test Patterns**: Ensure patterns are testable
5. **Document Patterns**: Explain why patterns are used

### Anti-Patterns to Avoid

1. **God Object**: Don't create facades that do everything
2. **Over-Abstraction**: Don't abstract unnecessarily
3. **Pattern Overuse**: Don't force patterns where not needed
4. **Tight Coupling**: Patterns should reduce coupling, not increase it
5. **Singleton Abuse**: Avoid singletons for business logic

---

## Complete Design Patterns Reference

### When to Use Each Pattern

**Adapter Pattern:**
- Multiple implementations of same interface
- Need to integrate with external systems
- Want to hide implementation details

**Circuit Breaker:**
- Calling external services
- Need to prevent cascading failures
- Want fail-fast behavior

**Saga Pattern:**
- Distributed transactions
- Need compensation on failure
- Multiple services involved

**Factory Pattern:**
- Complex object creation
- Multiple implementations
- Want to centralize creation logic

**Strategy Pattern:**
- Multiple algorithms for same task
- Want runtime algorithm selection
- Need to add new algorithms easily

**Observer Pattern:**
- Event-driven architecture
- Need loose coupling
- Multiple subscribers to events

**Repository Pattern:**
- Data access abstraction
- Want to switch data sources
- Need testable data access

**Facade Pattern:**
- Complex subsystem
- Want simple interface
- Need to hide complexity

**Builder Pattern:**
- Complex object construction
- Many optional parameters
- Want immutable objects

**Singleton Pattern:**
- Shared resources
- Configuration management
- Connection pools

---

## Summary

Part 3 covers:

1. **Facade Pattern**: Simple interface for complex subsystems
2. **Builder Pattern**: Step-by-step object construction
3. **Singleton Pattern**: Shared resources (use Spring DI)

Complete Design Patterns coverage:
- All 10 patterns explained with examples
- Real-world usage from the architecture
- Best practices and anti-patterns
- When to use each pattern

Key principles:
- Use patterns to solve real problems
- Prefer Spring's dependency injection
- Keep patterns simple and testable
- Document pattern usage and rationale
