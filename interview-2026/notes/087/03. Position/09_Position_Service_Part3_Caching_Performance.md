# Position Service - Part 3: Caching and Performance

## Question 79: Explain the caching strategy for positions (Redis + Database).

### Answer

### Multi-Level Caching Strategy

The Position Service uses a multi-level caching strategy to optimize performance while maintaining data consistency and accuracy.

#### 1. **Caching Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Caching Architecture               │
└─────────────────────────────────────────────────────────┘

Level 1: In-Memory Cache (Caffeine)
├─ Fastest access (< 1ms)
├─ Per-instance cache
├─ Limited size
└─ TTL: 5 minutes

Level 2: Redis Cache
├─ Fast access (5-10ms)
├─ Shared across instances
├─ Larger size
└─ TTL: 1 hour

Level 3: Database
├─ Slowest access (50-100ms)
├─ Persistent storage
├─ Unlimited size
└─ Source of truth
```

#### 2. **Cache Implementation**

```java
@Service
public class PositionCacheService {
    // Level 1: In-Memory Cache (Caffeine)
    private final Cache<String, Position> localCache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .recordStats()
        .build();
    
    // Level 2: Redis Cache
    private final RedisTemplate<String, Position> redisTemplate;
    
    // Level 3: Database
    private final PositionRepository positionRepository;
    
    public Position getPosition(String accountId, String instrumentId) {
        String key = "position:" + accountId + ":" + instrumentId;
        
        // Level 1: Try local cache
        Position position = localCache.getIfPresent(key);
        if (position != null) {
            return position;
        }
        
        // Level 2: Try Redis cache
        position = redisTemplate.opsForValue().get(key);
        if (position != null) {
            // Populate local cache
            localCache.put(key, position);
            return position;
        }
        
        // Level 3: Load from database
        position = positionRepository
            .findByAccountIdAndInstrumentId(accountId, instrumentId)
            .orElse(Position.zero(accountId, instrumentId));
        
        // Populate caches
        if (position.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
            redisTemplate.opsForValue().set(key, position, Duration.ofHours(1));
            localCache.put(key, position);
        }
        
        return position;
    }
}
```

#### 3. **Cache Update Strategy**

```java
@Service
public class PositionCacheUpdateService {
    private final RedisTemplate<String, Position> redisTemplate;
    private final Cache<String, Position> localCache;
    
    public void updatePosition(Position position) {
        String key = "position:" + position.getAccountId() + ":" + position.getInstrumentId();
        
        // 1. Update database (source of truth)
        positionRepository.save(position);
        
        // 2. Update Redis cache
        redisTemplate.opsForValue().set(key, position, Duration.ofHours(1));
        
        // 3. Update local cache
        localCache.put(key, position);
        
        // 4. Invalidate other instances' local cache
        invalidateOtherInstances(key);
    }
    
    private void invalidateOtherInstances(String key) {
        // Publish cache invalidation event
        CacheInvalidationEvent event = CacheInvalidationEvent.builder()
            .cacheKey(key)
            .timestamp(Instant.now())
            .build();
        
        redisTemplate.convertAndSend("cache:invalidation", serialize(event));
    }
}
```

#### 4. **Cache Warming**

```java
@Service
public class PositionCacheWarmingService {
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void warmCache() {
        // Get top 1000 most frequently accessed positions
        List<String> topPositions = getTopAccessedPositions(1000);
        
        for (String positionKey : topPositions) {
            String[] parts = positionKey.split(":");
            String accountId = parts[0];
            String instrumentId = parts[1];
            
            // Pre-load into cache
            Position position = positionRepository
                .findByAccountIdAndInstrumentId(accountId, instrumentId)
                .orElse(null);
            
            if (position != null) {
                redisTemplate.opsForValue().set(
                    positionKey, 
                    position, 
                    Duration.ofHours(1)
                );
            }
        }
    }
    
    private List<String> getTopAccessedPositions(int limit) {
        // Get from access log or metrics
        return accessLogService.getTopAccessedPositions(limit);
    }
}
```

#### 5. **Cache Statistics**

```java
@Service
public class PositionCacheMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordCacheHit(String level) {
        Counter.builder("position.cache.hits")
            .tag("level", level)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordCacheMiss(String level) {
        Counter.builder("position.cache.misses")
            .tag("level", level)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordCacheAccess(String level, Duration duration) {
        Timer.builder("position.cache.access.duration")
            .tag("level", level)
            .register(meterRegistry)
            .record(duration);
    }
    
    public double getCacheHitRate(String level) {
        long hits = meterRegistry.counter("position.cache.hits", "level", level).count();
        long misses = meterRegistry.counter("position.cache.misses", "level", level).count();
        
        if (hits + misses == 0) {
            return 0.0;
        }
        
        return (double) hits / (hits + misses);
    }
}
```

---

## Question 81: How do you handle high query volume for positions?

### Answer

### High Query Volume Handling

#### 1. **Query Volume Analysis**

```
┌─────────────────────────────────────────────────────────┐
│         Query Volume Requirements                      │
└─────────────────────────────────────────────────────────┘

Peak Query Volume:
├─ 10,000+ queries per second
├─ Real-time position queries
├─ Batch position queries
└─ Historical position queries

Query Types:
├─ Single position query (80%)
├─ All positions for account (15%)
├─ Position history (4%)
└─ Position aggregation (1%)
```

#### 2. **Query Optimization Strategy**

```java
@Service
public class PositionQueryService {
    private final PositionCacheService cacheService;
    private final PositionRepository positionRepository;
    private final RedisTemplate<String, Position> redisTemplate;
    
    // Single Position Query (Most Common - 80%)
    public Position getPosition(String accountId, String instrumentId) {
        // Use multi-level cache
        return cacheService.getPosition(accountId, instrumentId);
    }
    
    // All Positions for Account (15%)
    public List<Position> getAllPositionsForAccount(String accountId) {
        String cacheKey = "positions:account:" + accountId;
        
        // Try Redis cache first
        List<Position> positions = redisTemplate.opsForList()
            .range(cacheKey, 0, -1);
        
        if (positions != null && !positions.isEmpty()) {
            return positions;
        }
        
        // Load from database
        positions = positionRepository.findByAccountId(accountId);
        
        // Cache for 5 minutes
        if (!positions.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(cacheKey, positions);
            redisTemplate.expire(cacheKey, Duration.ofMinutes(5));
        }
        
        return positions;
    }
    
    // Batch Position Query
    public Map<String, Position> getPositionsBatch(List<String> positionKeys) {
        Map<String, Position> results = new HashMap<>();
        List<String> missingKeys = new ArrayList<>();
        
        // Try Redis pipeline for batch get
        List<Object> cached = redisTemplate.executePipelined(
            (RedisCallback<Object>) connection -> {
                for (String key : positionKeys) {
                    connection.get(key.getBytes());
                }
                return null;
            }
        );
        
        // Process cached results
        for (int i = 0; i < cached.size(); i++) {
            if (cached.get(i) != null) {
                results.put(positionKeys.get(i), (Position) cached.get(i));
            } else {
                missingKeys.add(positionKeys.get(i));
            }
        }
        
        // Load missing from database
        if (!missingKeys.isEmpty()) {
            List<Position> missing = positionRepository
                .findByPositionIds(missingKeys);
            
            for (Position position : missing) {
                String key = "position:" + position.getAccountId() + 
                             ":" + position.getInstrumentId();
                results.put(key, position);
                
                // Cache for future
                redisTemplate.opsForValue().set(key, position, Duration.ofHours(1));
            }
        }
        
        return results;
    }
}
```

#### 3. **Database Query Optimization**

```java
@Repository
public interface PositionRepository extends JpaRepository<Position, String> {
    // Optimized query with index
    @Query("SELECT p FROM Position p WHERE p.accountId = :accountId " +
           "AND p.instrumentId = :instrumentId")
    Optional<Position> findByAccountIdAndInstrumentId(
        @Param("accountId") String accountId,
        @Param("instrumentId") String instrumentId
    );
    
    // Batch query with IN clause
    @Query("SELECT p FROM Position p WHERE p.positionId IN :positionIds")
    List<Position> findByPositionIds(@Param("positionIds") List<String> positionIds);
    
    // Account positions with pagination
    @Query("SELECT p FROM Position p WHERE p.accountId = :accountId " +
           "ORDER BY p.instrumentId")
    Page<Position> findByAccountId(@Param("accountId") String accountId, Pageable pageable);
}

// Database indexes
@Table(name = "positions", indexes = {
    @Index(name = "idx_account_instrument", columnList = "accountId,instrumentId"),
    @Index(name = "idx_account", columnList = "accountId"),
    @Index(name = "idx_last_updated", columnList = "lastUpdated")
})
public class Position {
    // ...
}
```

#### 4. **Read Replica Strategy**

```java
@Configuration
public class DatabaseConfiguration {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        // Master database for writes
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://master-db:5432/positions")
            .build();
    }
    
    @Bean
    public DataSource readReplicaDataSource() {
        // Read replica for reads
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://replica-db:5432/positions")
            .build();
    }
    
    @Bean
    public DataSource routingDataSource() {
        RoutingDataSource routing = new RoutingDataSource();
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("write", primaryDataSource());
        dataSources.put("read", readReplicaDataSource());
        routing.setTargetDataSources(dataSources);
        routing.setDefaultTargetDataSource(primaryDataSource());
        return routing;
    }
}

@Service
public class PositionQueryService {
    @Transactional(readOnly = true)
    @ReadOnly
    public Position getPosition(String accountId, String instrumentId) {
        // Uses read replica
        return positionRepository.findByAccountIdAndInstrumentId(accountId, instrumentId)
            .orElse(Position.zero(accountId, instrumentId));
    }
    
    @Transactional
    public Position updatePosition(Position position) {
        // Uses master database
        return positionRepository.save(position);
    }
}
```

#### 5. **Query Result Caching**

```java
@Service
public class PositionQueryCacheService {
    private final RedisTemplate<String, Position> redisTemplate;
    
    // Cache query results with TTL
    public Position getPositionWithCache(String accountId, String instrumentId) {
        String cacheKey = "position:query:" + accountId + ":" + instrumentId;
        
        // Try cache
        Position cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Query database
        Position position = positionRepository
            .findByAccountIdAndInstrumentId(accountId, instrumentId)
            .orElse(Position.zero(accountId, instrumentId));
        
        // Cache result
        redisTemplate.opsForValue().set(cacheKey, position, Duration.ofMinutes(5));
        
        return position;
    }
    
    // Invalidate cache on update
    public void invalidatePositionCache(String accountId, String instrumentId) {
        String cacheKey = "position:query:" + accountId + ":" + instrumentId;
        redisTemplate.delete(cacheKey);
        
        // Also invalidate account-level cache
        String accountCacheKey = "positions:account:" + accountId;
        redisTemplate.delete(accountCacheKey);
    }
}
```

#### 6. **Query Performance Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Query Performance Targets                     │
└─────────────────────────────────────────────────────────┘

Single Position Query:
├─ Cache hit: < 5ms (P95)
├─ Cache miss: < 50ms (P95)
└─ Database query: < 100ms (P95)

Batch Position Query (100 positions):
├─ Cache hit: < 20ms (P95)
├─ Cache miss: < 200ms (P95)
└─ Database query: < 500ms (P95)

All Positions for Account:
├─ Cache hit: < 10ms (P95)
├─ Cache miss: < 100ms (P95)
└─ Database query: < 300ms (P95)
```

---

## Summary

Position Service Part 3 covers:

1. **Multi-Level Caching**: In-memory (Caffeine) → Redis → Database for optimal performance
2. **Cache Update Strategy**: Write-through cache with invalidation
3. **Cache Warming**: Pre-loading frequently accessed positions
4. **High Query Volume**: Batch queries, read replicas, query optimization
5. **Performance Metrics**: Cache hit rates and query latency targets

Key takeaways:
- Multi-level caching reduces database load by 90%+
- Cache hit rate target: 80%+ for single position queries
- Read replicas handle read-heavy workloads
- Batch queries optimize database round trips
- Query optimization through indexes and efficient queries
