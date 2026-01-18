# Service Communication Part 3: Retry Strategy

## Question 138: What's the retry strategy for service calls?

### Answer

### Retry Strategy Overview

```
┌─────────────────────────────────────────────────────────┐
│         Retry Strategy Components                      │
└─────────────────────────────────────────────────────────┘

1. Retry Conditions:
   ├─ Transient failures
   ├─ Network errors
   ├─ Timeout errors
   └─ 5xx server errors

2. Retry Policy:
   ├─ Max attempts
   ├─ Backoff strategy
   ├─ Jitter
   └─ Retryable exceptions

3. Retry Metrics:
   ├─ Retry count
   ├─ Success rate
   ├─ Failure rate
   └─ Average attempts
```

### Retry Implementation Patterns

#### 1. **Exponential Backoff Retry**

```java
@Service
public class ExponentialBackoffRetryService {
    
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_DELAY_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    
    public <T> T executeWithRetry(Supplier<T> operation) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
                
            } catch (RetryableException e) {
                lastException = e;
                attempt++;
                
                if (attempt >= MAX_RETRIES) {
                    break;
                }
                
                // Calculate delay with exponential backoff
                long delay = (long) (INITIAL_DELAY_MS * 
                    Math.pow(BACKOFF_MULTIPLIER, attempt - 1));
                
                // Add jitter to prevent thundering herd
                long jitter = (long) (Math.random() * delay * 0.1);
                delay += jitter;
                
                log.warn("Retry attempt {} after {}ms", attempt, delay);
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        
        throw new MaxRetriesExceededException(
            "Operation failed after " + MAX_RETRIES + " attempts", 
            lastException);
    }
}
```

#### 2. **Spring Retry Implementation**

```java
@Service
public class SpringRetryService {
    
    @Retryable(
        value = {ResourceAccessException.class, HttpServerErrorException.class},
        maxAttempts = 3,
        backoff = @Backoff(
            delay = 1000,
            multiplier = 2.0,
            maxDelay = 10000
        )
    )
    public Agent getAgent(String agentId) {
        return restTemplate.getForObject(
            "http://agent-service/agents/" + agentId,
            Agent.class
        );
    }
    
    @Recover
    public Agent recover(ResourceAccessException e, String agentId) {
        log.error("Failed to get agent after retries, using cache", e);
        return cacheService.getAgent(agentId)
            .orElseThrow(() -> new AgentNotFoundException(agentId));
    }
}
```

#### 3. **Resilience4j Retry**

```java
@Service
public class Resilience4jRetryService {
    
    private final Retry retry;
    
    public Resilience4jRetryService() {
        this.retry = Retry.of("agent-service",
            RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .intervalFunction(IntervalFunction.ofExponentialBackoff(
                    Duration.ofSeconds(1),
                    2.0))
                .retryExceptions(ResourceAccessException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build());
    }
    
    public Agent getAgent(String agentId) {
        return retry.executeSupplier(() -> {
            return restTemplate.getForObject(
                "http://agent-service/agents/" + agentId,
                Agent.class
            );
        });
    }
}
```

### Retry Configuration

#### 1. **Service-Specific Retry Configuration**

```yaml
# application.yml
resilience4j:
  retry:
    instances:
      agent-service:
        maxAttempts: 3
        waitDuration: 1000
        intervalFunction: exponential
        exponentialBackoffMultiplier: 2.0
        retryExceptions:
          - org.springframework.web.client.ResourceAccessException
          - java.net.SocketTimeoutException
        ignoreExceptions:
          - java.lang.IllegalArgumentException
      
      nlu-service:
        maxAttempts: 5
        waitDuration: 500
        exponentialBackoffMultiplier: 1.5
        retryExceptions:
          - org.springframework.web.client.HttpServerErrorException
      
      settlement-service:
        maxAttempts: 2
        waitDuration: 2000
        exponentialBackoffMultiplier: 2.0
```

#### 2. **Dynamic Retry Configuration**

```java
@Configuration
@ConfigurationProperties(prefix = "services.retry")
public class RetryConfiguration {
    
    private Map<String, RetrySettings> services = new HashMap<>();
    
    @Data
    public static class RetrySettings {
        private int maxAttempts = 3;
        private long waitDuration = 1000;
        private double backoffMultiplier = 2.0;
        private List<String> retryableExceptions = new ArrayList<>();
        private List<String> ignoredExceptions = new ArrayList<>();
    }
    
    public RetrySettings getRetrySettings(String serviceName) {
        return services.getOrDefault(serviceName, 
            new RetrySettings());
    }
}
```

### Retry Strategies

#### 1. **Fixed Delay Retry**

```
┌─────────────────────────────────────────────────────────┐
│         Fixed Delay Retry                              │
└─────────────────────────────────────────────────────────┘

Attempt 1: Immediate
    │
    ├─ Fails
    │
    ▼
Wait: 1 second (fixed)

Attempt 2: After 1 second
    │
    ├─ Fails
    │
    ▼
Wait: 1 second (fixed)

Attempt 3: After 1 second
    │
    └─ Success or Final Failure

Use Case: Predictable failures, low contention
```

#### 2. **Exponential Backoff Retry**

```
┌─────────────────────────────────────────────────────────┐
│         Exponential Backoff Retry                      │
└─────────────────────────────────────────────────────────┘

Attempt 1: Immediate
    │
    ├─ Fails
    │
    ▼
Wait: 1 second (1 * 2^0)

Attempt 2: After 1 second
    │
    ├─ Fails
    │
    ▼
Wait: 2 seconds (1 * 2^1)

Attempt 3: After 2 seconds
    │
    ├─ Fails
    │
    ▼
Wait: 4 seconds (1 * 2^2)

Attempt 4: After 4 seconds
    │
    └─ Success or Final Failure

Use Case: High contention, server overload
```

#### 3. **Linear Backoff Retry**

```
┌─────────────────────────────────────────────────────────┐
│         Linear Backoff Retry                           │
└─────────────────────────────────────────────────────────┘

Attempt 1: Immediate
    │
    ├─ Fails
    │
    ▼
Wait: 1 second

Attempt 2: After 1 second
    │
    ├─ Fails
    │
    ▼
Wait: 2 seconds

Attempt 3: After 2 seconds
    │
    └─ Success or Final Failure

Use Case: Moderate contention
```

### Retry with Jitter

```java
@Service
public class JitterRetryService {
    
    public <T> T executeWithJitter(Supplier<T> operation) {
        int attempt = 0;
        long baseDelay = 1000;
        
        while (attempt < MAX_RETRIES) {
            try {
                return operation.get();
                
            } catch (RetryableException e) {
                attempt++;
                
                if (attempt >= MAX_RETRIES) {
                    break;
                }
                
                // Exponential backoff
                long delay = baseDelay * (long) Math.pow(2, attempt - 1);
                
                // Add jitter (random 0-20% of delay)
                double jitterFactor = 0.2 * Math.random();
                long jitter = (long) (delay * jitterFactor);
                delay += jitter;
                
                log.info("Retry attempt {} with delay {}ms (jitter: {}ms)", 
                    attempt, delay, jitter);
                
                sleep(delay);
            }
        }
        
        throw new MaxRetriesExceededException();
    }
}
```

### Retryable vs Non-Retryable Exceptions

```java
@Service
public class SelectiveRetryService {
    
    @Retryable(
        value = {
            ResourceAccessException.class,      // Network errors
            SocketTimeoutException.class,       // Timeout errors
            HttpServerErrorException.class      // 5xx errors
        },
        maxAttempts = 3,
        exclude = {
            IllegalArgumentException.class,    // Don't retry client errors
            HttpClientErrorException.class      // Don't retry 4xx errors
        }
    )
    public Response callService(Request request) {
        return restTemplate.postForObject(
            "http://service/api",
            request,
            Response.class
        );
    }
}
```

### Retry Metrics and Monitoring

```java
@Component
public class RetryMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public <T> T executeWithMetrics(String serviceName, 
                                    Supplier<T> operation) {
        Counter retryCounter = Counter.builder("service.retry.count")
            .tag("service", serviceName)
            .register(meterRegistry);
        
        Timer retryTimer = Timer.builder("service.retry.duration")
            .tag("service", serviceName)
            .register(meterRegistry);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        int attempts = 0;
        
        while (attempts < MAX_RETRIES) {
            attempts++;
            
            try {
                T result = operation.get();
                
                // Record successful attempt
                sample.stop(retryTimer);
                Gauge.builder("service.retry.attempts", 
                    () -> attempts)
                    .tag("service", serviceName)
                    .tag("status", "success")
                    .register(meterRegistry);
                
                return result;
                
            } catch (RetryableException e) {
                retryCounter.increment();
                
                if (attempts >= MAX_RETRIES) {
                    // Record failure
                    sample.stop(retryTimer);
                    Gauge.builder("service.retry.attempts",
                        () -> attempts)
                        .tag("service", serviceName)
                        .tag("status", "failure")
                        .register(meterRegistry);
                    
                    throw new MaxRetriesExceededException(e);
                }
            }
        }
        
        throw new MaxRetriesExceededException();
    }
}
```

### Best Practices

```
┌─────────────────────────────────────────────────────────┐
│         Retry Best Practices                           │
└─────────────────────────────────────────────────────────┘

1. Retry Only Transient Failures:
   ├─ Network errors
   ├─ Timeout errors
   ├─ 5xx server errors
   └─ NOT 4xx client errors

2. Use Exponential Backoff:
   ├─ Prevents server overload
   ├─ Reduces contention
   └─ Allows recovery time

3. Add Jitter:
   ├─ Prevents thundering herd
   ├─ Distributes load
   └─ Reduces collisions

4. Set Max Attempts:
   ├─ Prevent infinite retries
   ├─ Balance between persistence and latency
   └─ Consider user experience

5. Monitor Retry Rates:
   ├─ Track retry success/failure
   ├─ Alert on high retry rates
   └─ Adjust strategy based on metrics

6. Idempotent Operations:
   ├─ Ensure operations are idempotent
   ├─ Handle duplicate requests
   └─ Use idempotency keys
```

---

## Summary

**Retry Strategy**:
- **Exponential Backoff**: Prevents server overload
- **Jitter**: Prevents thundering herd
- **Selective Retry**: Only retry transient failures
- **Max Attempts**: Balance persistence and latency
- **Monitoring**: Track and optimize retry rates

**Key Points**:
1. Retry only transient failures
2. Use exponential backoff with jitter
3. Set appropriate max attempts
4. Monitor retry metrics
5. Ensure idempotent operations
