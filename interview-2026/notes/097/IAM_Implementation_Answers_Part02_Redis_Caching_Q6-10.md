# IAM Implementation Answers - Part 2: Redis Caching Strategy (Questions 6-10)

## Question 6: How did you use Redis caching for permission evaluation?

### Answer

### Redis Caching for Permission Evaluation

#### 1. **Caching Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Redis Caching Architecture                     │
└─────────────────────────────────────────────────────────┘

Permission Request
    │
    ▼
Permission Service
    │
    ├─► Check Redis Cache
    │   ├─► Cache Hit? → Return Result (< 1ms)
    │   └─► Cache Miss? → Continue
    │
    └─► Evaluate with Trie
        │
        └─► Store in Redis
            └─► Return Result
```

#### 2. **Redis Implementation**

```java
@Service
public class RedisPermissionCache {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    
    private static final String CACHE_PREFIX = "perm:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    private static final Duration LONG_TTL = Duration.ofDays(1);
    
    /**
     * Get permission from cache
     */
    public PermissionResult getPermission(String cacheKey) {
        String redisKey = CACHE_PREFIX + cacheKey;
        return redisTemplate.opsForValue().get(redisKey);
    }
    
    /**
     * Cache permission result
     */
    public void cachePermission(String cacheKey, PermissionResult result) {
        String redisKey = CACHE_PREFIX + cacheKey;
        
        // Determine TTL based on result
        Duration ttl = result == PermissionResult.ALLOW 
            ? LONG_TTL  // Allow permissions cached longer
            : DEFAULT_TTL; // Deny permissions cached shorter
        
        redisTemplate.opsForValue().set(redisKey, result, ttl);
    }
    
    /**
     * Build cache key
     */
    private String buildCacheKey(String userId, String resource, String action) {
        return String.format("%s:%s:%s", userId, resource, action);
    }
}
```

#### 3. **Multi-Level Caching Strategy**

```java
@Service
public class MultiLevelPermissionCache {
    // L1: Local cache (Caffeine)
    private final Cache<String, PermissionResult> localCache;
    
    // L2: Redis cache (distributed)
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    
    // L3: Trie (in-memory structure)
    private final PermissionTrie permissionTrie;
    
    public PermissionResult evaluate(String userId, String resource, String action) {
        String cacheKey = buildCacheKey(userId, resource, action);
        
        // L1: Check local cache
        PermissionResult result = localCache.getIfPresent(cacheKey);
        if (result != null) {
            return result; // < 0.1ms
        }
        
        // L2: Check Redis cache
        result = redisTemplate.opsForValue()
            .get("perm:" + cacheKey);
        if (result != null) {
            localCache.put(cacheKey, result); // Populate L1
            return result; // < 1ms
        }
        
        // L3: Evaluate with Trie
        result = permissionTrie.search(buildPermissionPath(userId, resource, action));
        
        // Cache in both levels
        if (result != null) {
            redisTemplate.opsForValue()
                .set("perm:" + cacheKey, result, Duration.ofHours(1));
            localCache.put(cacheKey, result);
        }
        
        return result;
    }
}
```

#### 4. **Cache Key Design**

```java
/**
 * Cache key structure
 * 
 * Format: "perm:userId:resource:action"
 * Example: "perm:user123:trade:read"
 * 
 * Benefits:
 * - Simple and readable
 * - Easy to invalidate
 * - Supports pattern matching
 */
public class CacheKeyBuilder {
    public static String buildKey(String userId, String resource, String action) {
        return String.format("perm:%s:%s:%s", userId, resource, action);
    }
    
    public static String buildPattern(String userId, String resource) {
        return String.format("perm:%s:%s:*", userId, resource);
    }
    
    public static String buildUserPattern(String userId) {
        return String.format("perm:%s:*", userId);
    }
}
```

---

## Question 7: What caching strategy did you implement (cache-aside, write-through, write-behind)?

### Answer

### Caching Strategy Implementation

#### 1. **Cache-Aside Pattern (Chosen Strategy)**

```
┌─────────────────────────────────────────────────────────┐
│         Cache-Aside Pattern                            │
└─────────────────────────────────────────────────────────┘

Read Flow:
1. Check cache
   ├─ Hit? → Return result
   └─ Miss? → Continue
2. Load from database/trie
3. Store in cache
4. Return result

Write Flow:
1. Update database/trie
2. Invalidate cache
3. Return success
```

#### 2. **Cache-Aside Implementation**

```java
@Service
public class CacheAsidePermissionService {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    private final PermissionTrie permissionTrie;
    private final PermissionRepository repository;
    
    /**
     * Read with cache-aside
     */
    public PermissionResult getPermission(String userId, String resource, String action) {
        String cacheKey = buildCacheKey(userId, resource, action);
        
        // 1. Check cache
        PermissionResult cached = redisTemplate.opsForValue()
            .get("perm:" + cacheKey);
        if (cached != null) {
            return cached; // Cache hit
        }
        
        // 2. Load from source (trie or database)
        PermissionResult result = loadPermission(userId, resource, action);
        
        // 3. Store in cache
        if (result != null) {
            redisTemplate.opsForValue()
                .set("perm:" + cacheKey, result, Duration.ofHours(1));
        }
        
        // 4. Return result
        return result;
    }
    
    /**
     * Write with cache-aside
     */
    public void updatePermission(String userId, String resource, 
                                 String action, PermissionResult result) {
        // 1. Update source (database)
        repository.save(new Permission(userId, resource, action, result));
        
        // 2. Update trie
        permissionTrie.insert(
            buildPermissionPath(userId, resource, action), 
            result);
        
        // 3. Invalidate cache
        String cacheKey = buildCacheKey(userId, resource, action);
        redisTemplate.delete("perm:" + cacheKey);
    }
}
```

#### 3. **Why Cache-Aside?**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Strategy Comparison                      │
└─────────────────────────────────────────────────────────┘

Cache-Aside (Chosen):
├─ Pros:
│   ├─ Simple to implement
│   ├─ Works with any data source
│   ├─ Cache failures don't break system
│   └─ Flexible cache invalidation
├─ Cons:
│   ├─ Cache miss penalty (2 operations)
│   └─ Possible stale data if not invalidated
└─ Use case: Read-heavy, flexible writes ✓

Write-Through:
├─ Pros:
│   ├─ Always consistent
│   └─ No cache miss on read
├─ Cons:
│   ├─ Write latency (always writes to cache + DB)
│   └─ Cache failures break writes
└─ Use case: Write-heavy, consistency critical

Write-Behind:
├─ Pros:
│   ├─ Fast writes
│   └─ Batch database writes
├─ Cons:
│   ├─ Data loss risk
│   ├─ Complex implementation
│   └─ Eventual consistency
└─ Use case: Write-heavy, can tolerate delay

For IAM: Cache-Aside is best because:
- Read-heavy workload (permission checks)
- Flexible write patterns
- Need cache failure resilience
```

#### 4. **Cache Invalidation Strategy**

```java
/**
 * Smart cache invalidation
 */
@Service
public class CacheInvalidationService {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    
    /**
     * Invalidate specific permission
     */
    public void invalidatePermission(String userId, String resource, String action) {
        String key = buildCacheKey(userId, resource, action);
        redisTemplate.delete("perm:" + key);
    }
    
    /**
     * Invalidate all permissions for user
     */
    public void invalidateUserPermissions(String userId) {
        String pattern = "perm:" + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    /**
     * Invalidate all permissions for resource
     */
    public void invalidateResourcePermissions(String resource) {
        String pattern = "perm:*:" + resource + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    /**
     * Invalidate with TTL (lazy invalidation)
     */
    public void setPermissionWithTTL(String key, PermissionResult result, Duration ttl) {
        // TTL ensures automatic invalidation
        redisTemplate.opsForValue().set("perm:" + key, result, ttl);
    }
}
```

---

## Question 8: How did you handle cache invalidation for permissions?

### Answer

### Cache Invalidation Strategy

#### 1. **Invalidation Approaches**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Invalidation Strategies                  │
└─────────────────────────────────────────────────────────┘

1. Time-Based (TTL)
   ├─ Automatic expiration
   ├─ Simple to implement
   └─ May serve stale data

2. Event-Based
   ├─ Invalidate on permission changes
   ├─ Always fresh data
   └─ Requires event handling

3. Version-Based
   ├─ Cache version with data
   ├─ Check version on read
   └─ Invalidate if version mismatch

4. Hybrid (Used)
   ├─ TTL for automatic cleanup
   ├─ Event-based for immediate updates
   └─ Best of both worlds
```

#### 2. **Event-Based Invalidation**

```java
@Service
public class PermissionCacheInvalidation {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Invalidate on permission update
     */
    @EventListener
    public void handlePermissionUpdated(PermissionUpdatedEvent event) {
        Permission permission = event.getPermission();
        
        // Invalidate specific permission
        invalidatePermission(
            permission.getUserId(),
            permission.getResource(),
            permission.getAction()
        );
        
        // Invalidate related permissions
        invalidateRelatedPermissions(permission);
    }
    
    /**
     * Invalidate related permissions
     */
    private void invalidateRelatedPermissions(Permission permission) {
        // Invalidate wildcard permissions
        String wildcardKey = buildCacheKey(
            permission.getUserId(),
            permission.getResource(),
            "*"
        );
        redisTemplate.delete("perm:" + wildcardKey);
        
        // Invalidate role-based permissions if applicable
        if (permission.isRoleBased()) {
            invalidateRolePermissions(permission.getRole());
        }
    }
}
```

#### 3. **TTL-Based Invalidation**

```java
/**
 * TTL-based invalidation with different TTLs
 */
public class TTLBasedInvalidation {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    
    public void cachePermission(String key, PermissionResult result, 
                               PermissionType type) {
        Duration ttl = determineTTL(type, result);
        redisTemplate.opsForValue().set("perm:" + key, result, ttl);
    }
    
    private Duration determineTTL(PermissionType type, PermissionResult result) {
        // Different TTLs based on permission type
        if (type == PermissionType.STATIC) {
            return Duration.ofDays(1); // Static permissions cached longer
        } else if (type == PermissionType.DYNAMIC) {
            return Duration.ofMinutes(15); // Dynamic permissions cached shorter
        } else if (result == PermissionResult.DENY) {
            return Duration.ofHours(1); // Deny results cached shorter
        } else {
            return Duration.ofHours(6); // Default TTL
        }
    }
}
```

#### 4. **Version-Based Invalidation**

```java
/**
 * Version-based cache invalidation
 */
public class VersionBasedCache {
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String VERSION_KEY = "perm:version";
    
    /**
     * Get permission with version check
     */
    public PermissionResult getPermission(String key, long clientVersion) {
        // Get current version
        long serverVersion = getCurrentVersion();
        
        // Check version
        if (clientVersion < serverVersion) {
            // Version mismatch - cache invalid
            return null; // Force reload
        }
        
        // Version matches - return cached
        return getCachedPermission(key);
    }
    
    /**
     * Invalidate by incrementing version
     */
    public void invalidateAll() {
        redisTemplate.opsForValue().increment(VERSION_KEY);
        // All cached permissions become invalid
    }
}
```

#### 5. **Hybrid Invalidation Strategy**

```java
/**
 * Hybrid invalidation: TTL + Events
 */
@Service
public class HybridCacheInvalidation {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    
    /**
     * Cache with TTL and event-based invalidation
     */
    public void cachePermission(String key, PermissionResult result) {
        // Cache with TTL (safety net)
        redisTemplate.opsForValue().set(
            "perm:" + key, 
            result, 
            Duration.ofHours(1)
        );
        
        // Also set expiration listener
        setupExpirationListener(key);
    }
    
    /**
     * Invalidate on permission change (event-based)
     */
    @EventListener
    public void onPermissionChanged(PermissionChangedEvent event) {
        // Immediate invalidation
        invalidatePermission(event.getPermissionKey());
        
        // Publish invalidation event to other instances
        publishInvalidationEvent(event.getPermissionKey());
    }
    
    /**
     * Publish invalidation to other instances
     */
    private void publishInvalidationEvent(String key) {
        // Use Redis pub/sub for distributed invalidation
        redisTemplate.convertAndSend("cache:invalidate", key);
    }
    
    /**
     * Listen for invalidation events
     */
    @RedisListener(channel = "cache:invalidate")
    public void handleInvalidation(String key) {
        redisTemplate.delete("perm:" + key);
    }
}
```

---

## Question 9: What data did you cache in Redis, and why?

### Answer

### Redis Cache Data Strategy

#### 1. **Cached Data Types**

```
┌─────────────────────────────────────────────────────────┐
│         Cached Data in Redis                           │
└─────────────────────────────────────────────────────────┘

1. Permission Results (Primary)
   ├─ Key: "perm:userId:resource:action"
   ├─ Value: ALLOW/DENY
   ├─ TTL: 1 hour
   └─ Why: Most frequently accessed

2. User Permissions (Aggregated)
   ├─ Key: "perm:user:userId"
   ├─ Value: Set of permissions
   ├─ TTL: 30 minutes
   └─ Why: Batch permission checks

3. Role Permissions
   ├─ Key: "perm:role:roleName"
   ├─ Value: Set of permissions
   ├─ TTL: 1 hour
   └─ Why: Role-based access control

4. Permission Metadata
   ├─ Key: "perm:meta:userId"
   ├─ Value: Permission metadata
   ├─ TTL: 1 hour
   └─ Why: Permission context
```

#### 2. **Permission Results (Primary Cache)**

```java
/**
 * Primary cache: Individual permission results
 */
public class PermissionResultCache {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    
    /**
     * Cache individual permission check result
     * 
     * Key: "perm:userId:resource:action"
     * Value: ALLOW or DENY
     * TTL: 1 hour
     */
    public void cacheResult(String userId, String resource, 
                           String action, PermissionResult result) {
        String key = String.format("perm:%s:%s:%s", userId, resource, action);
        redisTemplate.opsForValue().set(key, result, Duration.ofHours(1));
    }
    
    // Why cache this:
    // - Most frequent access pattern
    // - Simple key-value lookup
    // - Fast retrieval (< 1ms)
    // - Reduces database/trie queries
}
```

#### 3. **User Permissions (Aggregated Cache)**

```java
/**
 * Aggregated cache: All permissions for a user
 */
public class UserPermissionsCache {
    private final RedisTemplate<String, Set<String>> redisTemplate;
    
    /**
     * Cache all permissions for a user
     * 
     * Key: "perm:user:userId"
     * Value: Set of "resource:action" strings
     * TTL: 30 minutes
     */
    public void cacheUserPermissions(String userId, Set<Permission> permissions) {
        String key = "perm:user:" + userId;
        Set<String> permissionStrings = permissions.stream()
            .map(p -> p.getResource() + ":" + p.getAction())
            .collect(Collectors.toSet());
        
        redisTemplate.opsForValue().set(key, permissionStrings, Duration.ofMinutes(30));
    }
    
    // Why cache this:
    // - Batch permission checks
    // - User permission listing
    // - Reduces multiple queries
    // - Faster user permission overview
}
```

#### 4. **Role Permissions Cache**

```java
/**
 * Role permissions cache
 */
public class RolePermissionsCache {
    private final RedisTemplate<String, Set<String>> redisTemplate;
    
    /**
     * Cache permissions for a role
     * 
     * Key: "perm:role:roleName"
     * Value: Set of permissions
     * TTL: 1 hour
     */
    public void cacheRolePermissions(String roleName, Set<Permission> permissions) {
        String key = "perm:role:" + roleName;
        Set<String> permissionStrings = permissions.stream()
            .map(p -> p.getResource() + ":" + p.getAction())
            .collect(Collectors.toSet());
        
        redisTemplate.opsForValue().set(key, permissionStrings, Duration.ofHours(1));
    }
    
    // Why cache this:
    // - Role-based access control
    // - Multiple users share same role
    // - High cache hit rate
    // - Reduces role permission queries
}
```

#### 5. **Cache Data Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Redis Cache Structure                         │
└─────────────────────────────────────────────────────────┘

Redis Keys:
├─ perm:userId:resource:action
│   └─ Value: ALLOW/DENY
│   └─ TTL: 1 hour
│   └─ Usage: Individual permission checks
│
├─ perm:user:userId
│   └─ Value: Set of permissions
│   └─ TTL: 30 minutes
│   └─ Usage: User permission listing
│
├─ perm:role:roleName
│   └─ Value: Set of permissions
│   └─ TTL: 1 hour
│   └─ Usage: Role permission checks
│
└─ perm:meta:userId
    └─ Value: Permission metadata JSON
    └─ TTL: 1 hour
    └─ Usage: Permission context
```

#### 6. **Why These Data Types?**

```
┌─────────────────────────────────────────────────────────┐
│         Caching Rationale                             │
└─────────────────────────────────────────────────────────┘

Permission Results:
├─ Why: Most frequent access (every API call)
├─ Benefit: 80% cache hit rate
├─ Impact: 70% latency reduction
└─ Trade-off: Memory vs Performance ✓

User Permissions:
├─ Why: Batch operations, user overview
├─ Benefit: Reduces N queries to 1
├─ Impact: Faster user permission checks
└─ Trade-off: Larger cache entries

Role Permissions:
├─ Why: Shared across multiple users
├─ Benefit: High cache reuse
├─ Impact: Reduces role queries
└─ Trade-off: Stale data risk (mitigated by TTL)
```

---

## Question 10: How did you ensure cache consistency across multiple instances?

### Answer

### Cache Consistency Across Instances

#### 1. **Consistency Challenge**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Consistency Challenge                    │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Multiple IAM service instances
├─ Each has local cache (Caffeine)
├─ Shared Redis cache
└─ Need consistency across instances

Challenge:
├─ Instance A updates permission
├─ Instance B still has old cached value
└─ Inconsistent state
```

#### 2. **Consistency Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Consistency Strategy                     │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Redis as source of truth (shared cache)
├─ Local cache with shorter TTL
├─ Event-based invalidation
├─ Version-based consistency
└─ Distributed invalidation
```

#### 3. **Distributed Cache Invalidation**

```java
/**
 * Distributed cache invalidation using Redis pub/sub
 */
@Service
public class DistributedCacheInvalidation {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    private final Cache<String, PermissionResult> localCache;
    private final RedisMessageListenerContainer messageListenerContainer;
    
    @PostConstruct
    public void setupInvalidationListener() {
        // Subscribe to invalidation channel
        messageListenerContainer.addMessageListener(
            new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {
                    String cacheKey = new String(message.getBody());
                    // Invalidate local cache
                    localCache.invalidate(cacheKey);
                }
            },
            new ChannelTopic("cache:invalidate")
        );
    }
    
    /**
     * Invalidate cache across all instances
     */
    public void invalidateDistributed(String cacheKey) {
        // 1. Invalidate Redis cache
        redisTemplate.delete("perm:" + cacheKey);
        
        // 2. Invalidate local cache
        localCache.invalidate(cacheKey);
        
        // 3. Publish invalidation event
        redisTemplate.convertAndSend("cache:invalidate", cacheKey);
    }
}
```

#### 4. **Version-Based Consistency**

```java
/**
 * Version-based cache consistency
 */
@Service
public class VersionBasedConsistency {
    private final RedisTemplate<String, String> redisTemplate;
    private final Cache<String, CachedPermission> localCache;
    
    private static final String VERSION_KEY = "perm:version";
    
    /**
     * Get permission with version check
     */
    public PermissionResult getPermission(String key) {
        // Get from local cache
        CachedPermission cached = localCache.getIfPresent(key);
        if (cached != null) {
            // Check version
            long currentVersion = getCurrentVersion();
            if (cached.getVersion() == currentVersion) {
                return cached.getResult(); // Version matches
            }
            // Version mismatch - invalidate
            localCache.invalidate(key);
        }
        
        // Get from Redis
        CachedPermission redisCached = getFromRedis(key);
        if (redisCached != null) {
            localCache.put(key, redisCached);
            return redisCached.getResult();
        }
        
        // Load from source
        return loadAndCache(key);
    }
    
    /**
     * Invalidate by incrementing version
     */
    public void invalidateAll() {
        redisTemplate.opsForValue().increment(VERSION_KEY);
        // All caches become invalid
    }
    
    private long getCurrentVersion() {
        String version = redisTemplate.opsForValue().get(VERSION_KEY);
        return version != null ? Long.parseLong(version) : 0;
    }
}

class CachedPermission {
    private PermissionResult result;
    private long version;
    // Getters and setters
}
```

#### 5. **TTL-Based Consistency**

```java
/**
 * TTL-based consistency with shorter local TTL
 */
@Service
public class TTLBasedConsistency {
    private final Cache<String, PermissionResult> localCache;
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    
    // Local cache: 5 minutes TTL
    // Redis cache: 1 hour TTL
    // Ensures local cache refreshes more frequently
    
    public PermissionResult getPermission(String key) {
        // Check local cache (short TTL)
        PermissionResult local = localCache.getIfPresent(key);
        if (local != null) {
            return local;
        }
        
        // Check Redis cache (longer TTL)
        PermissionResult redis = redisTemplate.opsForValue()
            .get("perm:" + key);
        if (redis != null) {
            // Populate local cache
            localCache.put(key, redis);
            return redis;
        }
        
        // Load from source
        PermissionResult result = loadFromSource(key);
        
        // Cache in both
        redisTemplate.opsForValue()
            .set("perm:" + key, result, Duration.ofHours(1));
        localCache.put(key, result);
        
        return result;
    }
}
```

#### 6. **Event-Driven Consistency**

```java
/**
 * Event-driven cache consistency
 */
@Service
public class EventDrivenConsistency {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    private final Cache<String, PermissionResult> localCache;
    
    /**
     * Handle permission update event
     */
    @EventListener
    public void handlePermissionUpdated(PermissionUpdatedEvent event) {
        String cacheKey = buildCacheKey(
            event.getUserId(),
            event.getResource(),
            event.getAction()
        );
        
        // 1. Update Redis (source of truth)
        redisTemplate.opsForValue().set(
            "perm:" + cacheKey,
            event.getNewResult(),
            Duration.ofHours(1)
        );
        
        // 2. Invalidate local cache
        localCache.invalidate(cacheKey);
        
        // 3. Publish invalidation to other instances
        publishInvalidation(cacheKey);
    }
    
    /**
     * Listen for invalidation events from other instances
     */
    @RedisListener(channel = "cache:invalidate")
    public void handleInvalidation(String cacheKey) {
        // Invalidate local cache
        localCache.invalidate(cacheKey);
        
        // Optionally refresh from Redis
        PermissionResult result = redisTemplate.opsForValue()
            .get("perm:" + cacheKey);
        if (result != null) {
            localCache.put(cacheKey, result);
        }
    }
}
```

#### 7. **Consistency Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Consistency Guarantees                         │
└─────────────────────────────────────────────────────────┘

Strategy: Event-Driven + TTL
├─ Immediate: Event-based invalidation
├─ Safety: TTL ensures eventual consistency
├─ Performance: Local cache for speed
└─ Reliability: Redis as source of truth

Consistency Levels:
├─ Strong: Event-based invalidation (< 100ms)
├─ Eventual: TTL-based refresh (5-60 minutes)
└─ Guaranteed: Always consistent within TTL

Trade-offs:
├─ Perfect consistency: Higher latency
├─ Eventual consistency: Better performance ✓
└─ Chosen: Eventual with event-based updates
```

---

## Summary

Part 2 covers questions 6-10 on Redis Caching Strategy:

6. **Redis Caching Usage**: Multi-level caching, Redis integration, cache key design
7. **Caching Strategy**: Cache-aside pattern, comparison with alternatives
8. **Cache Invalidation**: Event-based, TTL-based, version-based, hybrid approach
9. **Cached Data**: Permission results, user permissions, role permissions, metadata
10. **Cache Consistency**: Distributed invalidation, version-based, TTL-based, event-driven

Key techniques:
- Multi-level caching (Local + Redis + Trie)
- Cache-aside pattern for flexibility
- Hybrid invalidation (events + TTL)
- Strategic data caching
- Distributed cache consistency
