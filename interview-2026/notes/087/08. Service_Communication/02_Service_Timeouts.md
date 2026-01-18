# Service Communication Part 2: Service Timeouts

## Question 137: How do you handle service timeouts?

### Answer

### Timeout Strategy Overview

```
┌─────────────────────────────────────────────────────────┐
│         Timeout Layers                                │
└─────────────────────────────────────────────────────────┘

1. Connection Timeout:
   ├─ Time to establish connection
   ├─ Default: 5-10 seconds
   └─ Network-level

2. Read Timeout:
   ├─ Time to read response
   ├─ Default: 30-60 seconds
   └─ Application-level

3. Write Timeout:
   ├─ Time to write request
   ├─ Default: 10-30 seconds
   └─ Application-level

4. Overall Timeout:
   ├─ Total request time
   ├─ Default: 60-120 seconds
   └─ End-to-end
```

### HTTP Client Timeout Configuration

#### 1. **RestTemplate Configuration**

```java
@Configuration
public class HttpClientConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        
        // Connection timeout: 10 seconds
        factory.setConnectTimeout(10000);
        
        // Read timeout: 30 seconds
        factory.setReadTimeout(30000);
        
        // Connection request timeout: 10 seconds
        factory.setConnectionRequestTimeout(10000);
        
        return new RestTemplate(factory);
    }
}
```

#### 2. **WebClient Configuration (Reactive)**

```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(30))
                    .addHandlerLast(new WriteTimeoutHandler(10)));
        
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
```

#### 3. **Feign Client Configuration**

```java
@Configuration
public class FeignConfig {
    
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            10000,  // Connect timeout: 10 seconds
            30000   // Read timeout: 30 seconds
        );
    }
    
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
            100,    // Initial interval
            1000,   // Max interval
            3       // Max attempts
        );
    }
}

@FeignClient(name = "agent-service", 
             configuration = FeignConfig.class)
public interface AgentServiceClient {
    
    @GetMapping("/agents/{agentId}")
    Agent getAgent(@PathVariable String agentId);
}
```

### Timeout Handling Strategies

#### 1. **Timeout with Fallback**

```java
@Service
public class AgentServiceClient {
    
    private final RestTemplate restTemplate;
    private final AgentCacheService cacheService;
    
    public Agent getAgent(String agentId) {
        try {
            // Try remote service with timeout
            return restTemplate.getForObject(
                "http://agent-service/agents/" + agentId,
                Agent.class
            );
            
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                // Timeout occurred, use fallback
                log.warn("Timeout calling agent service, using cache", e);
                return cacheService.getAgent(agentId)
                    .orElseThrow(() -> new AgentNotFoundException(agentId));
            }
            throw e;
        }
    }
}
```

#### 2. **Timeout with Retry**

```java
@Service
public class ResilientServiceClient {
    
    @Retryable(
        value = {ResourceAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Response callService(Request request) {
        try {
            return restTemplate.postForObject(
                "http://service/api",
                request,
                Response.class
            );
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                // Retry on timeout
                throw e;
            }
            throw new ServiceException("Service call failed", e);
        }
    }
    
    @Recover
    public Response recover(ResourceAccessException e, Request request) {
        // Fallback after all retries failed
        return getDefaultResponse(request);
    }
}
```

#### 3. **Timeout with Circuit Breaker**

```java
@Service
public class CircuitBreakerServiceClient {
    
    private final CircuitBreaker circuitBreaker;
    private final RestTemplate restTemplate;
    
    public CircuitBreakerServiceClient() {
        this.circuitBreaker = CircuitBreaker.of("agent-service",
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .build());
    }
    
    public Agent getAgent(String agentId) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                return restTemplate.getForObject(
                    "http://agent-service/agents/" + agentId,
                    Agent.class
                );
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    throw new CircuitBreakerOpenException("Service timeout", e);
                }
                throw e;
            }
        });
    }
}
```

### Timeout Configuration by Service Type

#### 1. **Service-Specific Timeouts**

```yaml
# application.yml
services:
  timeouts:
    agent-service:
      connect: 5000      # 5 seconds
      read: 20000       # 20 seconds
      write: 10000      # 10 seconds
      overall: 30000    # 30 seconds
    
    nlu-service:
      connect: 5000
      read: 5000        # Short timeout for NLU
      write: 5000
      overall: 10000
    
    position-service:
      connect: 5000
      read: 10000       # Fast for position queries
      write: 5000
      overall: 15000
    
    settlement-service:
      connect: 10000
      read: 60000       # Longer for settlement
      write: 10000
      overall: 120000
```

#### 2. **Dynamic Timeout Configuration**

```java
@Configuration
@ConfigurationProperties(prefix = "services.timeouts")
public class ServiceTimeoutConfig {
    
    private Map<String, TimeoutSettings> services = new HashMap<>();
    
    public TimeoutSettings getTimeoutSettings(String serviceName) {
        return services.getOrDefault(serviceName, 
            TimeoutSettings.defaultSettings());
    }
    
    @Data
    public static class TimeoutSettings {
        private int connect = 5000;
        private int read = 30000;
        private int write = 10000;
        private int overall = 60000;
        
        public static TimeoutSettings defaultSettings() {
            return new TimeoutSettings();
        }
    }
}

@Service
public class ConfigurableServiceClient {
    
    private final ServiceTimeoutConfig timeoutConfig;
    private final Map<String, RestTemplate> restTemplates = new HashMap<>();
    
    public <T> T callService(String serviceName, String endpoint, 
                             Class<T> responseType) {
        RestTemplate restTemplate = getOrCreateRestTemplate(serviceName);
        return restTemplate.getForObject(endpoint, responseType);
    }
    
    private RestTemplate getOrCreateRestTemplate(String serviceName) {
        return restTemplates.computeIfAbsent(serviceName, name -> {
            ServiceTimeoutConfig.TimeoutSettings settings = 
                timeoutConfig.getTimeoutSettings(name);
            
            HttpComponentsClientHttpRequestFactory factory = 
                new HttpComponentsClientHttpRequestFactory();
            factory.setConnectTimeout(settings.getConnect());
            factory.setReadTimeout(settings.getRead());
            factory.setConnectionRequestTimeout(settings.getWrite());
            
            return new RestTemplate(factory);
        });
    }
}
```

### Timeout Monitoring and Alerting

#### 1. **Timeout Metrics**

```java
@Component
public class TimeoutMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    
    public <T> T measureServiceCall(String serviceName, 
                                    String operation,
                                    Supplier<T> supplier) {
        Timer timer = getOrCreateTimer(serviceName, operation);
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            T result = supplier.get();
            sample.stop(timer);
            return result;
            
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                // Record timeout
                Counter.builder("service.timeout")
                    .tag("service", serviceName)
                    .tag("operation", operation)
                    .register(meterRegistry)
                    .increment();
            }
            throw e;
        }
    }
    
    private Timer getOrCreateTimer(String serviceName, String operation) {
        String key = serviceName + ":" + operation;
        return timers.computeIfAbsent(key, k ->
            Timer.builder("service.call.duration")
                .tag("service", serviceName)
                .tag("operation", operation)
                .register(meterRegistry)
        );
    }
}
```

#### 2. **Timeout Alerting**

```java
@Component
public class TimeoutAlertService {
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkTimeoutRates() {
        Map<String, Double> timeoutRates = getTimeoutRates();
        
        timeoutRates.forEach((service, rate) -> {
            if (rate > 0.1) { // 10% timeout rate
                alertService.sendAlert(
                    AlertLevel.CRITICAL,
                    String.format("High timeout rate for %s: %.2f%%", 
                        service, rate * 100)
                );
            }
        });
    }
    
    private Map<String, Double> getTimeoutRates() {
        // Query metrics registry for timeout rates
        return metricsRegistry.getTimeoutRates();
    }
}
```

### Best Practices

#### 1. **Timeout Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Timeout Best Practices                        │
└─────────────────────────────────────────────────────────┘

1. Set Appropriate Timeouts:
   ├─ Based on service SLA
   ├─ Consider network latency
   ├─ Account for processing time
   └─ Add buffer for variability

2. Use Different Timeouts:
   ├─ Short for fast services
   ├─ Medium for standard services
   └─ Long for slow operations

3. Implement Fallbacks:
   ├─ Cache fallback
   ├─ Default values
   ├─ Degraded functionality
   └─ Error responses

4. Monitor Timeouts:
   ├─ Track timeout rates
   ├─ Alert on high rates
   ├─ Analyze timeout patterns
   └─ Adjust timeouts based on data

5. Retry Strategy:
   ├─ Retry on timeout
   ├─ Exponential backoff
   ├─ Max retry attempts
   └─ Circuit breaker integration
```

#### 2. **Timeout Configuration Example**

```java
@Service
public class TimeoutAwareServiceClient {
    
    // Fast service: 5s timeout
    public Agent getAgent(String agentId) {
        return callWithTimeout(
            () -> agentService.getAgent(agentId),
            Duration.ofSeconds(5),
            () -> cacheService.getAgent(agentId)
        );
    }
    
    // Medium service: 30s timeout
    public NLUResponse processMessage(String message) {
        return callWithTimeout(
            () -> nluService.processMessage(message),
            Duration.ofSeconds(30),
            () -> getCachedNLUResponse(message)
        );
    }
    
    // Slow service: 2min timeout
    public SettlementResult settleTrade(Trade trade) {
        return callWithTimeout(
            () -> settlementService.settle(trade),
            Duration.ofMinutes(2),
            () -> queueSettlement(trade)
        );
    }
    
    private <T> T callWithTimeout(Supplier<T> supplier,
                                  Duration timeout,
                                  Supplier<Optional<T>> fallback) {
        try {
            return CompletableFuture
                .supplyAsync(supplier)
                .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                
        } catch (TimeoutException e) {
            log.warn("Service call timed out after {}", timeout);
            return fallback.get()
                .orElseThrow(() -> new ServiceTimeoutException(
                    "Service timed out and no fallback available"));
        } catch (Exception e) {
            throw new ServiceException("Service call failed", e);
        }
    }
}
```

---

## Summary

**Timeout Strategy**:
- **Connection Timeout**: 5-10 seconds (network level)
- **Read Timeout**: 30-60 seconds (application level)
- **Service-Specific**: Based on service characteristics
- **Fallback**: Cache or default values on timeout
- **Monitoring**: Track and alert on timeout rates

**Key Points**:
1. Configure timeouts at multiple levels
2. Use service-specific timeouts
3. Implement fallback mechanisms
4. Monitor and adjust based on metrics
5. Integrate with circuit breakers and retries
