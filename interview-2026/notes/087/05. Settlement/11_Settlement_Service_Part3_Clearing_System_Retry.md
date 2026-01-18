# Settlement Service - Part 3: Clearing System Unavailability & Retry Logic

## Question 101: What happens if the clearing system is unavailable?

### Answer

### Clearing System Unavailability Scenarios

#### 1. **Failure Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Clearing System Failure Detection              │
└─────────────────────────────────────────────────────────┘

Failure Modes:
├─ Network timeout
├─ Service unavailable (503)
├─ Connection refused
├─ Slow response (> timeout)
└─ Invalid response

Detection Mechanisms:
├─ Health checks every 30 seconds
├─ Circuit breaker monitoring
├─ Response time monitoring
└─ Error rate tracking
```

#### 2. **Circuit Breaker Pattern**

```java
@Component
public class ClearingSystemCircuitBreaker {
    private final CircuitBreaker circuitBreaker;
    private final MeterRegistry meterRegistry;
    
    public ClearingSystemCircuitBreaker() {
        this.circuitBreaker = CircuitBreaker.of("clearing-system", 
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build());
    }
    
    public ClearingResponse executeWithCircuitBreaker(Supplier<ClearingResponse> supplier) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                ClearingResponse response = supplier.get();
                recordSuccess();
                return response;
            } catch (Exception e) {
                recordFailure(e);
                throw e;
            }
        });
    }
    
    public CircuitBreaker.State getState() {
        return circuitBreaker.getState();
    }
    
    public boolean isOpen() {
        return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
    }
}
```

#### 3. **Fallback Strategy**

```java
@Service
public class SettlementService {
    private final ClearingAdapter clearingAdapter;
    private final ClearingSystemCircuitBreaker circuitBreaker;
    private final SettlementQueueService queueService;
    
    public SettlementResult processSettlement(Settlement settlement) {
        // Check circuit breaker state
        if (circuitBreaker.isOpen()) {
            log.warn("Clearing system circuit breaker is OPEN, queueing settlement");
            return queueSettlement(settlement);
        }
        
        try {
            // Attempt settlement
            ClearingResponse response = circuitBreaker.executeWithCircuitBreaker(() -> {
                return clearingAdapter.settle(settlement);
            });
            
            // Success
            settlement.setStatus(SettlementStatus.COMPLETED);
            settlement.setClearingReference(response.getReference());
            settlementRepository.save(settlement);
            
            return SettlementResult.success(settlement);
            
        } catch (CircuitBreakerOpenException e) {
            // Circuit breaker opened during call
            return queueSettlement(settlement);
            
        } catch (ClearingSystemException e) {
            // Clearing system error
            return handleClearingSystemError(settlement, e);
            
        } catch (Exception e) {
            // Unexpected error
            return handleUnexpectedError(settlement, e);
        }
    }
    
    private SettlementResult queueSettlement(Settlement settlement) {
        // Queue for later processing
        queueService.enqueue(settlement);
        
        settlement.setStatus(SettlementStatus.QUEUED);
        settlement.setQueuedAt(Instant.now());
        settlementRepository.save(settlement);
        
        // Notify systems
        notificationService.notifySettlementQueued(settlement);
        
        return SettlementResult.queued(settlement);
    }
    
    private SettlementResult handleClearingSystemError(Settlement settlement, 
                                                       ClearingSystemException error) {
        // Determine if retryable
        if (isRetryable(error)) {
            // Schedule retry
            retryManager.scheduleRetry(settlement);
            return SettlementResult.retryScheduled(settlement);
        } else {
            // Non-retryable, escalate
            escalationService.escalate(settlement, error);
            return SettlementResult.failed(settlement, error);
        }
    }
    
    private boolean isRetryable(ClearingSystemException error) {
        return error instanceof NetworkException ||
               error instanceof TimeoutException ||
               error instanceof ServiceUnavailableException;
    }
}
```

#### 4. **Queue Management**

```java
@Service
public class SettlementQueueService {
    private final RedisTemplate<String, Settlement> redisTemplate;
    private final ScheduledExecutorService scheduler;
    
    public void enqueue(Settlement settlement) {
        String queueKey = "settlement:queue";
        
        // Add to priority queue
        // Priority: Higher priority = higher score
        double priority = calculatePriority(settlement);
        
        redisTemplate.opsForZSet().add(queueKey, 
            serialize(settlement), priority);
        
        log.info("Settlement queued: {}", settlement.getSettlementId());
    }
    
    private double calculatePriority(Settlement settlement) {
        // Higher priority for:
        // - T+0 settlements
        // - Older settlements
        // - High-value trades
        
        double priority = 0.0;
        
        // Settlement type priority
        priority += switch (settlement.getSettlementType()) {
            case T_PLUS_0 -> 1000.0;
            case T_PLUS_1 -> 100.0;
            case T_PLUS_2 -> 10.0;
        };
        
        // Age priority (older = higher)
        long ageMinutes = Duration.between(
            settlement.getCreatedAt(), Instant.now()).toMinutes();
        priority += ageMinutes;
        
        // Value priority (higher value = higher priority)
        BigDecimal value = settlement.getQuantity().multiply(settlement.getPrice());
        priority += value.doubleValue() / 1000.0;
        
        return priority;
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void processQueue() {
        // Check if clearing system is available
        if (circuitBreaker.isOpen()) {
            log.debug("Clearing system unavailable, skipping queue processing");
            return;
        }
        
        // Process queued settlements
        String queueKey = "settlement:queue";
        Set<ZSetOperations.TypedTuple<String>> queued = 
            redisTemplate.opsForZSet().rangeWithScores(queueKey, 0, 100);
        
        for (ZSetOperations.TypedTuple<String> tuple : queued) {
            Settlement settlement = deserialize(tuple.getValue());
            
            try {
                // Attempt settlement
                SettlementResult result = processSettlement(settlement);
                
                if (result.isSuccess()) {
                    // Remove from queue
                    redisTemplate.opsForZSet().remove(queueKey, tuple.getValue());
                    log.info("Queued settlement processed: {}", 
                        settlement.getSettlementId());
                } else if (result.isRetryScheduled()) {
                    // Retry scheduled, keep in queue
                    continue;
                } else {
                    // Failed, remove from queue and escalate
                    redisTemplate.opsForZSet().remove(queueKey, tuple.getValue());
                    escalationService.escalate(settlement, result.getError());
                }
                
            } catch (Exception e) {
                log.error("Error processing queued settlement: {}", 
                    settlement.getSettlementId(), e);
                // Keep in queue for next attempt
            }
        }
    }
}
```

#### 5. **Health Monitoring**

```java
@Service
public class ClearingSystemHealthMonitor {
    private final ClearingAdapter clearingAdapter;
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void checkHealth() {
        try {
            // Simple health check call
            boolean available = clearingAdapter.isAvailable();
            
            if (available) {
                recordHealthCheckSuccess();
            } else {
                recordHealthCheckFailure("Service unavailable");
            }
            
        } catch (Exception e) {
            recordHealthCheckFailure(e.getMessage());
        }
    }
    
    private void recordHealthCheckSuccess() {
        Counter.builder("clearing.system.health.check")
            .tag("status", "success")
            .register(meterRegistry)
            .increment();
    }
    
    private void recordHealthCheckFailure(String reason) {
        Counter.builder("clearing.system.health.check")
            .tag("status", "failure")
            .tag("reason", reason)
            .register(meterRegistry)
            .increment();
    }
}
```

---

## Question 102: Explain the retry logic for settlement failures.

### Answer

### Retry Strategy Overview

Retry logic handles transient failures in settlement processing, ensuring settlements eventually complete when the clearing system recovers.

### Retry Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Retry Strategy                                 │
└─────────────────────────────────────────────────────────┘

Settlement Failure
    │
    ├─► Check if Retryable
    │   ├─► Network errors: Yes
    │   ├─► Timeout errors: Yes
    │   ├─► Validation errors: No
    │   └─► Business errors: No
    │
    ├─► Check Retry Count
    │   ├─► < Max Attempts: Retry
    │   └─► >= Max Attempts: Escalate
    │
    ├─► Calculate Backoff Delay
    │   ├─► Exponential backoff
    │   ├─► Jitter added
    │   └─► Max delay cap
    │
    └─► Schedule Retry
        ├─► Update retry count
        ├─► Set next retry time
        └─► Schedule task
```

### Retry Implementation

#### 1. **Retry Manager**

```java
@Service
public class SettlementRetryManager {
    private final ScheduledExecutorService scheduler;
    private final SettlementRepository settlementRepository;
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofMinutes(5);
    private static final Duration MAX_RETRY_DELAY = Duration.ofHours(2);
    
    public void scheduleRetry(Settlement settlement, Exception error) {
        // Check if retryable
        if (!isRetryable(error)) {
            log.warn("Error not retryable for settlement: {}", 
                settlement.getSettlementId());
            escalationService.escalate(settlement, error);
            return;
        }
        
        // Check retry count
        int currentRetryCount = settlement.getRetryCount();
        if (currentRetryCount >= MAX_RETRY_ATTEMPTS) {
            log.error("Max retry attempts reached for settlement: {}", 
                settlement.getSettlementId());
            escalationService.escalate(settlement, 
                new MaxRetriesExceededException());
            return;
        }
        
        // Calculate retry delay
        Duration delay = calculateRetryDelay(currentRetryCount);
        
        // Update settlement
        settlement.setRetryCount(currentRetryCount + 1);
        settlement.setNextRetryAt(Instant.now().plus(delay));
        settlement.setStatus(SettlementStatus.RETRY_SCHEDULED);
        settlementRepository.save(settlement);
        
        // Schedule retry
        scheduler.schedule(() -> {
            try {
                retrySettlement(settlement);
            } catch (Exception e) {
                log.error("Retry failed for settlement: {}", 
                    settlement.getSettlementId(), e);
                // Schedule next retry or escalate
                if (settlement.getRetryCount() < MAX_RETRY_ATTEMPTS) {
                    scheduleRetry(settlement, e);
                } else {
                    escalationService.escalate(settlement, e);
                }
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
        
        log.info("Retry scheduled for settlement: {} (attempt {}/{})", 
            settlement.getSettlementId(), 
            currentRetryCount + 1, 
            MAX_RETRY_ATTEMPTS);
    }
    
    private boolean isRetryable(Exception error) {
        return error instanceof NetworkException ||
               error instanceof TimeoutException ||
               error instanceof ServiceUnavailableException ||
               error instanceof ClearingSystemException;
    }
    
    private Duration calculateRetryDelay(int attemptNumber) {
        // Exponential backoff: delay = initial * 2^(attempt-1)
        long delayMs = INITIAL_RETRY_DELAY.toMillis() * 
                      (long) Math.pow(2, attemptNumber);
        
        // Cap at max delay
        delayMs = Math.min(delayMs, MAX_RETRY_DELAY.toMillis());
        
        // Add jitter (±20%)
        double jitter = (Math.random() * 0.4 - 0.2); // -0.2 to +0.2
        delayMs = (long) (delayMs * (1 + jitter));
        
        return Duration.ofMillis(delayMs);
    }
    
    private void retrySettlement(Settlement settlement) {
        log.info("Retrying settlement: {} (attempt {})", 
            settlement.getSettlementId(), 
            settlement.getRetryCount());
        
        // Update status
        settlement.setStatus(SettlementStatus.RETRYING);
        settlementRepository.save(settlement);
        
        // Attempt settlement
        SettlementResult result = settlementProcessor.processSettlement(settlement);
        
        if (result.isSuccess()) {
            log.info("Settlement retry successful: {}", 
                settlement.getSettlementId());
        } else {
            // Retry failed, will be handled by caller
            throw new SettlementException("Retry failed", result.getError());
        }
    }
}
```

#### 2. **Retry with Spring Retry**

```java
@Service
public class SettlementService {
    
    @Retryable(
        value = {ClearingSystemException.class, NetworkException.class},
        maxAttempts = 5,
        backoff = @Backoff(
            delay = 300000, // 5 minutes
            multiplier = 2, // Exponential
            maxDelay = 7200000 // 2 hours
        )
    )
    public ClearingResponse settleTrade(Settlement settlement) {
        return clearingAdapter.settle(settlement);
    }
    
    @Recover
    public SettlementResult recover(ClearingSystemException e, Settlement settlement) {
        log.error("All retry attempts failed for settlement: {}", 
            settlement.getSettlementId(), e);
        
        // Escalate to manual intervention
        escalationService.escalate(settlement, e);
        
        return SettlementResult.failed(settlement, e);
    }
}
```

#### 3. **Retry Configuration**

```yaml
settlement:
  retry:
    max-attempts: 5
    initial-delay: "5 minutes"
    max-delay: "2 hours"
    multiplier: 2.0
    jitter: true
    jitter-percentage: 20
    
  retryable-errors:
    - "NetworkException"
    - "TimeoutException"
    - "ServiceUnavailableException"
    - "ClearingSystemException"
    
  non-retryable-errors:
    - "ValidationException"
    - "BusinessRuleException"
    - "AuthenticationException"
```

#### 4. **Retry Metrics**

```java
@Component
public class RetryMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordRetryAttempt(String settlementId, int attemptNumber) {
        Counter.builder("settlement.retry.attempt")
            .tag("attempt", String.valueOf(attemptNumber))
            .register(meterRegistry)
            .increment();
    }
    
    public void recordRetrySuccess(String settlementId, int attemptNumber, Duration duration) {
        Timer.builder("settlement.retry.duration")
            .tag("attempt", String.valueOf(attemptNumber))
            .tag("result", "success")
            .register(meterRegistry)
            .record(duration);
        
        Counter.builder("settlement.retry.success")
            .tag("attempt", String.valueOf(attemptNumber))
            .register(meterRegistry)
            .increment();
    }
    
    public void recordRetryFailure(String settlementId, int attemptNumber, String errorType) {
        Counter.builder("settlement.retry.failure")
            .tag("attempt", String.valueOf(attemptNumber))
            .tag("error", errorType)
            .register(meterRegistry)
            .increment();
    }
}
```

### Retry Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Retry Flow                                      │
└─────────────────────────────────────────────────────────┘

Settlement Attempt 1
    │
    ├─► Failure: NetworkException
    │
    ▼
Check Retryable: Yes
    │
    ├─► Retry Count: 1/5
    │
    ├─► Calculate Delay: 5 minutes
    │
    └─► Schedule Retry
        │
        ▼
Wait 5 minutes
    │
    ▼
Settlement Attempt 2
    │
    ├─► Failure: TimeoutException
    │
    ▼
Check Retryable: Yes
    │
    ├─► Retry Count: 2/5
    │
    ├─► Calculate Delay: 10 minutes (5 * 2^1)
    │
    └─► Schedule Retry
        │
        ▼
Wait 10 minutes
    │
    ▼
Settlement Attempt 3
    │
    ├─► Success!
    │
    └─► Settlement Complete
```

### Retry Backoff Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Backoff Strategies                             │
└─────────────────────────────────────────────────────────┘

1. Exponential Backoff (Used):
   ├─ Delay = initial * 2^(attempt-1)
   ├─ Attempt 1: 5 minutes
   ├─ Attempt 2: 10 minutes
   ├─ Attempt 3: 20 minutes
   ├─ Attempt 4: 40 minutes
   └─ Attempt 5: 80 minutes (capped at 2 hours)

2. Linear Backoff:
   ├─ Delay = initial * attempt
   ├─ Attempt 1: 5 minutes
   ├─ Attempt 2: 10 minutes
   ├─ Attempt 3: 15 minutes
   └─ Attempt 4: 20 minutes

3. Fixed Delay:
   ├─ Delay = constant
   ├─ All attempts: 5 minutes
   └─ Simple but less efficient
```

### Retry Decision Matrix

```
┌─────────────────────────────────────────────────────────┐
│         Retry Decision Matrix                          │
└─────────────────────────────────────────────────────────┘

Error Type              | Retryable | Max Attempts | Backoff
------------------------|-----------|--------------|----------
NetworkException        | Yes       | 5            | Exponential
TimeoutException        | Yes       | 5            | Exponential
ServiceUnavailable      | Yes       | 5            | Exponential
ClearingSystemException | Yes       | 3            | Exponential
ValidationException     | No        | 0            | N/A
BusinessRuleException   | No        | 0            | N/A
AuthenticationException | No        | 0            | N/A
```

---

## Summary

Part 3 covers:

1. **Clearing System Unavailability**: Circuit breaker, fallback, queue management
2. **Retry Logic**: Exponential backoff, retry limits, error classification
3. **Health Monitoring**: Continuous health checks and failure detection
4. **Queue Management**: Priority-based queue for unavailable systems

Key takeaways:
- Circuit breaker prevents cascading failures
- Queue management handles temporary unavailability
- Retry logic with exponential backoff handles transient failures
- Health monitoring enables proactive failure detection
