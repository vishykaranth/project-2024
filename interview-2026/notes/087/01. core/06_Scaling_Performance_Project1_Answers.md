# Scaling & Performance (Project 1) - Detailed Answers

## Question 46: How did you scale from 2 to 10+ service instances? What was the auto-scaling strategy?

### Answer

### Auto-Scaling Strategy

#### 1. **Horizontal Pod Autoscaler (HPA)**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: agent-match-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: agent-match-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 2
        periodSeconds: 15
      selectPolicy: Max
```

#### 2. **Scaling Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Scaling Metrics                                │
└─────────────────────────────────────────────────────────┘

CPU Utilization:
├─ Target: 70%
├─ Scale up: > 70% for 2 minutes
└─ Scale down: < 50% for 5 minutes

Memory Utilization:
├─ Target: 80%
├─ Scale up: > 80% for 2 minutes
└─ Scale down: < 60% for 5 minutes

Custom Metrics:
├─ Request rate (RPS)
├─ Queue depth
├─ Response time
└─ Error rate
```

#### 3. **Scaling Implementation**

```java
@Component
public class CustomMetricsExporter {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 5000)
    public void exportCustomMetrics() {
        // Request rate
        double requestRate = getRequestRate();
        Gauge.builder("custom.request.rate", () -> requestRate)
            .register(meterRegistry);
        
        // Queue depth
        int queueDepth = getQueueDepth();
        Gauge.builder("custom.queue.depth", () -> queueDepth)
            .register(meterRegistry);
    }
}
```

---

## Question 47: Explain the multi-level caching strategy (Application Cache → Redis → Database).

### Answer

### Multi-Level Caching

#### 1. **Cache Hierarchy**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Cache                              │
└─────────────────────────────────────────────────────────┘

L1: Application Cache (Caffeine)
├─ In-memory cache per instance
├─ Fastest access: < 1ms
├─ Limited size: 10,000 entries
└─ TTL: 1 minute

L2: Redis Cache
├─ Distributed cache
├─ Fast access: 5-10ms
├─ Large size: Millions of entries
└─ TTL: 10 minutes

L3: Database
├─ Persistent storage
├─ Slower access: 50-100ms
├─ Unlimited size
└─ No TTL
```

#### 2. **Implementation**

```java
@Service
public class ConversationCacheService {
    private final Cache<String, Conversation> localCache; // L1
    private final RedisTemplate<String, Conversation> redisTemplate; // L2
    private final ConversationRepository repository; // L3
    
    public Conversation getConversation(String conversationId) {
        // L1: Local cache
        Conversation conversation = localCache.getIfPresent(conversationId);
        if (conversation != null) {
            return conversation;
        }
        
        // L2: Redis cache
        conversation = redisTemplate.opsForValue()
            .get("conv:" + conversationId);
        if (conversation != null) {
            // Populate L1
            localCache.put(conversationId, conversation);
            return conversation;
        }
        
        // L3: Database
        conversation = repository.findById(conversationId).orElse(null);
        if (conversation != null) {
            // Populate L2 and L1
            redisTemplate.opsForValue().set(
                "conv:" + conversationId,
                conversation,
                Duration.ofMinutes(10)
            );
            localCache.put(conversationId, conversation);
        }
        
        return conversation;
    }
}
```

#### 3. **Cache Hit Rates**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Hit Rates                                │
└─────────────────────────────────────────────────────────┘

L1 (Local Cache):
├─ Hit Rate: 60%
├─ Access Time: < 1ms
└─ Reduces Redis load

L2 (Redis Cache):
├─ Hit Rate: 30%
├─ Access Time: 5-10ms
└─ Reduces database load

L3 (Database):
├─ Hit Rate: 10%
├─ Access Time: 50-100ms
└─ Source of truth

Overall:
├─ Cache Hit Rate: 90%
├─ Average Access Time: ~5ms
└─ Database Load: Reduced by 90%
```

---

## Question 48: How did you optimize database queries to handle 12M conversations/month?

### Answer

### Database Optimization

#### 1. **Query Optimization**

```java
// Before: N+1 Query Problem
@GetMapping("/conversations")
public List<ConversationDTO> getConversations() {
    List<Conversation> conversations = conversationRepository.findAll();
    List<ConversationDTO> dtos = new ArrayList<>();
    
    for (Conversation conv : conversations) {
        Agent agent = agentRepository.findById(conv.getAgentId()); // N queries
        ConversationDTO dto = mapToDTO(conv, agent);
        dtos.add(dto);
    }
    
    return dtos;
}

// After: Single Query with JOIN
@Query("SELECT c FROM Conversation c " +
       "JOIN FETCH c.agent " +
       "WHERE c.status = :status")
List<Conversation> findActiveConversationsWithAgent(@Param("status") ConversationStatus status);

@GetMapping("/conversations")
public List<ConversationDTO> getConversations() {
    List<Conversation> conversations = conversationRepository
        .findActiveConversationsWithAgent(ConversationStatus.ACTIVE);
    return conversations.stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
}
```

#### 2. **Indexing Strategy**

```sql
-- Indexes for frequently queried columns
CREATE INDEX idx_conversation_tenant_status 
ON conversations(tenant_id, status);

CREATE INDEX idx_conversation_agent_id 
ON conversations(agent_id);

CREATE INDEX idx_conversation_created_at 
ON conversations(created_at);

-- Composite index for common queries
CREATE INDEX idx_conversation_tenant_created 
ON conversations(tenant_id, created_at DESC);
```

#### 3. **Connection Pooling**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

---

## Question 49: What was the connection pooling strategy?

### Answer

### Connection Pooling

#### 1. **HikariCP Configuration**

```yaml
spring:
  datasource:
    hikari:
      # Pool size
      maximum-pool-size: 20
      minimum-idle: 5
      
      # Timeouts
      connection-timeout: 30000 # 30 seconds
      idle-timeout: 600000 # 10 minutes
      max-lifetime: 1800000 # 30 minutes
      
      # Leak detection
      leak-detection-threshold: 60000 # 1 minute
      
      # Performance
      connection-test-query: SELECT 1
      auto-commit: false
```

#### 2. **Connection Pool Sizing**

```
┌─────────────────────────────────────────────────────────┐
│         Connection Pool Sizing                        │
└─────────────────────────────────────────────────────────┘

Formula:
connections = ((core_count * 2) + effective_spindle_count)

For 10 service instances:
├─ Core count: 4 per instance
├─ Effective spindle: 1 (SSD)
├─ Per instance: (4 * 2) + 1 = 9
├─ Total: 10 * 9 = 90 connections
└─ Configured: 20 per instance (200 total)

Reasoning:
├─ Database can handle 200 connections
├─ Prevents connection exhaustion
└─ Allows for peak load
```

---

## Question 50: How did you reduce infrastructure costs by 40% while scaling 3x?

### Answer

### Cost Optimization

#### 1. **Cost Reduction Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Cost Optimization                              │
└─────────────────────────────────────────────────────────┘

1. Auto-Scaling:
   ├─ Scale down during off-peak hours
   ├─ Right-size instances
   └─ Reserved instances for baseline

2. Caching:
   ├─ Reduced database load
   ├─ Smaller database instances
   └─ Fewer database connections

3. Query Optimization:
   ├─ Reduced database CPU usage
   ├─ Smaller database instances
   └─ Lower I/O costs

4. Resource Optimization:
   ├─ Right-size containers
   ├─ Efficient resource allocation
   └─ Spot instances for non-critical workloads
```

#### 2. **Before vs After**

```
Before:
├─ 2 service instances: $200/month
├─ Database: $500/month
├─ Cache: $100/month
└─ Total: $800/month

After:
├─ 10 service instances (auto-scaling): $400/month
├─ Database (optimized): $300/month
├─ Cache: $100/month
└─ Total: $800/month

With 3x traffic:
├─ Would need: $2,400/month (3x scaling)
├─ Actual cost: $800/month (optimized)
└─ Savings: 67% (but achieved 40% reduction)
```

---

## Question 51: Explain the async processing implementation.

### Answer

### Async Processing

#### 1. **Async Implementation**

```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    @Bean
    public ExecutorService asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class MessageProcessingService {
    @Async
    public CompletableFuture<NLUResponse> processMessageAsync(String message) {
        return CompletableFuture.supplyAsync(() -> {
            return nluFacadeService.processMessage(message);
        });
    }
}
```

#### 2. **Non-Blocking I/O**

```java
@Service
public class AsyncService {
    private final WebClient webClient;
    
    public Mono<NLUResponse> processMessageReactive(String message) {
        return webClient.post()
            .uri("/nlu/process")
            .bodyValue(message)
            .retrieve()
            .bodyToMono(NLUResponse.class)
            .timeout(Duration.ofSeconds(2))
            .onErrorResume(e -> {
                // Fallback
                return Mono.just(createDefaultResponse());
            });
    }
}
```

---

## Question 52: What was the N+1 query problem, and how did you solve it?

### Answer

### N+1 Query Problem

#### 1. **Problem**

```java
// N+1 Query Problem
@GetMapping("/conversations")
public List<ConversationDTO> getConversations() {
    // 1 query: Get all conversations
    List<Conversation> conversations = conversationRepository.findAll();
    
    List<ConversationDTO> dtos = new ArrayList<>();
    for (Conversation conv : conversations) {
        // N queries: One per conversation
        Agent agent = agentRepository.findById(conv.getAgentId());
        ConversationDTO dto = mapToDTO(conv, agent);
        dtos.add(dto);
    }
    
    // Total: 1 + N queries
    return dtos;
}
```

#### 2. **Solution: JOIN FETCH**

```java
// Solution: Single query with JOIN
@Query("SELECT c FROM Conversation c " +
       "JOIN FETCH c.agent a " +
       "WHERE c.status = :status")
List<Conversation> findActiveConversationsWithAgent(@Param("status") ConversationStatus status);

@GetMapping("/conversations")
public List<ConversationDTO> getConversations() {
    // 1 query: Get conversations with agents
    List<Conversation> conversations = conversationRepository
        .findActiveConversationsWithAgent(ConversationStatus.ACTIVE);
    
    // No additional queries needed
    return conversations.stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
}
```

---

## Question 53: How do you handle cache warming?

### Answer

### Cache Warming

#### 1. **Cache Warming Strategy**

```java
@Service
public class CacheWarmingService {
    private final ConversationRepository conversationRepository;
    private final ConversationCacheService cacheService;
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void warmCache() {
        // Get frequently accessed conversations
        List<Conversation> hotConversations = conversationRepository
            .findTopConversationsByAccessCount(1000);
        
        for (Conversation conv : hotConversations) {
            try {
                // Pre-load into cache
                cacheService.getConversation(conv.getId());
            } catch (Exception e) {
                log.warn("Failed to warm cache for conversation {}", conv.getId(), e);
            }
        }
    }
    
    @PostConstruct
    public void warmCacheOnStartup() {
        // Warm cache on service startup
        warmCache();
    }
}
```

---

## Question 54: What's the cache eviction strategy?

### Answer

### Cache Eviction

#### 1. **Eviction Policies**

```java
@Configuration
public class CacheConfiguration {
    @Bean
    public Cache<String, Conversation> localCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .recordStats()
            .build();
    }
}

// Redis eviction
@Configuration
public class RedisConfiguration {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues();
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

---

## Question 55: How did you achieve < 100ms latency for real-time message delivery?

### Answer

### Latency Optimization

#### 1. **Optimization Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Latency Optimization                            │
└─────────────────────────────────────────────────────────┘

1. Caching:
   ├─ L1 cache: < 1ms
   ├─ L2 cache: 5-10ms
   └─ Avoid database: 50-100ms

2. Async Processing:
   ├─ Non-blocking I/O
   ├─ Parallel processing
   └─ Background jobs

3. Connection Pooling:
   ├─ Reuse connections
   ├─ Reduce connection overhead
   └─ Fast connection acquisition

4. WebSocket Optimization:
   ├─ Persistent connections
   ├─ Binary protocol
   └─ Message batching
```

#### 2. **Performance Breakdown**

```
Message Delivery Latency:
├─ API Gateway: 5ms
├─ Service Processing: 20ms
├─ Cache Lookup: 5ms
├─ Database (if needed): 50ms
├─ WebSocket Send: 10ms
└─ Total: < 100ms (90ms average)
```

---

## Summary

Scaling & Performance (Project 1) answers cover:

1. **Auto-Scaling**: HPA with CPU/memory metrics, 3-20 replicas
2. **Multi-Level Caching**: L1 (Caffeine) → L2 (Redis) → L3 (Database)
3. **Database Optimization**: Query optimization, indexing, connection pooling
4. **Connection Pooling**: HikariCP with 20 connections per instance
5. **Cost Optimization**: 40% reduction through auto-scaling and optimization
6. **Async Processing**: Non-blocking I/O, CompletableFuture
7. **N+1 Query Problem**: Solved with JOIN FETCH
8. **Cache Warming**: Scheduled and startup warming
9. **Cache Eviction**: TTL-based and size-based eviction
10. **Latency Optimization**: < 100ms through caching and async processing
