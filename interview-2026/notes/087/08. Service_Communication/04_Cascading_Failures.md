# Service Communication Part 4: Cascading Failures

## Question 139: How do you handle cascading failures?

### Answer

### Cascading Failure Scenario

```
┌─────────────────────────────────────────────────────────┐
│         Cascading Failure Chain                        │
└─────────────────────────────────────────────────────────┘

Service A (Down)
    │
    ├─ Service B depends on A
    │  │
    │  ├─ Retries → Overloads A
    │  ├─ Timeouts → Blocks threads
    │  └─ Fails
    │
    ├─ Service C depends on B
    │  │
    │  ├─ Retries → Overloads B
    │  ├─ Timeouts → Blocks threads
    │  └─ Fails
    │
    └─ Service D depends on C
       │
       └─ Entire system fails

Result: Single failure cascades to entire system
```

### Prevention Strategies

#### 1. **Circuit Breaker Pattern**

```java
@Service
public class CircuitBreakerService {
    
    private final CircuitBreaker circuitBreaker;
    
    public CircuitBreakerService() {
        this.circuitBreaker = CircuitBreaker.of("downstream-service",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)        // Open at 50% failure rate
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)         // Need 5 calls before opening
                .build());
    }
    
    public Response callDownstream(Request request) {
        return circuitBreaker.executeSupplier(() -> {
            return downstreamService.call(request);
        });
    }
}
```

#### 2. **Bulkhead Pattern**

```java
@Service
public class BulkheadService {
    
    // Separate thread pools for different services
    private final ExecutorService agentServicePool = 
        Executors.newFixedThreadPool(10);
    
    private final ExecutorService nluServicePool = 
        Executors.newFixedThreadPool(10);
    
    private final ExecutorService positionServicePool = 
        Executors.newFixedThreadPool(10);
    
    public Agent getAgent(String agentId) {
        return CompletableFuture
            .supplyAsync(() -> agentService.getAgent(agentId), 
                        agentServicePool)
            .get();
    }
    
    public NLUResponse processMessage(String message) {
        return CompletableFuture
            .supplyAsync(() -> nluService.processMessage(message),
                        nluServicePool)
            .get();
    }
}
```

#### 3. **Timeout Protection**

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
        scheduler.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new ServiceTimeoutException("Operation timed out", e);
        }
    }
}
```

### Failure Isolation

#### 1. **Service Isolation**

```
┌─────────────────────────────────────────────────────────┐
│         Service Isolation Strategy                     │
└─────────────────────────────────────────────────────────┘

Isolation Levels:
├─ Process Isolation (Microservices)
├─ Thread Pool Isolation (Bulkhead)
├─ Circuit Breaker Isolation
└─ Timeout Isolation

Benefits:
├─ Failure contained to one service
├─ Other services continue operating
├─ Graceful degradation
└─ Faster recovery
```

#### 2. **Resource Isolation**

```java
@Configuration
public class ResourceIsolationConfig {
    
    // Separate connection pools
    @Bean
    public RestTemplate agentServiceClient() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(createHttpClient(10)); // 10 connections
        return new RestTemplate(factory);
    }
    
    @Bean
    public RestTemplate nluServiceClient() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(createHttpClient(20)); // 20 connections
        return new RestTemplate(factory);
    }
    
    private CloseableHttpClient createHttpClient(int maxConnections) {
        return HttpClients.custom()
            .setMaxConnTotal(maxConnections)
            .setMaxConnPerRoute(maxConnections)
            .build();
    }
}
```

### Graceful Degradation

#### 1. **Fallback Mechanisms**

```java
@Service
public class DegradedService {
    
    @CircuitBreaker(name = "agent-service", 
                    fallbackMethod = "getAgentFallback")
    public Agent getAgent(String agentId) {
        return agentService.getAgent(agentId);
    }
    
    public Agent getAgentFallback(String agentId, Exception e) {
        log.warn("Agent service unavailable, using cache", e);
        
        // Fallback 1: Cache
        Optional<Agent> cached = cacheService.getAgent(agentId);
        if (cached.isPresent()) {
            return cached.get();
        }
        
        // Fallback 2: Default agent
        return getDefaultAgent();
    }
}
```

#### 2. **Degraded Functionality**

```java
@Service
public class ConversationService {
    
    public ConversationResponse processConversation(
            ConversationRequest request) {
        
        ConversationResponse response = new ConversationResponse();
        
        // Try to get agent
        try {
            Agent agent = agentService.getAgent(request.getAgentId());
            response.setAgent(agent);
        } catch (Exception e) {
            // Degrade: Continue without agent info
            log.warn("Could not get agent, continuing without it", e);
        }
        
        // Try to process NLU
        try {
            NLUResponse nlu = nluService.processMessage(request.getMessage());
            response.setNluResponse(nlu);
        } catch (Exception e) {
            // Degrade: Use simple keyword matching
            log.warn("NLU unavailable, using simple matching", e);
            response.setNluResponse(simpleKeywordMatch(request.getMessage()));
        }
        
        return response;
    }
}
```

### Rate Limiting

```java
@Service
public class RateLimitedService {
    
    private final RateLimiter rateLimiter;
    
    public RateLimitedService() {
        this.rateLimiter = RateLimiter.of("downstream-service",
            RateLimiterConfig.custom()
                .limitForPeriod(100)              // 100 requests
                .limitRefreshPeriod(Duration.ofSeconds(1))  // per second
                .timeoutDuration(Duration.ofSeconds(5))
                .build());
    }
    
    public Response callService(Request request) {
        // Rate limit prevents overwhelming downstream
        return rateLimiter.executeSupplier(() -> {
            return downstreamService.call(request);
        });
    }
}
```

### Monitoring and Alerting

```java
@Component
public class CascadingFailureDetector {
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void detectCascadingFailures() {
        Map<String, Double> failureRates = getFailureRates();
        Map<String, List<String>> dependencies = getDependencies();
        
        // Check for cascading pattern
        for (Map.Entry<String, Double> entry : failureRates.entrySet()) {
            String service = entry.getKey();
            double failureRate = entry.getValue();
            
            if (failureRate > 0.5) { // 50% failure rate
                List<String> dependents = dependencies.get(service);
                
                // Check if dependents also failing
                for (String dependent : dependents) {
                    double dependentFailureRate = 
                        failureRates.getOrDefault(dependent, 0.0);
                    
                    if (dependentFailureRate > 0.3) {
                        // Potential cascading failure
                        alertService.sendAlert(
                            AlertLevel.CRITICAL,
                            String.format(
                                "Potential cascading failure: %s (%.2f%%) -> %s (%.2f%%)",
                                service, failureRate * 100,
                                dependent, dependentFailureRate * 100
                            )
                        );
                    }
                }
            }
        }
    }
}
```

---

## Summary

**Cascading Failure Prevention**:
- **Circuit Breaker**: Fail fast to prevent overload
- **Bulkhead**: Isolate resources
- **Timeout**: Prevent thread blocking
- **Rate Limiting**: Prevent overwhelming downstream
- **Graceful Degradation**: Continue with reduced functionality

**Key Points**:
1. Use circuit breakers to fail fast
2. Isolate resources with bulkheads
3. Set timeouts to prevent blocking
4. Implement graceful degradation
5. Monitor for cascading patterns
