# NLU Facade Service - Detailed Answers

## Question 24: Explain the NLU Facade Service design. Why use the Adapter pattern?

### Answer

### NLU Facade Service Architecture

```
┌─────────────────────────────────────────────────────────┐
│              NLU Facade Service                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Provider    │  │   Adapter    │  │   Circuit    │  │
│  │  Registry    │  │   Factory    │  │   Breaker    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Retry      │  │   Fallback   │  │   Caching    │  │
│  │   Manager    │  │   Strategy   │  │   Layer      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────────┬─────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
        ┌─────────────┐ ┌──────────┐ ┌──────────┐
        │  IBM Watson│ │  Google  │ │  Redis   │
        │   Client   │ │ DialogFlow│ │  Cache   │
        └─────────────┘ └──────────┘ └──────────┘
```

### Why Adapter Pattern?

#### 1. **Provider Abstraction**

```
Problem:
├─ Multiple NLU providers (IBM Watson, Google Dialog Flow)
├─ Different API formats
├─ Different response structures
├─ Different capabilities
└─ Provider-specific implementations scattered

Solution - Adapter Pattern:
├─ Unified interface (NLUProvider)
├─ Provider-specific adapters
├─ Common response model
└─ Centralized provider management
```

#### 2. **Benefits**

```
┌─────────────────────────────────────────────────────────┐
│         Adapter Pattern Benefits                       │
└─────────────────────────────────────────────────────────┘

1. Provider Independence:
   ├─ Easy to switch providers
   ├─ Add new providers without changing code
   └─ Remove providers easily

2. Code Reusability:
   ├─ Common logic in facade
   ├─ Provider-specific in adapters
   └─ No code duplication

3. Testability:
   ├─ Mock adapters for testing
   ├─ Test facade independently
   └─ Easy unit testing

4. Maintainability:
   ├─ Changes isolated to adapters
   ├─ Clear separation of concerns
   └─ Easy to understand
```

#### 3. **Implementation**

```java
// Unified Interface
public interface NLUProvider {
    NLUResponse processMessage(String message, String conversationId);
    boolean isAvailable();
    String getProviderName();
    ProviderMetrics getMetrics();
}

// Common Response Model
public class NLUResponse {
    private String intent;
    private Map<String, Object> entities;
    private double confidence;
    private String provider;
    private Instant timestamp;
}

// IBM Watson Adapter
@Component
public class IBMWatsonAdapter implements NLUProvider {
    private final WatsonAssistantService watsonService;
    
    @Override
    public NLUResponse processMessage(String message, String conversationId) {
        // Convert to IBM Watson format
        WatsonRequest request = WatsonRequest.builder()
            .input(message)
            .context(createContext(conversationId))
            .build();
        
        // Call IBM Watson
        WatsonResponse watsonResponse = watsonService.message(request);
        
        // Convert to common format
        return NLUResponse.builder()
            .intent(watsonResponse.getIntent())
            .entities(convertEntities(watsonResponse.getEntities()))
            .confidence(watsonResponse.getConfidence())
            .provider("IBM_WATSON")
            .timestamp(Instant.now())
            .build();
    }
    
    @Override
    public boolean isAvailable() {
        try {
            watsonService.healthCheck();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

// Google Dialog Flow Adapter
@Component
public class GoogleDialogFlowAdapter implements NLUProvider {
    private final DialogFlowService dialogFlowService;
    
    @Override
    public NLUResponse processMessage(String message, String conversationId) {
        // Convert to Dialog Flow format
        DialogFlowRequest request = DialogFlowRequest.builder()
            .queryInput(QueryInput.newBuilder()
                .setText(TextInput.newBuilder()
                    .setText(message)
                    .setLanguageCode("en"))
                .build())
            .session("projects/" + projectId + "/sessions/" + conversationId)
            .build();
        
        // Call Dialog Flow
        DialogFlowResponse dialogFlowResponse = dialogFlowService.detectIntent(request);
        
        // Convert to common format
        return NLUResponse.builder()
            .intent(dialogFlowResponse.getIntent().getDisplayName())
            .entities(convertEntities(dialogFlowResponse.getParameters()))
            .confidence(dialogFlowResponse.getIntentDetectionConfidence())
            .provider("GOOGLE_DIALOG_FLOW")
            .timestamp(Instant.now())
            .build();
    }
}
```

---

## Question 25: How does the provider selection strategy work? What criteria are used?

### Answer

### Provider Selection Strategy

#### 1. **Selection Criteria**

```
┌─────────────────────────────────────────────────────────┐
│         Provider Selection Criteria                    │
└─────────────────────────────────────────────────────────┘

1. Availability:
   ├─ Provider health status
   ├─ Circuit breaker state
   └─ Recent failure rate

2. Performance:
   ├─ Average response time
   ├─ P95 response time
   └─ Success rate

3. Cost:
   ├─ Cost per request
   ├─ Monthly budget
   └─ Cost efficiency

4. Quality:
   ├─ Intent accuracy
   ├─ Entity extraction quality
   └─ Confidence scores

5. Tenant Configuration:
   ├─ Tenant-specific provider preference
   ├─ Feature requirements
   └─ Compliance requirements
```

#### 2. **Selection Algorithm**

```java
@Service
public class NLUProviderSelector {
    private final List<NLUProvider> providers;
    private final ProviderMetricsService metricsService;
    
    public NLUProvider selectProvider(String conversationId, String tenantId) {
        // Get tenant configuration
        TenantConfig tenantConfig = getTenantConfig(tenantId);
        
        // Get available providers
        List<NLUProvider> availableProviders = providers.stream()
            .filter(NLUProvider::isAvailable)
            .filter(p -> !isCircuitBreakerOpen(p))
            .collect(Collectors.toList());
        
        if (availableProviders.isEmpty()) {
            throw new NoAvailableProviderException();
        }
        
        // Apply tenant preference if configured
        if (tenantConfig.getPreferredProvider() != null) {
            NLUProvider preferred = findProvider(tenantConfig.getPreferredProvider());
            if (preferred != null && availableProviders.contains(preferred)) {
                return preferred;
            }
        }
        
        // Score providers
        Map<NLUProvider, Double> scores = availableProviders.stream()
            .collect(Collectors.toMap(
                provider -> provider,
                provider -> calculateScore(provider, tenantConfig)
            ));
        
        // Select best provider
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new NoAvailableProviderException());
    }
    
    private double calculateScore(NLUProvider provider, TenantConfig config) {
        ProviderMetrics metrics = metricsService.getMetrics(provider);
        
        // Performance score (0-0.4)
        double performanceScore = calculatePerformanceScore(metrics);
        
        // Cost score (0-0.3)
        double costScore = calculateCostScore(provider, config);
        
        // Quality score (0-0.2)
        double qualityScore = calculateQualityScore(metrics);
        
        // Availability score (0-0.1)
        double availabilityScore = calculateAvailabilityScore(metrics);
        
        return performanceScore + costScore + qualityScore + availabilityScore;
    }
    
    private double calculatePerformanceScore(ProviderMetrics metrics) {
        // Lower response time = higher score
        double normalizedResponseTime = 1.0 / (1.0 + metrics.getAverageResponseTime().toMillis() / 1000.0);
        return normalizedResponseTime * 0.4;
    }
    
    private double calculateCostScore(NLUProvider provider, TenantConfig config) {
        // Lower cost = higher score
        double costPerRequest = getCostPerRequest(provider);
        double normalizedCost = 1.0 / (1.0 + costPerRequest);
        return normalizedCost * 0.3;
    }
}
```

#### 3. **Provider Registry**

```java
@Component
public class NLUProviderRegistry {
    private final Map<String, NLUProvider> providers = new ConcurrentHashMap<>();
    private final List<NLUProvider> providerList = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        // Register all providers
        registerProvider(new IBMWatsonAdapter());
        registerProvider(new GoogleDialogFlowAdapter());
    }
    
    public void registerProvider(NLUProvider provider) {
        providers.put(provider.getProviderName(), provider);
        providerList.add(provider);
    }
    
    public NLUProvider getProvider(String name) {
        return providers.get(name);
    }
    
    public List<NLUProvider> getAllProviders() {
        return new ArrayList<>(providerList);
    }
}
```

---

## Question 26: Walk me through the fallback mechanism when the primary NLU provider fails.

### Answer

### Fallback Mechanism

#### 1. **Fallback Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Fallback Flow                                  │
└─────────────────────────────────────────────────────────┘

1. Select Primary Provider
   ├─ Based on selection criteria
   └─ Try primary provider

2. Primary Provider Fails
   ├─ Exception caught
   ├─ Circuit breaker checked
   └─ Fallback triggered

3. Select Secondary Provider
   ├─ Exclude failed provider
   ├─ Select next best provider
   └─ Try secondary provider

4. Secondary Provider Fails
   ├─ Try next provider
   └─ Continue until success or all fail

5. All Providers Fail
   ├─ Return cached response (if available)
   ├─ Return default response
   └─ Log error
```

#### 2. **Implementation**

```java
@Service
public class NLUFacadeService {
    private final NLUProviderSelector providerSelector;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final NLUCacheService cacheService;
    
    public NLUResponse processMessage(String message, String conversationId) {
        // Try to get from cache first
        String messageHash = hashMessage(message);
        NLUResponse cachedResponse = cacheService.getCachedResponse(messageHash);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        
        // Select primary provider
        NLUProvider primaryProvider = providerSelector.selectProvider(conversationId, getTenantId(conversationId));
        
        // Try primary provider
        try {
            if (isCircuitBreakerOpen(primaryProvider)) {
                return fallbackToSecondary(primaryProvider, message, conversationId);
            }
            
            NLUResponse response = primaryProvider.processMessage(message, conversationId);
            
            // Cache response
            cacheService.cacheResponse(messageHash, response);
            
            return response;
            
        } catch (Exception e) {
            log.warn("Primary provider {} failed, falling back", primaryProvider.getProviderName(), e);
            return fallbackToSecondary(primaryProvider, message, conversationId);
        }
    }
    
    private NLUResponse fallbackToSecondary(NLUProvider failedProvider, 
                                            String message, 
                                            String conversationId) {
        // Get available providers (excluding failed one)
        List<NLUProvider> availableProviders = providerSelector.getAllProviders().stream()
            .filter(p -> p != failedProvider)
            .filter(NLUProvider::isAvailable)
            .filter(p -> !isCircuitBreakerOpen(p))
            .sorted(Comparator.comparing(this::getProviderPriority).reversed())
            .collect(Collectors.toList());
        
        // Try each provider in order
        for (NLUProvider provider : availableProviders) {
            try {
                NLUResponse response = provider.processMessage(message, conversationId);
                
                // Cache response
                String messageHash = hashMessage(message);
                cacheService.cacheResponse(messageHash, response);
                
                // Record fallback
                recordFallback(failedProvider, provider);
                
                return response;
                
            } catch (Exception e) {
                log.warn("Provider {} also failed, trying next", provider.getProviderName(), e);
                continue;
            }
        }
        
        // All providers failed - try cache or default
        return handleAllProvidersFailed(message, conversationId);
    }
    
    private NLUResponse handleAllProvidersFailed(String message, String conversationId) {
        // Try to get from cache (even if stale)
        String messageHash = hashMessage(message);
        NLUResponse cachedResponse = cacheService.getCachedResponse(messageHash);
        if (cachedResponse != null) {
            log.warn("All providers failed, returning cached response");
            return cachedResponse;
        }
        
        // Return default response
        log.error("All NLU providers failed and no cache available");
        return NLUResponse.builder()
            .intent("UNKNOWN")
            .entities(Collections.emptyMap())
            .confidence(0.0)
            .provider("NONE")
            .timestamp(Instant.now())
            .build();
    }
}
```

#### 3. **Fallback Metrics**

```java
@Component
public class FallbackMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordFallback(String fromProvider, String toProvider) {
        Counter.builder("nlu.fallback.count")
            .tag("from", fromProvider)
            .tag("to", toProvider)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordFallbackSuccess(String provider) {
        Counter.builder("nlu.fallback.success")
            .tag("provider", provider)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordAllProvidersFailed() {
        Counter.builder("nlu.all.providers.failed")
            .register(meterRegistry)
            .increment();
    }
}
```

---

## Question 27: How does the circuit breaker pattern work in the NLU Facade?

### Answer

### Circuit Breaker Pattern

#### 1. **Circuit Breaker States**

```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker States                         │
└─────────────────────────────────────────────────────────┘

CLOSED (Normal):
├─ Requests flow through
├─ Monitor failure rate
├─ Count failures
└─ If failure rate > threshold → OPEN

OPEN (Failing):
├─ Requests fail fast
├─ No calls to provider
├─ After timeout → HALF_OPEN
└─ Prevents cascading failures

HALF_OPEN (Testing):
├─ Allow limited requests
├─ If success → CLOSED
├─ If failure → OPEN
└─ Test provider recovery
```

#### 2. **Implementation with Resilience4j**

```java
@Service
public class NLUFacadeService {
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    public NLUResponse processMessage(String message, String conversationId) {
        NLUProvider provider = providerSelector.selectProvider(conversationId, getTenantId(conversationId));
        
        CircuitBreaker circuitBreaker = getCircuitBreaker(provider);
        
        // Execute with circuit breaker
        return circuitBreaker.executeSupplier(() -> {
            return provider.processMessage(message, conversationId);
        });
    }
    
    private CircuitBreaker getCircuitBreaker(NLUProvider provider) {
        return circuitBreakerRegistry.circuitBreaker(
            provider.getProviderName(),
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before half-open
                .slidingWindowSize(10) // Last 10 calls
                .minimumNumberOfCalls(5) // Need 5 calls before opening
                .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in half-open
                .build()
        );
    }
}
```

#### 3. **Circuit Breaker Configuration**

```java
@Configuration
public class CircuitBreakerConfiguration {
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .recordExceptions(IOException.class, TimeoutException.class)
            .ignoreExceptions(IllegalArgumentException.class)
            .build();
        
        return CircuitBreakerRegistry.of(defaultConfig);
    }
}
```

#### 4. **Circuit Breaker Monitoring**

```java
@Component
public class CircuitBreakerMonitor {
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Scheduled(fixedRate = 5000)
    public void monitorCircuitBreakers() {
        circuitBreakerRegistry.getAllCircuitBreakers().forEach((name, circuitBreaker) -> {
            CircuitBreaker.State state = circuitBreaker.getState();
            
            // Log state changes
            if (state == CircuitBreaker.State.OPEN) {
                log.warn("Circuit breaker {} is OPEN", name);
                alertService.sendAlert("Circuit breaker " + name + " is OPEN");
            }
            
            // Record metrics
            recordMetrics(name, circuitBreaker);
        });
    }
    
    private void recordMetrics(String name, CircuitBreaker circuitBreaker) {
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        
        Gauge.builder("circuit.breaker.state", circuitBreaker, 
            cb -> cb.getState().ordinal())
            .tag("name", name)
            .register(meterRegistry);
        
        Gauge.builder("circuit.breaker.failure.rate", metrics, 
            m -> m.getFailureRate())
            .tag("name", name)
            .register(meterRegistry);
    }
}
```

---

## Question 28: What happens when all NLU providers are unavailable?

### Answer

### All Providers Unavailable Scenario

#### 1. **Handling Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         All Providers Unavailable Handling            │
└─────────────────────────────────────────────────────────┘

1. Check Cache:
   ├─ Try to get cached response
   ├─ Even if stale
   └─ Better than nothing

2. Return Default Response:
   ├─ Generic intent: "UNKNOWN"
   ├─ Empty entities
   └─ Low confidence

3. Queue for Retry:
   ├─ Queue message for later processing
   ├─ Retry when providers available
   └─ Async processing

4. Notify Operations:
   ├─ Alert on-call team
   ├─ Log critical error
   └─ Track downtime
```

#### 2. **Implementation**

```java
@Service
public class NLUFacadeService {
    private final NLUProviderRegistry providerRegistry;
    private final NLUCacheService cacheService;
    private final MessageQueueService queueService;
    private final AlertService alertService;
    
    public NLUResponse processMessage(String message, String conversationId) {
        // Check if any provider is available
        List<NLUProvider> availableProviders = providerRegistry.getAllProviders().stream()
            .filter(NLUProvider::isAvailable)
            .filter(p -> !isCircuitBreakerOpen(p))
            .collect(Collectors.toList());
        
        if (availableProviders.isEmpty()) {
            return handleAllProvidersUnavailable(message, conversationId);
        }
        
        // Normal processing...
    }
    
    private NLUResponse handleAllProvidersUnavailable(String message, String conversationId) {
        log.error("All NLU providers are unavailable");
        
        // 1. Try cache (even stale)
        String messageHash = hashMessage(message);
        NLUResponse cachedResponse = cacheService.getCachedResponse(messageHash);
        if (cachedResponse != null) {
            log.warn("Returning cached response as all providers unavailable");
            return cachedResponse;
        }
        
        // 2. Queue for retry
        queueService.enqueueMessage(conversationId, message);
        
        // 3. Alert operations
        alertService.sendCriticalAlert("All NLU providers unavailable");
        
        // 4. Return default response
        return createDefaultResponse(message);
    }
    
    private NLUResponse createDefaultResponse(String message) {
        // Simple keyword-based intent detection as fallback
        String intent = detectIntentFromKeywords(message);
        
        return NLUResponse.builder()
            .intent(intent)
            .entities(Collections.emptyMap())
            .confidence(0.3) // Low confidence
            .provider("FALLBACK")
            .timestamp(Instant.now())
            .build();
    }
    
    private String detectIntentFromKeywords(String message) {
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("billing") || lowerMessage.contains("payment")) {
            return "BILLING_INQUIRY";
        } else if (lowerMessage.contains("technical") || lowerMessage.contains("help")) {
            return "TECHNICAL_SUPPORT";
        } else {
            return "UNKNOWN";
        }
    }
}
```

#### 3. **Retry Queue Processing**

```java
@Service
public class NLURetryService {
    private final MessageQueueService queueService;
    private final NLUFacadeService nluFacadeService;
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void processRetryQueue() {
        // Check if providers are available
        if (!areProvidersAvailable()) {
            return; // Still unavailable, skip
        }
        
        // Process queued messages
        List<QueuedMessage> queuedMessages = queueService.dequeueMessages(100);
        
        for (QueuedMessage queued : queuedMessages) {
            try {
                NLUResponse response = nluFacadeService.processMessage(
                    queued.getMessage(), 
                    queued.getConversationId()
                );
                
                // Send response to conversation
                conversationService.sendNLUResponse(
                    queued.getConversationId(), 
                    response
                );
                
            } catch (Exception e) {
                log.error("Failed to process queued message", e);
                // Re-queue with backoff
                queueService.requeueWithBackoff(queued);
            }
        }
    }
}
```

---

## Question 29: How did you implement caching for NLU responses? What's the cache invalidation strategy?

### Answer

### NLU Response Caching

#### 1. **Caching Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Caching Strategy                               │
└─────────────────────────────────────────────────────────┘

Cache Key:
├─ Message hash (SHA-256)
├─ Tenant ID (for multi-tenancy)
└─ Language (if multi-language)

Cache Value:
├─ NLUResponse object
├─ Timestamp
└─ Provider used

Cache TTL:
├─ Default: 5 minutes
├─ Configurable per tenant
└─ Stale cache: 1 hour (fallback)
```

#### 2. **Implementation**

```java
@Service
public class NLUCacheService {
    private final RedisTemplate<String, NLUResponse> redisTemplate;
    private final Cache<String, NLUResponse> localCache; // Caffeine
    
    public NLUResponse getCachedResponse(String messageHash, String tenantId) {
        // L1: Local cache
        String localKey = "nlu:" + tenantId + ":" + messageHash;
        NLUResponse response = localCache.getIfPresent(localKey);
        if (response != null) {
            return response;
        }
        
        // L2: Redis cache
        String redisKey = "nlu:" + tenantId + ":" + messageHash;
        response = redisTemplate.opsForValue().get(redisKey);
        if (response != null) {
            // Populate local cache
            localCache.put(localKey, response);
            return response;
        }
        
        return null;
    }
    
    public void cacheResponse(String messageHash, String tenantId, NLUResponse response) {
        String localKey = "nlu:" + tenantId + ":" + messageHash;
        String redisKey = "nlu:" + tenantId + ":" + messageHash;
        
        // Cache in local cache (1 minute TTL)
        localCache.put(localKey, response);
        
        // Cache in Redis (5 minutes TTL)
        redisTemplate.opsForValue().set(
            redisKey, 
            response, 
            Duration.ofMinutes(5)
        );
    }
    
    public String hashMessage(String message) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
```

#### 3. **Cache Invalidation**

```java
@Service
public class NLUCacheService {
    public void invalidateCache(String messageHash, String tenantId) {
        String localKey = "nlu:" + tenantId + ":" + messageHash;
        String redisKey = "nlu:" + tenantId + ":" + messageHash;
        
        // Invalidate local cache
        localCache.invalidate(localKey);
        
        // Invalidate Redis cache
        redisTemplate.delete(redisKey);
    }
    
    public void invalidateTenantCache(String tenantId) {
        // Invalidate all cache for tenant
        Set<String> keys = redisTemplate.keys("nlu:" + tenantId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        
        // Clear local cache (pattern matching not supported, clear all)
        localCache.invalidateAll();
    }
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupStaleCache() {
        // Remove entries older than 1 hour
        // Redis TTL handles this automatically
        // Local cache TTL also handles this
    }
}
```

#### 4. **Cache Warming**

```java
@Service
public class NLUCacheWarmingService {
    private final NLUCacheService cacheService;
    private final ConversationRepository conversationRepository;
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void warmCache() {
        // Get frequently asked questions
        List<String> commonMessages = getCommonMessages();
        
        for (String message : commonMessages) {
            try {
                // Process and cache
                NLUResponse response = nluFacadeService.processMessage(message, "warmup");
                String messageHash = cacheService.hashMessage(message);
                cacheService.cacheResponse(messageHash, "default", response);
            } catch (Exception e) {
                log.warn("Failed to warm cache for message", e);
            }
        }
    }
    
    private List<String> getCommonMessages() {
        // Get top 100 most common messages from last 24 hours
        return conversationRepository.findTopMessages(100, Duration.ofHours(24));
    }
}
```

---

## Question 30: Explain the cost optimization strategies for NLU API calls.

### Answer

### Cost Optimization Strategies

#### 1. **Caching**

```
┌─────────────────────────────────────────────────────────┐
│         Caching Impact                                │
└─────────────────────────────────────────────────────────┘

Before Caching:
├─ 12M conversations/month
├─ 12M NLU API calls
└─ Cost: $X per month

After Caching (30% hit rate):
├─ 12M conversations/month
├─ 8.4M NLU API calls (30% cached)
└─ Cost: $0.7X per month (30% reduction)
```

#### 2. **Message Deduplication**

```java
@Service
public class NLUFacadeService {
    private final Set<String> recentMessages = new ConcurrentHashMap<>().keySet(ConcurrentHashMap.newKeySet());
    
    public NLUResponse processMessage(String message, String conversationId) {
        String normalizedMessage = normalizeMessage(message);
        String messageHash = hashMessage(normalizedMessage);
        
        // Check if same message processed recently (last 5 minutes)
        if (recentMessages.contains(messageHash)) {
            log.debug("Duplicate message detected, using cached response");
            return cacheService.getCachedResponse(messageHash, getTenantId(conversationId));
        }
        
        // Process and mark as processed
        NLUResponse response = processWithProvider(message, conversationId);
        recentMessages.add(messageHash);
        
        // Remove after 5 minutes
        scheduler.schedule(() -> recentMessages.remove(messageHash), 
            5, TimeUnit.MINUTES);
        
        return response;
    }
    
    private String normalizeMessage(String message) {
        // Remove extra whitespace, convert to lowercase
        return message.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
```

#### 3. **Provider Selection Based on Cost**

```java
@Service
public class NLUProviderSelector {
    private final Map<String, Double> providerCosts = Map.of(
        "IBM_WATSON", 0.0025, // $0.0025 per request
        "GOOGLE_DIALOG_FLOW", 0.0020 // $0.0020 per request
    );
    
    private double calculateCostScore(NLUProvider provider, TenantConfig config) {
        double costPerRequest = providerCosts.getOrDefault(
            provider.getProviderName(), 
            0.003 // Default cost
        );
        
        // Lower cost = higher score
        double normalizedCost = 1.0 / (1.0 + costPerRequest * 100);
        return normalizedCost * 0.3;
    }
}
```

#### 4. **Batch Processing**

```java
@Service
public class NLUBatchProcessor {
    private final Queue<NLURequest> batchQueue = new LinkedBlockingQueue<>();
    private final int BATCH_SIZE = 10;
    private final Duration BATCH_TIMEOUT = Duration.ofSeconds(2);
    
    @Scheduled(fixedRate = 2000)
    public void processBatch() {
        List<NLURequest> batch = new ArrayList<>();
        
        // Collect requests
        NLURequest request;
        while (batch.size() < BATCH_SIZE && 
               (request = batchQueue.poll()) != null) {
            batch.add(request);
        }
        
        if (batch.isEmpty()) {
            return;
        }
        
        // Process batch (if provider supports it)
        processBatchWithProvider(batch);
    }
    
    private void processBatchWithProvider(List<NLURequest> batch) {
        // Some providers support batch processing
        // Reduces API call overhead
        // Example: Google Dialog Flow batch detect intent
    }
}
```

#### 5. **Cost Monitoring**

```java
@Component
public class NLUCostMonitor {
    private final MeterRegistry meterRegistry;
    private final Map<String, Double> providerCosts;
    
    public void recordNLUCall(String provider, boolean cached) {
        Counter.builder("nlu.api.calls")
            .tag("provider", provider)
            .tag("cached", String.valueOf(cached))
            .register(meterRegistry)
            .increment();
        
        if (!cached) {
            // Record cost
            double cost = providerCosts.getOrDefault(provider, 0.0);
            Counter.builder("nlu.api.cost")
                .tag("provider", provider)
                .register(meterRegistry)
                .increment(cost);
        }
    }
    
    @Scheduled(cron = "0 0 * * * *") // Daily
    public void reportDailyCosts() {
        // Calculate daily costs
        // Send to finance team
        // Alert if over budget
    }
}
```

---

## Question 31: How do you handle provider-specific differences in response formats?

### Answer

### Provider Response Normalization

#### 1. **Response Format Differences**

```
┌─────────────────────────────────────────────────────────┐
│         Provider Response Formats                     │
└─────────────────────────────────────────────────────────┘

IBM Watson:
{
  "intent": {
    "intent": "billing_inquiry",
    "confidence": 0.95
  },
  "entities": [
    {"entity": "account_number", "value": "12345"}
  ]
}

Google Dialog Flow:
{
  "queryResult": {
    "intent": {
      "displayName": "billing.inquiry"
    },
    "intentDetectionConfidence": 0.92,
    "parameters": {
      "account_number": "12345"
    }
  }
}

Common Format (Normalized):
{
  "intent": "billing_inquiry",
  "confidence": 0.95,
  "entities": {
    "account_number": "12345"
  }
}
```

#### 2. **Normalization Implementation**

```java
// IBM Watson Adapter
@Component
public class IBMWatsonAdapter implements NLUProvider {
    @Override
    public NLUResponse processMessage(String message, String conversationId) {
        WatsonResponse watsonResponse = watsonService.message(createRequest(message, conversationId));
        
        // Normalize response
        return NLUResponse.builder()
            .intent(normalizeIntent(watsonResponse.getIntent().getIntent()))
            .entities(normalizeEntities(watsonResponse.getEntities()))
            .confidence(watsonResponse.getIntent().getConfidence())
            .provider("IBM_WATSON")
            .timestamp(Instant.now())
            .build();
    }
    
    private String normalizeIntent(String watsonIntent) {
        // Convert "billing_inquiry" format to standard format
        return watsonIntent.replace("_", ".").toLowerCase();
    }
    
    private Map<String, Object> normalizeEntities(List<WatsonEntity> watsonEntities) {
        return watsonEntities.stream()
            .collect(Collectors.toMap(
                WatsonEntity::getEntity,
                WatsonEntity::getValue
            ));
    }
}

// Google Dialog Flow Adapter
@Component
public class GoogleDialogFlowAdapter implements NLUProvider {
    @Override
    public NLUResponse processMessage(String message, String conversationId) {
        DialogFlowResponse dialogFlowResponse = dialogFlowService.detectIntent(createRequest(message, conversationId));
        
        // Normalize response
        return NLUResponse.builder()
            .intent(normalizeIntent(dialogFlowResponse.getQueryResult().getIntent().getDisplayName()))
            .entities(normalizeEntities(dialogFlowResponse.getQueryResult().getParameters()))
            .confidence(dialogFlowResponse.getQueryResult().getIntentDetectionConfidence())
            .provider("GOOGLE_DIALOG_FLOW")
            .timestamp(Instant.now())
            .build();
    }
    
    private String normalizeIntent(String dialogFlowIntent) {
        // Convert "billing.inquiry" format to standard format
        return dialogFlowIntent.replace(".", "_").toLowerCase();
    }
    
    private Map<String, Object> normalizeEntities(Struct parameters) {
        Map<String, Object> entities = new HashMap<>();
        parameters.getFieldsMap().forEach((key, value) -> {
            entities.put(key, extractValue(value));
        });
        return entities;
    }
}
```

---

## Question 32: What's the retry strategy for NLU provider calls?

### Answer

### Retry Strategy

#### 1. **Retry Configuration**

```java
@Configuration
public class RetryConfiguration {
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .retryOnException(exception -> 
                exception instanceof IOException || 
                exception instanceof TimeoutException)
            .retryExceptions(IOException.class, TimeoutException.class)
            .ignoreExceptions(IllegalArgumentException.class)
            .exponentialBackoff() // Exponential backoff
            .build();
        
        return RetryRegistry.of(config);
    }
}
```

#### 2. **Retry Implementation**

```java
@Service
public class NLUFacadeService {
    private final RetryRegistry retryRegistry;
    
    public NLUResponse processMessage(String message, String conversationId) {
        NLUProvider provider = providerSelector.selectProvider(conversationId, getTenantId(conversationId));
        
        Retry retry = retryRegistry.retry(provider.getProviderName());
        
        return retry.executeSupplier(() -> {
            try {
                return provider.processMessage(message, conversationId);
            } catch (Exception e) {
                log.warn("NLU provider call failed, retrying", e);
                throw e;
            }
        });
    }
}
```

#### 3. **Retry Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Retry Flow                                     │
└─────────────────────────────────────────────────────────┘

Attempt 1: Immediate
    │
    ├─► Success → Return response
    │
    └─► Failure (IOException/Timeout) → Retry
        │
        ▼
Wait: 1 second

Attempt 2: After 1s
    │
    ├─► Success → Return response
    │
    └─► Failure → Retry
        │
        ▼
Wait: 2 seconds (exponential backoff)

Attempt 3: After 2s
    │
    ├─► Success → Return response
    │
    └─► Failure → Fallback to secondary provider
```

---

## Question 33: How did you achieve 50% improvement in NLU response time?

### Answer

### Performance Optimization

#### 1. **Optimization Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Improvements                      │
└─────────────────────────────────────────────────────────┘

Before Optimization:
├─ Average response time: 2s
├─ P95 response time: 4s
└─ Synchronous processing

After Optimization:
├─ Average response time: 1s (50% improvement)
├─ P95 response time: 2s (50% improvement)
└─ Async processing + caching
```

#### 2. **Async Processing**

```java
@Service
public class NLUFacadeService {
    private final ExecutorService executorService;
    
    public CompletableFuture<NLUResponse> processMessageAsync(String message, String conversationId) {
        return CompletableFuture.supplyAsync(() -> {
            return processMessage(message, conversationId);
        }, executorService);
    }
    
    public NLUResponse processMessage(String message, String conversationId) {
        // Check cache first (fast path)
        String messageHash = hashMessage(message);
        NLUResponse cached = cacheService.getCachedResponse(messageHash, getTenantId(conversationId));
        if (cached != null) {
            return cached; // < 10ms
        }
        
        // Process with provider
        NLUProvider provider = providerSelector.selectProvider(conversationId, getTenantId(conversationId));
        return provider.processMessage(message, conversationId);
    }
}
```

#### 3. **Parallel Provider Calls**

```java
@Service
public class NLUFacadeService {
    public NLUResponse processMessageWithRace(String message, String conversationId) {
        List<NLUProvider> providers = getAvailableProviders();
        
        // Call multiple providers in parallel
        List<CompletableFuture<NLUResponse>> futures = providers.stream()
            .map(provider -> CompletableFuture.supplyAsync(() -> {
                try {
                    return provider.processMessage(message, conversationId);
                } catch (Exception e) {
                    return null;
                }
            }))
            .collect(Collectors.toList());
        
        // Return first successful response
        CompletableFuture<NLUResponse> firstSuccess = CompletableFuture.anyOf(
            futures.toArray(new CompletableFuture[0])
        ).thenApply(result -> {
            for (CompletableFuture<NLUResponse> future : futures) {
                if (future.isDone() && !future.isCompletedExceptionally()) {
                    try {
                        NLUResponse response = future.get();
                        if (response != null) {
                            return response;
                        }
                    } catch (Exception e) {
                        // Continue
                    }
                }
            }
            return null;
        });
        
        try {
            return firstSuccess.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // Fallback to single provider
            return processMessage(message, conversationId);
        }
    }
}
```

#### 4. **Connection Pooling**

```java
@Configuration
public class HttpClientConfiguration {
    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClients.custom()
            .setMaxConnTotal(100)
            .setMaxConnPerRoute(20)
            .setConnectionTimeToLive(30, TimeUnit.SECONDS)
            .evictIdleConnections(60, TimeUnit.SECONDS)
            .build();
    }
}
```

---

## Question 34: How do you handle rate limiting from NLU providers?

### Answer

### Rate Limiting Handling

#### 1. **Rate Limiter Implementation**

```java
@Service
public class NLURateLimiter {
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    public boolean tryAcquire(String provider) {
        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(
            provider,
            p -> createRateLimiter(p)
        );
        
        return rateLimiter.tryAcquire();
    }
    
    private RateLimiter createRateLimiter(String provider) {
        // Provider-specific rate limits
        int requestsPerSecond = getProviderRateLimit(provider);
        return RateLimiter.create(requestsPerSecond);
    }
    
    private int getProviderRateLimit(String provider) {
        return switch (provider) {
            case "IBM_WATSON" -> 100; // 100 requests/second
            case "GOOGLE_DIALOG_FLOW" -> 200; // 200 requests/second
            default -> 50;
        };
    }
}
```

#### 2. **Rate Limit Handling**

```java
@Service
public class NLUFacadeService {
    private final NLURateLimiter rateLimiter;
    
    public NLUResponse processMessage(String message, String conversationId) {
        NLUProvider provider = providerSelector.selectProvider(conversationId, getTenantId(conversationId));
        
        // Check rate limit
        if (!rateLimiter.tryAcquire(provider.getProviderName())) {
            // Rate limit exceeded, queue or fallback
            return handleRateLimitExceeded(provider, message, conversationId);
        }
        
        return provider.processMessage(message, conversationId);
    }
    
    private NLUResponse handleRateLimitExceeded(NLUProvider provider, 
                                                 String message, 
                                                 String conversationId) {
        // Option 1: Queue for later
        queueService.enqueueMessage(conversationId, message);
        
        // Option 2: Fallback to another provider
        return fallbackToSecondary(provider, message, conversationId);
        
        // Option 3: Return cached response
        // return cacheService.getCachedResponse(hashMessage(message), getTenantId(conversationId));
    }
}
```

---

## Question 35: What happens if an NLU provider returns inconsistent results?

### Answer

### Inconsistent Results Handling

#### 1. **Result Validation**

```java
@Service
public class NLUFacadeService {
    public NLUResponse processMessage(String message, String conversationId) {
        NLUResponse response = processWithProvider(message, conversationId);
        
        // Validate response
        if (!isValidResponse(response)) {
            log.warn("Invalid response from provider, retrying with different provider");
            return fallbackToSecondary(getProvider(message, conversationId), message, conversationId);
        }
        
        return response;
    }
    
    private boolean isValidResponse(NLUResponse response) {
        // Check confidence threshold
        if (response.getConfidence() < 0.5) {
            return false;
        }
        
        // Check intent is not null
        if (response.getIntent() == null || response.getIntent().isEmpty()) {
            return false;
        }
        
        // Check intent is not "UNKNOWN" with high confidence (inconsistent)
        if ("UNKNOWN".equals(response.getIntent()) && response.getConfidence() > 0.8) {
            return false;
        }
        
        return true;
    }
}
```

#### 2. **Result Comparison**

```java
@Service
public class NLUResultValidator {
    public NLUResponse validateWithMultipleProviders(String message, String conversationId) {
        List<NLUProvider> providers = getAvailableProviders();
        
        // Get responses from multiple providers
        List<NLUResponse> responses = providers.stream()
            .limit(2) // Compare top 2 providers
            .map(provider -> {
                try {
                    return provider.processMessage(message, conversationId);
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (responses.size() < 2) {
            return responses.get(0);
        }
        
        // Compare responses
        if (areResponsesConsistent(responses)) {
            return responses.get(0); // Both agree
        } else {
            // Inconsistent, use higher confidence
            return responses.stream()
                .max(Comparator.comparing(NLUResponse::getConfidence))
                .orElse(responses.get(0));
        }
    }
    
    private boolean areResponsesConsistent(List<NLUResponse> responses) {
        NLUResponse first = responses.get(0);
        NLUResponse second = responses.get(1);
        
        // Same intent
        if (!first.getIntent().equals(second.getIntent())) {
            return false;
        }
        
        // Similar confidence (within 20%)
        double confidenceDiff = Math.abs(first.getConfidence() - second.getConfidence());
        if (confidenceDiff > 0.2) {
            return false;
        }
        
        return true;
    }
}
```

---

## Summary

NLU Facade Service answers cover:

1. **Adapter Pattern**: Provider abstraction and unified interface
2. **Provider Selection**: Multi-criteria scoring algorithm
3. **Fallback Mechanism**: Automatic failover to secondary providers
4. **Circuit Breaker**: Failure detection and protection
5. **All Providers Unavailable**: Cache, queue, and default response handling
6. **Caching**: Multi-level caching with invalidation
7. **Cost Optimization**: Caching, deduplication, provider selection
8. **Response Normalization**: Provider-specific format conversion
9. **Retry Strategy**: Exponential backoff with Resilience4j
10. **Performance**: Async processing and parallel calls (50% improvement)
11. **Rate Limiting**: Provider-specific rate limiters
12. **Inconsistent Results**: Validation and comparison strategies
