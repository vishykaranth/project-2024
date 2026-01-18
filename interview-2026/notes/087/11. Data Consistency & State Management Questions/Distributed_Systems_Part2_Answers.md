# Distributed Systems - Part 2: Failure Detection & Clock Synchronization

## Question 236: What's the failure detection mechanism?

### Answer

### Failure Detection Strategies

#### 1. **Heartbeat-Based Detection**

```java
@Service
public class HeartbeatFailureDetection {
    private final Map<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();
    private static final Duration HEARTBEAT_TIMEOUT = Duration.ofSeconds(10);
    
    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        String instanceId = getInstanceId();
        Instant now = Instant.now();
        
        // Send heartbeat
        sendHeartbeatToCoordinator(instanceId, now);
        
        // Update local timestamp
        lastHeartbeat.put(instanceId, now);
    }
    
    @Scheduled(fixedRate = 5000)
    public void detectFailures() {
        Instant now = Instant.now();
        
        for (Map.Entry<String, Instant> entry : lastHeartbeat.entrySet()) {
            Duration sinceLastHeartbeat = Duration.between(entry.getValue(), now);
            
            if (sinceLastHeartbeat.compareTo(HEARTBEAT_TIMEOUT) > 0) {
                // Instance failed
                handleFailure(entry.getKey());
            }
        }
    }
}
```

#### 2. **Ping-Based Detection**

```java
@Service
public class PingBasedFailureDetection {
    @Scheduled(fixedRate = 10000)
    public void pingInstances() {
        List<ServiceInstance> instances = getAllInstances();
        
        for (ServiceInstance instance : instances) {
            try {
                boolean reachable = ping(instance);
                if (!reachable) {
                    handleFailure(instance);
                }
            } catch (Exception e) {
                handleFailure(instance);
            }
        }
    }
}
```

---

## Question 237: How do you handle Byzantine failures?

### Answer

### Byzantine Failure Handling

#### 1. **Byzantine Fault Tolerance (BFT)**

```java
@Service
public class ByzantineFaultTolerance {
    private static final int TOTAL_INSTANCES = 3;
    private static final int BYZANTINE_INSTANCES = 1;
    private static final int REQUIRED_AGREEMENT = (2 * BYZANTINE_INSTANCES) + 1;
    
    public Result executeWithBFT(Operation operation) {
        // Send operation to all instances
        List<Result> results = new ArrayList<>();
        for (ServiceInstance instance : getAllInstances()) {
            Result result = instance.execute(operation);
            results.add(result);
        }
        
        // Require agreement from majority
        Map<Result, Integer> votes = new HashMap<>();
        for (Result result : results) {
            votes.put(result, votes.getOrDefault(result, 0) + 1);
        }
        
        // Find result with majority votes
        for (Map.Entry<Result, Integer> entry : votes.entrySet()) {
            if (entry.getValue() >= REQUIRED_AGREEMENT) {
                return entry.getKey();
            }
        }
        
        throw new ByzantineFailureException("No majority agreement");
    }
}
```

---

## Question 238: What's the gossip protocol usage?

### Answer

### Gossip Protocol

#### 1. **Gossip-Based State Propagation**

```java
@Service
public class GossipProtocolService {
    private final Random random = new Random();
    
    @Scheduled(fixedRate = 5000)
    public void gossip() {
        // Select random peer
        ServiceInstance peer = selectRandomPeer();
        
        // Exchange state
        exchangeState(peer);
    }
    
    private void exchangeState(ServiceInstance peer) {
        // Send local state
        State localState = getLocalState();
        sendState(peer, localState);
        
        // Receive peer state
        State peerState = receiveState(peer);
        
        // Merge states
        State merged = mergeStates(localState, peerState);
        updateLocalState(merged);
    }
}
```

---

## Question 239: How do you handle clock synchronization?

### Answer

### Clock Synchronization

#### 1. **NTP-Based Synchronization**

```java
@Service
public class ClockSynchronizationService {
    @Scheduled(fixedRate = 3600000) // Every hour
    public void synchronizeClock() {
        // Sync with NTP server
        syncWithNTP();
    }
    
    public Instant getSynchronizedTime() {
        // Use synchronized clock
        return Instant.now();
    }
}
```

#### 2. **Logical Clocks (Lamport Timestamps)**

```java
@Entity
public class EventWithLamportTimestamp {
    private String eventId;
    private long lamportTimestamp;
    
    public void updateTimestamp(long receivedTimestamp) {
        this.lamportTimestamp = Math.max(
            this.lamportTimestamp + 1,
            receivedTimestamp + 1
        );
    }
}
```

---

## Question 240: What's the vector clock implementation?

### Answer

### Vector Clocks

#### 1. **Vector Clock Implementation**

```java
@Entity
public class StateWithVectorClock {
    private String entityId;
    private State state;
    private Map<String, Long> vectorClock; // Service -> Timestamp
    
    public void updateVectorClock(String serviceId) {
        vectorClock.put(
            serviceId,
            vectorClock.getOrDefault(serviceId, 0L) + 1
        );
    }
    
    public void mergeVectorClock(Map<String, Long> otherClock) {
        for (Map.Entry<String, Long> entry : otherClock.entrySet()) {
            vectorClock.put(
                entry.getKey(),
                Math.max(
                    vectorClock.getOrDefault(entry.getKey(), 0L),
                    entry.getValue()
                )
            );
        }
    }
    
    public boolean happensBefore(StateWithVectorClock other) {
        boolean strictlyLess = false;
        
        for (String serviceId : vectorClock.keySet()) {
            long thisTime = vectorClock.get(serviceId);
            long otherTime = other.vectorClock.getOrDefault(serviceId, 0L);
            
            if (thisTime > otherTime) {
                return false; // Not happens-before
            }
            if (thisTime < otherTime) {
                strictlyLess = true;
            }
        }
        
        return strictlyLess;
    }
}
```

#### 2. **Vector Clock Usage**

```java
@Service
public class VectorClockService {
    public void updateState(String entityId, State newState) {
        StateWithVectorClock current = getState(entityId);
        
        // Update vector clock
        current.updateVectorClock(getServiceId());
        
        // Update state
        current.setState(newState);
        
        // Save
        saveState(current);
    }
    
    public void mergeStates(StateWithVectorClock state1, 
                           StateWithVectorClock state2) {
        // Check causality
        if (state1.happensBefore(state2)) {
            // state1 is older, use state2
            return state2;
        } else if (state2.happensBefore(state1)) {
            // state2 is older, use state1
            return state1;
        } else {
            // Concurrent updates, merge
            StateWithVectorClock merged = new StateWithVectorClock();
            merged.mergeVectorClock(state1.getVectorClock());
            merged.mergeVectorClock(state2.getVectorClock());
            merged.setState(mergeStates(state1.getState(), state2.getState()));
            return merged;
        }
    }
}
```

---

## Summary

Part 2 covers:

1. **Failure Detection**: Heartbeat, ping-based detection
2. **Byzantine Failures**: BFT algorithms
3. **Gossip Protocol**: State propagation
4. **Clock Synchronization**: NTP, logical clocks
5. **Vector Clocks**: Causality tracking, conflict detection

Key principles:
- Detect failures quickly with heartbeats
- Handle Byzantine failures with BFT
- Use gossip for efficient state propagation
- Synchronize clocks for ordering
- Use vector clocks for causality
