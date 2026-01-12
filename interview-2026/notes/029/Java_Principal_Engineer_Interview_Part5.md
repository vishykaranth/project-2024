# Java Principal Engineer Interview Questions - Part 5

## Distributed Systems

This part covers distributed system patterns, consistency models, CAP theorem, and microservices architecture.

---

## 1. Distributed Systems Fundamentals

### Q1: Explain CAP theorem and how it applies to distributed systems. Provide examples.

**Answer:**

**CAP Theorem:**
- **Consistency**: All nodes see same data simultaneously
- **Availability**: System remains operational
- **Partition Tolerance**: System continues despite network failures

**You can only guarantee 2 out of 3.**

**1. CP (Consistency + Partition Tolerance)**

```java
// Example: Distributed Database (MongoDB, HBase)
// - Strong consistency
// - Tolerates partitions
// - May sacrifice availability during partitions

public class CPSystem {
    // During partition, system may become unavailable
    // to maintain consistency
    public void write(String key, String value) {
        // Write to all replicas synchronously
        for (Node node : allNodes) {
            if (!node.isReachable()) {
                throw new UnavailableException();  // Sacrifice availability
            }
            node.write(key, value);
        }
    }
}
```

**2. AP (Availability + Partition Tolerance)**

```java
// Example: DNS, CouchDB, Cassandra
// - High availability
// - Tolerates partitions
// - May have eventual consistency

public class APSystem {
    // During partition, system remains available
    // but may serve stale data
    public String read(String key) {
        Node node = getAvailableNode();  // Any available node
        return node.read(key);  // May return stale data
    }
    
    public void write(String key, String value) {
        // Write to available nodes
        // Replicate to others when partition heals
        for (Node node : getAvailableNodes()) {
            node.write(key, value);
        }
    }
}
```

**3. CA (Consistency + Availability)**

```java
// Example: Traditional RDBMS (single node)
// - Strong consistency
// - High availability
// - No partition tolerance (single node)

// Note: In distributed systems, you MUST have partition tolerance
// So CA is not really possible in true distributed systems
```

**Real-World Examples:**

```java
// 1. MongoDB (CP)
// - Strong consistency within replica set
// - During partition, may become unavailable
// - Use case: Financial transactions

// 2. Cassandra (AP)
// - Eventual consistency
// - Always available
// - Use case: Social media feeds

// 3. Redis Cluster (AP)
// - Eventual consistency between nodes
// - High availability
// - Use case: Caching

// 4. Raft Consensus (CP)
// - Strong consistency
// - Tolerates partitions
// - Use case: etcd, Consul
```

---

### Q2: Explain different consistency models (Strong, Eventual, Causal). When would you use each?

**Answer:**

**1. Strong Consistency**

```java
// All reads see latest write immediately
public class StrongConsistency {
    private final ReplicatedDatabase database;
    
    public void write(String key, String value) {
        // Write to all replicas synchronously
        for (Replica replica : database.getReplicas()) {
            replica.write(key, value);  // Wait for all
        }
    }
    
    public String read(String key) {
        // Read from any replica (all have same data)
        return database.getReplicas().get(0).read(key);
    }
}

// Use Cases:
// - Financial transactions
// - Inventory management
// - Critical business data
```

**2. Eventual Consistency**

```java
// System will become consistent eventually
public class EventualConsistency {
    private final DistributedCache cache;
    
    public void write(String key, String value) {
        // Write to primary node
        cache.getPrimaryNode().write(key, value);
        
        // Replicate asynchronously
        asyncReplicate(key, value);
    }
    
    public String read(String key) {
        // Read from any node (may be stale)
        return cache.getAnyNode().read(key);
    }
    
    private void asyncReplicate(String key, String value) {
        executor.submit(() -> {
            for (Node node : cache.getReplicaNodes()) {
                node.write(key, value);  // Eventually all nodes updated
            }
        });
    }
}

// Use Cases:
// - Social media feeds
// - User profiles
// - Content delivery
// - Analytics data
```

**3. Causal Consistency**

```java
// Preserves cause-and-effect relationships
public class CausalConsistency {
    private final VectorClock vectorClock = new VectorClock();
    
    public void write(String key, String value, String userId) {
        // Increment vector clock
        vectorClock.increment(userId);
        
        // Write with vector clock
        WriteOperation op = new WriteOperation(key, value, vectorClock.copy());
        database.write(op);
        
        // Replicate with vector clock
        replicate(op);
    }
    
    public String read(String key) {
        // Read operations with vector clock
        // Can determine causal relationships
        return database.read(key);
    }
}

// Use Cases:
// - Collaborative editing
// - Social networks
// - Event ordering
```

**4. Read-Your-Writes Consistency**

```java
// User always sees their own writes
public class ReadYourWrites {
    private final Map<String, Long> userTimestamps = new ConcurrentHashMap<>();
    
    public void write(String userId, String key, String value) {
        long timestamp = System.currentTimeMillis();
        database.write(key, value, timestamp);
        userTimestamps.put(userId, timestamp);
    }
    
    public String read(String userId, String key) {
        Long userWriteTime = userTimestamps.get(userId);
        
        // Read from node that has at least user's write timestamp
        return database.read(key, userWriteTime);
    }
}
```

---

### Q3: Design a distributed lock service. How would you handle failures and ensure fairness?

**Answer:**

**Distributed Lock Implementation:**

```java
// Using Redis
public class DistributedLock {
    private final JedisPool jedisPool;
    private final String lockKey;
    private final String lockValue;
    private final int expireTimeSeconds;
    
    public DistributedLock(JedisPool jedisPool, String lockKey, int expireTimeSeconds) {
        this.jedisPool = jedisPool;
        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString();
        this.expireTimeSeconds = expireTimeSeconds;
    }
    
    public boolean tryLock() {
        try (Jedis jedis = jedisPool.getResource()) {
            // SET with NX (only if not exists) and EX (expiration)
            String result = jedis.set(lockKey, lockValue, 
                SetParams.setParams().nx().ex(expireTimeSeconds));
            return "OK".equals(result);
        }
    }
    
    public void unlock() {
        try (Jedis jedis = jedisPool.getResource()) {
            // Lua script for atomic unlock
            String script = 
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";
            
            jedis.eval(script, Collections.singletonList(lockKey), 
                      Collections.singletonList(lockValue));
        }
    }
    
    public boolean tryLockWithTimeout(long timeoutMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (tryLock()) {
                return true;
            }
            Thread.sleep(100);  // Retry after 100ms
        }
        
        return false;
    }
}

// Using ZooKeeper
public class ZooKeeperDistributedLock {
    private final ZooKeeper zookeeper;
    private final String lockPath;
    private String lockNode;
    
    public ZooKeeperDistributedLock(ZooKeeper zookeeper, String lockPath) {
        this.zookeeper = zookeeper;
        this.lockPath = lockPath;
    }
    
    public void lock() throws Exception {
        // Create ephemeral sequential node
        lockNode = zookeeper.create(lockPath + "/lock-", 
            new byte[0], 
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        // Wait for lock
        while (true) {
            List<String> children = zookeeper.getChildren(lockPath, false);
            Collections.sort(children);
            
            if (lockNode.equals(lockPath + "/" + children.get(0))) {
                // We have the lock
                return;
            }
            
            // Watch previous node
            String previousNode = lockPath + "/" + 
                children.get(Collections.binarySearch(children, 
                    lockNode.substring(lockNode.lastIndexOf("/") + 1)) - 1);
            
            CountDownLatch latch = new CountDownLatch(1);
            zookeeper.exists(previousNode, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted) {
                    latch.countDown();
                }
            });
            latch.await();
        }
    }
    
    public void unlock() throws Exception {
        zookeeper.delete(lockNode, -1);
    }
}
```

**Handling Failures:**

```java
// 1. Lock Expiration (Prevent deadlock)
public class SafeDistributedLock {
    private final DistributedLock lock;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> renewalTask;
    
    public void lockWithRenewal() {
        if (lock.tryLock()) {
            // Start renewal task
            renewalTask = scheduler.scheduleAtFixedRate(
                () -> lock.renew(), 
                0, 
                lock.getExpireTime() / 2, 
                TimeUnit.SECONDS
            );
        }
    }
    
    public void unlock() {
        if (renewalTask != null) {
            renewalTask.cancel(false);
        }
        lock.unlock();
    }
}

// 2. Fencing Token (Prevent split-brain)
public class FencedDistributedLock {
    private final DistributedLock lock;
    private final AtomicLong fencingToken = new AtomicLong(0);
    
    public long lockWithFencing() {
        if (lock.tryLock()) {
            long token = fencingToken.incrementAndGet();
            // Store token with lock
            lock.setFencingToken(token);
            return token;
        }
        throw new LockAcquisitionException();
    }
    
    public void performOperation(long token) {
        // Verify token is still valid
        if (lock.getFencingToken() != token) {
            throw new StaleLockException();
        }
        // Perform operation
    }
}
```

**Fairness:**

```java
// Fair Lock using Queue
public class FairDistributedLock {
    private final JedisPool jedisPool;
    private final String lockKey;
    private final String queueKey;
    
    public void lock() {
        String requestId = UUID.randomUUID().toString();
        
        try (Jedis jedis = jedisPool.getResource()) {
            // Add to queue
            jedis.lpush(queueKey, requestId);
            
            // Wait for turn
            while (true) {
                String head = jedis.lindex(queueKey, 0);
                if (requestId.equals(head)) {
                    // Try to acquire lock
                    if (tryLock(lockKey)) {
                        return;  // Got lock
                    }
                }
                Thread.sleep(100);
            }
        }
    }
    
    public void unlock() {
        try (Jedis jedis = jedisPool.getResource()) {
            unlock(lockKey);
            // Remove from queue
            jedis.lpop(queueKey);
        }
    }
}
```

---

## Summary: Part 5

### Key Topics Covered:
1. CAP Theorem
2. Consistency Models
3. Distributed Lock Service

### Principal Engineer Focus:
- Distributed system design
- Trade-off analysis
- Failure handling
- Consistency guarantees

---

**Next**: Part 6 will cover Design Patterns & Best Practices.

