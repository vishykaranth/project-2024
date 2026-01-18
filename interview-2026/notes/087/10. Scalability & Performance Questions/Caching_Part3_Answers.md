# Caching - Part 3: Advanced Patterns & Best Practices

## Summary of Caching Questions 181-190

This document consolidates advanced caching patterns and best practices, providing a comprehensive guide to caching strategies.

### Complete Caching Strategy

#### 1. **Multi-Level Cache Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Complete Cache Architecture                    │
└─────────────────────────────────────────────────────────┘

Level 1: Application Cache (Caffeine)
├─ Size: 10,000 entries
├─ TTL: 5 minutes
├─ Policy: LRU
├─ Hit Rate: 60-70%
└─ Access Time: < 1ms

Level 2: Distributed Cache (Redis)
├─ Size: 1M+ entries
├─ TTL: 10 minutes
├─ Policy: LRU
├─ Hit Rate: 25-30%
└─ Access Time: 5-10ms

Level 3: Database
├─ Size: Unlimited
├─ TTL: N/A
├─ Policy: N/A
├─ Hit Rate: 5-10%
└─ Access Time: 50-100ms

Overall Performance:
├─ Total Hit Rate: 85-90%
├─ Average Access Time: < 10ms
└─ Database Load Reduction: 85-90%
```

#### 2. **Complete Cache Service Implementation**

```java
@Service
public class CompleteCacheService {
    // Level 1: Local cache
    private final Cache<String, Conversation> localCache;
    
    // Level 2: Redis cache
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    // Level 3: Database
    private final ConversationRepository repository;
    
    // Health checker
    private final RedisHealthChecker healthChecker;
    
    // Metrics
    private final CacheMetricsCollector metrics;
    
    public Conversation getConversation(String conversationId) {
        String cacheKey = "conv:" + conversationId;
        
        // Level 1: Local cache
        Conversation conversation = localCache.getIfPresent(cacheKey);
        if (conversation != null) {
            metrics.recordHit("L1");
            return conversation;
        }
        
        // Level 2: Redis cache (if available)
        if (healthChecker.isRedisAvailable()) {
            try {
                conversation = redisTemplate.opsForValue().get(cacheKey);
                if (conversation != null) {
                    // Store in L1
                    localCache.put(cacheKey, conversation);
                    metrics.recordHit("L2");
                    return conversation;
                }
            } catch (Exception e) {
                log.warn("Redis access failed", e);
                healthChecker.markRedisUnavailable();
            }
        }
        
        // Level 3: Database
        conversation = repository.findById(conversationId).orElse(null);
        if (conversation != null) {
            // Store in L2 and L1
            if (healthChecker.isRedisAvailable()) {
                try {
                    redisTemplate.opsForValue().set(
                        cacheKey, 
                        conversation, 
                        Duration.ofMinutes(10)
                    );
                } catch (Exception e) {
                    log.warn("Failed to cache in Redis", e);
                }
            }
            localCache.put(cacheKey, conversation);
            metrics.recordMiss("L3");
        }
        
        return conversation;
    }
    
    @EventListener
    public void handleConversationUpdated(ConversationUpdatedEvent event) {
        String cacheKey = "conv:" + event.getConversationId();
        
        // Invalidate all levels
        localCache.invalidate(cacheKey);
        if (healthChecker.isRedisAvailable()) {
            redisTemplate.delete(cacheKey);
            // Notify other instances
            redisTemplate.convertAndSend("cache:invalidate", cacheKey);
        }
        
        // Optionally: Pre-warm with new data
        Conversation updated = repository.findById(event.getConversationId()).orElse(null);
        if (updated != null) {
            cacheConversation(updated);
        }
    }
}
```

### Best Practices Summary

#### 1. **Cache Key Design**

```java
// Good cache key design
public class CacheKeyBuilder {
    public static String conversationKey(String conversationId) {
        return "conv:" + conversationId;
    }
    
    public static String agentKey(String agentId) {
        return "agent:" + agentId;
    }
    
    public static String tenantConversationsKey(String tenantId) {
        return "tenant:" + tenantId + ":conversations";
    }
    
    // Include version for cache invalidation
    public static String versionedKey(String baseKey, long version) {
        return baseKey + ":v" + version;
    }
}
```

#### 2. **Cache Warming Strategy**

```java
@Component
public class ComprehensiveCacheWarming {
    // Startup warming
    @PostConstruct
    public void warmOnStartup() {
        warmFrequentlyAccessedData();
    }
    
    // Scheduled warming
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void warmHourly() {
        warmActiveData();
    }
    
    // Predictive warming
    @Scheduled(cron = "0 0 8 * * *") // 8 AM
    public void warmForPeakHours() {
        warmPredictedData();
    }
    
    private void warmFrequentlyAccessedData() {
        // Load top 1000 most accessed conversations
        List<Conversation> popular = repository
            .findTop1000ByOrderByAccessCountDesc();
        popular.forEach(this::cacheConversation);
    }
}
```

#### 3. **Cache Invalidation Strategy**

```java
@Service
public class ComprehensiveInvalidation {
    // Time-based invalidation
    public void cacheWithTTL(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }
    
    // Event-based invalidation
    @EventListener
    public void invalidateOnUpdate(UpdateEvent event) {
        invalidateCache(event.getKey());
    }
    
    // Pattern-based invalidation
    public void invalidateByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    // Distributed invalidation
    public void invalidateDistributed(String key) {
        localCache.invalidate(key);
        redisTemplate.delete(key);
        redisTemplate.convertAndSend("cache:invalidate", key);
    }
}
```

#### 4. **Cache Monitoring Dashboard**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Performance Dashboard                    │
└─────────────────────────────────────────────────────────┘

Hit Rates:
├─ L1 Hit Rate: 65% (Target: > 60%)
├─ L2 Hit Rate: 25% (Target: > 20%)
├─ Overall Hit Rate: 90% (Target: > 85%)
└─ Status: ✅ Healthy

Performance:
├─ L1 Access Time: 0.5ms (P95: 1ms)
├─ L2 Access Time: 8ms (P95: 15ms)
├─ DB Access Time: 50ms (P95: 100ms)
└─ Average Access Time: 8ms

Memory Usage:
├─ L1 Memory: 80MB / 100MB (80%)
├─ L2 Memory: 2.5GB / 4GB (62.5%)
└─ Status: ✅ Healthy

Evictions:
├─ L1 Evictions: 1,000/min
├─ L2 Evictions: 500/min
└─ Status: ✅ Normal

Errors:
├─ Redis Failures: 0 (Last hour)
├─ Cache Errors: 0 (Last hour)
└─ Status: ✅ Healthy
```

### Common Pitfalls and Solutions

#### 1. **Cache Stampede**

**Problem:** Multiple requests for same data when cache expires

**Solution:**
- Use distributed locks
- Implement probabilistic early expiration
- Use stale-while-revalidate pattern
- Request coalescing

#### 2. **Cache Invalidation Complexity**

**Problem:** Keeping cache consistent with database

**Solution:**
- Event-driven invalidation
- Version-based consistency
- Write-through pattern
- Distributed invalidation via pub/sub

#### 3. **Memory Exhaustion**

**Problem:** Cache grows too large, causes OOM

**Solution:**
- Set appropriate size limits
- Use eviction policies (LRU/LFU)
- Monitor memory usage
- Implement TTL-based expiration

#### 4. **Stale Data**

**Problem:** Cache serves outdated data

**Solution:**
- Appropriate TTL configuration
- Event-based invalidation
- Version-based consistency
- Write-through pattern

### Performance Optimization

#### 1. **Cache Hit Rate Optimization**

```java
// Strategies to improve hit rate
public class HitRateOptimization {
    // 1. Increase cache size
    public void increaseCacheSize() {
        // More entries = higher hit rate
        // But more memory usage
    }
    
    // 2. Increase TTL
    public void increaseTTL() {
        // Longer TTL = higher hit rate
        // But potentially stale data
    }
    
    // 3. Better cache warming
    public void improveWarming() {
        // Warm frequently accessed data
        // Predictive warming
    }
    
    // 4. Better key design
    public void optimizeKeys() {
        // Use consistent key patterns
        // Include relevant identifiers
    }
}
```

#### 2. **Access Time Optimization**

```java
// Strategies to reduce access time
public class AccessTimeOptimization {
    // 1. Optimize local cache
    public void optimizeLocalCache() {
        // Use efficient data structures
        // Minimize serialization overhead
    }
    
    // 2. Reduce network latency
    public void optimizeNetwork() {
        // Use Redis cluster close to services
        // Minimize round trips
    }
    
    // 3. Batch operations
    public void batchOperations() {
        // Batch cache reads/writes
        // Use pipeline for Redis
    }
}
```

### Complete Configuration Example

```java
@Configuration
public class CompleteCacheConfiguration {
    // Level 1: Local cache
    @Bean
    public Cache<String, Conversation> localCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .maximumWeight(100_000_000) // 100MB
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .recordStats()
            .removalListener((key, value, cause) -> {
                log.debug("Cache eviction: key={}, cause={}", key, cause);
            })
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
    
    // Redis connection factory with cluster support
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration();
        clusterConfig.setClusterNodes(Arrays.asList(
            new RedisNode("redis-1", 6379),
            new RedisNode("redis-2", 6379),
            new RedisNode("redis-3", 6379)
        ));
        return new JedisConnectionFactory(clusterConfig);
    }
}
```

---

## Summary

Part 3 consolidates all caching concepts:

1. **Complete Architecture**: Multi-level cache with proper configuration
2. **Best Practices**: Key design, warming, invalidation, monitoring
3. **Common Pitfalls**: Stampede, invalidation, memory, stale data
4. **Performance Optimization**: Hit rate and access time improvements
5. **Complete Configuration**: Production-ready setup

Key takeaways:
- Use multi-level caching for optimal performance
- Implement proper invalidation strategies
- Monitor hit rates and adjust configuration
- Handle failures gracefully
- Optimize for both hit rate and access time
- Use appropriate eviction policies and TTL

Complete Caching Strategy:
- **Level 1 (Local)**: Fast, small, per-instance
- **Level 2 (Redis)**: Fast, large, shared
- **Level 3 (Database)**: Slow, unlimited, persistent
- **Overall**: 85-90% hit rate, < 10ms average access time
