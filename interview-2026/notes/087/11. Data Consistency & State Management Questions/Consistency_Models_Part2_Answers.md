# Consistency Models - Part 2: Conflict Resolution & Concurrency

## Question 216: What's the conflict resolution strategy?

### Answer

### Conflict Resolution Strategies

#### 1. **Conflict Types**

```
┌─────────────────────────────────────────────────────────┐
│         Conflict Types                                  │
└─────────────────────────────────────────────────────────┘

Write-Write Conflicts:
├─ Multiple writes to same data
├─ Last write wins
└─ Timestamp-based resolution

Read-Write Conflicts:
├─ Read during write
├─ Dirty reads
└─ Isolation level handling

Concurrent Update Conflicts:
├─ Optimistic locking failures
├─ Version conflicts
└─ Retry or merge
```

#### 2. **Last-Write-Wins (LWW)**

```java
@Service
public class LastWriteWinsService {
    private final RedisTemplate<String, State> redisTemplate;
    
    public void updateState(String entityId, State newState) {
        String key = "state:" + entityId;
        
        // Get current state
        State currentState = redisTemplate.opsForValue().get(key);
        
        if (currentState == null) {
            // First write
            newState.setTimestamp(Instant.now());
            redisTemplate.opsForValue().set(key, newState);
            return;
        }
        
        // Compare timestamps
        if (newState.getTimestamp().isAfter(currentState.getTimestamp())) {
            // Newer write wins
            redisTemplate.opsForValue().set(key, newState);
        } else {
            // Older write, ignore or throw exception
            throw new ConflictException("Write conflict: newer state exists");
        }
    }
}
```

#### 3. **Version-Based Conflict Resolution**

```java
@Entity
public class VersionedState {
    @Id
    private String entityId;
    private State state;
    @Version
    private Long version; // Optimistic locking
    
    // Getters and setters
}

@Service
public class VersionBasedConflictResolution {
    @Transactional
    public void updateState(String entityId, State newState, Long expectedVersion) {
        VersionedState current = stateRepository.findByEntityId(entityId);
        
        // Check version
        if (!current.getVersion().equals(expectedVersion)) {
            throw new OptimisticLockingFailureException(
                "Version conflict: expected " + expectedVersion + 
                " but found " + current.getVersion()
            );
        }
        
        // Update state and version
        current.setState(newState);
        current.setVersion(current.getVersion() + 1);
        stateRepository.save(current);
    }
}
```

#### 4. **Merge-Based Conflict Resolution**

```java
@Service
public class MergeBasedConflictResolution {
    public State resolveConflict(State state1, State state2) {
        State merged = new State();
        
        // Merge non-conflicting fields
        merged.setField1(state1.getField1() != null ? 
                        state1.getField1() : state2.getField1());
        merged.setField2(state1.getField2() != null ? 
                        state1.getField2() : state2.getField2());
        
        // For conflicting fields, use strategy
        if (state1.getField3() != null && state2.getField3() != null) {
            // Use most recent
            merged.setField3(
                state1.getTimestamp().isAfter(state2.getTimestamp()) ?
                state1.getField3() : state2.getField3()
            );
        }
        
        return merged;
    }
}
```

#### 5. **CRDT (Conflict-Free Replicated Data Types)**

```java
// Example: G-Set (Grow-only Set)
public class GSet<T> {
    private final Set<T> elements = new HashSet<>();
    
    public void add(T element) {
        elements.add(element);
    }
    
    public boolean contains(T element) {
        return elements.contains(element);
    }
    
    public GSet<T> merge(GSet<T> other) {
        GSet<T> merged = new GSet<>();
        merged.elements.addAll(this.elements);
        merged.elements.addAll(other.elements);
        return merged;
    }
}
```

---

## Question 217: How do you handle concurrent updates?

### Answer

### Concurrent Update Handling

#### 1. **Optimistic Locking**

```java
@Entity
public class Agent {
    @Id
    private String agentId;
    private AgentStatus status;
    @Version
    private Long version; // Optimistic locking field
    
    // Getters and setters
}

@Service
public class OptimisticLockingService {
    @Transactional
    public void updateAgentStatus(String agentId, AgentStatus newStatus) {
        Agent agent = agentRepository.findByAgentId(agentId);
        
        // Update status
        agent.setStatus(newStatus);
        
        try {
            // Save will check version
            agentRepository.save(agent);
        } catch (OptimisticLockingFailureException e) {
            // Another transaction updated, retry
            throw new ConcurrentUpdateException("Agent was updated by another transaction", e);
        }
    }
    
    public void updateWithRetry(String agentId, AgentStatus newStatus) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                updateAgentStatus(agentId, newStatus);
                return; // Success
            } catch (ConcurrentUpdateException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw e;
                }
                // Wait and retry
                try {
                    Thread.sleep(100 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }
}
```

#### 2. **Pessimistic Locking**

```java
@Service
public class PessimisticLockingService {
    @Transactional
    public void updateAgentStatus(String agentId, AgentStatus newStatus) {
        // Lock row for update
        Agent agent = agentRepository.findByAgentIdForUpdate(agentId);
        
        // Update status
        agent.setStatus(newStatus);
        agentRepository.save(agent);
        
        // Lock released when transaction commits
    }
}

// Repository method
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Agent a WHERE a.agentId = :agentId")
Agent findByAgentIdForUpdate(@Param("agentId") String agentId);
```

#### 3. **Distributed Locks**

```java
@Service
public class DistributedLockService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void updateWithDistributedLock(String agentId, AgentStatus newStatus) {
        String lockKey = "lock:agent:" + agentId;
        String lockValue = UUID.randomUUID().toString();
        
        // Acquire lock
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));
        
        if (!lockAcquired) {
            throw new LockAcquisitionException("Failed to acquire lock");
        }
        
        try {
            // Critical section
            Agent agent = agentRepository.findByAgentId(agentId);
            agent.setStatus(newStatus);
            agentRepository.save(agent);
            
        } finally {
            // Release lock
            releaseLock(lockKey, lockValue);
        }
    }
    
    private void releaseLock(String lockKey, String lockValue) {
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";
        
        redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(lockKey),
            lockValue
        );
    }
}
```

#### 4. **Event Sourcing for Concurrent Updates**

```java
@Service
public class EventSourcedUpdateService {
    private final EventStore eventStore;
    
    public void updateState(String entityId, StateUpdate update) {
        // Append event (atomic operation)
        StateUpdatedEvent event = new StateUpdatedEvent(
            entityId, 
            update, 
            Instant.now()
        );
        
        eventStore.appendEvent(entityId, event);
        
        // Events are processed sequentially per entity
        // No conflicts possible
    }
}
```

---

## Question 218: What's the optimistic vs pessimistic locking strategy?

### Answer

### Locking Strategy Comparison

#### 1. **Optimistic Locking**

```
┌─────────────────────────────────────────────────────────┐
│         Optimistic Locking                             │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Assume no conflicts
├─ Check version on update
├─ Retry on conflict
└─ No locks during read

Pros:
├─ Better performance
├─ No deadlocks
├─ Non-blocking reads
└─ Good for low contention

Cons:
├─ Retry overhead
├─ May fail after retries
└─ Not suitable for high contention
```

**Implementation:**

```java
@Entity
public class OptimisticEntity {
    @Version
    private Long version;
    
    // Fields
}

@Service
public class OptimisticLockingService {
    @Transactional
    public void update(OptimisticEntity entity) {
        // No lock acquired
        // Version checked on save
        repository.save(entity);
    }
}
```

#### 2. **Pessimistic Locking**

```
┌─────────────────────────────────────────────────────────┐
│         Pessimistic Locking                             │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Lock on read
├─ Hold lock until commit
├─ Prevent conflicts
└─ Blocking operations

Pros:
├─ Guaranteed consistency
├─ No retries needed
├─ Good for high contention
└─ Prevents conflicts

Cons:
├─ Lower performance
├─ Deadlock risk
├─ Blocking reads
└─ Resource contention
```

**Implementation:**

```java
@Service
public class PessimisticLockingService {
    @Transactional
    public void update(String entityId) {
        // Lock acquired on read
        Entity entity = repository.findByIdForUpdate(entityId);
        
        // Update
        entity.setField("new value");
        repository.save(entity);
        
        // Lock released on commit
    }
}
```

#### 3. **Strategy Selection**

```java
public class LockingStrategySelector {
    public LockingStrategy selectStrategy(
            int contentionLevel,
            boolean requiresGuaranteedConsistency) {
        
        if (requiresGuaranteedConsistency) {
            return LockingStrategy.PESSIMISTIC;
        }
        
        if (contentionLevel > 50) { // High contention
            return LockingStrategy.PESSIMISTIC;
        } else {
            return LockingStrategy.OPTIMISTIC;
        }
    }
}
```

#### 4. **Hybrid Approach**

```java
@Service
public class HybridLockingService {
    public void update(String entityId, Update update) {
        // Try optimistic first
        try {
            updateOptimistic(entityId, update);
        } catch (OptimisticLockingFailureException e) {
            // Fallback to pessimistic
            updatePessimistic(entityId, update);
        }
    }
}
```

---

## Question 219: How do you handle distributed locks?

### Answer

### Distributed Lock Implementation

#### 1. **Redis Distributed Lock**

```java
@Service
public class RedisDistributedLock {
    private final RedisTemplate<String, String> redisTemplate;
    
    public LockHandle acquireLock(String lockKey, Duration timeout) {
        String lockValue = UUID.randomUUID().toString();
        
        // Try to acquire lock
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, timeout);
        
        if (!acquired) {
            return null; // Lock not acquired
        }
        
        return new LockHandle(lockKey, lockValue);
    }
    
    public void releaseLock(LockHandle handle) {
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";
        
        redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(handle.getLockKey()),
            handle.getLockValue()
        );
    }
}
```

#### 2. **Lock with Auto-Renewal**

```java
@Service
public class AutoRenewingLock {
    private final RedisTemplate<String, String> redisTemplate;
    private final ScheduledExecutorService scheduler;
    
    public LockHandle acquireLockWithRenewal(String lockKey, Duration timeout) {
        String lockValue = UUID.randomUUID().toString();
        
        // Acquire lock
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, timeout);
        
        if (!acquired) {
            return null;
        }
        
        // Start renewal task
        ScheduledFuture<?> renewalTask = scheduler.scheduleAtFixedRate(
            () -> renewLock(lockKey, lockValue, timeout),
            timeout.toMillis() / 2,
            timeout.toMillis() / 2,
            TimeUnit.MILLISECONDS
        );
        
        return new LockHandle(lockKey, lockValue, renewalTask);
    }
    
    private void renewLock(String lockKey, String lockValue, Duration timeout) {
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('pexpire', KEYS[1], ARGV[2]) " +
            "else " +
            "  return 0 " +
            "end";
        
        redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(lockKey),
            lockValue,
            String.valueOf(timeout.toMillis())
        );
    }
}
```

#### 3. **Redlock Algorithm (Multiple Redis Instances)**

```java
@Service
public class RedlockService {
    private final List<RedisTemplate<String, String>> redisInstances;
    
    public boolean acquireRedlock(String lockKey, Duration timeout) {
        String lockValue = UUID.randomUUID().toString();
        int quorum = (redisInstances.size() / 2) + 1;
        int acquiredCount = 0;
        
        long startTime = System.currentTimeMillis();
        
        // Try to acquire lock on all instances
        for (RedisTemplate<String, String> redis : redisInstances) {
            if (System.currentTimeMillis() - startTime > timeout.toMillis()) {
                break; // Timeout
            }
            
            Boolean acquired = redis.opsForValue()
                .setIfAbsent(lockKey, lockValue, timeout);
            
            if (acquired) {
                acquiredCount++;
            }
        }
        
        // Check if quorum reached
        if (acquiredCount >= quorum) {
            // Verify lock still valid
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed < timeout.toMillis()) {
                return true; // Lock acquired
            }
        }
        
        // Release locks on all instances
        releaseOnAllInstances(lockKey, lockValue);
        return false;
    }
}
```

---

## Question 220: What's the CAP theorem trade-off in your system?

### Answer

### CAP Theorem Analysis

#### 1. **CAP Theorem Overview**

```
┌─────────────────────────────────────────────────────────┐
│         CAP Theorem                                    │
└─────────────────────────────────────────────────────────┘

C - Consistency:
├─ All nodes see same data
└─ Strong consistency

A - Availability:
├─ System remains operational
└─ High availability

P - Partition Tolerance:
├─ System continues despite network failures
└─ Network partitions

Reality: Can only guarantee 2 out of 3
```

#### 2. **System-Specific Trade-offs**

**Conversational AI Platform: AP System**

```
┌─────────────────────────────────────────────────────────┐
│         AP System (Availability + Partition Tolerance) │
└─────────────────────────────────────────────────────────┘

Choice: Availability + Partition Tolerance
Sacrifice: Strong Consistency

Rationale:
├─ Real-time chat requires availability
├─ Network partitions are common
└─ Eventual consistency acceptable

Examples:
├─ Agent state: Eventually consistent
├─ Message delivery: At-least-once
├─ Cache: Eventually consistent
└─ Event processing: Eventually consistent

Consistency Mechanisms:
├─ Event ordering per partition
├─ Idempotent operations
├─ Conflict resolution
└─ Reconciliation jobs
```

**Prime Broker System: CP System**

```
┌─────────────────────────────────────────────────────────┐
│         CP System (Consistency + Partition Tolerance) │
└─────────────────────────────────────────────────────────┘

Choice: Consistency + Partition Tolerance
Sacrifice: Availability (temporary)

Rationale:
├─ Financial accuracy critical
├─ Data must be consistent
└─ Temporary unavailability acceptable

Examples:
├─ Position calculations: Strong consistency
├─ Ledger entries: Strong consistency
├─ Trade processing: Strong consistency
└─ Settlement: Strong consistency

Consistency Mechanisms:
├─ Distributed transactions (Saga)
├─ Event ordering guarantees
├─ Double-entry bookkeeping
└─ Reconciliation jobs
```

#### 3. **Trade-off Matrix**

```
┌─────────────────────────────────────────────────────────┐
│         Trade-off Matrix                               │
└─────────────────────────────────────────────────────────┘

Component              | Consistency | Availability | Partition
----------------------|-------------|--------------|-----------
Agent State           | Eventual    | High         | High
Message Delivery      | Eventual    | High         | High
NLU Responses         | Eventual    | High         | High
Cache                 | Eventual    | High         | High
----------------------|-------------|--------------|-----------
Position Calculations | Strong      | Medium       | High
Ledger Entries        | Strong      | Medium       | High
Trade Processing      | Strong      | Medium       | High
Settlement            | Strong      | Medium       | High
```

#### 4. **Partition Handling**

```java
@Service
public class PartitionHandlingService {
    public void handlePartition(PartitionEvent event) {
        if (event.getType() == PartitionType.NETWORK_PARTITION) {
            // AP System: Continue serving, accept eventual consistency
            continueServingWithDegradedConsistency();
            
            // CP System: Stop accepting writes, maintain consistency
            stopAcceptingWrites();
        }
    }
}
```

---

## Summary

Part 2 covers:

1. **Conflict Resolution**: LWW, version-based, merge-based, CRDTs
2. **Concurrent Updates**: Optimistic, pessimistic, distributed locks
3. **Locking Strategies**: Optimistic vs pessimistic, selection criteria
4. **Distributed Locks**: Redis locks, auto-renewal, Redlock
5. **CAP Theorem**: AP vs CP trade-offs, system-specific choices

Key principles:
- Use appropriate conflict resolution for your use case
- Choose locking strategy based on contention level
- Implement distributed locks carefully with proper release
- Understand CAP trade-offs and choose accordingly
