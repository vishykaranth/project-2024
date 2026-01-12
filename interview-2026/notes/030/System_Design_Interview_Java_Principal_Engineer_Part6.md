# System Design Interview Questions for Java Principal Engineers - Part 6

## Distributed Systems, Consistency, and CAP Theorem

This part covers distributed systems fundamentals, consistency models, and handling distributed system challenges.

---

## Interview Question 25: Explain CAP Theorem and Design Trade-offs

### CAP Theorem

**CAP Theorem** states that in a distributed system, you can only guarantee two out of three:
- **Consistency**: All nodes see the same data simultaneously
- **Availability**: System remains operational
- **Partition Tolerance**: System continues despite network failures

### Design Choices

#### CP System (Consistency + Partition Tolerance)
```java
// Example: Distributed Lock Service
@Service
public class DistributedLockService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    public boolean acquireLock(String resource, Duration timeout) {
        // Strong consistency - all nodes must agree
        // If partition occurs, system may become unavailable
        return redis.opsForValue().setIfAbsent(
            "lock:" + resource,
            UUID.randomUUID().toString(),
            timeout
        );
    }
}
```

#### AP System (Availability + Partition Tolerance)
```java
// Example: CDN or Cache
@Service
public class CacheService {
    @Autowired
    private List<RedisTemplate<String, String>> redisInstances;
    
    public String get(String key) {
        // Try any available node
        // May return stale data (eventual consistency)
        for (RedisTemplate<String, String> redis : redisInstances) {
            try {
                return redis.opsForValue().get(key);
            } catch (Exception e) {
                // Try next node
                continue;
            }
        }
        return null;
    }
}
```

#### CA System (Consistency + Availability)
```java
// Example: Single-node database
// Not partition-tolerant - fails if network partitions
// Only works in single data center
```

---

## Interview Question 26: Design a Distributed Lock with Consensus

### Requirements

- Mutual exclusion
- Deadlock prevention
- Fault tolerance
- Leader election

### Raft Consensus Implementation

```java
public enum NodeState {
    FOLLOWER, CANDIDATE, LEADER
}

@Component
public class RaftNode {
    private NodeState state = NodeState.FOLLOWER;
    private String currentLeader;
    private long currentTerm = 0;
    private long lastHeartbeat = System.currentTimeMillis();
    
    @Scheduled(fixedRate = 1000)
    public void checkHeartbeat() {
        if (state == NodeState.LEADER) {
            sendHeartbeat();
        } else if (System.currentTimeMillis() - lastHeartbeat > 5000) {
            // No heartbeat received - become candidate
            startElection();
        }
    }
    
    private void startElection() {
        state = NodeState.CANDIDATE;
        currentTerm++;
        
        // Request votes from other nodes
        int votes = 1; // Vote for self
        for (String node : otherNodes) {
            if (requestVote(node, currentTerm)) {
                votes++;
            }
        }
        
        // If majority, become leader
        if (votes > otherNodes.size() / 2) {
            state = NodeState.LEADER;
            currentLeader = nodeId;
        }
    }
    
    public boolean acquireLock(String resource, String clientId) {
        if (state != NodeState.LEADER) {
            // Redirect to leader
            return redirectToLeader(resource, clientId);
        }
        
        // Leader can grant lock
        return lockService.acquire(resource, clientId);
    }
}
```

### ZooKeeper-Based Distributed Lock

```java
@Service
public class ZooKeeperLockService {
    @Autowired
    private CuratorFramework zkClient;
    
    public InterProcessMutex acquireLock(String lockPath) {
        InterProcessMutex lock = new InterProcessMutex(zkClient, lockPath);
        try {
            lock.acquire(30, TimeUnit.SECONDS);
            return lock;
        } catch (Exception e) {
            throw new LockAcquisitionException("Failed to acquire lock", e);
        }
    }
    
    public void releaseLock(InterProcessMutex lock) {
        try {
            lock.release();
        } catch (Exception e) {
            throw new LockReleaseException("Failed to release lock", e);
        }
    }
}
```

---

## Interview Question 27: Design Eventual Consistency System

### Requirements

- High availability
- Eventual consistency
- Conflict resolution
- Vector clocks

### Vector Clock Implementation

```java
public class VectorClock {
    private final Map<String, Long> clock = new ConcurrentHashMap<>();
    
    public void tick(String nodeId) {
        clock.put(nodeId, clock.getOrDefault(nodeId, 0L) + 1);
    }
    
    public VectorClock merge(VectorClock other) {
        VectorClock merged = new VectorClock();
        Set<String> allNodes = new HashSet<>(this.clock.keySet());
        allNodes.addAll(other.clock.keySet());
        
        for (String node : allNodes) {
            long max = Math.max(
                this.clock.getOrDefault(node, 0L),
                other.clock.getOrDefault(node, 0L)
            );
            merged.clock.put(node, max);
        }
        
        return merged;
    }
    
    public boolean happensBefore(VectorClock other) {
        boolean strictlyLess = false;
        for (String node : clock.keySet()) {
            long thisTime = clock.get(node);
            long otherTime = other.clock.getOrDefault(node, 0L);
            
            if (thisTime > otherTime) {
                return false;
            }
            if (thisTime < otherTime) {
                strictlyLess = true;
            }
        }
        return strictlyLess;
    }
}

@Entity
public class ReplicatedData {
    @Id
    private String key;
    private String value;
    
    @Embedded
    private VectorClock vectorClock;
    
    private Instant lastUpdated;
}

@Service
public class EventuallyConsistentStore {
    @Autowired
    private ReplicatedDataRepository repository;
    
    public void put(String key, String value, String nodeId) {
        ReplicatedData data = repository.findById(key).orElse(new ReplicatedData());
        data.setKey(key);
        data.setValue(value);
        
        // Update vector clock
        if (data.getVectorClock() == null) {
            data.setVectorClock(new VectorClock());
        }
        data.getVectorClock().tick(nodeId);
        data.setLastUpdated(Instant.now());
        
        repository.save(data);
    }
    
    public void sync(ReplicatedData incoming) {
        ReplicatedData local = repository.findById(incoming.getKey())
            .orElse(new ReplicatedData());
        
        if (local.getVectorClock() == null) {
            // No local data - accept incoming
            repository.save(incoming);
        } else if (incoming.getVectorClock().happensBefore(local.getVectorClock())) {
            // Incoming is older - keep local
            return;
        } else if (local.getVectorClock().happensBefore(incoming.getVectorClock())) {
            // Local is older - accept incoming
            repository.save(incoming);
        } else {
            // Conflict - need resolution
            resolveConflict(local, incoming);
        }
    }
    
    private void resolveConflict(ReplicatedData local, ReplicatedData incoming) {
        // Last-write-wins (simple strategy)
        if (incoming.getLastUpdated().isAfter(local.getLastUpdated())) {
            // Merge vector clocks
            VectorClock merged = local.getVectorClock().merge(incoming.getVectorClock());
            incoming.setVectorClock(merged);
            repository.save(incoming);
        } else {
            VectorClock merged = local.getVectorClock().merge(incoming.getVectorClock());
            local.setVectorClock(merged);
            repository.save(local);
        }
    }
}
```

---

## Interview Question 28: Design a Distributed Transaction Coordinator

### Requirements

- Two-Phase Commit (2PC)
- Transaction logging
- Recovery mechanism
- Participant coordination

### Two-Phase Commit Implementation

```java
@Service
public class TransactionCoordinator {
    @Autowired
    private TransactionLog transactionLog;
    
    @Autowired
    private List<TransactionParticipant> participants;
    
    public void executeTransaction(Transaction transaction) {
        String transactionId = UUID.randomUUID().toString();
        transaction.setId(transactionId);
        
        // Log transaction start
        transactionLog.log(transactionId, TransactionState.PREPARING);
        
        try {
            // Phase 1: Prepare
            List<Boolean> prepareResults = new ArrayList<>();
            for (TransactionParticipant participant : participants) {
                try {
                    boolean prepared = participant.prepare(transaction);
                    prepareResults.add(prepared);
                } catch (Exception e) {
                    prepareResults.add(false);
                }
            }
            
            // Check if all prepared
            boolean allPrepared = prepareResults.stream().allMatch(b -> b);
            
            if (allPrepared) {
                // Phase 2: Commit
                transactionLog.log(transactionId, TransactionState.COMMITTING);
                for (TransactionParticipant participant : participants) {
                    participant.commit(transaction);
                }
                transactionLog.log(transactionId, TransactionState.COMMITTED);
            } else {
                // Phase 2: Abort
                transactionLog.log(transactionId, TransactionState.ABORTING);
                for (TransactionParticipant participant : participants) {
                    participant.abort(transaction);
                }
                transactionLog.log(transactionId, TransactionState.ABORTED);
            }
        } catch (Exception e) {
            // Abort on error
            abortTransaction(transactionId);
            throw new TransactionException("Transaction failed", e);
        }
    }
    
    @Scheduled(fixedRate = 60000)
    public void recoverTransactions() {
        // Find transactions in intermediate states
        List<Transaction> pendingTransactions = transactionLog
            .findByStateIn(TransactionState.PREPARING, TransactionState.COMMITTING);
        
        for (Transaction transaction : pendingTransactions) {
            recoverTransaction(transaction);
        }
    }
}

public interface TransactionParticipant {
    boolean prepare(Transaction transaction);
    void commit(Transaction transaction);
    void abort(Transaction transaction);
}
```

---

## Interview Question 29: Design a Quorum-Based System

### Requirements

- Read and write quorums
- Consistency guarantees
- Fault tolerance

### Quorum Implementation

```java
@Service
public class QuorumBasedStore {
    private final List<Replica> replicas;
    private final int readQuorum;
    private final int writeQuorum;
    
    public QuorumBasedStore(List<Replica> replicas) {
        this.replicas = replicas;
        int n = replicas.size();
        this.writeQuorum = (n / 2) + 1; // Majority
        this.readQuorum = n - writeQuorum + 1; // Read majority
    }
    
    public void write(String key, String value) {
        int successCount = 0;
        Version version = new Version(System.currentTimeMillis());
        
        for (Replica replica : replicas) {
            try {
                replica.write(key, value, version);
                successCount++;
                
                if (successCount >= writeQuorum) {
                    return; // Write quorum achieved
                }
            } catch (Exception e) {
                // Continue to other replicas
            }
        }
        
        throw new QuorumException("Failed to achieve write quorum");
    }
    
    public String read(String key) {
        Map<Version, String> values = new HashMap<>();
        Map<Version, Integer> versionCounts = new HashMap<>();
        
        for (Replica replica : replicas) {
            try {
                ReplicaValue value = replica.read(key);
                if (value != null) {
                    values.put(value.getVersion(), value.getValue());
                    versionCounts.merge(value.getVersion(), 1, Integer::sum);
                }
            } catch (Exception e) {
                // Continue to other replicas
            }
        }
        
        // Find version with read quorum
        for (Map.Entry<Version, Integer> entry : versionCounts.entrySet()) {
            if (entry.getValue() >= readQuorum) {
                return values.get(entry.getKey());
            }
        }
        
        throw new QuorumException("Failed to achieve read quorum");
    }
}
```

---

## Interview Question 30: Design a Distributed Rate Limiter

### Requirements

- Distributed rate limiting
- Sliding window
- High performance

### Redis-Based Distributed Rate Limiter

```java
@Service
public class DistributedRateLimiter {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    public boolean isAllowed(String key, int maxRequests, Duration window) {
        String redisKey = "ratelimit:" + key;
        long now = System.currentTimeMillis();
        long windowStart = now - window.toMillis();
        
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

// Token Bucket Algorithm
@Service
public class TokenBucketRateLimiter {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    public boolean isAllowed(String key, int capacity, int refillRate, Duration refillPeriod) {
        String redisKey = "tokenbucket:" + key;
        long now = System.currentTimeMillis();
        
        // Lua script for atomic operations
        String luaScript = 
            "local key = KEYS[1] " +
            "local capacity = tonumber(ARGV[1]) " +
            "local refillRate = tonumber(ARGV[2]) " +
            "local refillPeriod = tonumber(ARGV[3]) " +
            "local now = tonumber(ARGV[4]) " +
            "local tokens = tonumber(redis.call('get', key) or capacity) " +
            "local lastRefill = tonumber(redis.call('get', key .. ':last') or now) " +
            "local elapsed = now - lastRefill " +
            "local tokensToAdd = math.floor(elapsed / refillPeriod * refillRate) " +
            "tokens = math.min(capacity, tokens + tokensToAdd) " +
            "if tokens >= 1 then " +
            "    tokens = tokens - 1 " +
            "    redis.call('set', key, tokens) " +
            "    redis.call('set', key .. ':last', now) " +
            "    return 1 " +
            "else " +
            "    redis.call('set', key, tokens) " +
            "    redis.call('set', key .. ':last', now) " +
            "    return 0 " +
            "end";
        
        Long result = redis.execute(
            new DefaultRedisScript<>(luaScript, Long.class),
            Collections.singletonList(redisKey),
            String.valueOf(capacity),
            String.valueOf(refillRate),
            String.valueOf(refillPeriod.toMillis()),
            String.valueOf(now)
        );
        
        return result == 1;
    }
}
```

---

## Summary: Part 6

### Key Topics Covered:
1. ✅ CAP Theorem and trade-offs
2. ✅ Distributed consensus (Raft, ZooKeeper)
3. ✅ Eventual consistency and vector clocks
4. ✅ Two-Phase Commit (2PC)
5. ✅ Quorum-based systems
6. ✅ Distributed rate limiting

### Key Concepts:
- **CAP Trade-offs**: Choose based on requirements
- **Consistency Models**: Strong vs eventual
- **Consensus Algorithms**: For coordination
- **Quorum**: Majority-based operations

---

**Next**: Part 7 will cover Security, Authentication, Authorization, and Encryption.

