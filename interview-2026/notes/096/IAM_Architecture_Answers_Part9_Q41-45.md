# IAM Architecture Answers - Part 9: Performance Requirements (Questions 41-45)

## Question 41: You "achieved 99.9% availability and handling 1M+ authentication requests daily." How did you design for this scale?

### Answer

### High-Scale Design

#### 1. **Scale Requirements**

```
┌─────────────────────────────────────────────────────────┐
│         Scale Requirements                             │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ 1M+ requests/day
├─ ~11.6 requests/second average
├─ Peak: 100+ requests/second
├─ 99.9% availability (8.76 hours downtime/year)
└─ < 100ms latency target
```

#### 2. **Architecture for Scale**

```
┌─────────────────────────────────────────────────────────┐
│         High-Scale Architecture                        │
└─────────────────────────────────────────────────────────┘

                    ┌─────────────┐
                    │   Clients   │
                    │  (1M+ req)  │
                    └──────┬──────┘
                           │
                           ▼
            ┌──────────────────────────┐
            │   Load Balancer          │
            │   (10+ instances)        │
            └──────┬───────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
┌──────────────┐    ┌──────────────┐
│  Auth Service│    │  Auth Service│
│  (Instance 1)│    │  (Instance N)│
│  Auto-scale  │    │  Auto-scale  │
└──────┬───────┘    └──────┬───────┘
       │                   │
       └──────────┬─────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌──────────────┐    ┌──────────────┐
│   Redis      │    │  PostgreSQL  │
│  Cluster     │    │  (Primary)    │
│  (3+ nodes)  │    └──────┬───────┘
└──────────────┘           │
                            ▼
                  ┌──────────────┐
                  │  PostgreSQL  │
                  │  (Replicas)  │
                  └──────────────┘
```

#### 3. **Horizontal Scaling**

```java
// Horizontal scaling configuration
@Configuration
public class ScalingConfig {
    // Stateless service design
    // Any instance can handle any request
    // Auto-scaling based on load
    
    @Bean
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate(
            new LoadBalancerClientHttpRequestFactory(
                loadBalancerClient()
            )
        );
    }
}

// Kubernetes auto-scaling
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: auth-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: auth-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

#### 4. **Caching Strategy**

```java
// Multi-level caching for scale
@Service
public class ScalableAuthService {
    // L1: Local cache (Caffeine)
    private final Cache<String, AuthResult> localCache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();
    
    // L2: Redis cache
    private final RedisTemplate<String, AuthResult> redisCache;
    
    public AuthResult authenticate(AuthRequest request) {
        String cacheKey = generateCacheKey(request);
        
        // L1: Local cache (fastest)
        AuthResult result = localCache.getIfPresent(cacheKey);
        if (result != null) return result;
        
        // L2: Redis cache
        result = redisCache.opsForValue().get(cacheKey);
        if (result != null) {
            localCache.put(cacheKey, result);
            return result;
        }
        
        // L3: Database (slowest)
        result = authenticateFromDatabase(request);
        
        // Cache at both levels
        redisCache.opsForValue().set(cacheKey, result, 
            Duration.ofMinutes(10));
        localCache.put(cacheKey, result);
        
        return result;
    }
}
```

#### 5. **Database Optimization**

```sql
-- Database optimization for scale
-- Indexes
CREATE INDEX idx_users_username_tenant ON users(username, tenant_id);
CREATE INDEX idx_users_email_tenant ON users(email, tenant_id);
CREATE INDEX idx_user_roles_user ON user_roles(user_id, tenant_id);

-- Connection pooling
-- Max connections: 100
-- Min idle: 20
-- Connection timeout: 30s

-- Read replicas
-- Primary: Writes
-- Replicas: Reads
```

---

## Question 42: What performance bottlenecks did you identify, and how did you address them?

### Answer

### Performance Bottlenecks & Solutions

#### 1. **Bottleneck Identification**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Bottlenecks                        │
└─────────────────────────────────────────────────────────┘

Bottlenecks Identified:
├─ Database queries (slow)
├─ Permission evaluation (complex)
├─ Token validation (frequent)
├─ External service calls (Keycloak)
└─ Session management (overhead)
```

#### 2. **Database Query Optimization**

**Problem:**
```
Slow database queries
N+1 query problem
Missing indexes
```

**Solution:**
```java
// Query optimization
@Repository
public class OptimizedUserRepository {
    // Use JOIN FETCH to avoid N+1
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "LEFT JOIN FETCH r.permissions " +
           "WHERE u.userId = :userId")
    User findUserWithRolesAndPermissions(@Param("userId") String userId);
    
    // Batch loading
    @Query("SELECT u FROM User u WHERE u.userId IN :userIds")
    List<User> findUsersByIds(@Param("userIds") List<String> userIds);
}
```

#### 3. **Permission Evaluation Optimization**

**Problem:**
```
Complex permission evaluation
Slow permission checks
```

**Solution:**
```java
// Optimized permission evaluation with Trie
@Service
public class OptimizedPermissionService {
    private final PermissionTrie permissionTrie;
    private final RedisTemplate<String, Set<String>> redisCache;
    
    public boolean hasPermission(String userId, String permission) {
        // Load permissions from cache
        Set<String> userPermissions = getUserPermissions(userId);
        
        // Fast lookup using Trie (O(m) where m is permission length)
        return permissionTrie.contains(permission, userPermissions);
    }
    
    // Cache permissions
    private Set<String> getUserPermissions(String userId) {
        String cacheKey = "user:permissions:" + userId;
        Set<String> permissions = redisCache.opsForValue().get(cacheKey);
        
        if (permissions == null) {
            permissions = loadPermissionsFromDatabase(userId);
            redisCache.opsForValue().set(cacheKey, permissions, 
                Duration.ofMinutes(30));
        }
        
        return permissions;
    }
}
```

#### 4. **Token Validation Optimization**

**Problem:**
```
Frequent token validation
Database lookups for each validation
```

**Solution:**
```java
// Optimized token validation
@Service
public class OptimizedTokenValidationService {
    private final JwtDecoder jwtDecoder;
    private final RedisTemplate<String, Boolean> tokenCache;
    
    public boolean validateToken(String token) {
        // Check cache first
        String cacheKey = "token:valid:" + hashToken(token);
        Boolean cached = tokenCache.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Validate JWT (stateless, no DB lookup)
        try {
            Jwt jwt = jwtDecoder.decode(token);
            
            // Cache validation result
            tokenCache.opsForValue().set(cacheKey, true, 
                Duration.ofMinutes(15));
            
            return true;
        } catch (Exception e) {
            tokenCache.opsForValue().set(cacheKey, false, 
                Duration.ofMinutes(1));
            return false;
        }
    }
}
```

#### 5. **External Service Call Optimization**

**Problem:**
```
Slow Keycloak calls
Network latency
```

**Solution:**
```java
// Optimize external service calls
@Service
public class OptimizedKeycloakService {
    private final Cache<String, UserInfo> userInfoCache;
    private final CircuitBreaker circuitBreaker;
    
    public UserInfo getUserInfo(String userId) {
        // Cache user info
        return userInfoCache.get(userId, () -> {
            // Use circuit breaker
            return circuitBreaker.executeSupplier(() -> {
                return keycloakClient.getUserInfo(userId);
            });
        });
    }
    
    // Batch requests
    public List<UserInfo> getUsersInfo(List<String> userIds) {
        // Batch request to Keycloak
        return keycloakClient.getUsersInfo(userIds);
    }
}
```

---

## Question 43: How did you optimize authentication request processing?

### Answer

### Authentication Request Optimization

#### 1. **Optimization Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Authentication Optimization                    │
└─────────────────────────────────────────────────────────┘

Optimizations:
├─ Caching
├─ Async processing
├─ Connection pooling
├─ Batch operations
└─ Parallel processing
```

#### 2. **Caching Authentication Results**

```java
// Cache authentication results
@Service
public class OptimizedAuthService {
    private final Cache<String, AuthResult> authCache;
    
    public AuthResult authenticate(AuthRequest request) {
        String cacheKey = generateCacheKey(request);
        
        // Check cache
        AuthResult cached = authCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Authenticate
        AuthResult result = performAuthentication(request);
        
        // Cache result (short TTL for security)
        authCache.put(cacheKey, result);
        
        return result;
    }
}
```

#### 3. **Async Processing**

```java
// Async processing for non-critical operations
@Service
public class AsyncAuthService {
    @Async
    public CompletableFuture<Void> auditAuthentication(
            AuthRequest request, AuthResult result) {
        // Async audit logging
        auditService.logAuthentication(request, result);
        return CompletableFuture.completedFuture(null);
    }
    
    @Async
    public CompletableFuture<Void> sendNotification(
            User user, String event) {
        // Async notifications
        notificationService.send(user, event);
        return CompletableFuture.completedFuture(null);
    }
    
    public AuthResult authenticate(AuthRequest request) {
        // Synchronous: Core authentication
        AuthResult result = performAuthentication(request);
        
        // Async: Non-critical operations
        auditAuthentication(request, result);
        sendNotification(result.getUser(), "LOGIN");
        
        return result;
    }
}
```

#### 4. **Connection Pooling**

```java
// Connection pooling
@Configuration
public class ConnectionPoolConfig {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/iam");
        config.setUsername("user");
        config.setPassword("password");
        config.setMaximumPoolSize(100);
        config.setMinimumIdle(20);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        return new HikariDataSource(config);
    }
}
```

#### 5. **Parallel Processing**

```java
// Parallel processing for independent operations
@Service
public class ParallelAuthService {
    public AuthResult authenticate(AuthRequest request) {
        // Parallel execution of independent operations
        CompletableFuture<User> userFuture = 
            CompletableFuture.supplyAsync(() -> 
                userService.getUser(request.getUsername()));
        
        CompletableFuture<Set<String>> permissionsFuture = 
            CompletableFuture.supplyAsync(() -> 
                permissionService.getPermissions(request.getUsername()));
        
        // Wait for both
        CompletableFuture.allOf(userFuture, permissionsFuture).join();
        
        User user = userFuture.get();
        Set<String> permissions = permissionsFuture.get();
        
        // Generate token
        Token token = tokenService.generateToken(user, permissions);
        
        return new AuthResult(token, user);
    }
}
```

---

## Question 44: What caching strategies did you use for the IAM system?

### Answer

### Caching Strategies

#### 1. **Multi-Level Caching**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Caching                            │
└─────────────────────────────────────────────────────────┘

L1: Local Cache (Caffeine)
├─ Fastest access
├─ Limited size
└─ Per-instance

L2: Distributed Cache (Redis)
├─ Shared across instances
├─ Larger capacity
└─ Persistent

L3: Database
├─ Source of truth
├─ Slowest
└─ Always consistent
```

#### 2. **Local Cache (Caffeine)**

```java
// Local cache with Caffeine
@Configuration
public class LocalCacheConfig {
    @Bean
    public Cache<String, User> userCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }
    
    @Bean
    public Cache<String, Set<String>> permissionCache() {
        return Caffeine.newBuilder()
            .maximumSize(50_000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
    }
}
```

#### 3. **Redis Cache**

```java
// Redis cache configuration
@Configuration
public class RedisCacheConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
    
    // Cache configuration
    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(config)
            .build();
    }
}
```

#### 4. **Cache-Aside Pattern**

```java
// Cache-aside pattern
@Service
public class CacheAsideService {
    public User getUser(String userId) {
        // Check cache
        User user = userCache.getIfPresent(userId);
        if (user != null) {
            return user;
        }
        
        // Load from database
        user = userRepository.findById(userId).orElse(null);
        
        // Store in cache
        if (user != null) {
            userCache.put(userId, user);
        }
        
        return user;
    }
}
```

#### 5. **Cache Invalidation**

```java
// Cache invalidation
@Service
public class CacheInvalidationService {
    @EventListener
    public void handleUserUpdated(UserUpdatedEvent event) {
        // Invalidate local cache
        userCache.invalidate(event.getUserId());
        
        // Invalidate Redis cache
        redisTemplate.delete("user:" + event.getUserId());
        
        // Invalidate related caches
        permissionCache.invalidate("user:" + event.getUserId());
    }
}
```

---

## Question 45: How did you handle peak load scenarios?

### Answer

### Peak Load Handling

#### 1. **Peak Load Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Peak Load Strategy                            │
└─────────────────────────────────────────────────────────┘

Strategies:
├─ Auto-scaling
├─ Load balancing
├─ Rate limiting
├─ Queue management
└─ Graceful degradation
```

#### 2. **Auto-Scaling**

```yaml
# Kubernetes auto-scaling
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: auth-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: auth-service
  minReplicas: 3
  maxReplicas: 50  # Scale up to 50 instances
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
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 4
        periodSeconds: 15
      selectPolicy: Max
```

#### 3. **Load Balancing**

```java
// Load balancing configuration
@Configuration
public class LoadBalancerConfig {
    @Bean
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate(
            new LoadBalancerClientHttpRequestFactory(
                loadBalancerClient()
            )
        );
    }
    
    // Load balancing algorithm: Round-robin, Least-connections
    @Bean
    public LoadBalancerClient loadBalancerClient() {
        return new LoadBalancerClient() {
            @Override
            public ServiceInstance choose(String serviceId, 
                    LoadBalancerRequest<ServiceInstance> request) {
                // Implement load balancing logic
                // Round-robin, least-connections, etc.
            }
        };
    }
}
```

#### 4. **Rate Limiting**

```java
// Rate limiting for peak load
@Service
public class PeakLoadRateLimiter {
    private final RedisTemplate<String, Integer> redisTemplate;
    
    public boolean isAllowed(String clientId, String endpoint) {
        String key = "rate_limit:" + clientId + ":" + endpoint;
        
        // Sliding window rate limiting
        int current = redisTemplate.opsForValue().get(key) != null ?
            redisTemplate.opsForValue().get(key) : 0;
        
        int limit = getRateLimit(endpoint);
        
        if (current >= limit) {
            return false;
        }
        
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(1));
        
        return true;
    }
}
```

#### 5. **Queue Management**

```java
// Queue management for peak load
@Service
public class QueueManagementService {
    private final BlockingQueue<AuthRequest> requestQueue;
    
    public AuthResult authenticate(AuthRequest request) {
        // Add to queue if system is overloaded
        if (isSystemOverloaded()) {
            return queueRequest(request);
        }
        
        // Process immediately
        return processRequest(request);
    }
    
    private boolean isSystemOverloaded() {
        // Check system metrics
        // CPU, memory, active requests
        return getSystemLoad() > 0.8;
    }
}
```

#### 6. **Graceful Degradation**

```java
// Graceful degradation during peak load
@Service
public class DegradableAuthService {
    public AuthResult authenticate(AuthRequest request) {
        // During peak load, use simplified authentication
        if (isPeakLoad()) {
            return authenticateSimplified(request);
        }
        
        // Normal authentication
        return authenticateFull(request);
    }
    
    private AuthResult authenticateSimplified(AuthRequest request) {
        // Simplified authentication
        // Skip non-critical checks
        // Use cached data
        // Return basic token
    }
}
```

---

## Summary

Part 9 covers questions 41-45 on Performance Requirements:

41. **High-Scale Design**: Architecture, horizontal scaling, caching, database optimization
42. **Performance Bottlenecks**: Database queries, permission evaluation, token validation, external services
43. **Authentication Optimization**: Caching, async processing, connection pooling, parallel processing
44. **Caching Strategies**: Multi-level caching, local cache, Redis, cache-aside, invalidation
45. **Peak Load Handling**: Auto-scaling, load balancing, rate limiting, queue management, graceful degradation

Key techniques:
- Horizontal scaling
- Multi-level caching
- Async processing
- Database optimization
- Auto-scaling for peak loads
