# Deep Technical Answers - Part 9: Fault Tolerance & Resilience (Questions 41-45)

## Question 41: You "achieved 99.9% system uptime." How do you design for fault tolerance?

### Answer

### Fault Tolerance Design

#### 1. **Fault Tolerance Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Fault Tolerance Design                        │
└─────────────────────────────────────────────────────────┘

Design Principles:
├─ Redundancy (multiple instances)
├─ Health checks and auto-recovery
├─ Circuit breakers
├─ Retry with backoff
├─ Graceful degradation
└─ Monitoring and alerting
```

#### 2. **Redundancy and High Availability**

```java
// Deploy multiple instances
// Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: trade-service
spec:
  replicas: 3  # Multiple instances
  template:
    spec:
      containers:
      - name: trade-service
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

#### 3. **Health Checks**

```java
@Component
public class HealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check database connectivity
        if (!isDatabaseHealthy()) {
            return Health.down()
                .withDetail("database", "unavailable")
                .build();
        }
        
        // Check external services
        if (!isExternalServiceHealthy()) {
            return Health.down()
                .withDetail("external-service", "unavailable")
                .build();
        }
        
        return Health.up().build();
    }
}
```

---

## Question 42: You "implemented circuit breaker and retry patterns." Explain these patterns.

### Answer

### Circuit Breaker and Retry Patterns

#### 1. **Circuit Breaker Pattern**

```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker States                         │
└─────────────────────────────────────────────────────────┘

CLOSED (Normal):
├─ Requests flow through
├─ Monitor failures
└─ Open on threshold

OPEN (Failing):
├─ Requests fail fast
├─ No calls to downstream
└─ Half-open after timeout

HALF-OPEN (Testing):
├─ Allow limited requests
├─ Close if successful
└─ Open if failures continue
```

#### 2. **Circuit Breaker Implementation**

```java
@Service
public class TradeService {
    private final CircuitBreaker circuitBreaker;
    
    public TradeService() {
        this.circuitBreaker = CircuitBreaker.of("tradeService",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // Open at 50% failure
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .build());
    }
    
    public Trade getTrade(String tradeId) {
        return circuitBreaker.executeSupplier(() -> {
            // Call external service
            return externalService.getTrade(tradeId);
        });
    }
}
```

#### 3. **Retry Pattern**

```java
@Service
public class TradeService {
    private final Retry retry;
    
    public TradeService() {
        this.retry = Retry.of("tradeService",
            RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .retryOnException(e -> e instanceof TimeoutException)
                .build());
    }
    
    public Trade processTrade(Trade trade) {
        return retry.executeSupplier(() -> {
            return externalService.processTrade(trade);
        });
    }
}
```

---

## Question 43: How do you handle cascading failures?

### Answer

### Cascading Failure Prevention

#### 1. **Cascading Failure Prevention**

```
┌─────────────────────────────────────────────────────────┐
│         Cascading Failure Prevention                   │
└─────────────────────────────────────────────────────────┘

Prevention Mechanisms:
├─ Circuit breakers
├─ Timeouts
├─ Rate limiting
├─ Bulkhead pattern
└─ Graceful degradation
```

#### 2. **Timeout Implementation**

```java
@Service
public class TradeService {
    public Trade getTrade(String tradeId) {
        try {
            return CompletableFuture.supplyAsync(() -> 
                externalService.getTrade(tradeId))
                .get(500, TimeUnit.MILLISECONDS); // Timeout
        } catch (TimeoutException e) {
            // Return cached or default
            return getCachedTrade(tradeId);
        }
    }
}
```

---

## Question 44: What's your approach to bulkhead pattern?

### Answer

### Bulkhead Pattern

#### 1. **Bulkhead Pattern**

```
┌─────────────────────────────────────────────────────────┐
│         Bulkhead Pattern                               │
└─────────────────────────────────────────────────────────┘

Concept:
├─ Isolate resources
├─ Prevent failure propagation
├─ Separate thread pools
└─ Independent resource pools
```

#### 2. **Thread Pool Isolation**

```java
@Configuration
public class ThreadPoolConfiguration {
    @Bean("tradeThreadPool")
    public ExecutorService tradeThreadPool() {
        return Executors.newFixedThreadPool(10);
    }
    
    @Bean("notificationThreadPool")
    public ExecutorService notificationThreadPool() {
        return Executors.newFixedThreadPool(5);
    }
}

// Use separate thread pools
@Service
public class TradeService {
    @Autowired
    @Qualifier("tradeThreadPool")
    private ExecutorService tradeExecutor;
    
    public void processTrade(Trade trade) {
        tradeExecutor.submit(() -> {
            // Process trade
        });
    }
}
```

---

## Question 45: How do you design for graceful degradation?

### Answer

### Graceful Degradation

#### 1. **Graceful Degradation Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Graceful Degradation                           │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Identify critical vs non-critical features
├─ Disable non-critical features on failure
├─ Return cached data
├─ Use default values
└─ Provide partial functionality
```

#### 2. **Implementation**

```java
@Service
public class TradeService {
    public TradeResponse processTrade(TradeRequest request) {
        try {
            // Try full processing
            return processTradeFull(request);
        } catch (Exception e) {
            // Fallback to degraded mode
            return processTradeDegraded(request);
        }
    }
    
    private TradeResponse processTradeDegraded(TradeRequest request) {
        // Skip non-critical operations
        // Use cached data
        // Return basic response
        return new TradeResponse(
            request.getTradeId(),
            "Processing in degraded mode"
        );
    }
}
```

---

## Summary

Part 9 covers questions 41-45 on Fault Tolerance:

41. **99.9% Uptime Design**: Redundancy, health checks, monitoring
42. **Circuit Breaker & Retry**: Circuit breaker states, retry configuration
43. **Cascading Failures**: Timeouts, rate limiting, circuit breakers
44. **Bulkhead Pattern**: Resource isolation, thread pool separation
45. **Graceful Degradation**: Fallback mechanisms, partial functionality

Key techniques:
- Circuit breakers for failure isolation
- Retry with exponential backoff
- Resource isolation (bulkhead)
- Graceful degradation for resilience
