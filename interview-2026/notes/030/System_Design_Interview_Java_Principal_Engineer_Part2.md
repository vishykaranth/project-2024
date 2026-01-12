# System Design Interview Questions for Java Principal Engineers - Part 2

## Load Balancing, Caching, and CDN

This part covers critical infrastructure components for scalable systems.

---

## Interview Question 4: Design a Load Balancer

### Requirements

- Distribute traffic across multiple servers
- Health checking and failover
- Session persistence (sticky sessions)
- Multiple algorithms (round-robin, least connections, etc.)

### Load Balancing Algorithms

#### 1. Round Robin
```java
@Component
public class RoundRobinLoadBalancer {
    private final List<Server> servers;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public Server getNextServer() {
        int index = currentIndex.getAndIncrement() % servers.size();
        return servers.get(index);
    }
}
```

#### 2. Least Connections
```java
@Component
public class LeastConnectionsLoadBalancer {
    private final Map<Server, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    
    public Server getNextServer() {
        return connectionCounts.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    public void incrementConnections(Server server) {
        connectionCounts.computeIfAbsent(server, s -> new AtomicInteger(0))
                        .incrementAndGet();
    }
    
    public void decrementConnections(Server server) {
        connectionCounts.get(server).decrementAndGet();
    }
}
```

#### 3. Weighted Round Robin
```java
@Component
public class WeightedRoundRobinLoadBalancer {
    private final List<WeightedServer> servers;
    private int currentWeight = 0;
    private int currentIndex = -1;
    
    public Server getNextServer() {
        while (true) {
            currentIndex = (currentIndex + 1) % servers.size();
            if (currentIndex == 0) {
                currentWeight = currentWeight - gcd();
                if (currentWeight <= 0) {
                    currentWeight = maxWeight();
                }
            }
            
            WeightedServer server = servers.get(currentIndex);
            if (server.getWeight() >= currentWeight) {
                return server.getServer();
            }
        }
    }
}
```

### Health Checking

```java
@Component
public class HealthChecker {
    private final ScheduledExecutorService scheduler;
    private final Map<Server, HealthStatus> serverHealth = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void startHealthChecks() {
        scheduler.scheduleAtFixedRate(this::checkHealth, 0, 10, TimeUnit.SECONDS);
    }
    
    private void checkHealth() {
        servers.parallelStream().forEach(server -> {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                    server.getHealthCheckUrl(), String.class
                );
                serverHealth.put(server, 
                    response.getStatusCode().is2xxSuccessful() 
                        ? HealthStatus.HEALTHY 
                        : HealthStatus.UNHEALTHY
                );
            } catch (Exception e) {
                serverHealth.put(server, HealthStatus.UNHEALTHY);
            }
        });
    }
    
    public boolean isHealthy(Server server) {
        return serverHealth.getOrDefault(server, HealthStatus.UNHEALTHY) 
               == HealthStatus.HEALTHY;
    }
}
```

### Session Persistence (Sticky Sessions)

```java
@Component
public class StickySessionLoadBalancer {
    private final ConsistentHash consistentHash;
    
    public Server getServerForSession(String sessionId) {
        return consistentHash.getServer(sessionId);
    }
    
    @Component
    public class SessionAwareFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, 
                           FilterChain chain) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String sessionId = httpRequest.getSession().getId();
            
            Server server = loadBalancer.getServerForSession(sessionId);
            // Route request to same server
            routeToServer(server, httpRequest);
        }
    }
}
```

---

## Interview Question 5: Design a Multi-Level Caching System

### Requirements

- Multiple cache layers (L1, L2, L3)
- Different eviction policies
- Cache invalidation strategy
- Distributed cache support

### Multi-Level Cache Implementation

```java
@Service
public class MultiLevelCacheService {
    // L1: Local in-memory cache (fastest, smallest)
    private final Cache<String, Object> l1Cache = 
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    
    // L2: Distributed cache (Redis)
    @Autowired
    private RedisTemplate<String, Object> redisCache;
    
    // L3: Database (slowest, largest)
    @Autowired
    private DataRepository dataRepository;
    
    public <T> T get(String key, Class<T> type) {
        // Check L1 cache
        T value = (T) l1Cache.getIfPresent(key);
        if (value != null) {
            return value;
        }
        
        // Check L2 cache
        value = (T) redisCache.opsForValue().get("cache:" + key);
        if (value != null) {
            // Populate L1 cache
            l1Cache.put(key, value);
            return value;
        }
        
        // Check L3 (database)
        value = dataRepository.findByKey(key, type);
        if (value != null) {
            // Populate L2 and L1
            redisCache.opsForValue().set("cache:" + key, value, 
                Duration.ofHours(1));
            l1Cache.put(key, value);
        }
        
        return value;
    }
    
    public void put(String key, Object value) {
        // Write-through: update all levels
        l1Cache.put(key, value);
        redisCache.opsForValue().set("cache:" + key, value, 
            Duration.ofHours(1));
        dataRepository.save(key, value);
    }
    
    public void invalidate(String key) {
        // Invalidate all levels
        l1Cache.invalidate(key);
        redisCache.delete("cache:" + key);
        // Database doesn't need invalidation
    }
}
```

### Cache-Aside Pattern

```java
@Service
public class CacheAsideService {
    @Autowired
    private RedisTemplate<String, Object> cache;
    
    @Autowired
    private UserRepository userRepository;
    
    public User getUser(String userId) {
        // 1. Check cache
        User user = (User) cache.opsForValue().get("user:" + userId);
        if (user != null) {
            return user;
        }
        
        // 2. Cache miss - load from database
        user = userRepository.findById(userId);
        
        // 3. Populate cache
        if (user != null) {
            cache.opsForValue().set("user:" + userId, user, 
                Duration.ofMinutes(10));
        }
        
        return user;
    }
    
    public void updateUser(User user) {
        // 1. Update database
        userRepository.save(user);
        
        // 2. Invalidate cache
        cache.delete("user:" + user.getId());
        
        // Alternative: Update cache (write-through)
        // cache.opsForValue().set("user:" + user.getId(), user);
    }
}
```

### Write-Through Cache

```java
@Service
public class WriteThroughCacheService {
    @Autowired
    private RedisTemplate<String, Object> cache;
    
    @Autowired
    private UserRepository userRepository;
    
    public void saveUser(User user) {
        // 1. Write to cache
        cache.opsForValue().set("user:" + user.getId(), user, 
            Duration.ofMinutes(10));
        
        // 2. Write to database
        userRepository.save(user);
    }
}
```

### Write-Back Cache

```java
@Service
public class WriteBackCacheService {
    @Autowired
    private RedisTemplate<String, Object> cache;
    
    @Autowired
    private UserRepository userRepository;
    
    private final BlockingQueue<User> writeQueue = new LinkedBlockingQueue<>();
    
    @PostConstruct
    public void startWriteBackProcessor() {
        executorService.submit(() -> {
            while (true) {
                try {
                    User user = writeQueue.take();
                    // Batch write to database
                    userRepository.save(user);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    public void saveUser(User user) {
        // 1. Write to cache immediately
        cache.opsForValue().set("user:" + user.getId(), user, 
            Duration.ofMinutes(10));
        
        // 2. Queue for async database write
        writeQueue.offer(user);
    }
}
```

---

## Interview Question 6: Design a CDN (Content Delivery Network)

### Requirements

- Serve static content (images, videos, CSS, JS)
- Low latency worldwide
- High availability
- Cache invalidation

### CDN Architecture

```
┌─────────────┐
│   Origin    │
│   Server    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Edge Servers   │
│  (Geographically│
│   Distributed)  │
└─────────────────┘
       │
       ▼
┌─────────────┐
│   Clients   │
└─────────────┘
```

### CDN Implementation

```java
@RestController
public class CDNController {
    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private RedisTemplate<String, String> cache;
    
    @GetMapping("/cdn/{path}")
    public ResponseEntity<Resource> getContent(@PathVariable String path) {
        // 1. Check edge cache
        String cacheKey = "cdn:" + path;
        byte[] content = getFromCache(cacheKey);
        
        if (content != null) {
            return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(new ByteArrayResource(content));
        }
        
        // 2. Cache miss - fetch from origin
        content = s3Service.getObject(path);
        
        // 3. Cache at edge
        cache.opsForValue().set(cacheKey, Base64.getEncoder().encodeToString(content),
            Duration.ofHours(24));
        
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .body(new ByteArrayResource(content));
    }
    
    @PostMapping("/cdn/invalidate")
    public void invalidateCache(@RequestParam String path) {
        // Invalidate cache across all edge servers
        cacheMessagingService.broadcastInvalidation("cdn:" + path);
    }
}
```

### Cache Invalidation Strategy

```java
@Service
public class CDNInvalidationService {
    @Autowired
    private RedisTemplate<String, String> cache;
    
    @Autowired
    private MessageQueue messageQueue;
    
    public void invalidate(String path) {
        // 1. Invalidate local cache
        cache.delete("cdn:" + path);
        
        // 2. Broadcast to all edge servers
        InvalidationMessage message = new InvalidationMessage(path);
        messageQueue.publish("cdn-invalidation", message);
    }
    
    @EventListener
    public void handleInvalidation(InvalidationMessage message) {
        // Edge server receives invalidation message
        cache.delete("cdn:" + message.getPath());
    }
}
```

---

## Interview Question 7: Design a Distributed Session Store

### Requirements

- Store user sessions across servers
- High availability
- Low latency
- Session expiration

### Implementation with Redis

```java
@Service
public class DistributedSessionStore {
    @Autowired
    private RedisTemplate<String, Object> redis;
    
    private final Duration sessionTimeout = Duration.ofMinutes(30);
    
    public void createSession(String sessionId, SessionData data) {
        String key = "session:" + sessionId;
        redis.opsForValue().set(key, data, sessionTimeout);
    }
    
    public SessionData getSession(String sessionId) {
        String key = "session:" + sessionId;
        SessionData data = (SessionData) redis.opsForValue().get(key);
        
        if (data != null) {
            // Refresh expiration
            redis.expire(key, sessionTimeout);
        }
        
        return data;
    }
    
    public void updateSession(String sessionId, SessionData data) {
        String key = "session:" + sessionId;
        redis.opsForValue().set(key, data, sessionTimeout);
    }
    
    public void deleteSession(String sessionId) {
        redis.delete("session:" + sessionId);
    }
}
```

### Session Replication Strategy

```java
@Service
public class SessionReplicationService {
    @Autowired
    private RedisTemplate<String, Object> primaryRedis;
    
    @Autowired
    private RedisTemplate<String, Object> replicaRedis;
    
    public void saveSession(String sessionId, SessionData data) {
        // Write to primary
        primaryRedis.opsForValue().set("session:" + sessionId, data);
        
        // Async replication to replica
        CompletableFuture.runAsync(() -> {
            replicaRedis.opsForValue().set("session:" + sessionId, data);
        });
    }
    
    public SessionData getSession(String sessionId) {
        // Try primary first
        try {
            return (SessionData) primaryRedis.opsForValue()
                .get("session:" + sessionId);
        } catch (Exception e) {
            // Fallback to replica
            return (SessionData) replicaRedis.opsForValue()
                .get("session:" + sessionId);
        }
    }
}
```

---

## Interview Question 8: Design a Search Autocomplete System

### Requirements

- Fast prefix matching
- Handle millions of queries
- Real-time updates
- Ranking by popularity

### Trie-Based Implementation

```java
public class AutocompleteTrie {
    private final TrieNode root = new TrieNode();
    
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        List<String> suggestions = new ArrayList<>();
        int frequency = 0;
    }
    
    public void insert(String word, int frequency) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
            node.frequency += frequency;
        }
    }
    
    public List<String> search(String prefix, int limit) {
        TrieNode node = root;
        
        // Navigate to prefix node
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }
        
        // Collect all words with this prefix
        List<String> results = new ArrayList<>();
        collectWords(node, prefix, results, limit);
        
        // Sort by frequency
        return results.stream()
            .sorted((a, b) -> Integer.compare(
                getFrequency(b), getFrequency(a)))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private void collectWords(TrieNode node, String prefix, 
                              List<String> results, int limit) {
        if (results.size() >= limit) {
            return;
        }
        
        if (node.frequency > 0) {
            results.add(prefix);
        }
        
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            collectWords(entry.getValue(), prefix + entry.getKey(), 
                        results, limit);
        }
    }
}
```

### Distributed Autocomplete with Redis

```java
@Service
public class DistributedAutocompleteService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    public void addQuery(String query) {
        // Increment frequency
        redis.opsForZSet().incrementScore("queries", query, 1);
        
        // Add all prefixes to sorted set
        for (int i = 1; i <= query.length(); i++) {
            String prefix = query.substring(0, i);
            redis.opsForZSet().add("prefix:" + prefix, query, 
                redis.opsForZSet().score("queries", query));
        }
    }
    
    public List<String> getSuggestions(String prefix, int limit) {
        // Get top queries for this prefix
        Set<String> suggestions = redis.opsForZSet()
            .reverseRange("prefix:" + prefix, 0, limit - 1);
        
        return new ArrayList<>(suggestions);
    }
}
```

---

## Caching Best Practices for Principal Engineers

### 1. Cache Key Design
```java
// Good: Descriptive, namespaced
String key = "user:profile:" + userId + ":v2";

// Bad: Ambiguous
String key = userId;
```

### 2. Cache Invalidation
```java
// Event-driven invalidation
@EventListener
public void onUserUpdate(UserUpdatedEvent event) {
    cacheService.invalidate("user:" + event.getUserId());
}
```

### 3. Cache Warming
```java
@Scheduled(fixedRate = 3600000) // Every hour
public void warmCache() {
    // Pre-load frequently accessed data
    List<String> popularUserIds = getPopularUserIds();
    popularUserIds.forEach(userId -> 
        userService.getUser(userId) // Triggers cache
    );
}
```

### 4. Cache Monitoring
```java
@Component
public class CacheMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordCacheHit(String cacheName) {
        meterRegistry.counter("cache.hits", "cache", cacheName).increment();
    }
    
    public void recordCacheMiss(String cacheName) {
        meterRegistry.counter("cache.misses", "cache", cacheName).increment();
    }
}
```

---

## Summary: Part 2

### Key Topics Covered:
1. ✅ Load balancing algorithms and health checking
2. ✅ Multi-level caching strategies
3. ✅ CDN design and cache invalidation
4. ✅ Distributed session management
5. ✅ Autocomplete system design

### Java-Specific Implementations:
- Spring Boot components
- Redis integration
- Async processing
- Metrics and monitoring

---

**Next**: Part 3 will cover Database Design, Replication, and Sharding strategies.

