# Service Communication Part 7: Service Degradation

## Question 142: How do you handle service degradation?

### Answer

### Service Degradation Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Service Degradation Levels                     │
└─────────────────────────────────────────────────────────┘

Level 1: Full Functionality
├─ All services available
├─ All features enabled
└─ Optimal performance

Level 2: Reduced Functionality
├─ Some features disabled
├─ Fallback mechanisms
└─ Acceptable performance

Level 3: Minimal Functionality
├─ Core features only
├─ Cached data only
└─ Basic operations

Level 4: Read-Only Mode
├─ No writes
├─ Cached/static data
└─ Emergency mode
```

### Degradation Triggers

#### 1. **Circuit Breaker Open**

```java
@Service
public class DegradedConversationService {
    
    @CircuitBreaker(name = "nlu-service", 
                    fallbackMethod = "processConversationDegraded")
    public ConversationResponse processConversation(
            ConversationRequest request) {
        // Full functionality
        NLUResponse nlu = nluService.processMessage(request.getMessage());
        Agent agent = agentService.getAgent(request.getAgentId());
        
        return ConversationResponse.builder()
            .nluResponse(nlu)
            .agent(agent)
            .features(FeatureSet.FULL)
            .build();
    }
    
    public ConversationResponse processConversationDegraded(
            ConversationRequest request, Exception e) {
        log.warn("NLU service unavailable, using degraded mode", e);
        
        // Degraded functionality
        NLUResponse nlu = simpleKeywordMatch(request.getMessage());
        Agent agent = getCachedAgent(request.getAgentId());
        
        return ConversationResponse.builder()
            .nluResponse(nlu)
            .agent(agent)
            .features(FeatureSet.REDUCED)
            .build();
    }
}
```

#### 2. **High Load Detection**

```java
@Service
public class LoadBasedDegradation {
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void checkSystemLoad() {
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();
        int activeThreads = getActiveThreadCount();
        
        if (cpuUsage > 0.9 || memoryUsage > 0.9 || activeThreads > 100) {
            // High load detected, enable degradation
            enableDegradation();
        } else if (cpuUsage < 0.7 && memoryUsage < 0.7 && activeThreads < 50) {
            // Load normalized, disable degradation
            disableDegradation();
        }
    }
    
    private void enableDegradation() {
        featureFlags.disable("advanced-nlu");
        featureFlags.disable("real-time-analytics");
        featureFlags.enable("cached-responses-only");
    }
}
```

### Degradation Strategies

#### 1. **Feature Flags**

```java
@Service
public class FeatureFlagService {
    
    private final Map<String, Boolean> featureFlags = new ConcurrentHashMap<>();
    
    public boolean isFeatureEnabled(String feature) {
        return featureFlags.getOrDefault(feature, true);
    }
    
    public void enableFeature(String feature) {
        featureFlags.put(feature, true);
    }
    
    public void disableFeature(String feature) {
        featureFlags.put(feature, false);
    }
    
    public ConversationResponse processConversation(
            ConversationRequest request) {
        ConversationResponse response = new ConversationResponse();
        
        // Core feature (always enabled)
        response.setMessage(request.getMessage());
        
        // Optional features
        if (isFeatureEnabled("advanced-nlu")) {
            response.setNluResponse(nluService.processMessage(request.getMessage()));
        } else {
            response.setNluResponse(simpleKeywordMatch(request.getMessage()));
        }
        
        if (isFeatureEnabled("real-time-analytics")) {
            response.setAnalytics(analyticsService.getAnalytics(request));
        }
        
        return response;
    }
}
```

#### 2. **Cache-Only Mode**

```java
@Service
public class CacheOnlyService {
    
    private volatile boolean cacheOnlyMode = false;
    
    public Agent getAgent(String agentId) {
        if (cacheOnlyMode) {
            // Degraded: Cache only
            return cacheService.getAgent(agentId)
                .orElse(Agent.defaultAgent(agentId));
        } else {
            // Normal: Try service, fallback to cache
            try {
                Agent agent = agentService.getAgent(agentId);
                cacheService.cacheAgent(agentId, agent);
                return agent;
            } catch (Exception e) {
                return cacheService.getAgent(agentId)
                    .orElse(Agent.defaultAgent(agentId));
            }
        }
    }
    
    public void enableCacheOnlyMode() {
        log.warn("Enabling cache-only mode");
        this.cacheOnlyMode = true;
    }
    
    public void disableCacheOnlyMode() {
        log.info("Disabling cache-only mode");
        this.cacheOnlyMode = false;
    }
}
```

#### 3. **Read-Only Mode**

```java
@Service
public class ReadOnlyService {
    
    private volatile boolean readOnlyMode = false;
    
    @PreAuthorize("!@readOnlyService.isReadOnlyMode()")
    public Agent updateAgent(String agentId, AgentUpdate update) {
        if (readOnlyMode) {
            throw new ReadOnlyModeException("System is in read-only mode");
        }
        return agentService.updateAgent(agentId, update);
    }
    
    public Agent getAgent(String agentId) {
        // Reads always allowed
        return agentService.getAgent(agentId);
    }
    
    public void enableReadOnlyMode() {
        log.warn("Enabling read-only mode");
        this.readOnlyMode = true;
        // Queue writes for later processing
        writeQueueService.enableQueueing();
    }
}
```

### Degradation Monitoring

```java
@Component
public class DegradationMonitor {
    
    private final MeterRegistry meterRegistry;
    
    public void recordDegradedRequest(String service, String reason) {
        Counter.builder("service.degraded.requests")
            .tag("service", service)
            .tag("reason", reason)
            .register(meterRegistry)
            .increment();
    }
    
    @Scheduled(fixedRate = 60000)
    public void reportDegradationMetrics() {
        // Get degradation rates
        Map<String, Double> degradationRates = getDegradationRates();
        
        degradationRates.forEach((service, rate) -> {
            if (rate > 0.1) { // 10% degradation rate
                alertService.sendAlert(
                    AlertLevel.WARNING,
                    String.format("High degradation rate for %s: %.2f%%", 
                        service, rate * 100)
                );
            }
        });
    }
}
```

---

## Summary

**Service Degradation**:
- **Levels**: Full → Reduced → Minimal → Read-Only
- **Triggers**: Circuit breaker, high load, errors
- **Strategies**: Feature flags, cache-only, read-only
- **Monitoring**: Track degradation rates and reasons

**Key Points**:
1. Define degradation levels
2. Implement fallback mechanisms
3. Use feature flags for control
4. Monitor degradation metrics
5. Automate degradation based on conditions
