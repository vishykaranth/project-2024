# Service Communication Part 5: Circuit Breaker Pattern

## Question 140: Explain the circuit breaker pattern implementation.

### Answer

### Circuit Breaker Pattern Overview

```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker States                         │
└─────────────────────────────────────────────────────────┘

CLOSED (Normal Operation):
├─ Requests flow through
├─ Monitor failure rate
├─ If failure rate > threshold → OPEN
└─ Healthy state

OPEN (Failing):
├─ Requests fail fast
├─ No calls to downstream
├─ After timeout → HALF_OPEN
└─ Protects downstream

HALF_OPEN (Testing):
├─ Allow limited requests
├─ If success → CLOSED
├─ If failure → OPEN
└─ Recovery testing
```

### Resilience4j Implementation

#### 1. **Basic Circuit Breaker**

```java
@Service
public class CircuitBreakerService {
    
    private final CircuitBreaker circuitBreaker;
    
    public CircuitBreakerService() {
        this.circuitBreaker = CircuitBreaker.of("agent-service",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)              // 50% failure rate
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)                 // Last 10 calls
                .minimumNumberOfCalls(5)               // Need 5 calls
                .permittedNumberOfCallsInHalfOpenState(3)  // 3 test calls
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(IOException.class, TimeoutException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build());
    }
    
    public Agent getAgent(String agentId) {
        return circuitBreaker.executeSupplier(() -> {
            return agentService.getAgent(agentId);
        });
    }
}
```

#### 2. **Circuit Breaker with Fallback**

```java
@Service
public class CircuitBreakerWithFallback {
    
    private final CircuitBreaker circuitBreaker;
    private final AgentCacheService cacheService;
    
    @CircuitBreaker(name = "agent-service", 
                    fallbackMethod = "getAgentFallback")
    public Agent getAgent(String agentId) {
        return agentService.getAgent(agentId);
    }
    
    public Agent getAgentFallback(String agentId, Exception e) {
        log.warn("Circuit breaker open, using fallback", e);
        
        // Fallback 1: Cache
        Optional<Agent> cached = cacheService.getAgent(agentId);
        if (cached.isPresent()) {
            return cached.get();
        }
        
        // Fallback 2: Default
        return Agent.defaultAgent(agentId);
    }
}
```

### Circuit Breaker Configuration

#### 1. **YAML Configuration**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      agent-service:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 30s
        failureRateThreshold: 50
        slowCallRateThreshold: 100
        slowCallDurationThreshold: 2s
        recordExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
        ignoreExceptions:
          - java.lang.IllegalArgumentException
      
      nlu-service:
        slidingWindowSize: 20
        minimumNumberOfCalls: 10
        failureRateThreshold: 30
        waitDurationInOpenState: 60s
```

#### 2. **State Transitions**

```java
@Component
public class CircuitBreakerStateListener 
    implements CircuitBreakerStateTransitionListener {
    
    @Override
    public void onStateTransition(CircuitBreakerStateTransition transition) {
        CircuitBreaker.State fromState = transition.getFromState();
        CircuitBreaker.State toState = transition.getToState();
        
        log.info("Circuit breaker state transition: {} -> {}", 
            fromState, toState);
        
        if (toState == CircuitBreaker.State.OPEN) {
            // Alert on circuit open
            alertService.sendAlert(
                AlertLevel.WARNING,
                "Circuit breaker opened for " + transition.getName()
            );
        } else if (toState == CircuitBreaker.State.CLOSED) {
            // Log recovery
            log.info("Circuit breaker recovered for " + transition.getName());
        }
    }
}
```

### Circuit Breaker Metrics

```java
@Component
public class CircuitBreakerMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void registerCircuitBreaker(CircuitBreaker circuitBreaker) {
        // State gauge
        Gauge.builder("circuitbreaker.state", circuitBreaker,
            cb -> cb.getState().getOrder())
            .tag("name", circuitBreaker.getName())
            .register(meterRegistry);
        
        // Failure rate
        Gauge.builder("circuitbreaker.failure.rate", circuitBreaker,
            cb -> cb.getMetrics().getFailureRate())
            .tag("name", circuitBreaker.getName())
            .register(meterRegistry);
        
        // Number of calls
        Gauge.builder("circuitbreaker.calls", circuitBreaker,
            cb -> cb.getMetrics().getNumberOfSuccessfulCalls() + 
                  cb.getMetrics().getNumberOfFailedCalls())
            .tag("name", circuitBreaker.getName())
            .register(meterRegistry);
    }
}
```

### Advanced Circuit Breaker Patterns

#### 1. **Slow Call Detection**

```java
@Service
public class SlowCallCircuitBreaker {
    
    private final CircuitBreaker circuitBreaker;
    
    public SlowCallCircuitBreaker() {
        this.circuitBreaker = CircuitBreaker.of("slow-service",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)           // 50% slow calls
                .slowCallDurationThreshold(Duration.ofSeconds(2))  // > 2s is slow
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build());
    }
    
    public Response callService(Request request) {
        return circuitBreaker.executeSupplier(() -> {
            return slowService.call(request);
        });
    }
}
```

#### 2. **Custom Circuit Breaker**

```java
@Service
public class CustomCircuitBreaker {
    
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile CircuitState state = CircuitState.CLOSED;
    private volatile long lastFailureTime = 0;
    
    private static final int FAILURE_THRESHOLD = 5;
    private static final long OPEN_DURATION_MS = 30000;
    
    public <T> T execute(Supplier<T> operation) {
        if (state == CircuitState.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > OPEN_DURATION_MS) {
                state = CircuitState.HALF_OPEN;
            } else {
                throw new CircuitBreakerOpenException("Circuit is open");
            }
        }
        
        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
    
    private void onSuccess() {
        if (state == CircuitState.HALF_OPEN) {
            state = CircuitState.CLOSED;
            failureCount.set(0);
        }
        successCount.incrementAndGet();
    }
    
    private void onFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        
        if (failures >= FAILURE_THRESHOLD) {
            state = CircuitState.OPEN;
        }
    }
    
    private enum CircuitState {
        CLOSED, OPEN, HALF_OPEN
    }
}
```

---

## Summary

**Circuit Breaker Pattern**:
- **CLOSED**: Normal operation, monitoring failures
- **OPEN**: Failing fast, protecting downstream
- **HALF_OPEN**: Testing recovery
- **Configuration**: Failure rate, window size, timeout
- **Fallback**: Graceful degradation

**Key Points**:
1. Fail fast to protect downstream
2. Configure appropriate thresholds
3. Implement fallback mechanisms
4. Monitor circuit breaker metrics
5. Test recovery scenarios
