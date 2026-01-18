# Service Communication Part 9: Service Dependencies

## Question 144: How do you handle service dependencies?

### Answer

### Dependency Management Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Dependency Types                                │
└─────────────────────────────────────────────────────────┘

1. Critical Dependencies:
   ├─ Service cannot function without
   ├─ Must be available
   └─ Fail if unavailable

2. Optional Dependencies:
   ├─ Service can function without
   ├─ Degraded functionality
   └─ Fallback available

3. Cascading Dependencies:
   ├─ Chain of dependencies
   ├─ One failure affects many
   └─ Need isolation
```

### Dependency Graph

```
┌─────────────────────────────────────────────────────────┐
│         Service Dependency Graph                        │
└─────────────────────────────────────────────────────────┘

Conversation Service
    ├─→ Agent Match Service (Critical)
    ├─→ NLU Facade Service (Optional)
    ├─→ Message Service (Critical)
    └─→ Session Service (Optional)

Agent Match Service
    ├─→ Redis (Critical)
    ├─→ Kafka (Optional)
    └─→ PostgreSQL (Critical)

NLU Facade Service
    ├─→ IBM Watson (Optional)
    ├─→ Google DialogFlow (Optional)
    └─→ Redis Cache (Optional)
```

### Dependency Health Checks

#### 1. **Dependency Health Monitoring**

```java
@Component
public class DependencyHealthMonitor {
    
    private final Map<String, DependencyHealth> dependencies = 
        new ConcurrentHashMap<>();
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void checkDependencies() {
        checkDependency("agent-service", () -> agentService.healthCheck());
        checkDependency("redis", () -> redisTemplate.hasKey("health:check"));
        checkDependency("kafka", () -> kafkaTemplate.getProducerFactory() != null);
        checkDependency("postgres", () -> dataSource.getConnection().isValid(1));
    }
    
    private void checkDependency(String name, Supplier<Boolean> check) {
        try {
            boolean healthy = check.get();
            dependencies.put(name, new DependencyHealth(healthy, Instant.now()));
        } catch (Exception e) {
            dependencies.put(name, new DependencyHealth(false, Instant.now()));
            log.error("Dependency {} is unhealthy", name, e);
        }
    }
    
    public boolean isDependencyHealthy(String name) {
        DependencyHealth health = dependencies.get(name);
        return health != null && health.isHealthy();
    }
    
    public Map<String, DependencyHealth> getAllDependencies() {
        return new HashMap<>(dependencies);
    }
}
```

#### 2. **Readiness Based on Dependencies**

```java
@Component
public class DependencyBasedReadiness {
    
    private final DependencyHealthMonitor dependencyMonitor;
    
    @GetMapping("/health/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        Map<String, Boolean> dependencies = new HashMap<>();
        
        // Check critical dependencies
        boolean agentServiceHealthy = 
            dependencyMonitor.isDependencyHealthy("agent-service");
        boolean redisHealthy = 
            dependencyMonitor.isDependencyHealthy("redis");
        boolean postgresHealthy = 
            dependencyMonitor.isDependencyHealthy("postgres");
        
        dependencies.put("agent-service", agentServiceHealthy);
        dependencies.put("redis", redisHealthy);
        dependencies.put("postgres", postgresHealthy);
        
        // Service is ready if all critical dependencies are healthy
        boolean ready = agentServiceHealthy && redisHealthy && postgresHealthy;
        
        readiness.put("status", ready ? "READY" : "NOT_READY");
        readiness.put("dependencies", dependencies);
        
        HttpStatus status = ready ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(readiness);
    }
}
```

### Dependency Failure Handling

#### 1. **Circuit Breaker for Dependencies**

```java
@Service
public class DependencyCircuitBreaker {
    
    private final Map<String, CircuitBreaker> circuitBreakers = 
        new ConcurrentHashMap<>();
    
    public <T> T callWithCircuitBreaker(String dependencyName, 
                                        Supplier<T> operation) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(dependencyName);
        
        return circuitBreaker.executeSupplier(() -> {
            try {
                return operation.get();
            } catch (Exception e) {
                log.error("Dependency {} call failed", dependencyName, e);
                throw e;
            }
        });
    }
    
    private CircuitBreaker getOrCreateCircuitBreaker(String name) {
        return circuitBreakers.computeIfAbsent(name, n ->
            CircuitBreaker.of(n, CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .build())
        );
    }
}
```

#### 2. **Dependency Fallback**

```java
@Service
public class DependencyFallbackService {
    
    public ConversationResponse processConversation(
            ConversationRequest request) {
        ConversationResponse response = new ConversationResponse();
        
        // Critical dependency: Agent Service
        try {
            Agent agent = agentService.getAgent(request.getAgentId());
            response.setAgent(agent);
        } catch (Exception e) {
            // No fallback for critical dependency
            throw new ServiceUnavailableException(
                "Agent service unavailable", e);
        }
        
        // Optional dependency: NLU Service
        try {
            NLUResponse nlu = nluService.processMessage(request.getMessage());
            response.setNluResponse(nlu);
        } catch (Exception e) {
            // Fallback for optional dependency
            log.warn("NLU service unavailable, using fallback", e);
            response.setNluResponse(simpleKeywordMatch(request.getMessage()));
        }
        
        return response;
    }
}
```

### Dependency Injection and Configuration

```java
@Configuration
public class DependencyConfiguration {
    
    @Bean
    @ConditionalOnProperty(name = "dependencies.agent-service.enabled", 
                          havingValue = "true", matchIfMissing = true)
    public AgentServiceClient agentServiceClient() {
        return new AgentServiceClient();
    }
    
    @Bean
    @ConditionalOnProperty(name = "dependencies.nlu-service.enabled", 
                          havingValue = "true", matchIfMissing = true)
    public NLUServiceClient nluServiceClient() {
        return new NLUServiceClient();
    }
    
    @Bean
    @ConditionalOnProperty(name = "dependencies.analytics-service.enabled", 
                          havingValue = "false", matchIfMissing = false)
    public AnalyticsServiceClient analyticsServiceClient() {
        return new AnalyticsServiceClient();
    }
}
```

---

## Summary

**Service Dependencies**:
- **Types**: Critical, Optional, Cascading
- **Health Monitoring**: Track dependency status
- **Failure Handling**: Circuit breakers and fallbacks
- **Readiness**: Based on critical dependencies

**Key Points**:
1. Identify critical vs optional dependencies
2. Monitor dependency health
3. Use circuit breakers for protection
4. Implement fallbacks for optional dependencies
5. Configure readiness based on dependencies
