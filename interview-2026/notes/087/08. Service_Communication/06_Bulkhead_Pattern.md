# Service Communication Part 6: Bulkhead Pattern

## Question 141: What's the bulkhead pattern, and where did you use it?

### Answer

### Bulkhead Pattern Overview

```
┌─────────────────────────────────────────────────────────┐
│         Bulkhead Pattern Analogy                      │
└─────────────────────────────────────────────────────────┘

Ship Bulkheads:
├─ Separate compartments
├─ If one floods, others stay dry
├─ Prevents total sinking
└─ Isolates failures

Software Bulkheads:
├─ Separate thread pools
├─ Separate connection pools
├─ Separate resources
└─ Isolates failures
```

### Thread Pool Isolation

#### 1. **Service-Specific Thread Pools**

```java
@Configuration
public class BulkheadConfiguration {
    
    // Agent Service Thread Pool
    @Bean("agentServiceExecutor")
    public ExecutorService agentServiceExecutor() {
        return new ThreadPoolExecutor(
            5,                      // Core pool size
            10,                     // Max pool size
            60L, TimeUnit.SECONDS,  // Keep alive
            new LinkedBlockingQueue<>(100),  // Queue
            new ThreadFactoryBuilder()
                .setNameFormat("agent-service-%d")
                .build()
        );
    }
    
    // NLU Service Thread Pool
    @Bean("nluServiceExecutor")
    public ExecutorService nluServiceExecutor() {
        return new ThreadPoolExecutor(
            10,                     // Core pool size
            20,                     // Max pool size
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadFactoryBuilder()
                .setNameFormat("nlu-service-%d")
                .build()
        );
    }
    
    // Position Service Thread Pool
    @Bean("positionServiceExecutor")
    public ExecutorService positionServiceExecutor() {
        return new ThreadPoolExecutor(
            3,                      // Core pool size
            5,                      // Max pool size
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(50),
            new ThreadFactoryBuilder()
                .setNameFormat("position-service-%d")
                .build()
        );
    }
}
```

#### 2. **Using Thread Pool Isolation**

```java
@Service
public class BulkheadService {
    
    @Qualifier("agentServiceExecutor")
    private final ExecutorService agentExecutor;
    
    @Qualifier("nluServiceExecutor")
    private final ExecutorService nluExecutor;
    
    @Qualifier("positionServiceExecutor")
    private final ExecutorService positionExecutor;
    
    public Agent getAgent(String agentId) {
        return CompletableFuture
            .supplyAsync(() -> agentService.getAgent(agentId), 
                        agentExecutor)
            .join();
    }
    
    public NLUResponse processMessage(String message) {
        return CompletableFuture
            .supplyAsync(() -> nluService.processMessage(message),
                        nluExecutor)
            .join();
    }
    
    public Position getPosition(String accountId, String instrumentId) {
        return CompletableFuture
            .supplyAsync(() -> 
                positionService.getPosition(accountId, instrumentId),
                positionExecutor)
            .join();
    }
}
```

### Connection Pool Isolation

#### 1. **HTTP Client Isolation**

```java
@Configuration
public class HttpClientBulkheadConfig {
    
    @Bean("agentServiceHttpClient")
    public CloseableHttpClient agentServiceHttpClient() {
        return HttpClients.custom()
            .setMaxConnTotal(20)        // Total connections
            .setMaxConnPerRoute(10)     // Per route
            .setConnectionTimeToLive(30, TimeUnit.SECONDS)
            .build();
    }
    
    @Bean("nluServiceHttpClient")
    public CloseableHttpClient nluServiceHttpClient() {
        return HttpClients.custom()
            .setMaxConnTotal(50)        // More for NLU
            .setMaxConnPerRoute(25)
            .setConnectionTimeToLive(30, TimeUnit.SECONDS)
            .build();
    }
    
    @Bean("positionServiceHttpClient")
    public CloseableHttpClient positionServiceHttpClient() {
        return HttpClients.custom()
            .setMaxConnTotal(10)        // Fewer for position
            .setMaxConnPerRoute(5)
            .setConnectionTimeToLive(30, TimeUnit.SECONDS)
            .build();
    }
}
```

#### 2. **Database Connection Pool Isolation**

```yaml
# application.yml
spring:
  datasource:
    agent-service:
      hikari:
        maximum-pool-size: 10
        minimum-idle: 5
        connection-timeout: 30000
    
    nlu-service:
      hikari:
        maximum-pool-size: 20
        minimum-idle: 10
        connection-timeout: 30000
    
    position-service:
      hikari:
        maximum-pool-size: 5
        minimum-idle: 2
        connection-timeout: 30000
```

### Resilience4j Bulkhead

```java
@Service
public class Resilience4jBulkheadService {
    
    private final Bulkhead agentServiceBulkhead;
    private final Bulkhead nluServiceBulkhead;
    
    public Resilience4jBulkheadService() {
        this.agentServiceBulkhead = Bulkhead.of("agent-service",
            BulkheadConfig.custom()
                .maxConcurrentCalls(10)          // Max 10 concurrent
                .maxWaitDuration(Duration.ofSeconds(5))  // Wait 5s
                .build());
        
        this.nluServiceBulkhead = Bulkhead.of("nlu-service",
            BulkheadConfig.custom()
                .maxConcurrentCalls(20)          // Max 20 concurrent
                .maxWaitDuration(Duration.ofSeconds(10))
                .build());
    }
    
    public Agent getAgent(String agentId) {
        return agentServiceBulkhead.executeSupplier(() -> {
            return agentService.getAgent(agentId);
        });
    }
    
    public NLUResponse processMessage(String message) {
        return nluServiceBulkhead.executeSupplier(() -> {
            return nluService.processMessage(message);
        });
    }
}
```

### Where We Used Bulkhead Pattern

#### 1. **NLU Facade Service**

```
┌─────────────────────────────────────────────────────────┐
│         NLU Service Bulkhead                           │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Multiple NLU providers
├─ One provider slow → blocks others
└─ Thread exhaustion

Solution:
├─ Separate thread pool per provider
├─ IBM Watson: 10 threads
├─ Google DialogFlow: 10 threads
└─ Isolated execution
```

#### 2. **Trade Processing Service**

```
┌─────────────────────────────────────────────────────────┐
│         Trade Service Bulkhead                         │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Trade processing blocks position updates
├─ Position updates block ledger entries
└─ Cascading delays

Solution:
├─ Trade processing: 20 threads
├─ Position updates: 10 threads
├─ Ledger entries: 5 threads
└─ Isolated execution
```

#### 3. **Agent Match Service**

```
┌─────────────────────────────────────────────────────────┐
│         Agent Service Bulkhead                         │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Agent queries block routing
├─ Routing blocks state updates
└─ Performance degradation

Solution:
├─ Agent queries: 15 threads
├─ Routing: 10 threads
├─ State updates: 5 threads
└─ Isolated execution
```

### Bulkhead Benefits

```
┌─────────────────────────────────────────────────────────┐
│         Bulkhead Benefits                              │
└─────────────────────────────────────────────────────────┘

1. Failure Isolation:
   ├─ One service failure doesn't affect others
   ├─ Resources isolated
   └─ Graceful degradation

2. Resource Protection:
   ├─ Prevents resource exhaustion
   ├─ Fair resource allocation
   └─ Predictable performance

3. Scalability:
   ├─ Independent scaling
   ├─ Service-specific tuning
   └─ Better resource utilization

4. Reliability:
   ├─ Higher availability
   ├─ Faster recovery
   └─ Better fault tolerance
```

---

## Summary

**Bulkhead Pattern**:
- **Thread Pool Isolation**: Separate pools per service
- **Connection Pool Isolation**: Separate HTTP/DB pools
- **Resource Isolation**: Prevent resource exhaustion
- **Failure Isolation**: One failure doesn't cascade

**Where Used**:
1. NLU Facade Service (provider isolation)
2. Trade Processing Service (operation isolation)
3. Agent Match Service (function isolation)

**Key Points**:
1. Isolate resources to prevent cascading failures
2. Use separate thread pools for different services
3. Configure appropriate pool sizes
4. Monitor resource utilization
5. Adjust pools based on load
