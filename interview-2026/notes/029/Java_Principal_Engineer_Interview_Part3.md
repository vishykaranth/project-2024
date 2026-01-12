# Java Principal Engineer Interview Questions - Part 3

## System Design & Architecture

This part covers system design principles, scalability patterns, microservices architecture, and architectural decision-making.

---

## 1. System Design Fundamentals

### Q1: Design a distributed caching system. How would you handle cache invalidation, consistency, and scalability?

**Answer:**

**Architecture:**

```java
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Client    │────▶│  Cache      │────▶│  Database   │
│             │     │  Layer      │     │             │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                    ┌──────┴──────┐
                    │             │
              ┌─────▼─────┐  ┌───▼────┐
              │  Cache    │  │ Cache  │
              │  Node 1   │  │ Node 2 │
              └───────────┘  └────────┘
```

**Implementation:**

```java
// Cache Interface
public interface DistributedCache<K, V> {
    V get(K key);
    void put(K key, V value);
    void invalidate(K key);
    void invalidatePattern(String pattern);
}

// Redis-based Implementation
public class RedisDistributedCache<K, V> implements DistributedCache<K, V> {
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final StringSerializer keySerializer;
    
    public RedisDistributedCache(String host, int port) {
        this.jedisPool = new JedisPool(host, port);
        this.objectMapper = new ObjectMapper();
        this.keySerializer = new StringSerializer();
    }
    
    @Override
    public V get(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String keyStr = serializeKey(key);
            String valueStr = jedis.get(keyStr);
            return valueStr != null ? deserializeValue(valueStr) : null;
        }
    }
    
    @Override
    public void put(K key, V value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String keyStr = serializeKey(key);
            String valueStr = serializeValue(value);
            jedis.setex(keyStr, ttl, valueStr);  // TTL for expiration
        }
    }
    
    @Override
    public void invalidate(K key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(serializeKey(key));
        }
    }
    
    @Override
    public void invalidatePattern(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> keys = jedis.keys(pattern);
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
            }
        }
    }
}
```

**Cache Invalidation Strategies:**

```java
// 1. Time-based Expiration (TTL)
public void putWithTTL(K key, V value, int ttlSeconds) {
    jedis.setex(key, ttlSeconds, serializeValue(value));
}

// 2. Event-driven Invalidation
@Component
public class CacheInvalidationListener {
    @EventListener
    public void onUserUpdate(UserUpdatedEvent event) {
        cache.invalidate("user:" + event.getUserId());
        cache.invalidatePattern("user:" + event.getUserId() + ":*");
    }
}

// 3. Write-through Cache
public void updateUser(User user) {
    // Update database
    userRepository.save(user);
    // Update cache
    cache.put("user:" + user.getId(), user);
}

// 4. Write-behind Cache
public void updateUser(User user) {
    // Update cache immediately
    cache.put("user:" + user.getId(), user);
    // Queue for database update
    asyncUpdateQueue.offer(user);
}

// 5. Cache-aside Pattern
public User getUser(Long id) {
    // Try cache first
    User user = cache.get("user:" + id);
    if (user != null) {
        return user;
    }
    
    // Cache miss: load from database
    user = userRepository.findById(id);
    if (user != null) {
        cache.put("user:" + id, user);
    }
    return user;
}
```

**Consistency Models:**

```java
// 1. Strong Consistency (Synchronous replication)
public void putWithReplication(K key, V value) {
    // Write to all replicas synchronously
    for (CacheNode node : allNodes) {
        node.put(key, value);
    }
}

// 2. Eventual Consistency (Asynchronous replication)
public void putWithAsyncReplication(K key, V value) {
    // Write to primary
    primaryNode.put(key, value);
    // Replicate asynchronously
    replicationQueue.offer(new ReplicationEvent(key, value));
}

// 3. Read Repair
public V getWithReadRepair(K key) {
    // Read from multiple nodes
    List<V> values = readFromMultipleNodes(key);
    V majorityValue = getMajorityValue(values);
    
    // Repair inconsistent nodes
    repairInconsistentNodes(key, majorityValue, values);
    
    return majorityValue;
}
```

**Scalability Patterns:**

```java
// 1. Consistent Hashing for Sharding
public class ConsistentHashCache {
    private final ConsistentHash<CacheNode> hashRing;
    
    public V get(K key) {
        CacheNode node = hashRing.get(serializeKey(key));
        return node.get(key);
    }
    
    public void put(K key, V value) {
        CacheNode node = hashRing.get(serializeKey(key));
        node.put(key, value);
    }
}

// 2. Cache Warming
public void warmCache() {
    // Pre-load frequently accessed data
    List<Long> popularUserIds = getPopularUserIds();
    for (Long userId : popularUserIds) {
        User user = userRepository.findById(userId);
        cache.put("user:" + userId, user);
    }
}

// 3. Multi-level Cache
public class MultiLevelCache<K, V> {
    private final Cache<K, V> l1Cache;  // Local (in-memory)
    private final Cache<K, V> l2Cache;  // Distributed (Redis)
    
    public V get(K key) {
        // Try L1 first
        V value = l1Cache.get(key);
        if (value != null) {
            return value;
        }
        
        // Try L2
        value = l2Cache.get(key);
        if (value != null) {
            l1Cache.put(key, value);  // Populate L1
            return value;
        }
        
        return null;
    }
}
```

---

### Q2: Design a rate limiting system. How would you implement sliding window, token bucket, and leaky bucket algorithms?

**Answer:**

**1. Sliding Window Rate Limiter**

```java
public class SlidingWindowRateLimiter {
    private final Map<String, Queue<Long>> requests = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowSizeMs;
    
    public SlidingWindowRateLimiter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
    }
    
    public boolean allowRequest(String key) {
        long now = System.currentTimeMillis();
        Queue<Long> window = requests.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());
        
        // Remove old requests outside window
        while (!window.isEmpty() && now - window.peek() > windowSizeMs) {
            window.poll();
        }
        
        // Check if under limit
        if (window.size() < maxRequests) {
            window.offer(now);
            return true;
        }
        
        return false;
    }
}

// Redis-based (distributed)
public class RedisSlidingWindowRateLimiter {
    private final JedisPool jedisPool;
    private final int maxRequests;
    private final long windowSizeMs;
    
    public boolean allowRequest(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            long now = System.currentTimeMillis();
            String redisKey = "ratelimit:" + key;
            
            // Remove old entries
            jedis.zremrangeByScore(redisKey, 0, now - windowSizeMs);
            
            // Count current requests
            long count = jedis.zcard(redisKey);
            
            if (count < maxRequests) {
                // Add current request
                jedis.zadd(redisKey, now, UUID.randomUUID().toString());
                jedis.expire(redisKey, windowSizeMs / 1000);
                return true;
            }
            
            return false;
        }
    }
}
```

**2. Token Bucket Rate Limiter**

```java
public class TokenBucketRateLimiter {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;
    private final int refillRate;  // tokens per second
    
    public TokenBucketRateLimiter(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
    }
    
    public boolean allowRequest(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, 
            k -> new TokenBucket(capacity, refillRate));
        return bucket.tryConsume(1);
    }
    
    private static class TokenBucket {
        private final int capacity;
        private final int refillRate;
        private int tokens;
        private long lastRefillTime;
        
        public TokenBucket(int capacity, int refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        public synchronized boolean tryConsume(int tokensRequested) {
            refill();
            if (tokens >= tokensRequested) {
                tokens -= tokensRequested;
                return true;
            }
            return false;
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            int tokensToAdd = (int) (elapsed * refillRate / 1000);
            
            if (tokensToAdd > 0) {
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}
```

**3. Leaky Bucket Rate Limiter**

```java
public class LeakyBucketRateLimiter {
    private final Map<String, LeakyBucket> buckets = new ConcurrentHashMap<>();
    private final int capacity;
    private final int leakRate;  // requests per second
    
    public LeakyBucketRateLimiter(int capacity, int leakRate) {
        this.capacity = capacity;
        this.leakRate = leakRate;
    }
    
    public boolean allowRequest(String key) {
        LeakyBucket bucket = buckets.computeIfAbsent(key,
            k -> new LeakyBucket(capacity, leakRate));
        return bucket.tryAdd();
    }
    
    private static class LeakyBucket {
        private final int capacity;
        private final int leakRate;
        private int currentSize;
        private long lastLeakTime;
        
        public LeakyBucket(int capacity, int leakRate) {
            this.capacity = capacity;
            this.leakRate = leakRate;
            this.lastLeakTime = System.currentTimeMillis();
        }
        
        public synchronized boolean tryAdd() {
            leak();
            if (currentSize < capacity) {
                currentSize++;
                return true;
            }
            return false;  // Bucket full
        }
        
        private void leak() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastLeakTime;
            int requestsToLeak = (int) (elapsed * leakRate / 1000);
            
            if (requestsToLeak > 0) {
                currentSize = Math.max(0, currentSize - requestsToLeak);
                lastLeakTime = now;
            }
        }
    }
}
```

**Rate Limiter Service:**

```java
@Service
public class RateLimiterService {
    private final RateLimiter rateLimiter;
    
    @Autowired
    public RateLimiterService(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }
    
    public boolean isAllowed(String key) {
        return rateLimiter.allowRequest(key);
    }
}

// Interceptor
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private RateLimiterService rateLimiterService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        String key = getClientKey(request);
        
        if (!rateLimiterService.isAllowed(key)) {
            response.setStatus(429);  // Too Many Requests
            return false;
        }
        
        return true;
    }
    
    private String getClientKey(HttpServletRequest request) {
        // Use IP, user ID, or API key
        return request.getRemoteAddr();
    }
}
```

---

### Q3: Design a message queue system. How would you ensure message ordering, durability, and exactly-once delivery?

**Answer:**

**Architecture:**

```java
┌──────────┐     ┌──────────────┐     ┌──────────┐
│Producer  │────▶│ Message Queue│────▶│Consumer  │
└──────────┘     └──────────────┘     └──────────┘
                       │
                  ┌────┴────┐
                  │         │
            ┌─────▼───┐ ┌───▼────┐
            │Replica 1│ │Replica2│
            └─────────┘ └────────┘
```

**Message Queue Implementation:**

```java
// Message Interface
public interface Message {
    String getId();
    String getTopic();
    byte[] getPayload();
    Map<String, String> getHeaders();
    long getTimestamp();
}

// Queue Interface
public interface MessageQueue {
    void publish(String topic, Message message);
    Message consume(String topic, String consumerGroup);
    void acknowledge(String messageId);
    void commitOffset(String consumerGroup, String topic, long offset);
}

// Kafka-like Implementation
public class KafkaMessageQueue implements MessageQueue {
    private final Map<String, List<Partition>> topics = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Long>> consumerOffsets = new ConcurrentHashMap<>();
    private final int replicationFactor;
    
    public KafkaMessageQueue(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
    
    @Override
    public void publish(String topic, Message message) {
        List<Partition> partitions = topics.computeIfAbsent(topic, 
            k -> createPartitions(k, replicationFactor));
        
        // Determine partition (for ordering)
        int partitionIndex = getPartitionIndex(message, partitions.size());
        Partition partition = partitions.get(partitionIndex);
        
        // Write to leader and replicas
        partition.append(message);
        replicateToFollowers(partition, message);
    }
    
    @Override
    public Message consume(String topic, String consumerGroup) {
        List<Partition> partitions = topics.get(topic);
        if (partitions == null || partitions.isEmpty()) {
            return null;
        }
        
        // Get offset for consumer group
        long offset = getOffset(consumerGroup, topic);
        
        // Read from partition
        Partition partition = selectPartition(partitions, consumerGroup);
        Message message = partition.read(offset);
        
        if (message != null) {
            // Update offset (but don't commit yet)
            updateOffset(consumerGroup, topic, offset + 1);
        }
        
        return message;
    }
    
    @Override
    public void acknowledge(String messageId) {
        // Mark message as processed
        // In Kafka, this is done via offset commit
    }
    
    @Override
    public void commitOffset(String consumerGroup, String topic, long offset) {
        consumerOffsets.computeIfAbsent(consumerGroup, k -> new ConcurrentHashMap<>())
                      .put(topic, offset);
        // Persist to disk for durability
        persistOffsets(consumerGroup, topic, offset);
    }
}
```

**Message Ordering:**

```java
// 1. Single Partition per Topic (Simple)
public class OrderedMessageQueue {
    private final Map<String, Queue<Message>> topics = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    
    public void publish(String topic, Message message) {
        synchronized (lock) {
            topics.computeIfAbsent(topic, k -> new LinkedBlockingQueue<>())
                  .offer(message);
        }
    }
    
    public Message consume(String topic) {
        synchronized (lock) {
            Queue<Message> queue = topics.get(topic);
            return queue != null ? queue.poll() : null;
        }
    }
}

// 2. Partitioning by Key (Kafka-style)
public int getPartitionIndex(Message message, int numPartitions) {
    String key = message.getHeaders().get("partition-key");
    if (key != null) {
        // Same key → same partition → ordering guaranteed
        return Math.abs(key.hashCode()) % numPartitions;
    }
    // Round-robin for messages without key
    return (int) (System.currentTimeMillis() % numPartitions);
}
```

**Durability:**

```java
// Write-Ahead Log (WAL)
public class DurableMessageQueue {
    private final WAL writeAheadLog;
    private final Map<String, Queue<Message>> inMemoryQueue;
    
    public void publish(String topic, Message message) {
        // 1. Write to WAL first (durable)
        writeAheadLog.append(topic, message);
        
        // 2. Then add to in-memory queue
        inMemoryQueue.computeIfAbsent(topic, k -> new LinkedBlockingQueue<>())
                     .offer(message);
        
        // 3. Flush to disk
        writeAheadLog.flush();
    }
    
    // Recovery on startup
    public void recover() {
        // Replay WAL to rebuild in-memory state
        writeAheadLog.replay((topic, message) -> {
            inMemoryQueue.computeIfAbsent(topic, k -> new LinkedBlockingQueue<>())
                         .offer(message);
        });
    }
}
```

**Exactly-Once Delivery:**

```java
// Idempotent Consumer
public class ExactlyOnceConsumer {
    private final Set<String> processedMessages = new ConcurrentHashMap<>().keySet();
    private final MessageQueue queue;
    
    public void consume(String topic, String consumerGroup) {
        Message message = queue.consume(topic, consumerGroup);
        
        if (message == null) {
            return;
        }
        
        String messageId = message.getId();
        
        // Check if already processed (idempotency)
        if (processedMessages.contains(messageId)) {
            // Already processed, just acknowledge
            queue.acknowledge(messageId);
            return;
        }
        
        try {
            // Process message
            processMessage(message);
            
            // Mark as processed
            processedMessages.add(messageId);
            
            // Commit offset (atomic operation)
            queue.commitOffset(consumerGroup, topic, getOffset(message));
            
        } catch (Exception e) {
            // Processing failed, don't commit offset
            // Message will be redelivered
            throw e;
        }
    }
    
    private void processMessage(Message message) {
        // Business logic
    }
}

// Transactional Producer
public class TransactionalProducer {
    private final MessageQueue queue;
    private final TransactionManager transactionManager;
    
    @Transactional
    public void publishWithTransaction(String topic, Message message) {
        // 1. Begin transaction
        Transaction tx = transactionManager.begin();
        
        try {
            // 2. Publish message
            queue.publish(topic, message);
            
            // 3. Update database (same transaction)
            updateDatabase(message);
            
            // 4. Commit transaction
            transactionManager.commit(tx);
            
        } catch (Exception e) {
            // 5. Rollback on error
            transactionManager.rollback(tx);
            throw e;
        }
    }
}
```

---

## Summary: Part 3

### Key Topics Covered:
1. Distributed Caching System Design
2. Rate Limiting Algorithms
3. Message Queue System Design

### Principal Engineer Focus:
- System design and architecture
- Scalability and performance
- Distributed systems patterns
- Trade-off analysis

---

**Next**: Part 4 will cover Performance & Optimization.

