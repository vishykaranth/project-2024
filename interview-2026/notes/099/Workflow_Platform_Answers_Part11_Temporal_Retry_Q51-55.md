# Workflow Platform Answers - Part 11: Temporal SDK Integration - Automatic Retry (Questions 51-55)

## Question 51: You mention "automatic retry." How did you configure retry policies in Temporal?

### Answer

### Retry Policy Configuration

#### 1. **Retry Policy Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Retry Policy Configuration                     │
└─────────────────────────────────────────────────────────┘

Retry Policy Components:
├─ Initial Interval (first retry delay)
├─ Maximum Interval (max retry delay)
├─ Backoff Coefficient (multiplier)
├─ Maximum Attempts (total retries)
└─ Non-Retryable Errors (exceptions to skip)
```

#### 2. **Retry Policy Configuration**

```java
@ActivityInterface
public interface WorkflowActivities {
    @ActivityMethod(
        startToCloseTimeoutSeconds = 30,
        retryPolicy = @RetryPolicy(
            initialIntervalSeconds = 1,
            maximumIntervalSeconds = 100,
            maximumAttempts = 3,
            backoffCoefficient = 2.0,
            nonRetryableErrorTypes = {
                IllegalArgumentException.class,
                NullPointerException.class
            }
        )
    )
    String executeActivity(ActivityRequest request);
}
```

#### 3. **Programmatic Retry Policy**

```java
@Service
public class RetryPolicyConfigurator {
    public RetryOptions createRetryPolicy(Step step) {
        RetryOptions.Builder builder = RetryOptions.newBuilder();
        
        // Initial interval
        builder.setInitialInterval(
            Duration.ofSeconds(step.getRetryConfig().getInitialInterval()));
        
        // Maximum interval
        builder.setMaximumInterval(
            Duration.ofSeconds(step.getRetryConfig().getMaxInterval()));
        
        // Backoff coefficient
        builder.setBackoffCoefficient(
            step.getRetryConfig().getBackoffCoefficient());
        
        // Maximum attempts
        builder.setMaximumAttempts(
            step.getRetryConfig().getMaxAttempts());
        
        // Non-retryable errors
        if (step.getRetryConfig().getNonRetryableErrors() != null) {
            for (String errorType : 
                 step.getRetryConfig().getNonRetryableErrors()) {
                builder.addDoNotRetry(errorType);
            }
        }
        
        return builder.build();
    }
}
```

---

## Question 52: What retry strategies did you implement?

### Answer

### Retry Strategies

#### 1. **Retry Strategy Types**

```
┌─────────────────────────────────────────────────────────┐
│         Retry Strategies                               │
└─────────────────────────────────────────────────────────┘

1. Exponential Backoff
   ├─ Doubles delay each retry
   ├─ Caps at maximum interval
   └─ Good for transient failures

2. Linear Backoff
   ├─ Fixed increment each retry
   ├─ Predictable delays
   └─ Good for rate limiting

3. Fixed Interval
   ├─ Same delay each retry
   ├─ Simple implementation
   └─ Good for quick retries

4. Custom Backoff
   ├─ Custom delay calculation
   ├─ Business logic based
   └─ Good for specific scenarios
```

#### 2. **Exponential Backoff Implementation**

```java
@Service
public class ExponentialBackoffRetry {
    public RetryOptions createExponentialBackoff(
            int initialInterval,
            int maxInterval,
            double backoffCoefficient,
            int maxAttempts) {
        
        return RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(initialInterval))
            .setMaximumInterval(Duration.ofSeconds(maxInterval))
            .setBackoffCoefficient(backoffCoefficient)
            .setMaximumAttempts(maxAttempts)
            .build();
    }
}

// Example: 1s, 2s, 4s, 8s, 16s (capped at 100s)
RetryOptions exponentialBackoff = createExponentialBackoff(
    1,      // initial: 1 second
    100,    // max: 100 seconds
    2.0,    // backoff: double each time
    5       // max attempts: 5
);
```

#### 3. **Linear Backoff Implementation**

```java
@Service
public class LinearBackoffRetry {
    public RetryOptions createLinearBackoff(
            int initialInterval,
            int increment,
            int maxInterval,
            int maxAttempts) {
        
        // Linear backoff using custom calculation
        return RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(initialInterval))
            .setMaximumInterval(Duration.ofSeconds(maxInterval))
            .setBackoffCoefficient(1.0 + (increment / initialInterval))
            .setMaximumAttempts(maxAttempts)
            .build();
    }
}

// Example: 1s, 2s, 3s, 4s, 5s
RetryOptions linearBackoff = createLinearBackoff(
    1,      // initial: 1 second
    1,      // increment: 1 second
    10,     // max: 10 seconds
    5       // max attempts: 5
);
```

---

## Question 53: How did you handle retry exhaustion?

### Answer

### Retry Exhaustion Handling

#### 1. **Retry Exhaustion Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Retry Exhaustion Handling                     │
└─────────────────────────────────────────────────────────┘

When Retries Exhausted:
├─ 1. Execute fallback action
├─ 2. Mark step as failed
├─ 3. Notify stakeholders
├─ 4. Log error details
└─ 5. Continue or abort workflow
```

#### 2. **Implementation**

```java
@Service
public class RetryExhaustionHandler {
    public ExecutionResult handleRetryExhaustion(
            Step step,
            Exception lastError,
            int attemptCount) {
        
        // Log retry exhaustion
        log.error("Retry exhausted for step {} after {} attempts",
            step.getId(), attemptCount, lastError);
        
        // Check for fallback action
        if (step.getFallbackAction() != null) {
            return executeFallback(step, lastError);
        }
        
        // Check workflow-level error handler
        if (step.getWorkflow().getErrorHandlers() != null) {
            return handleWorkflowError(step, lastError);
        }
        
        // Mark step as failed
        return ExecutionResult.failure(
            "Retry exhausted: " + lastError.getMessage(),
            lastError);
    }
    
    private ExecutionResult executeFallback(
            Step step, Exception error) {
        try {
            log.info("Executing fallback for step {}", step.getId());
            return step.getFallbackAction().execute(error);
        } catch (Exception e) {
            log.error("Fallback execution failed", e);
            return ExecutionResult.failure(
                "Fallback failed: " + e.getMessage(), e);
        }
    }
}
```

#### 3. **Workflow-Level Handling**

```yaml
# Workflow with retry exhaustion handling
workflow:
  steps:
    - id: critical-step
      type: task
      action: criticalService.action
      retry:
        maxAttempts: 3
        backoff: exponential
      onRetryExhaustion:
        type: fallback
        action: fallbackService.action
      onError:
        type: notify
        action: notificationService.notifyError
```

---

## Question 54: What exponential backoff strategies did you use?

### Answer

### Exponential Backoff Strategies

#### 1. **Backoff Strategy Types**

```
┌─────────────────────────────────────────────────────────┐
│         Exponential Backoff Strategies                 │
└─────────────────────────────────────────────────────────┘

1. Standard Exponential
   ├─ Double each retry
   ├─ 1s, 2s, 4s, 8s, 16s
   └─ Good for most cases

2. Aggressive Exponential
   ├─ Higher coefficient
   ├─ 1s, 3s, 9s, 27s, 81s
   └─ Good for overloaded services

3. Conservative Exponential
   ├─ Lower coefficient
   ├─ 1s, 1.5s, 2.25s, 3.375s
   └─ Good for rate-limited services

4. Jittered Exponential
   ├─ Add randomness
   ├─ Prevents thundering herd
   └─ Good for distributed systems
```

#### 2. **Standard Exponential Backoff**

```java
@Service
public class ExponentialBackoffStrategies {
    // Standard: coefficient = 2.0
    public RetryOptions standardExponential() {
        return RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1))
            .setMaximumInterval(Duration.ofSeconds(100))
            .setBackoffCoefficient(2.0)
            .setMaximumAttempts(5)
            .build();
    }
    // Delays: 1s, 2s, 4s, 8s, 16s (capped at 100s)
}
```

#### 3. **Aggressive Exponential Backoff**

```java
// Aggressive: coefficient = 3.0
public RetryOptions aggressiveExponential() {
    return RetryOptions.newBuilder()
        .setInitialInterval(Duration.ofSeconds(1))
        .setMaximumInterval(Duration.ofSeconds(300))
        .setBackoffCoefficient(3.0)
        .setMaximumAttempts(5)
        .build();
}
// Delays: 1s, 3s, 9s, 27s, 81s (capped at 300s)
```

#### 4. **Jittered Exponential Backoff**

```java
// Jittered: add randomness to prevent thundering herd
public RetryOptions jitteredExponential() {
    RetryOptions base = RetryOptions.newBuilder()
        .setInitialInterval(Duration.ofSeconds(1))
        .setMaximumInterval(Duration.ofSeconds(100))
        .setBackoffCoefficient(2.0)
        .setMaximumAttempts(5)
        .build();
    
    // Add jitter (Temporal handles this automatically)
    return base;
}
```

---

## Question 55: How did you ensure retries don't cause duplicate operations?

### Answer

### Preventing Duplicate Operations

#### 1. **Idempotency Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Idempotency Strategy                           │
└─────────────────────────────────────────────────────────┘

1. Idempotent Operations
   ├─ Safe to retry
   ├─ No side effects
   └─ Same result on retry

2. Idempotency Keys
   ├─ Unique operation identifier
   ├─ Check before execution
   └─ Skip if already executed

3. Idempotency Tokens
   ├─ Generate unique token
   ├─ Store with operation
   └─ Verify on retry

4. Idempotency Checks
   ├─ Check operation status
   ├─ Skip if completed
   └─ Return cached result
```

#### 2. **Idempotency Implementation**

```java
@Service
public class IdempotentActivityExecution {
    @Autowired
    private IdempotencyStore idempotencyStore;
    
    public StepResult executeIdempotent(
            Step step,
            WorkflowContext context) {
        
        // Generate idempotency key
        String idempotencyKey = generateIdempotencyKey(
            step.getId(), context.getExecutionId());
        
        // Check if already executed
        StepResult cachedResult = idempotencyStore.get(idempotencyKey);
        if (cachedResult != null) {
            log.info("Returning cached result for step {}", step.getId());
            return cachedResult;
        }
        
        // Execute step
        try {
            StepResult result = step.execute(context);
            
            // Store result for idempotency
            idempotencyStore.put(idempotencyKey, result);
            
            return result;
        } catch (Exception e) {
            // Don't store failed results
            throw e;
        }
    }
    
    private String generateIdempotencyKey(
            String stepId, String executionId) {
        return stepId + ":" + executionId;
    }
}
```

#### 3. **Idempotency Store**

```java
@Service
public class IdempotencyStore {
    @Autowired
    private RedisTemplate<String, StepResult> redisTemplate;
    
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);
    
    public StepResult get(String idempotencyKey) {
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void put(String idempotencyKey, StepResult result) {
        String key = IDEMPOTENCY_PREFIX + idempotencyKey;
        redisTemplate.opsForValue().set(key, result, TTL);
    }
}
```

---

## Summary

Part 11 covers questions 51-55 on Automatic Retry:

51. **Retry Policy Configuration**: Initial interval, max interval, backoff coefficient, max attempts
52. **Retry Strategies**: Exponential, linear, fixed interval, custom backoff
53. **Retry Exhaustion**: Fallback actions, error handling, workflow-level handling
54. **Exponential Backoff**: Standard, aggressive, conservative, jittered strategies
55. **Duplicate Prevention**: Idempotency keys, idempotency store, cached results

Key concepts:
- Configurable retry policies
- Multiple retry strategies
- Handling retry exhaustion
- Exponential backoff variations
- Idempotency for duplicate prevention
