# Technical Architecture Answers - Part 7: Hexagonal Architecture & Design Patterns

## Question 31: You mention Hexagonal Architecture. What are the key principles?

### Answer

### Hexagonal Architecture Principles

#### 1. **Core Concept**

```
┌─────────────────────────────────────────────────────────┐
│         Hexagonal Architecture                         │
└─────────────────────────────────────────────────────────┘

Core Idea:
├─ Application at center
├─ Ports define interfaces
├─ Adapters implement interfaces
└─ Isolation from external dependencies

Benefits:
├─ Testability
├─ Technology independence
├─ Business logic isolation
└─ Easy to replace adapters
```

#### 2. **Architecture Layers**

```
┌─────────────────────────────────────────────────────────┐
│         Hexagonal Architecture Layers                  │
└─────────────────────────────────────────────────────────┘

Core (Domain):
├─ Business logic
├─ Domain entities
├─ Value objects
└─ Domain services

Ports (Interfaces):
├─ Input ports (driving)
├─ Output ports (driven)
└─ Define contracts

Adapters (Implementations):
├─ Primary adapters (HTTP, CLI)
├─ Secondary adapters (Database, External APIs)
└─ Implement ports
```

#### 3. **Ports**

```java
// Input port (driving port)
public interface OrderServicePort { // Port
    Order createOrder(OrderRequest request);
    Order getOrder(String orderId);
}

// Output port (driven port)
public interface OrderRepositoryPort { // Port
    void save(Order order);
    Order findById(String orderId);
}

public interface PaymentServicePort { // Port
    Payment processPayment(PaymentRequest request);
}
```

#### 4. **Adapters**

```java
// Primary adapter (driving adapter)
@RestController
public class OrderController { // Primary Adapter
    private final OrderServicePort orderService; // Uses port
    
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(order);
    }
}

// Secondary adapter (driven adapter)
@Repository
public class JpaOrderRepository implements OrderRepositoryPort { // Secondary Adapter
    private final OrderJpaRepository jpaRepository;
    
    @Override
    public void save(Order order) {
        jpaRepository.save(order);
    }
    
    @Override
    public Order findById(String orderId) {
        return jpaRepository.findById(orderId).orElse(null);
    }
}

// Secondary adapter for external service
@Component
public class RestPaymentServiceAdapter implements PaymentServicePort {
    private final RestTemplate restTemplate;
    
    @Override
    public Payment processPayment(PaymentRequest request) {
        // Call external payment service
        PaymentResponse response = restTemplate.postForObject(
            "http://payment-service/process",
            request,
            PaymentResponse.class
        );
        return convertToPayment(response);
    }
}
```

---

## Question 32: How do you design ports and adapters?

### Answer

### Ports and Adapters Design

#### 1. **Port Design**

```java
// Port design principles
public interface PortDesign {
    // ✅ GOOD: Port defines interface
    public interface OrderRepositoryPort {
        Order findById(String orderId);
        void save(Order order);
    }
    
    // ❌ BAD: Port exposes implementation details
    public interface BadOrderRepositoryPort {
        Order findByOrderIdAndCustomerId(String orderId, String customerId);
        // Too specific, exposes implementation
    }
}

// Port should be technology-agnostic
public interface PaymentServicePort {
    Payment processPayment(PaymentRequest request);
    // Doesn't specify REST, gRPC, etc.
}
```

#### 2. **Adapter Design**

```java
// Adapter implementation
@Component
public class DatabaseOrderAdapter implements OrderRepositoryPort {
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public Order findById(String orderId) {
        // Technology-specific implementation
        return jdbcTemplate.queryForObject(
            "SELECT * FROM orders WHERE id = ?",
            new OrderRowMapper(),
            orderId
        );
    }
    
    @Override
    public void save(Order order) {
        // Technology-specific implementation
        jdbcTemplate.update(
            "INSERT INTO orders (id, customer_id, amount) VALUES (?, ?, ?)",
            order.getId(),
            order.getCustomerId(),
            order.getAmount()
        );
    }
}

// Can be replaced with different implementation
@Component
public class MongoOrderAdapter implements OrderRepositoryPort {
    private final MongoTemplate mongoTemplate;
    
    @Override
    public Order findById(String orderId) {
        // Different technology, same interface
        return mongoTemplate.findById(orderId, Order.class);
    }
    
    @Override
    public void save(Order order) {
        mongoTemplate.save(order);
    }
}
```

---

## Question 33: What's the benefit of Hexagonal Architecture over traditional layered architecture?

### Answer

### Hexagonal vs Layered Architecture

#### 1. **Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         Architecture Comparison                        │
└─────────────────────────────────────────────────────────┘

Layered Architecture:
├─ Presentation Layer
├─ Business Layer
├─ Data Access Layer
└─ Database Layer

Problem:
├─ Dependencies flow downward
├─ Business logic depends on data access
├─ Hard to test
└─ Technology coupling

Hexagonal Architecture:
├─ Core (Domain)
├─ Ports (Interfaces)
└─ Adapters (Implementations)

Benefits:
├─ Dependencies point inward
├─ Business logic isolated
├─ Easy to test
└─ Technology independent
```

#### 2. **Dependency Direction**

```java
// Layered Architecture (BAD)
@Service
public class OrderService { // Business Layer
    @Autowired
    private OrderRepository orderRepository; // Depends on Data Access Layer
    // Business logic depends on data access - wrong direction
}

// Hexagonal Architecture (GOOD)
@Service
public class OrderService { // Core
    private final OrderRepositoryPort orderRepository; // Depends on Port (interface)
    // Business logic depends on interface, not implementation
    // Dependencies point inward
}
```

---

## Question 36: You mention "adapter pattern" for payment gateway integration. Explain this.

### Answer

### Adapter Pattern for Payment Gateway

#### 1. **Problem Statement**

```
┌─────────────────────────────────────────────────────────┐
│         Payment Gateway Integration Problem            │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Multiple payment providers (Adyen, SEPA)
├─ Different APIs
├─ Different response formats
└─ Need unified interface

Solution: Adapter Pattern
├─ Common interface
├─ Provider-specific adapters
├─ Unified interface for application
└─ Easy to add/remove providers
```

#### 2. **Adapter Pattern Implementation**

```java
// Common interface (Target)
public interface PaymentGateway {
    Payment processPayment(PaymentRequest request);
    PaymentStatus checkStatus(String paymentId);
    void refundPayment(String paymentId, Money amount);
}

// Adyen adapter
@Component
public class AdyenPaymentAdapter implements PaymentGateway {
    private final AdyenClient adyenClient;
    
    @Override
    public Payment processPayment(PaymentRequest request) {
        // Convert to Adyen format
        AdyenRequest adyenRequest = convertToAdyen(request);
        
        // Call Adyen API
        AdyenResponse adyenResponse = adyenClient.processPayment(adyenRequest);
        
        // Convert to common format
        return convertFromAdyen(adyenResponse);
    }
    
    private AdyenRequest convertToAdyen(PaymentRequest request) {
        AdyenRequest adyen = new AdyenRequest();
        adyen.setAmount(request.getAmount().getAmount().doubleValue());
        adyen.setCurrency(request.getAmount().getCurrency());
        adyen.setCardNumber(request.getCardDetails().getNumber());
        return adyen;
    }
    
    private Payment convertFromAdyen(AdyenResponse response) {
        Payment payment = new Payment();
        payment.setPaymentId(response.getTransactionId());
        payment.setStatus(convertStatus(response.getStatus()));
        payment.setAmount(new Money(
            BigDecimal.valueOf(response.getAmount()),
            response.getCurrency()
        ));
        return payment;
    }
}

// SEPA adapter
@Component
public class SEPAPaymentAdapter implements PaymentGateway {
    private final SEPAClient sepaClient;
    
    @Override
    public Payment processPayment(PaymentRequest request) {
        // Convert to SEPA format
        SEPARequest sepaRequest = convertToSEPA(request);
        
        // Call SEPA API
        SEPAResponse sepaResponse = sepaClient.processPayment(sepaRequest);
        
        // Convert to common format
        return convertFromSEPA(sepaResponse);
    }
}

// Payment service uses adapter
@Service
public class PaymentService {
    private final PaymentGateway paymentGateway; // Use interface, not implementation
    
    public Payment processPayment(PaymentRequest request) {
        // Use adapter - doesn't know which provider
        return paymentGateway.processPayment(request);
    }
}
```

#### 3. **Provider Selection**

```java
// Provider selection strategy
@Service
public class PaymentGatewaySelector {
    private final Map<String, PaymentGateway> adapters;
    
    public PaymentGateway selectGateway(PaymentRequest request) {
        // Select based on criteria
        String provider = determineProvider(request);
        return adapters.get(provider);
    }
    
    private String determineProvider(PaymentRequest request) {
        // Selection logic
        if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD) {
            return "adyen";
        } else if (request.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            return "sepa";
        }
        return "adyen"; // Default
    }
}
```

---

## Question 37: You implemented "circuit breaker and retry patterns." When and why?

### Answer

### Circuit Breaker and Retry Patterns

#### 1. **When to Use**

```
┌─────────────────────────────────────────────────────────┐
│         When to Use Circuit Breaker                   │
└─────────────────────────────────────────────────────────┘

Use Circuit Breaker When:
├─ External service calls
├─ Service failures common
├─ Need to prevent cascading failures
└─ Fast failure response needed

Example:
├─ Payment gateway calls
├─ NLU provider calls
├─ Database calls
└─ Third-party API calls
```

#### 2. **Circuit Breaker Implementation**

```java
// Circuit breaker implementation
@Service
public class ResilientPaymentService {
    private final CircuitBreaker circuitBreaker;
    private final PaymentGateway paymentGateway;
    
    public Payment processPayment(PaymentRequest request) {
        return circuitBreaker.executeSupplier(() -> {
            return paymentGateway.processPayment(request);
        });
    }
}

// Circuit breaker configuration
@Configuration
public class CircuitBreakerConfig {
    @Bean
    public CircuitBreaker paymentCircuitBreaker() {
        return CircuitBreaker.of("payment-circuit-breaker",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // Open after 50% failures
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .permittedNumberOfCallsInHalfOpenState(3)
                .build()
        );
    }
}
```

#### 3. **Retry Pattern**

```java
// Retry pattern implementation
@Service
public class RetryablePaymentService {
    private final PaymentGateway paymentGateway;
    
    @Retryable(
        value = {PaymentException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Payment processPayment(PaymentRequest request) {
        return paymentGateway.processPayment(request);
    }
    
    @Recover
    public Payment recover(PaymentException ex, PaymentRequest request) {
        // Fallback logic
        return fallbackPayment(request);
    }
}
```

---

## Question 38: What design patterns do you use most frequently?

### Answer

### Frequently Used Design Patterns

#### 1. **Patterns Used**

```
┌─────────────────────────────────────────────────────────┐
│         Frequently Used Patterns                       │
└─────────────────────────────────────────────────────────┘

1. Adapter Pattern:
├─ External service integration
├─ Payment gateway adapters
└─ NLU provider adapters

2. Circuit Breaker:
├─ Resilient service calls
├─ Prevent cascading failures
└─ Fast failure

3. Saga Pattern:
├─ Distributed transactions
├─ Compensation logic
└─ Event-driven coordination

4. Repository Pattern:
├─ Data access abstraction
├─ Testability
└─ Technology independence

5. Factory Pattern:
├─ Object creation
├─ Provider selection
└─ Complex object construction
```

---

## Summary

Part 7 covers:
1. **Hexagonal Architecture**: Principles, ports, adapters, benefits
2. **Ports and Adapters Design**: Design principles, implementation
3. **Hexagonal vs Layered**: Comparison, benefits
4. **Adapter Pattern**: Payment gateway integration example
5. **Circuit Breaker & Retry**: When to use, implementation
6. **Common Patterns**: Frequently used patterns

Key takeaways:
- Hexagonal Architecture isolates business logic
- Use adapters to integrate external systems
- Implement circuit breakers for resilience
- Choose patterns based on problem requirements
