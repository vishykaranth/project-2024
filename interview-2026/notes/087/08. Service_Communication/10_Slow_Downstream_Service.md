# Service Communication Part 10: Slow Downstream Service

## Question 145: What happens if a downstream service is slow?

### Answer

### Slow Downstream Service Scenarios

```
┌─────────────────────────────────────────────────────────┐
│         Slow Service Impact                            │
└─────────────────────────────────────────────────────────┘

Downstream Service Slow:
├─ Blocks calling service threads
├─ Increases response time
├─ Reduces throughput
├─ May cause timeouts
└─ Can cascade to other services
```

### Detection Strategies

#### 1. **Response Time Monitoring**

```java
@Component
public class ResponseTimeMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    
    public <T> T measureCall(String serviceName, 
                             String operation,
                             Supplier<T> supplier) {
        Timer timer = getOrCreateTimer(serviceName, operation);
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            T result = supplier.get();
            sample.stop(timer);
            
            // Check if response time is high
            double p95 = timer.percentile(0.95, TimeUnit.MILLISECONDS);
            if (p95 > 1000) { // 1 second
                alertSlowService(serviceName, operation, p95);
            }
            
            return result;
        } catch (Exception e) {
            sample.stop(timer);
            throw e;
        }
    }
    
    private void alertSlowService(String serviceName, 
                                  String operation, 
                                  double p95) {
        alertService.sendAlert(
            AlertLevel.WARNING,
            String.format("Slow service detected: %s/%s P95: %.2fms",
                serviceName, operation, p95)
        );
    }
}
```

#### 2. **Timeout Detection**

```java
@Service
public class TimeoutDetectionService {
    
    private final Counter timeoutCounter;
    
    public TimeoutDetectionService(MeterRegistry meterRegistry) {
        this.timeoutCounter = Counter.builder("service.timeout")
            .register(meterRegistry);
    }
    
    public <T> T callWithTimeoutDetection(String serviceName,
                                          Supplier<T> operation,
                                          Duration timeout) {
        try {
            return CompletableFuture
                .supplyAsync(operation)
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                
        } catch (TimeoutException e) {
            timeoutCounter.increment(
                Tags.of("service", serviceName)
            );
            
            log.warn("Service {} timed out after {}ms", 
                serviceName, timeout.toMillis());
            
            throw new ServiceTimeoutException(
                "Service " + serviceName + " timed out", e);
        }
    }
}
```

### Mitigation Strategies

#### 1. **Timeout Protection**

```java
@Service
public class TimeoutProtectedService {
    
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(10);
    
    public <T> T callWithTimeout(Supplier<T> operation, 
                                 Duration timeout) {
        CompletableFuture<T> future = CompletableFuture
            .supplyAsync(operation);
        
        // Cancel after timeout
        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        
        try {
            T result = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            timeoutTask.cancel(false);
            return result;
        } catch (TimeoutException e) {
            throw new ServiceTimeoutException("Operation timed out", e);
        } catch (CancellationException e) {
            throw new ServiceTimeoutException("Operation cancelled", e);
        }
    }
}
```

#### 2. **Circuit Breaker for Slow Services**

```java
@Service
public class SlowServiceCircuitBreaker {
    
    private final CircuitBreaker circuitBreaker;
    
    public SlowServiceCircuitBreaker() {
        this.circuitBreaker = CircuitBreaker.of("slow-service",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)           // 50% slow calls
                .slowCallDurationThreshold(Duration.ofSeconds(2))  // > 2s is slow
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build());
    }
    
    public Response callSlowService(Request request) {
        return circuitBreaker.executeSupplier(() -> {
            return slowService.call(request);
        });
    }
}
```

#### 3. **Async Processing**

```java
@Service
public class AsyncServiceCall {
    
    private final ExecutorService executorService = 
        Executors.newFixedThreadPool(20);
    
    public CompletableFuture<Response> callAsync(Request request) {
        return CompletableFuture.supplyAsync(() -> {
            return slowService.call(request);
        }, executorService);
    }
    
    public Response callWithTimeout(Request request, Duration timeout) {
        try {
            return callAsync(request)
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // Return default or throw
            return getDefaultResponse(request);
        }
    }
}
```

#### 4. **Request Queuing**

```java
@Service
public class QueuedServiceCall {
    
    private final BlockingQueue<Request> requestQueue = 
        new LinkedBlockingQueue<>(1000);
    
    @PostConstruct
    public void init() {
        // Process queue in background
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Request request = requestQueue.take();
                    processRequest(request);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    public void submitRequest(Request request) {
        if (!requestQueue.offer(request)) {
            // Queue full, reject or use fallback
            throw new QueueFullException("Request queue is full");
        }
    }
}
```

#### 5. **Load Shedding**

```java
@Service
public class LoadSheddingService {
    
    private final RateLimiter rateLimiter;
    private volatile boolean loadSheddingEnabled = false;
    
    public LoadSheddingService() {
        this.rateLimiter = RateLimiter.create(100.0); // 100 requests/second
    }
    
    public Response callService(Request request) {
        if (loadSheddingEnabled) {
            // Check rate limit
            if (!rateLimiter.tryAcquire()) {
                // Reject request
                throw new LoadSheddingException(
                    "Request rejected due to load shedding");
            }
        }
        
        return slowService.call(request);
    }
    
    @Scheduled(fixedRate = 10000)
    public void checkLoad() {
        double responseTime = getAverageResponseTime();
        if (responseTime > 2000) { // 2 seconds
            enableLoadShedding();
        } else if (responseTime < 500) { // 500ms
            disableLoadShedding();
        }
    }
}
```

### Best Practices

```
┌─────────────────────────────────────────────────────────┐
│         Best Practices for Slow Services               │
└─────────────────────────────────────────────────────────┘

1. Set Appropriate Timeouts:
   ├─ Based on service SLA
   ├─ Consider normal response time
   └─ Add buffer for variability

2. Use Circuit Breakers:
   ├─ Detect slow calls
   ├─ Fail fast when slow
   └─ Allow recovery

3. Implement Async Processing:
   ├─ Don't block threads
   ├─ Use CompletableFuture
   └─ Process in background

4. Monitor Response Times:
   ├─ Track P50, P95, P99
   ├─ Alert on high percentiles
   └─ Adjust timeouts based on data

5. Use Load Shedding:
   ├─ Reject requests when overloaded
   ├─ Protect downstream services
   └─ Maintain service quality

6. Implement Queuing:
   ├─ Queue requests when busy
   ├─ Process as capacity allows
   └─ Set queue size limits
```

---

## Summary

**Slow Downstream Service Handling**:
- **Detection**: Monitor response times and timeouts
- **Protection**: Timeouts, circuit breakers, async processing
- **Mitigation**: Load shedding, queuing, fallbacks
- **Monitoring**: Track and alert on slow services

**Key Points**:
1. Set appropriate timeouts
2. Use circuit breakers for slow call detection
3. Implement async processing
4. Monitor response times
5. Use load shedding when necessary
