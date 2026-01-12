# System Design Interview Questions for Java Principal Engineers - Part 1

## Introduction to System Design Interviews

### What is System Design?

System Design is the process of defining the architecture, components, modules, interfaces, and data for a system to satisfy specified requirements. For Principal Engineers, it involves:

- **Scalability**: Handling growth in users, data, and traffic
- **Reliability**: System availability and fault tolerance
- **Performance**: Response time and throughput
- **Maintainability**: Code quality and system evolution
- **Cost Efficiency**: Optimizing resource usage

---

## Core Concepts for Principal Engineers

### 1. Scalability Fundamentals

#### Vertical Scaling (Scale Up)
- **Definition**: Increase resources of a single machine (CPU, RAM, storage)
- **Pros**: Simple, no code changes needed
- **Cons**: Limited by hardware, single point of failure, expensive
- **When to Use**: Small to medium applications, stateful services

#### Horizontal Scaling (Scale Out)
- **Definition**: Add more machines to handle load
- **Pros**: Unlimited scaling, cost-effective, fault-tolerant
- **Cons**: Requires stateless design, load balancing, distributed systems complexity
- **When to Use**: Large-scale applications, stateless services

**Java Implementation Example:**
```java
// Stateless service design for horizontal scaling
@Service
public class UserService {
    // No instance variables storing state
    // All state in database or cache
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RedisCache cache;
    
    public User getUser(String userId) {
        // Check cache first
        User user = cache.get("user:" + userId);
        if (user != null) {
            return user;
        }
        
        // Fetch from database
        user = userRepository.findById(userId);
        if (user != null) {
            cache.put("user:" + userId, user, Duration.ofMinutes(10));
        }
        
        return user;
    }
}
```

---

## Interview Question 1: Design a URL Shortener (like bit.ly)

### Requirements Gathering

**Functional Requirements:**
- Shorten long URLs
- Redirect short URLs to original URLs
- Custom short URLs (optional)
- Analytics (click count, etc.)

**Non-Functional Requirements:**
- High availability (99.9%)
- Low latency (< 100ms for redirect)
- Scalability (100M URLs/day)
- URL expiration (optional)

### Capacity Estimation

```
Assumptions:
- 100M URLs/day = ~1,160 URLs/second
- Peak traffic: 5x average = 5,800 URLs/second
- 100:1 read:write ratio
- URLs stored for 5 years
- Average URL length: 500 bytes

Storage:
- 100M URLs/day × 365 days × 5 years = 182.5 billion URLs
- 182.5B × 500 bytes = 91.25 TB

Bandwidth:
- Write: 1,160 URLs/sec × 500 bytes = 580 KB/sec
- Read: 116,000 URLs/sec × 500 bytes = 58 MB/sec
```

### High-Level Design

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Load Balancer  │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐      ┌──────────────┐
│  Web Servers    │◄─────►│   Cache      │
│  (Stateless)    │      │  (Redis)     │
└──────┬──────────┘      └──────────────┘
       │
       ▼
┌─────────────────┐
│  Application    │
│  Servers        │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐      ┌──────────────┐
│  Database       │      │  Key Gen     │
│  (Sharded)      │      │  Service     │
└─────────────────┘      └──────────────┘
```

### Detailed Design

#### 1. URL Encoding

**Option 1: Base62 Encoding**
```java
public class URLShortener {
    private static final String BASE62 = 
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = 62;
    
    public String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62.charAt((int)(id % BASE)));
            id /= BASE;
        }
        return sb.reverse().toString();
    }
    
    public long decode(String shortUrl) {
        long id = 0;
        for (char c : shortUrl.toCharArray()) {
            id = id * BASE + BASE62.indexOf(c);
        }
        return id;
    }
}
```

**Option 2: Using Hash**
```java
public class URLShortener {
    private MessageDigest md5;
    
    public String shorten(String longUrl) {
        byte[] hash = md5.digest(longUrl.getBytes());
        // Take first 6 bytes and encode to base62
        return base62Encode(hash);
    }
}
```

#### 2. Database Schema

```sql
-- URLs table (sharded by short_url)
CREATE TABLE urls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    short_url VARCHAR(10) UNIQUE NOT NULL,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    user_id BIGINT,
    click_count BIGINT DEFAULT 0,
    INDEX idx_short_url (short_url),
    INDEX idx_expires_at (expires_at)
);

-- Analytics table
CREATE TABLE url_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    short_url VARCHAR(10) NOT NULL,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer TEXT,
    INDEX idx_short_url_clicked (short_url, clicked_at)
);
```

#### 3. Key Generation Service

**Approach 1: Database Auto-Increment**
```java
@Service
public class KeyGenerationService {
    @Autowired
    private KeyRepository keyRepository;
    
    public long getNextKey() {
        // Use database sequence or auto-increment
        return keyRepository.getNextSequence();
    }
}
```

**Approach 2: Range-Based (Better for Scale)**
```java
@Service
public class KeyGenerationService {
    // Pre-allocate ranges to each server
    private AtomicLong currentId;
    private long maxId;
    
    @PostConstruct
    public void initialize() {
        // Get range from Zookeeper/Consul
        Range range = rangeService.allocateRange(serverId);
        currentId = new AtomicLong(range.start);
        maxId = range.end;
    }
    
    public long getNextKey() {
        long id = currentId.getAndIncrement();
        if (id > maxId) {
            // Request new range
            allocateNewRange();
        }
        return id;
    }
}
```

#### 4. Caching Strategy

```java
@Service
public class URLService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    @Autowired
    private URLRepository urlRepository;
    
    public String getLongURL(String shortUrl) {
        // Check cache first
        String longUrl = redis.opsForValue().get("url:" + shortUrl);
        if (longUrl != null) {
            // Update analytics asynchronously
            updateAnalyticsAsync(shortUrl);
            return longUrl;
        }
        
        // Cache miss - fetch from database
        URL url = urlRepository.findByShortUrl(shortUrl);
        if (url == null) {
            throw new URLNotFoundException();
        }
        
        // Cache for 24 hours
        redis.opsForValue().set(
            "url:" + shortUrl, 
            url.getLongUrl(), 
            Duration.ofHours(24)
        );
        
        updateAnalyticsAsync(shortUrl);
        return url.getLongUrl();
    }
    
    @Async
    private void updateAnalyticsAsync(String shortUrl) {
        // Update click count asynchronously
        analyticsService.recordClick(shortUrl);
    }
}
```

---

## Interview Question 2: Design a Distributed Cache

### Requirements

- **Functional**: Get, Set, Delete operations
- **Non-Functional**: 
  - Low latency (< 10ms)
  - High availability (99.99%)
  - Consistency (eventual)
  - Eviction policy (LRU)

### Design

#### 1. Consistent Hashing

```java
public class ConsistentHash {
    private final SortedMap<Long, String> ring = new TreeMap<>();
    private final int numberOfReplicas = 3;
    private final HashFunction hashFunction;
    
    public ConsistentHash(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }
    
    public void addServer(String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            long hash = hashFunction.hash(server + ":" + i);
            ring.put(hash, server);
        }
    }
    
    public String getServer(String key) {
        if (ring.isEmpty()) {
            return null;
        }
        long hash = hashFunction.hash(key);
        SortedMap<Long, String> tailMap = ring.tailMap(hash);
        return tailMap.isEmpty() ? ring.get(ring.firstKey()) : tailMap.get(tailMap.firstKey());
    }
}
```

#### 2. Cache Server Implementation

```java
@Service
public class DistributedCacheService {
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ConsistentHash consistentHash;
    private final int maxSize = 10000;
    private final Duration ttl = Duration.ofHours(1);
    
    public void put(String key, String value) {
        String server = consistentHash.getServer(key);
        if (isLocalServer(server)) {
            evictIfNeeded();
            cache.put(key, new CacheEntry(value, System.currentTimeMillis()));
        } else {
            // Forward to correct server
            forwardPut(server, key, value);
        }
    }
    
    public String get(String key) {
        String server = consistentHash.getServer(key);
        if (isLocalServer(server)) {
            CacheEntry entry = cache.get(key);
            if (entry != null && !entry.isExpired()) {
                return entry.getValue();
            }
        } else {
            return forwardGet(server, key);
        }
        return null;
    }
    
    private void evictIfNeeded() {
        if (cache.size() >= maxSize) {
            // LRU eviction
            String lruKey = findLRUKey();
            cache.remove(lruKey);
        }
    }
}
```

---

## Interview Question 3: Design a Rate Limiter

### Requirements

- Limit requests per user/IP
- Multiple rate limit strategies
- Distributed system support
- Low latency

### Implementation: Token Bucket Algorithm

```java
@Component
public class RateLimiter {
    private final RedisTemplate<String, String> redis;
    
    public boolean isAllowed(String key, int maxRequests, Duration window) {
        String redisKey = "ratelimit:" + key;
        
        // Get current count
        String countStr = redis.opsForValue().get(redisKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        if (count >= maxRequests) {
            return false;
        }
        
        // Increment count
        redis.opsForValue().increment(redisKey);
        redis.expire(redisKey, window);
        
        return true;
    }
}
```

### Sliding Window Log

```java
@Component
public class SlidingWindowRateLimiter {
    private final RedisTemplate<String, String> redis;
    
    public boolean isAllowed(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        long windowStart = now - window.toMillis();
        
        String redisKey = "ratelimit:" + key;
        
        // Remove old entries
        redis.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
        
        // Count current requests
        Long count = redis.opsForZSet().count(redisKey, windowStart, now);
        
        if (count >= maxRequests) {
            return false;
        }
        
        // Add current request
        redis.opsForZSet().add(redisKey, String.valueOf(now), now);
        redis.expire(redisKey, window);
        
        return true;
    }
}
```

---

## Key Principles for Principal Engineers

### 1. Start with Requirements
- **Functional**: What the system should do
- **Non-Functional**: Performance, scalability, availability
- **Constraints**: Budget, timeline, team size

### 2. Capacity Estimation
- **Traffic**: Requests per second
- **Storage**: Data size and growth
- **Bandwidth**: Network requirements
- **Memory**: Cache and in-memory data

### 3. API Design
- **RESTful**: Use standard HTTP methods
- **Versioning**: API versioning strategy
- **Authentication**: Security considerations
- **Rate Limiting**: Prevent abuse

### 4. Database Design
- **Schema**: Tables, indexes, relationships
- **Sharding**: Horizontal partitioning
- **Replication**: Read replicas, master-slave
- **Consistency**: ACID vs BASE

### 5. Caching Strategy
- **Where**: Client, CDN, Application, Database
- **What**: Frequently accessed data
- **Eviction**: LRU, LFU, TTL
- **Invalidation**: Cache invalidation strategy

---

## Common System Design Patterns

### 1. Load Balancing
- **Round Robin**: Distribute evenly
- **Least Connections**: Send to least busy server
- **IP Hash**: Sticky sessions
- **Weighted**: Based on server capacity

### 2. Database Patterns
- **Master-Slave Replication**: Read scaling
- **Master-Master Replication**: High availability
- **Sharding**: Horizontal partitioning
- **Federation**: Split by function

### 3. Caching Patterns
- **Cache-Aside**: Application manages cache
- **Write-Through**: Write to cache and DB
- **Write-Back**: Write to cache, async to DB
- **Refresh-Ahead**: Proactive cache refresh

---

## Interview Tips for Principal Engineers

### 1. Communication
- **Clarify Requirements**: Ask questions
- **Explain Trade-offs**: Discuss pros/cons
- **Think Aloud**: Show your thought process
- **Be Open to Feedback**: Accept suggestions

### 2. Problem-Solving Approach
1. **Understand**: Clarify requirements
2. **Estimate**: Capacity and scale
3. **Design**: High-level architecture
4. **Detail**: Components and APIs
5. **Optimize**: Identify bottlenecks
6. **Scale**: Handle growth

### 3. Java-Specific Considerations
- **JVM Tuning**: Heap size, GC algorithms
- **Threading**: Thread pools, async processing
- **Frameworks**: Spring Boot, Spring Cloud
- **Libraries**: Resilience4j, Hystrix, Micrometer

---

## Summary: Part 1

### Key Topics Covered:
1. ✅ Scalability fundamentals (vertical vs horizontal)
2. ✅ URL Shortener design
3. ✅ Distributed Cache design
4. ✅ Rate Limiter implementation
5. ✅ Core principles and patterns

### Next Steps:
- Part 2: Load Balancing, Caching Strategies, CDN
- Part 3: Database Design, Replication, Sharding
- Part 4: Microservices Architecture
- Part 5: Message Queues and Event-Driven Systems

---

**Remember**: As a Principal Engineer, focus on trade-offs, scalability, and real-world constraints. Show deep understanding of distributed systems and Java ecosystem.

