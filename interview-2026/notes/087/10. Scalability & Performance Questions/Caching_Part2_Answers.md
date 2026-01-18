# Caching - Part 2: Cache Configuration & Management

## Question 185: How do you determine cache TTL?

### Answer

### Cache TTL (Time To Live) Strategy

#### 1. **TTL Selection Factors**

```
┌─────────────────────────────────────────────────────────┐
│         TTL Selection Factors                          │
└─────────────────────────────────────────────────────────┘

Data Characteristics:
├─ Update frequency
├─ Staleness tolerance
├─ Data criticality
└─ Access patterns

Business Requirements:
├─ Real-time requirements
├─ Consistency requirements
├─ Performance requirements
└─ Cost considerations

Technical Constraints:
├─ Memory limits
├─ Cache size
├─ Eviction policies
└─ Network latency
```

#### 2. **TTL by Data Type**

```java
@Service
public class CacheTTLManager {
    // Static data: Long TTL
    private static final Duration STATIC_DATA_TTL = Duration.ofDays(7);
    // Examples: Configuration, reference data
    
    // Semi-static data: Medium TTL
    private static final Duration SEMI_STATIC_TTL = Duration.ofHours(1);
    // Examples: User profiles, agent information
    
    // Dynamic data: Short TTL
    private static final Duration DYNAMIC_TTL = Duration.ofMinutes(5);
    // Examples: Conversations, messages
    
    // Real-time data: Very short TTL
    private static final Duration REALTIME_TTL = Duration.ofSeconds(30);
    // Examples: Agent status, active sessions
    
    public Duration getTTL(String dataType) {
        return switch (dataType) {
            case "configuration" -> STATIC_DATA_TTL;
            case "user_profile" -> SEMI_STATIC_TTL;
            case "conversation" -> DYNAMIC_TTL;
            case "agent_status" -> REALTIME_TTL;
            default -> Duration.ofMinutes(10);
        };
    }
}
```

#### 3. **Adaptive TTL**

```java
@Service
public class AdaptiveTTLManager {
    private final AccessPatternAnalyzer analyzer;
    
    public Duration calculateAdaptiveTTL(String key, String dataType) {
        // Base TTL by data type
        Duration baseTTL = getBaseTTL(dataType);
        
        // Adjust based on access pattern
        AccessPattern pattern = analyzer.getAccessPattern(key);
        
        if (pattern.isFrequentlyAccessed()) {
            // Increase TTL for frequently accessed data
            return baseTTL.multipliedBy(2);
        } else if (pattern.isRarelyAccessed()) {
            // Decrease TTL for rarely accessed data
            return baseTTL.dividedBy(2);
        }
        
        return baseTTL;
    }
    
    public Duration calculateTTLByUpdateFrequency(String dataType, 
                                                  Duration avgUpdateInterval) {
        // TTL should be shorter than update frequency
        // But not too short to avoid cache churn
        return avgUpdateInterval.dividedBy(2);
    }
}
```

#### 4. **TTL Configuration Examples**

```java
@Configuration
public class CacheTTLConfiguration {
    // Conversation cache: 10 minutes
    // Updated frequently, but acceptable staleness
    public static final Duration CONVERSATION_TTL = Duration.ofMinutes(10);
    
    // Agent state cache: 5 minutes
    // Changes frequently, need fresh data
    public static final Duration AGENT_STATE_TTL = Duration.ofMinutes(5);
    
    // User profile cache: 1 hour
    // Changes infrequently, can tolerate longer TTL
    public static final Duration USER_PROFILE_TTL = Duration.ofHours(1);
    
    // Configuration cache: 24 hours
    // Rarely changes, long TTL acceptable
    public static final Duration CONFIG_TTL = Duration.ofHours(24);
    
    // NLU response cache: 5 minutes
    // Same query should return same result
    public static final Duration NLU_RESPONSE_TTL = Duration.ofMinutes(5);
}
```

---

## Question 186: What's the cache eviction policy (LRU, LFU, etc.)?

### Answer

### Cache Eviction Policies

#### 1. **Eviction Policy Types**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Eviction Policies                        │
└─────────────────────────────────────────────────────────┘

LRU (Least Recently Used):
├─ Evicts least recently accessed
├─ Good for temporal locality
└─ Simple to implement

LFU (Least Frequently Used):
├─ Evicts least frequently accessed
├─ Good for frequency-based patterns
└─ More complex

FIFO (First In First Out):
├─ Evicts oldest entry
├─ Simple but not optimal
└─ Rarely used

TTL-Based:
├─ Evicts expired entries
├─ Time-based expiration
└─ Common in distributed caches

Size-Based:
├─ Evicts when size limit reached
├─ Combined with other policies
└─ Prevents memory overflow
```

#### 2. **LRU Implementation (Caffeine)**

```java
@Configuration
public class LRUCacheConfiguration {
    @Bean
    public Cache<String, Conversation> lruCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000) // Size limit
            .expireAfterAccess(10, TimeUnit.MINUTES) // LRU with TTL
            .recordStats()
            .build();
    }
}
```

**LRU Behavior:**
```
┌─────────────────────────────────────────────────────────┐
│         LRU Eviction Example                           │
└─────────────────────────────────────────────────────────┘

Cache State (max 3 entries):
├─ Entry A (accessed 10:00)
├─ Entry B (accessed 10:05)
└─ Entry C (accessed 10:10)

New Entry D arrives:
├─ Cache full, evict least recently used
├─ Entry A evicted (oldest access)
└─ Entry D added

New Access to Entry B:
├─ Entry B becomes most recently used
└─ Entry C becomes least recently used
```

#### 3. **LFU Implementation**

```java
@Service
public class LFUCacheService {
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Map<String, Integer> accessCount = new ConcurrentHashMap<>();
    private final int maxSize = 10_000;
    
    public Conversation get(String key) {
        // Increment access count
        accessCount.merge(key, 1, Integer::sum);
        
        CacheEntry entry = cache.get(key);
        if (entry != null) {
            // Update access time
            entry.setLastAccess(Instant.now());
            return entry.getValue();
        }
        
        return null;
    }
    
    public void put(String key, Conversation value) {
        if (cache.size() >= maxSize) {
            // Evict least frequently used
            evictLFU();
        }
        
        cache.put(key, new CacheEntry(value, Instant.now()));
        accessCount.put(key, 1);
    }
    
    private void evictLFU() {
        // Find entry with lowest access count
        String lfuKey = accessCount.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        if (lfuKey != null) {
            cache.remove(lfuKey);
            accessCount.remove(lfuKey);
        }
    }
}
```

#### 4. **Redis Eviction Policies**

```yaml
# Redis eviction policy configuration
maxmemory-policy: allkeys-lru
# Options:
# - noeviction: Don't evict, return errors
# - allkeys-lru: Evict least recently used
# - allkeys-lfu: Evict least frequently used
# - allkeys-random: Evict random keys
# - volatile-lru: Evict LRU among keys with TTL
# - volatile-lfu: Evict LFU among keys with TTL
# - volatile-random: Evict random keys with TTL
# - volatile-ttl: Evict keys with shortest TTL
```

#### 5. **Hybrid Eviction Policy**

```java
@Service
public class HybridEvictionCache {
    private final Cache<String, Conversation> cache;
    
    @Bean
    public Cache<String, Conversation> hybridCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000) // Size-based
            .expireAfterWrite(10, TimeUnit.MINUTES) // TTL-based
            .expireAfterAccess(5, TimeUnit.MINUTES) // LRU-based
            .removalListener((key, value, cause) -> {
                // Log eviction reason
                logEviction(key, cause);
            })
            .recordStats()
            .build();
    }
    
    // Eviction causes:
    // - EXPIRED: TTL expired
    // - SIZE: Size limit reached
    // - EXPLICIT: Manually removed
    // - REPLACED: Replaced by new value
}
```

#### 6. **Eviction Policy Selection**

```
┌─────────────────────────────────────────────────────────┐
│         Eviction Policy Selection Guide                │
└─────────────────────────────────────────────────────────┘

For Temporal Locality (Recent Access):
├─ Use: LRU
├─ Example: Recently viewed conversations
└─ Benefit: Keeps hot data in cache

For Frequency Patterns (Access Count):
├─ Use: LFU
├─ Example: Popular conversations
└─ Benefit: Keeps frequently accessed data

For Time-Sensitive Data:
├─ Use: TTL-based
├─ Example: Agent status, real-time data
└─ Benefit: Ensures data freshness

For Mixed Patterns:
├─ Use: Hybrid (LRU + TTL)
├─ Example: General purpose cache
└─ Benefit: Best of both worlds
```

---

## Question 187: How do you handle cache consistency?

### Answer

### Cache Consistency Strategies

#### 1. **Consistency Models**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Consistency Models                       │
└─────────────────────────────────────────────────────────┘

Strong Consistency:
├─ Cache always matches database
├─ Immediate invalidation on update
└─ Higher complexity, lower performance

Eventual Consistency:
├─ Cache may be stale temporarily
├─ Updates propagate eventually
└─ Better performance, simpler

Read-Through:
├─ Cache miss → Load from DB → Cache
├─ Always fresh on read
└─ Good for read-heavy workloads

Write-Through:
├─ Write to cache and DB together
├─ Cache always up-to-date
└─ Slower writes, consistent reads

Write-Behind:
├─ Write to cache first, async to DB
├─ Fast writes, eventual consistency
└─ Risk of data loss
```

#### 2. **Write-Through Pattern**

```java
@Service
public class WriteThroughCache {
    private final RedisTemplate<String, Conversation> redisTemplate;
    private final ConversationRepository repository;
    
    @Transactional
    public Conversation updateConversation(Conversation conversation) {
        // Write to database first
        Conversation saved = repository.save(conversation);
        
        // Update cache immediately
        String cacheKey = "conv:" + saved.getId();
        redisTemplate.opsForValue().set(cacheKey, saved, Duration.ofMinutes(10));
        
        // Invalidate local cache
        localCache.invalidate(cacheKey);
        
        return saved;
    }
    
    public Conversation getConversation(String conversationId) {
        String cacheKey = "conv:" + conversationId;
        
        // Try cache
        Conversation conversation = redisTemplate.opsForValue().get(cacheKey);
        if (conversation != null) {
            return conversation;
        }
        
        // Cache miss - load from DB and cache
        conversation = repository.findById(conversationId).orElse(null);
        if (conversation != null) {
            redisTemplate.opsForValue().set(cacheKey, conversation, Duration.ofMinutes(10));
        }
        
        return conversation;
    }
}
```

#### 3. **Event-Driven Invalidation**

```java
@Service
public class EventDrivenCacheConsistency {
    private final RedisTemplate<String, Conversation> redisTemplate;
    private final Cache<String, Conversation> localCache;
    
    @EventListener
    public void handleConversationUpdated(ConversationUpdatedEvent event) {
        String conversationId = event.getConversationId();
        
        // Invalidate all cache levels
        invalidateCache(conversationId);
        
        // Optionally: Pre-warm with new data
        Conversation updated = loadFromDatabase(conversationId);
        if (updated != null) {
            cacheConversation(conversationId, updated);
        }
    }
    
    @KafkaListener(topics = "conversation-events")
    public void handleKafkaEvent(ConversationEvent event) {
        // Invalidate cache on event
        invalidateCache(event.getConversationId());
    }
}
```

#### 4. **Version-Based Consistency**

```java
@Service
public class VersionBasedCache {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void cacheWithVersion(String key, Object value, long version) {
        String versionKey = key + ":version";
        String cachedVersion = redisTemplate.opsForValue().get(versionKey);
        
        if (cachedVersion == null || Long.parseLong(cachedVersion) < version) {
            // New version - update cache
            redisTemplate.opsForValue().set(key, serialize(value));
            redisTemplate.opsForValue().set(versionKey, String.valueOf(version));
        }
    }
    
    public Object getWithVersion(String key, long requiredVersion) {
        String versionKey = key + ":version";
        String cachedVersion = redisTemplate.opsForValue().get(versionKey);
        
        if (cachedVersion != null && Long.parseLong(cachedVersion) >= requiredVersion) {
            return deserialize(redisTemplate.opsForValue().get(key));
        }
        
        return null; // Cache invalid or version mismatch
    }
}
```

#### 5. **Distributed Cache Consistency**

```java
@Service
public class DistributedCacheConsistency {
    private final RedisTemplate<String, String> redisTemplate;
    private final Cache<String, Conversation> localCache;
    
    @PostConstruct
    public void init() {
        // Subscribe to cache invalidation channel
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(
            new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {
                    String key = deserialize(message.getBody());
                    invalidateLocalCache(key);
                }
            },
            new ChannelTopic("cache:invalidate")
        );
        container.start();
    }
    
    public void invalidateCache(String key) {
        // Invalidate local cache
        localCache.invalidate(key);
        
        // Invalidate Redis cache
        redisTemplate.delete(key);
        
        // Notify other instances
        redisTemplate.convertAndSend("cache:invalidate", key);
    }
}
```

---

## Question 188: What happens if Redis goes down?

### Answer

### Redis Failure Handling

#### 1. **Failure Detection**

```java
@Component
public class RedisHealthChecker {
    private final RedisTemplate<String, String> redisTemplate;
    private volatile boolean redisAvailable = true;
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void checkRedisHealth() {
        try {
            redisTemplate.opsForValue().get("health:check");
            redisAvailable = true;
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis health check failed", e);
            alertService.redisUnavailable();
        }
    }
    
    public boolean isRedisAvailable() {
        return redisAvailable;
    }
}
```

#### 2. **Graceful Degradation**

```java
@Service
public class ResilientCacheService {
    private final RedisTemplate<String, Conversation> redisTemplate;
    private final ConversationRepository repository;
    private final RedisHealthChecker healthChecker;
    
    public Conversation getConversation(String conversationId) {
        // Try Redis if available
        if (healthChecker.isRedisAvailable()) {
            try {
                Conversation cached = redisTemplate.opsForValue()
                    .get("conv:" + conversationId);
                if (cached != null) {
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Redis access failed, falling back to database", e);
                healthChecker.markRedisUnavailable();
            }
        }
        
        // Fallback to database
        return repository.findById(conversationId).orElse(null);
    }
    
    public void cacheConversation(Conversation conversation) {
        if (healthChecker.isRedisAvailable()) {
            try {
                redisTemplate.opsForValue().set(
                    "conv:" + conversation.getId(),
                    conversation,
                    Duration.ofMinutes(10)
                );
            } catch (Exception e) {
                log.warn("Failed to cache in Redis", e);
                // Continue without caching
            }
        }
    }
}
```

#### 3. **Multi-Level Fallback**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Fallback Strategy                  │
└─────────────────────────────────────────────────────────┘

Level 1: Redis Cache (Primary)
├─ Fast, distributed
├─ Failure: Fall to Level 2
└─ Recovery: Automatic

Level 2: Local Cache (Secondary)
├─ Fast, per-instance
├─ Failure: Fall to Level 3
└─ Limited size

Level 3: Database (Tertiary)
├─ Slow, persistent
├─ Always available
└─ Source of truth
```

#### 4. **Circuit Breaker Pattern**

```java
@Service
public class CircuitBreakerCache {
    private final CircuitBreaker circuitBreaker;
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    public Conversation getConversation(String conversationId) {
        return circuitBreaker.executeSupplier(() -> {
            // Try Redis
            Conversation cached = redisTemplate.opsForValue()
                .get("conv:" + conversationId);
            
            if (cached != null) {
                return cached;
            }
            
            // Cache miss - load from DB
            return loadFromDatabase(conversationId);
        });
    }
    
    @Bean
    public CircuitBreaker redisCircuitBreaker() {
        return CircuitBreaker.of("redis", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .build());
    }
}
```

#### 5. **Recovery Strategy**

```java
@Service
public class CacheRecoveryService {
    private final RedisTemplate<String, Conversation> redisTemplate;
    private final ConversationRepository repository;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void recoverCache() {
        if (isRedisAvailable()) {
            // Warm cache with frequently accessed data
            List<Conversation> popular = repository
                .findTop1000ByOrderByAccessCountDesc();
            
            for (Conversation conversation : popular) {
                try {
                    redisTemplate.opsForValue().set(
                        "conv:" + conversation.getId(),
                        conversation,
                        Duration.ofMinutes(10)
                    );
                } catch (Exception e) {
                    log.warn("Failed to warm cache", e);
                }
            }
        }
    }
}
```

---

## Question 189: How do you monitor cache hit rates?

### Answer

### Cache Hit Rate Monitoring

#### 1. **Metrics Collection**

```java
@Component
public class CacheMetricsCollector {
    private final MeterRegistry meterRegistry;
    private final Cache<String, Conversation> localCache;
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void collectCacheMetrics() {
        // Local cache stats
        CacheStats localStats = localCache.stats();
        recordLocalCacheMetrics(localStats);
        
        // Redis cache stats (via INFO command)
        Properties redisInfo = redisTemplate.getConnectionFactory()
            .getConnection()
            .info("stats");
        recordRedisCacheMetrics(redisInfo);
    }
    
    private void recordLocalCacheMetrics(CacheStats stats) {
        // Hit rate
        double hitRate = stats.hitRate();
        Gauge.builder("cache.hit.rate")
            .tag("level", "L1")
            .register(meterRegistry)
            .set(hitRate);
        
        // Hit count
        Counter.builder("cache.hits")
            .tag("level", "L1")
            .register(meterRegistry)
            .increment((long) stats.hitCount());
        
        // Miss count
        Counter.builder("cache.misses")
            .tag("level", "L1")
            .register(meterRegistry)
            .increment((long) stats.missCount());
        
        // Eviction count
        Counter.builder("cache.evictions")
            .tag("level", "L1")
            .register(meterRegistry)
            .increment((long) stats.evictionCount());
    }
}
```

#### 2. **Hit Rate Calculation**

```java
@Service
public class CacheHitRateCalculator {
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    
    public double getHitRate() {
        long total = hits.get() + misses.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) hits.get() / total;
    }
    
    public void recordHit() {
        hits.incrementAndGet();
    }
    
    public void recordMiss() {
        misses.incrementAndGet();
    }
    
    @Scheduled(fixedRate = 60000)
    public void logHitRate() {
        double hitRate = getHitRate();
        log.info("Cache hit rate: {}%", hitRate * 100);
        
        if (hitRate < 0.8) {
            alertService.lowCacheHitRate(hitRate);
        }
    }
}
```

#### 3. **Dashboard Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Metrics Dashboard                        │
└─────────────────────────────────────────────────────────┘

Level 1 (Local Cache):
├─ Hit Rate: 65%
├─ Hits: 65,000/min
├─ Misses: 35,000/min
└─ Evictions: 1,000/min

Level 2 (Redis Cache):
├─ Hit Rate: 25%
├─ Hits: 8,750/min
├─ Misses: 26,250/min
└─ Memory Usage: 2.5 GB

Level 3 (Database):
├─ Queries: 26,250/min
├─ Average Latency: 50ms
└─ Load: 35%

Overall:
├─ Total Hit Rate: 90%
├─ Average Access Time: 8ms
└─ Database Load Reduction: 90%
```

#### 4. **Alerting on Low Hit Rates**

```java
@Component
public class CacheHitRateMonitor {
    private static final double LOW_HIT_RATE_THRESHOLD = 0.7;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void checkHitRates() {
        double l1HitRate = getL1HitRate();
        double l2HitRate = getL2HitRate();
        double overallHitRate = getOverallHitRate();
        
        if (l1HitRate < LOW_HIT_RATE_THRESHOLD) {
            alertService.lowL1HitRate(l1HitRate);
        }
        
        if (l2HitRate < LOW_HIT_RATE_THRESHOLD) {
            alertService.lowL2HitRate(l2HitRate);
        }
        
        if (overallHitRate < LOW_HIT_RATE_THRESHOLD) {
            alertService.lowOverallHitRate(overallHitRate);
        }
    }
}
```

---

## Question 190: What's the cache size limit?

### Answer

### Cache Size Management

#### 1. **Size Limit Configuration**

```java
@Configuration
public class CacheSizeConfiguration {
    // Local cache size limits
    public static final int LOCAL_CACHE_MAX_SIZE = 10_000;
    public static final long LOCAL_CACHE_MAX_WEIGHT = 100_000_000; // 100MB
    
    // Redis cache size limits
    public static final long REDIS_MAX_MEMORY = 4_294_967_296L; // 4GB
    public static final double REDIS_MEMORY_THRESHOLD = 0.9; // 90%
    
    @Bean
    public Cache<String, Conversation> localCache() {
        return Caffeine.newBuilder()
            .maximumSize(LOCAL_CACHE_MAX_SIZE)
            .maximumWeight(LOCAL_CACHE_MAX_WEIGHT)
            .weigher((key, value) -> estimateSize(value))
            .build();
    }
}
```

#### 2. **Memory-Based Limits**

```yaml
# Redis memory configuration
maxmemory: 4gb
maxmemory-policy: allkeys-lru

# Monitor memory usage
maxmemory-samples: 5
```

#### 3. **Dynamic Size Adjustment**

```java
@Service
public class DynamicCacheSizeManager {
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void adjustCacheSize() {
        // Check memory usage
        long usedMemory = getRedisMemoryUsage();
        long maxMemory = getRedisMaxMemory();
        double usageRatio = (double) usedMemory / maxMemory;
        
        if (usageRatio > 0.9) {
            // Memory usage high - reduce TTL
            reduceCacheTTL();
        } else if (usageRatio < 0.5) {
            // Memory usage low - can increase TTL
            increaseCacheTTL();
        }
    }
    
    private void reduceCacheTTL() {
        // Reduce TTL to allow more evictions
        // This is handled by Redis eviction policy
        log.info("Redis memory usage high, eviction policy active");
    }
}
```

#### 4. **Size Monitoring**

```java
@Component
public class CacheSizeMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000)
    public void monitorCacheSize() {
        // Local cache size
        long localCacheSize = getLocalCacheSize();
        Gauge.builder("cache.size")
            .tag("level", "L1")
            .register(meterRegistry)
            .set(localCacheSize);
        
        // Redis cache size
        long redisCacheSize = getRedisCacheSize();
        Gauge.builder("cache.size")
            .tag("level", "L2")
            .register(meterRegistry)
            .set(redisCacheSize);
        
        // Redis memory usage
        long redisMemory = getRedisMemoryUsage();
        Gauge.builder("cache.memory")
            .tag("level", "L2")
            .register(meterRegistry)
            .set(redisMemory);
    }
}
```

#### 5. **Size Limit Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Size Best Practices                      │
└─────────────────────────────────────────────────────────┘

Local Cache (L1):
├─ Size: 10,000 - 100,000 entries
├─ Memory: 100MB - 1GB per instance
├─ Limit: Based on available heap
└─ Eviction: LRU when limit reached

Redis Cache (L2):
├─ Size: 1M - 10M entries
├─ Memory: 2GB - 16GB
├─ Limit: Based on Redis instance size
└─ Eviction: Policy-based (LRU/LFU)

Considerations:
├─ Monitor memory usage
├─ Set appropriate eviction policies
├─ Balance size vs hit rate
└─ Adjust based on access patterns
```

---

## Summary

Part 2 covers:

1. **Cache TTL**: Selection factors, adaptive TTL, configuration by data type
2. **Eviction Policies**: LRU, LFU, TTL-based, hybrid policies
3. **Cache Consistency**: Write-through, event-driven, version-based, distributed consistency
4. **Redis Failure Handling**: Graceful degradation, fallback strategies, recovery
5. **Hit Rate Monitoring**: Metrics collection, calculation, alerting
6. **Cache Size Limits**: Configuration, monitoring, dynamic adjustment

Key principles:
- Configure TTL based on data characteristics and business requirements
- Choose eviction policy based on access patterns
- Implement consistency strategies appropriate for use case
- Handle failures gracefully with fallback mechanisms
- Monitor hit rates and adjust cache configuration
- Set appropriate size limits and monitor memory usage
