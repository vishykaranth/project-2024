# Distributed Systems - Part 1: Network Partitions & Consensus

## Question 231: How do you handle network partitions?

### Answer

### Network Partition Handling

#### 1. **Partition Detection**

```java
@Service
public class PartitionDetectionService {
    @Scheduled(fixedRate = 5000)
    public void detectPartitions() {
        List<ServiceInstance> instances = getAllInstances();
        
        for (ServiceInstance instance : instances) {
            if (!isReachable(instance)) {
                handlePartition(instance);
            }
        }
    }
    
    private void handlePartition(ServiceInstance instance) {
        // Mark as partitioned
        markAsPartitioned(instance);
        
        // Choose strategy based on CAP trade-off
        if (requiresConsistency()) {
            // CP: Stop accepting writes
            stopAcceptingWrites();
        } else {
            // AP: Continue serving with degraded consistency
            continueServingWithDegradedConsistency();
        }
    }
}
```

#### 2. **Partition Recovery**

```java
@Service
public class PartitionRecoveryService {
    public void recoverFromPartition(PartitionEvent event) {
        // Detect partition resolution
        if (event.getType() == PartitionType.RESOLVED) {
            // Reconcile state
            reconcileState();
            
            // Resume normal operations
            resumeNormalOperations();
        }
    }
    
    private void reconcileState() {
        // Compare state across partitions
        State stateFromPartition1 = getStateFromPartition1();
        State stateFromPartition2 = getStateFromPartition2();
        
        // Resolve conflicts
        State reconciled = resolveConflicts(stateFromPartition1, stateFromPartition2);
        
        // Update all partitions
        updateAllPartitions(reconciled);
    }
}
```

---

## Question 232: What's the leader election strategy?

### Answer

### Leader Election Strategies

#### 1. **ZooKeeper Leader Election**

```java
@Service
public class ZooKeeperLeaderElection {
    private final CuratorFramework client;
    
    public void electLeader() {
        LeaderLatch leaderLatch = new LeaderLatch(
            client,
            "/leader-election"
        );
        
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                // This instance is the leader
                becomeLeader();
            }
            
            @Override
            public void notLeader() {
                // This instance is not the leader
                becomeFollower();
            }
        });
        
        leaderLatch.start();
    }
}
```

#### 2. **Redis Leader Election**

```java
@Service
public class RedisLeaderElection {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean tryElectLeader(String serviceId) {
        String lockKey = "leader:" + serviceId;
        String lockValue = UUID.randomUUID().toString();
        
        // Try to acquire leadership
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(30));
        
        if (acquired) {
            // This instance is the leader
            startLeaderHeartbeat(lockKey, lockValue);
            return true;
        }
        
        return false;
    }
    
    @Scheduled(fixedRate = 10000)
    private void startLeaderHeartbeat(String lockKey, String lockValue) {
        // Renew leadership
        redisTemplate.expire(lockKey, Duration.ofSeconds(30));
    }
}
```

---

## Question 233: How do you handle split-brain scenarios?

### Answer

### Split-Brain Prevention

#### 1. **Quorum-Based Prevention**

```java
@Service
public class QuorumBasedService {
    private static final int QUORUM = (getTotalInstances() / 2) + 1;
    
    public boolean canPerformOperation() {
        int availableInstances = getAvailableInstances();
        return availableInstances >= QUORUM;
    }
    
    public void performOperation(Operation op) {
        if (!canPerformOperation()) {
            throw new QuorumNotMetException("Not enough instances available");
        }
        
        // Perform operation
        executeOperation(op);
    }
}
```

#### 2. **Fencing Tokens**

```java
@Service
public class FencingTokenService {
    private final AtomicLong fencingToken = new AtomicLong(0);
    
    public long getFencingToken() {
        return fencingToken.incrementAndGet();
    }
    
    public boolean validateFencingToken(long token) {
        // Only accept operations with higher token
        return token > getLastAcceptedToken();
    }
}
```

---

## Question 234: What's the quorum strategy?

### Answer

### Quorum Strategies

#### 1. **Majority Quorum**

```java
@Service
public class MajorityQuorumService {
    public boolean hasQuorum() {
        int totalInstances = getTotalInstances();
        int availableInstances = getAvailableInstances();
        int quorum = (totalInstances / 2) + 1;
        
        return availableInstances >= quorum;
    }
}
```

#### 2. **Weighted Quorum**

```java
@Service
public class WeightedQuorumService {
    public boolean hasQuorum() {
        int totalWeight = getTotalWeight();
        int availableWeight = getAvailableWeight();
        int quorumWeight = (totalWeight / 2) + 1;
        
        return availableWeight >= quorumWeight;
    }
}
```

---

## Question 235: How do you ensure distributed consensus?

### Answer

### Distributed Consensus Algorithms

#### 1. **Raft Algorithm**

```
┌─────────────────────────────────────────────────────────┐
│         Raft Consensus                                  │
└─────────────────────────────────────────────────────────┘

Roles:
├─ Leader: Handles all client requests
├─ Follower: Replicates log entries
└─ Candidate: Runs for leadership

Consensus:
├─ Leader election
├─ Log replication
└─ Majority agreement
```

#### 2. **Paxos Algorithm**

```java
@Service
public class PaxosConsensus {
    public Value propose(Proposal proposal) {
        // Phase 1: Prepare
        PrepareResponse response = prepare(proposal);
        
        if (response.isAccepted()) {
            // Phase 2: Accept
            AcceptResponse acceptResponse = accept(proposal, response.getValue());
            
            if (acceptResponse.isAccepted()) {
                // Consensus reached
                return acceptResponse.getValue();
            }
        }
        
        // Retry with higher proposal number
        return propose(incrementProposalNumber(proposal));
    }
}
```

---

## Summary

Part 1 covers:

1. **Network Partitions**: Detection, handling, recovery
2. **Leader Election**: ZooKeeper, Redis-based
3. **Split-Brain**: Quorum, fencing tokens
4. **Quorum Strategy**: Majority, weighted
5. **Distributed Consensus**: Raft, Paxos algorithms

Key principles:
- Detect and handle partitions appropriately
- Use leader election for coordination
- Prevent split-brain with quorum
- Ensure consensus for critical operations
