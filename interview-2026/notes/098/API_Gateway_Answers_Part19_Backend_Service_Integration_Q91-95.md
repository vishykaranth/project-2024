# API Gateway Answers - Part 19: Backend Service Integration (Questions 91-95)

## Question 91: How did the gateway integrate with backend services?

### Answer

### Backend Integration

- Service discovery
- Load balancing
- Health checks
- Circuit breakers

---

## Question 92: What service discovery mechanism did you use?

### Answer

### Service Discovery

- Kubernetes service discovery
- Eureka (if used)
- Consul (if used)
- DNS-based discovery

---

## Question 93: How did you handle backend service failures?

### Answer

### Backend Failure Handling

- Circuit breakers
- Retry mechanisms
- Fallback responses
- Health check monitoring

---

## Question 94: What circuit breaker patterns did you implement?

### Answer

### Circuit Breaker

```java
@Bean
public CircuitBreakerConfig circuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .slidingWindowSize(10)
        .build();
}
```

---

## Question 95: How did you handle retries and timeouts?

### Answer

### Retries and Timeouts

```java
@Bean
public RetryConfig retryConfig() {
    return RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofSeconds(1))
        .build();
}
```

---

## Summary

Part 19 covers questions 91-95 on Backend Service Integration:
- Backend integration
- Service discovery
- Failure handling
- Circuit breakers
- Retries and timeouts
