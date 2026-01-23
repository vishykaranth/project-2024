# Deep Technical Answers - Part 33: Code Quality - Design Patterns (Questions 161-165)

## Question 161: You used "adapter pattern for payment gateway integration." When do you use adapter pattern?

### Answer

### Adapter Pattern Usage

#### 1. **Adapter Pattern**

```
┌─────────────────────────────────────────────────────────┐
│         Adapter Pattern                               │
└─────────────────────────────────────────────────────────┘

Use Cases:
├─ Integrating incompatible interfaces
├─ Wrapping legacy systems
├─ Third-party library integration
└─ API versioning
```

#### 2. **Payment Gateway Adapter**

```java
// Payment gateway interfaces
public interface PaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
}

// Adyen adapter
@Service
public class AdyenPaymentAdapter implements PaymentGateway {
    private final AdyenClient adyenClient;
    
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Adapt to Adyen format
        AdyenPaymentRequest adyenRequest = adaptToAdyen(request);
        AdyenResponse response = adyenClient.pay(adyenRequest);
        return adaptFromAdyen(response);
    }
}

// SEPA adapter
@Service
public class SEPAPaymentAdapter implements PaymentGateway {
    private final SEPAClient sepaClient;
    
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Adapt to SEPA format
        SEPAPaymentRequest sepaRequest = adaptToSEPA(request);
        SEPAResponse response = sepaClient.process(sepaRequest);
        return adaptFromSEPA(response);
    }
}

// Usage
@Service
public class PaymentService {
    private final Map<String, PaymentGateway> gateways;
    
    public PaymentResult processPayment(PaymentRequest request) {
        PaymentGateway gateway = gateways.get(request.getGatewayType());
        return gateway.processPayment(request);
    }
}
```

---

## Question 162: You implemented "circuit breaker and retry patterns." When do you use these?

### Answer

### Circuit Breaker & Retry Usage

#### 1. **Pattern Usage**

```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker & Retry Usage                  │
└─────────────────────────────────────────────────────────┘

Circuit Breaker:
├─ External service calls
├─ Unreliable dependencies
├─ Prevent cascading failures
└─ Fast failure

Retry:
├─ Transient failures
├─ Network issues
├─ Temporary unavailability
└─ Idempotent operations
```

#### 2. **Implementation**

```java
@Service
public class ExternalServiceClient {
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    
    public ExternalServiceClient() {
        this.circuitBreaker = CircuitBreaker.of("external-service",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build());
        
        this.retry = Retry.of("external-service",
            RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .retryOnException(e -> e instanceof TimeoutException)
                .build());
    }
    
    public Response callExternalService(Request request) {
        return circuitBreaker.executeSupplier(() ->
            retry.executeSupplier(() ->
                externalService.call(request)
            )
        );
    }
}
```

---

## Question 163: What design patterns do you use most frequently?

### Answer

### Frequently Used Design Patterns

#### 1. **Common Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Frequently Used Patterns                      │
└─────────────────────────────────────────────────────────┘

Patterns:
├─ Dependency Injection
├─ Factory Pattern
├─ Strategy Pattern
├─ Observer Pattern
├─ Adapter Pattern
└─ Builder Pattern
```

#### 2. **Examples**

```java
// Dependency Injection (Spring)
@Service
public class TradeService {
    private final TradeRepository repository;
    
    @Autowired
    public TradeService(TradeRepository repository) {
        this.repository = repository;
    }
}

// Factory Pattern
public class TradeProcessorFactory {
    public TradeProcessor createProcessor(TradeType type) {
        switch (type) {
            case STANDARD: return new StandardTradeProcessor();
            case PREMIUM: return new PremiumTradeProcessor();
            default: return new DefaultTradeProcessor();
        }
    }
}

// Strategy Pattern
public interface PricingStrategy {
    BigDecimal calculatePrice(Trade trade);
}

@Service
public class StandardPricingStrategy implements PricingStrategy {
    // Standard pricing
}

@Service
public class PremiumPricingStrategy implements PricingStrategy {
    // Premium pricing
}
```

---

## Question 164: How do you decide which pattern to use?

### Answer

### Pattern Selection

#### 1. **Selection Criteria**

```
┌─────────────────────────────────────────────────────────┐
│         Pattern Selection Criteria                    │
└─────────────────────────────────────────────────────────┘

Criteria:
├─ Problem type
├─ Requirements
├─ Complexity
├─ Maintainability
└─ Team familiarity
```

#### 2. **Decision Framework**

```java
// Problem: Need to support multiple payment gateways
// Solution: Strategy Pattern

// Problem: Integrate incompatible interfaces
// Solution: Adapter Pattern

// Problem: Create complex objects
// Solution: Builder Pattern

// Problem: Need to notify multiple observers
// Solution: Observer Pattern
```

---

## Question 165: What's your approach to pattern documentation?

### Answer

### Pattern Documentation

#### 1. **Documentation Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Pattern Documentation Strategy                │
└─────────────────────────────────────────────────────────┘

Documentation:
├─ Pattern name
├─ Problem it solves
├─ Implementation
├─ Usage examples
└─ Trade-offs
```

#### 2. **Documentation Example**

```java
/**
 * Adapter Pattern Implementation
 * 
 * Problem: Integrate multiple payment gateways with different interfaces
 * Solution: Adapter pattern to provide unified interface
 * 
 * Usage:
 * PaymentGateway gateway = paymentGatewayFactory.getGateway("adyen");
 * PaymentResult result = gateway.processPayment(request);
 * 
 * Trade-offs:
 * - Adds abstraction layer (slight overhead)
 * - Provides flexibility and maintainability
 */
public interface PaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
}
```

---

## Summary

Part 33 covers questions 161-165 on Design Patterns:

161. **Adapter Pattern**: Payment gateway integration, legacy systems
162. **Circuit Breaker & Retry**: External services, transient failures
163. **Frequently Used Patterns**: DI, Factory, Strategy, Observer
164. **Pattern Selection**: Criteria, decision framework
165. **Pattern Documentation**: Problem, solution, usage, trade-offs

Key techniques:
- Adapter for incompatible interfaces
- Circuit breaker for resilience
- Common design patterns
- Pattern selection criteria
- Pattern documentation
