# Caching - Part 1: Multi-Level Caching Strategy

## Question 181: Explain the multi-level caching strategy.

### Answer

### Multi-Level Caching Architecture

#### 1. **Three-Level Cache Hierarchy**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Cache Architecture                 │
└─────────────────────────────────────────────────────────┘

Level 1: Application Cache (Caffeine)
├─ In-memory cache per instance
├─ Fastest access (nanoseconds)
├─ Limited size
└─ Lost on restart

Level 2: Distributed Cache (Redis)
├─ Shared across instances
├─ Fast access (milliseconds)
├─ Large size
└─ Persistent

Level 3: Database
├─ Source of truth
├─ Slowest access (tens of milliseconds)
├─ Unlimited size
└─ Persistent
```

#### 2. **Cache Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Lookup Flow                              │
└─────────────────────────────────────────────────────────┘

Request for Data:
    │
    ▼
Level 1: Application Cache
    │
    ├─► Hit? → Return (1ms)
    │
    └─► Miss? → Check Level 2
        │
        ▼
Level 2: Redis Cache
        │
        ├─► Hit? → Return + Store in L1 (5ms)
        │
        └─► Miss? → Check Level 3
            │
            ▼
Level 3: Database
            │
            ├─► Found? → Return + Store in L2 + L1 (50ms)
            │
            └─► Not Found? → Return null
```

#### 3. **Implementation**

```java
@Service
public class MultiLevelCacheService {
    // Level 1: Application cache (Caffeine)
    private final Cache<String, Conversation> localCache;
    
    // Level 2: Distributed cache (Redis)
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    // Level 3: Database
    private final ConversationRepository conversationRepository;
    
    public Conversation getConversation(String conversationId) {
        // Level 1: Check local cache
        Conversation conversation = localCache.getIfPresent(conversationId);
        if (conversation != null) {
            recordCacheHit("L1");
            return conversation;
        }
        
        // Level 2: Check Redis
        conversation = redisTemplate.opsForValue()
            .get("conv:" + conversationId);
        if (conversation != null) {
            // Store in L1 for next time
            localCache.put(conversationId, conversation);
            recordCacheHit("L2");
            return conversation;
        }
        
        // Level 3: Check database
        conversation = conversationRepository.findById(conversationId)
            .orElse(null);
        
        if (conversation != null) {
            // Store in L2 and L1
            redisTemplate.opsForValue().set(
                "conv:" + conversationId, 
                conversation, 
                Duration.ofMinutes(10)
            );
            localCache.put(conversationId, conversation);
            recordCacheHit("L3");
        }
        
        return conversation;
    }
    
    private void recordCacheHit(String level) {
        Counter.builder("cache.hits")
            .tag("level", level)
            .register(meterRegistry)
            .increment();
    }
}
```

#### 4. **Cache Configuration**

```java
@Configuration
public class CacheConfiguration {
    // Level 1: Caffeine cache
    @Bean
    public Cache<String, Conversation> localCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }
    
    // Level 2: Redis configuration
    @Bean
    public RedisTemplate<String, Conversation> redisTemplate() {
        RedisTemplate<String, Conversation> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(Conversation.class));
        return template;
    }
}
```

#### 5. **Cache Hit Rates**

```
┌─────────────────────────────────────────────────────────┐
│         Expected Cache Hit Rates                       │
└─────────────────────────────────────────────────────────┘

Level 1 (Application Cache):
├─ Hit Rate: 60-70%
├─ Access Time: < 1ms
└─ Size: 10,000 entries

Level 2 (Redis Cache):
├─ Hit Rate: 25-30%
├─ Access Time: 5-10ms
└─ Size: 1M+ entries

Level 3 (Database):
├─ Hit Rate: 5-10%
├─ Access Time: 50-100ms
└─ Size: Unlimited

Overall:
├─ Total Cache Hit Rate: 85-90%
├─ Average Access Time: < 10ms
└─ Database Load Reduction: 85-90%
```

---

## Question 182: What's the cache invalidation strategy?

### Answer

### Cache Invalidation Strategies

#### 1. **Invalidation Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Invalidation Patterns                    │
└─────────────────────────────────────────────────────────┘

1. Time-Based (TTL):
   ├─ Cache expires after time
   ├─ Simple to implement
   └─ May serve stale data

2. Event-Based:
   ├─ Invalidate on data change
   ├─ Always fresh data
   └─ More complex

3. Write-Through:
   ├─ Write to cache and database
   ├─ Cache always up-to-date
   └─ Slower writes

4. Write-Behind:
   ├─ Write to cache first
   ├─ Async write to database
   └─ Faster writes, risk of data loss
```

#### 2. **Time-Based Invalidation (TTL)**

```java
@Service
public class TTLBasedCacheService {
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    public void cacheConversation(Conversation conversation) {
        String key = "conv:" + conversation.getId();
        
        // Cache with TTL
        redisTemplate.opsForValue().set(
            key, 
            conversation, 
            Duration.ofMinutes(10) // Expires in 10 minutes
        );
    }
    
    public Conversation getConversation(String conversationId) {
        String key = "conv:" + conversationId;
        Conversation conversation = redisTemplate.opsForValue().get(key);
        
        if (conversation == null) {
            // Cache expired, load from database
            conversation = loadFromDatabase(conversationId);
            if (conversation != null) {
                cacheConversation(conversation);
            }
        }
        
        return conversation;
    }
}
```

#### 3. **Event-Based Invalidation**

```java
@Service
public class EventBasedCacheService {
    private final RedisTemplate<String, Conversation> redisTemplate;
    private final Cache<String, Conversation> localCache;
    
    @EventListener
    public void handleConversationUpdated(ConversationUpdatedEvent event) {
        String conversationId = event.getConversationId();
        
        // Invalidate L1 cache
        localCache.invalidate(conversationId);
        
        // Invalidate L2 cache
        redisTemplate.delete("conv:" + conversationId);
        
        // Optionally: Pre-warm cache with new data
        Conversation updated = loadFromDatabase(conversationId);
        if (updated != null) {
            cacheConversation(updated);
        }
    }
    
    @EventListener
    public void handleConversationDeleted(ConversationDeletedEvent event) {
        String conversationId = event.getConversationId();
        
        // Remove from all cache levels
        localCache.invalidate(conversationId);
        redisTemplate.delete("conv:" + conversationId);
    }
}
```

#### 4. **Redis Pub/Sub for Distributed Invalidation**

```java
@Service
public class DistributedCacheInvalidation {
    private final RedisTemplate<String, String> redisTemplate;
    private final Cache<String, Conversation> localCache;
    
    @PostConstruct
    public void init() {
        // Subscribe to invalidation channel
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(
            new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {
                    String conversationId = deserialize(message.getBody());
                    invalidateLocalCache(conversationId);
                }
            },
            new ChannelTopic("cache:invalidate")
        );
        container.start();
    }
    
    public void invalidateCache(String conversationId) {
        // Invalidate local cache
        localCache.invalidate(conversationId);
        
        // Invalidate Redis cache
        redisTemplate.delete("conv:" + conversationId);
        
        // Notify other instances via pub/sub
        redisTemplate.convertAndSend("cache:invalidate", conversationId);
    }
    
    private void invalidateLocalCache(String conversationId) {
        localCache.invalidate(conversationId);
    }
}
```

#### 5. **Pattern-Based Invalidation**

```java
@Service
public class PatternBasedInvalidation {
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    public void invalidateByPattern(String pattern) {
        // Invalidate all keys matching pattern
        Set<String> keys = redisTemplate.keys("conv:" + pattern + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    // Example: Invalidate all conversations for a tenant
    public void invalidateTenantConversations(String tenantId) {
        invalidateByPattern(tenantId + ":*");
    }
}
```

#### 6. **Version-Based Invalidation**

```java
@Service
public class VersionBasedCache {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void cacheWithVersion(String key, Object value, long version) {
        String versionKey = key + ":version";
        String currentVersion = redisTemplate.opsForValue().get(versionKey);
        
        if (currentVersion == null || Long.parseLong(currentVersion) < version) {
            // New version, update cache
            redisTemplate.opsForValue().set(key, serialize(value));
            redisTemplate.opsForValue().set(versionKey, String.valueOf(version));
        }
    }
    
    public Object getWithVersion(String key, long requiredVersion) {
        String versionKey = key + ":version";
        String currentVersion = redisTemplate.opsForValue().get(versionKey);
        
        if (currentVersion != null && Long.parseLong(currentVersion) >= requiredVersion) {
            return deserialize(redisTemplate.opsForValue().get(key));
        }
        
        return null; // Cache invalid, need to reload
    }
}
```

---

## Question 183: How do you handle cache stampede?

### Answer

### Cache Stampede Prevention

#### 1. **What is Cache Stampede?**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Stampede Scenario                        │
└─────────────────────────────────────────────────────────┘

Time    | Request 1 | Request 2 | Request 3 | Database
--------|-----------|-----------|-----------|----------
T0      | Cache miss| Cache miss| Cache miss|
T1      | Query DB  | Query DB  | Query DB  | 3 queries
T2      | Get result| Get result| Get result|
T3      | Write cache| Write cache| Write cache|

Problem:
├─ Multiple requests for same data
├─ Cache expires simultaneously
├─ All requests hit database
└─ Database overload
```

#### 2. **Solution 1: Lock-Based Approach**

```java
@Service
public class LockBasedCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ConversationRepository repository;
    
    public Conversation getConversation(String conversationId) {
        String cacheKey = "conv:" + conversationId;
        
        // Try to get from cache
        Conversation conversation = getFromCache(cacheKey);
        if (conversation != null) {
            return conversation;
        }
        
        // Cache miss - acquire lock
        String lockKey = "lock:conv:" + conversationId;
        String lockValue = UUID.randomUUID().toString();
        
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));
        
        if (lockAcquired) {
            try {
                // Double-check cache (another thread might have loaded it)
                conversation = getFromCache(cacheKey);
                if (conversation != null) {
                    return conversation;
                }
                
                // Load from database
                conversation = repository.findById(conversationId).orElse(null);
                
                if (conversation != null) {
                    // Cache the result
                    cacheConversation(cacheKey, conversation);
                }
                
                return conversation;
                
            } finally {
                // Release lock
                releaseLock(lockKey, lockValue);
            }
        } else {
            // Another thread is loading, wait and retry
            try {
                Thread.sleep(100);
                return getConversation(conversationId); // Retry
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
    }
}
```

#### 3. **Solution 2: Probabilistic Early Expiration**

```java
@Service
public class ProbabilisticCacheService {
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    public Conversation getConversation(String conversationId) {
        String cacheKey = "conv:" + conversationId;
        Conversation conversation = redisTemplate.opsForValue().get(cacheKey);
        
        if (conversation != null) {
            // Check if near expiration
            Long ttl = redisTemplate.getExpire(cacheKey);
            if (ttl != null && ttl < 60) { // Less than 1 minute left
                // Probabilistic early refresh
                double probability = (60.0 - ttl) / 60.0;
                if (Math.random() < probability) {
                    // Refresh in background
                    refreshInBackground(conversationId);
                }
            }
            return conversation;
        }
        
        // Cache miss - load from database
        return loadFromDatabase(conversationId);
    }
    
    @Async
    private void refreshInBackground(String conversationId) {
        // Load fresh data and update cache
        Conversation fresh = loadFromDatabase(conversationId);
        if (fresh != null) {
            cacheConversation("conv:" + conversationId, fresh);
        }
    }
}
```

#### 4. **Solution 3: Stale-While-Revalidate**

```java
@Service
public class StaleWhileRevalidateCache {
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    public Conversation getConversation(String conversationId) {
        String cacheKey = "conv:" + conversationId;
        String staleKey = "stale:conv:" + conversationId;
        
        // Try to get fresh data
        Conversation conversation = redisTemplate.opsForValue().get(cacheKey);
        if (conversation != null) {
            // Check if stale
            Long ttl = redisTemplate.getExpire(cacheKey);
            if (ttl != null && ttl < 60) {
                // Return stale data, refresh in background
                Conversation stale = redisTemplate.opsForValue().get(staleKey);
                if (stale != null) {
                    refreshInBackground(conversationId);
                    return stale; // Return stale data immediately
                }
            }
            return conversation;
        }
        
        // Cache miss - load from database
        conversation = loadFromDatabase(conversationId);
        if (conversation != null) {
            // Cache both fresh and stale
            cacheConversation(cacheKey, conversation, Duration.ofMinutes(10));
            cacheConversation(staleKey, conversation, Duration.ofMinutes(20));
        }
        
        return conversation;
    }
}
```

#### 5. **Solution 4: Request Coalescing**

```java
@Service
public class RequestCoalescingCache {
    private final Map<String, CompletableFuture<Conversation>> pendingRequests = 
        new ConcurrentHashMap<>();
    
    public CompletableFuture<Conversation> getConversationAsync(String conversationId) {
        String cacheKey = "conv:" + conversationId;
        
        // Try cache first
        Conversation cached = getFromCache(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        
        // Check if request is already in progress
        CompletableFuture<Conversation> pending = pendingRequests.get(conversationId);
        if (pending != null) {
            return pending; // Return existing future
        }
        
        // Create new request
        CompletableFuture<Conversation> future = CompletableFuture.supplyAsync(() -> {
            try {
                // Load from database
                Conversation conversation = loadFromDatabase(conversationId);
                if (conversation != null) {
                    cacheConversation(cacheKey, conversation);
                }
                return conversation;
            } finally {
                // Remove from pending
                pendingRequests.remove(conversationId);
            }
        });
        
        pendingRequests.put(conversationId, future);
        return future;
    }
}
```

---

## Question 184: What's the cache warming strategy?

### Answer

### Cache Warming Strategies

#### 1. **What is Cache Warming?**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Warming Purpose                          │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Cold cache after restart
├─ High database load initially
├─ Slow response times
└─ Poor user experience

Solution:
├─ Pre-populate cache
├─ Load frequently accessed data
├─ Reduce cold start impact
└─ Improve initial performance
```

#### 2. **Application Startup Warming**

```java
@Component
public class CacheWarmingService {
    private final ConversationRepository repository;
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    @PostConstruct
    public void warmCacheOnStartup() {
        // Load most frequently accessed conversations
        List<Conversation> popularConversations = repository
            .findTop100ByOrderByAccessCountDesc();
        
        for (Conversation conversation : popularConversations) {
            cacheConversation(conversation);
        }
        
        log.info("Warmed cache with {} conversations", popularConversations.size());
    }
}
```

#### 3. **Scheduled Cache Warming**

```java
@Component
public class ScheduledCacheWarming {
    private final ConversationRepository repository;
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    // Warm cache every hour
    @Scheduled(cron = "0 0 * * * *")
    public void warmCacheHourly() {
        // Load active conversations
        List<Conversation> activeConversations = repository
            .findByStatusAndLastActivityAfter(
                ConversationStatus.ACTIVE,
                Instant.now().minus(Duration.ofHours(1))
            );
        
        for (Conversation conversation : activeConversations) {
            cacheConversation(conversation);
        }
    }
    
    // Warm cache before peak hours
    @Scheduled(cron = "0 0 8 * * *") // 8 AM
    public void warmCacheForPeakHours() {
        // Load conversations likely to be accessed
        List<Conversation> likelyConversations = repository
            .findByLastActivityBetween(
                Instant.now().minus(Duration.ofDays(1)),
                Instant.now()
            );
        
        for (Conversation conversation : likelyConversations) {
            cacheConversation(conversation);
        }
    }
}
```

#### 4. **Predictive Cache Warming**

```java
@Service
public class PredictiveCacheWarming {
    private final ConversationRepository repository;
    private final AccessPatternAnalyzer analyzer;
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void warmCachePredictively() {
        // Analyze access patterns
        AccessPattern pattern = analyzer.analyzeAccessPatterns();
        
        // Predict likely accesses
        List<String> likelyConversationIds = pattern.predictNextAccesses();
        
        // Warm cache with predicted data
        for (String conversationId : likelyConversationIds) {
            Conversation conversation = repository.findById(conversationId).orElse(null);
            if (conversation != null) {
                cacheConversation(conversation);
            }
        }
    }
}
```

#### 5. **Lazy Cache Warming**

```java
@Service
public class LazyCacheWarming {
    private final ConversationRepository repository;
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    public Conversation getConversation(String conversationId) {
        // Try cache
        Conversation conversation = getFromCache(conversationId);
        if (conversation != null) {
            return conversation;
        }
        
        // Cache miss - load from database
        conversation = repository.findById(conversationId).orElse(null);
        if (conversation != null) {
            cacheConversation(conversation);
            
            // Warm related data in background
            warmRelatedData(conversation);
        }
        
        return conversation;
    }
    
    @Async
    private void warmRelatedData(Conversation conversation) {
        // Warm related conversations
        List<Conversation> related = repository
            .findByCustomerId(conversation.getCustomerId());
        
        for (Conversation relatedConv : related) {
            cacheConversation(relatedConv);
        }
    }
}
```

---

## Summary

Part 1 covers:

1. **Multi-Level Caching**: Three-level hierarchy (Application → Redis → Database)
2. **Cache Invalidation**: Time-based, event-based, distributed invalidation
3. **Cache Stampede Prevention**: Lock-based, probabilistic, stale-while-revalidate, request coalescing
4. **Cache Warming**: Startup, scheduled, predictive, lazy warming

Key principles:
- Use multiple cache levels for optimal performance
- Implement proper invalidation to maintain data freshness
- Prevent cache stampede to protect database
- Warm cache to reduce cold start impact
