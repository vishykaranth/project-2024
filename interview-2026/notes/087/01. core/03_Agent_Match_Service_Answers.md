# Agent Match Service - Detailed Answers

## Question 14: Explain the Agent Match Service architecture. Why did you make it stateless?

### Answer

### Agent Match Service Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Agent Match Service                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Agent State │  │   Routing    │  │   Event      │  │
│  │  Manager     │  │   Engine     │  │  Generator   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────────┬─────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
        ┌─────────────┐ ┌──────────┐ ┌──────────┐
        │   Redis     │ │  Kafka   │ │ Postgres │
        │ (Agent State)│ │ (Events) │ │ (Config) │
        └─────────────┘ └──────────┘ └──────────┘
```

### Why Stateless?

#### 1. **Horizontal Scaling**

```
Stateless Design Benefits:
├─ Any instance can handle any request
├─ No sticky sessions required
├─ Easy to add/remove instances
└─ Load distribution

Stateful Design Problems:
├─ Sticky sessions required
├─ State loss on instance failure
├─ Difficult to scale
└─ Load imbalance
```

#### 2. **Fault Tolerance**

```
Stateless Service:
├─ Instance fails → Other instances continue
├─ No state loss
├─ Automatic recovery
└─ Zero downtime

Stateful Service:
├─ Instance fails → State lost
├─ User sessions lost
├─ Manual recovery needed
└─ Service interruption
```

#### 3. **State Storage Strategy**

```
State Storage:
├─ Agent State: Redis (fast access)
├─ Configuration: PostgreSQL (persistent)
├─ Events: Kafka (audit trail)
└─ No in-memory state

Benefits:
├─ State survives instance restarts
├─ Shared state across instances
├─ Consistent state
└─ Easy to scale
```

### Service Components

#### 1. **Agent State Manager**

```java
@Service
public class AgentStateManager {
    private final RedisTemplate<String, AgentState> redisTemplate;
    
    public AgentState getAgentState(String agentId) {
        String key = "agent:state:" + agentId;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void updateAgentState(String agentId, AgentStatus status) {
        String key = "agent:state:" + agentId;
        AgentState state = getAgentState(agentId);
        state.setStatus(status);
        state.setLastUpdated(Instant.now());
        redisTemplate.opsForValue().set(key, state, Duration.ofHours(1));
    }
    
    public List<Agent> getAvailableAgents(String tenantId) {
        // Scan Redis for available agents
        Set<String> keys = redisTemplate.keys("agent:state:*");
        return keys.stream()
            .map(key -> redisTemplate.opsForValue().get(key))
            .filter(state -> state.getStatus() == AgentStatus.AVAILABLE)
            .filter(state -> state.getTenantId().equals(tenantId))
            .map(state -> state.toAgent())
            .collect(Collectors.toList());
    }
}
```

#### 2. **Routing Engine**

```java
@Service
public class AgentRoutingEngine {
    public Agent selectAgent(List<Agent> agents, ConversationRequest request) {
        return agents.stream()
            .filter(agent -> matchesSkills(agent, request))
            .filter(agent -> isAvailable(agent))
            .min(Comparator
                .comparing(Agent::getCurrentLoad)
                .thenComparing(Agent::getAverageResponseTime)
                .thenComparing(Agent::getSuccessRate))
            .orElseThrow(() -> new NoAvailableAgentException());
    }
    
    private boolean matchesSkills(Agent agent, ConversationRequest request) {
        return agent.getSkills().containsAll(request.getRequiredSkills());
    }
    
    private boolean isAvailable(Agent agent) {
        return agent.getStatus() == AgentStatus.AVAILABLE &&
               agent.getCurrentLoad() < agent.getMaxLoad();
    }
}
```

#### 3. **Event Generator**

```java
@Service
public class EventGenerator {
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    
    public void emitAgentMatchedEvent(Agent agent, ConversationRequest request) {
        AgentMatchedEvent event = AgentMatchedEvent.builder()
            .agentId(agent.getId())
            .conversationId(request.getConversationId())
            .timestamp(Instant.now())
            .previousState(agent.getPreviousState())
            .newState(AgentStatus.BUSY)
            .build();
        
        kafkaTemplate.send("agent-events", agent.getId(), event);
    }
    
    public void emitAgentStateChangedEvent(Agent agent, AgentStatus newStatus) {
        AgentStateChangedEvent event = AgentStateChangedEvent.builder()
            .agentId(agent.getId())
            .status(newStatus)
            .timestamp(Instant.now())
            .reason("Manual update")
            .build();
        
        kafkaTemplate.send("agent-events", agent.getId(), event);
    }
}
```

---

## Question 15: How does the Agent Match Service handle agent state consistency across multiple instances?

### Answer

### State Consistency Strategy

#### 1. **Distributed Locking**

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Lock Flow                           │
└─────────────────────────────────────────────────────────┘

Instance 1:
├─ Acquires lock for agent-123
├─ Updates state: AVAILABLE → BUSY
├─ Releases lock
└─ Emits event

Instance 2:
├─ Tries to acquire lock for agent-123
├─ Lock already held → Wait or fail
└─ Retry after lock release
```

**Implementation:**

```java
@Service
public class AgentStateManager {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void updateAgentStateWithLock(String agentId, AgentStatus newStatus) {
        String lockKey = "lock:agent:" + agentId;
        String lockValue = UUID.randomUUID().toString();
        
        // Try to acquire lock (10 second timeout)
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));
        
        if (!lockAcquired) {
            throw new ConcurrentModificationException(
                "Agent state is being updated by another instance");
        }
        
        try {
            // Update state
            AgentState state = getAgentState(agentId);
            state.setStatus(newStatus);
            state.setVersion(state.getVersion() + 1); // Optimistic locking
            saveAgentState(agentId, state);
            
            // Emit event
            emitStateChangeEvent(agentId, newStatus);
            
        } finally {
            // Release lock (only if we own it)
            String currentLockValue = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(currentLockValue)) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}
```

#### 2. **Optimistic Locking**

```
┌─────────────────────────────────────────────────────────┐
│         Optimistic Locking Flow                        │
└─────────────────────────────────────────────────────────┘

Agent State:
├─ version: 1
├─ status: AVAILABLE
└─ lastUpdated: 2024-01-15 10:00:00

Instance 1:
├─ Reads state (version: 1)
├─ Updates status: BUSY
├─ Saves with version: 2
└─ Success

Instance 2:
├─ Reads state (version: 1) - stale
├─ Updates status: AWAY
├─ Tries to save with version: 2
└─ Fails (version mismatch)
```

**Implementation:**

```java
@Entity
public class AgentState {
    @Version
    private Long version; // Optimistic locking
    
    private String agentId;
    private AgentStatus status;
    private Instant lastUpdated;
    
    // Getters and setters
}

@Service
public class AgentStateManager {
    @Transactional
    public void updateAgentStateOptimistic(String agentId, AgentStatus newStatus) {
        AgentState state = agentStateRepository.findByAgentId(agentId);
        
        // Update state
        state.setStatus(newStatus);
        state.setLastUpdated(Instant.now());
        
        try {
            agentStateRepository.save(state); // Version check happens here
        } catch (OptimisticLockingFailureException e) {
            // Another instance updated the state
            // Retry or throw exception
            throw new ConcurrentModificationException(
                "Agent state was modified by another instance", e);
        }
    }
}
```

#### 3. **Event-Driven Consistency**

```
┌─────────────────────────────────────────────────────────┐
│         Event-Driven Consistency                      │
└─────────────────────────────────────────────────────────┘

State Update Flow:
1. Instance 1 updates state in Redis
2. Instance 1 emits event to Kafka
3. Kafka distributes event to all consumers
4. All instances consume event
5. All instances update local cache
6. Eventual consistency achieved
```

**Implementation:**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-match-service")
public void handleAgentStateChangedEvent(AgentStateChangedEvent event) {
    // Update local cache
    String key = "agent:state:" + event.getAgentId();
    AgentState state = getAgentState(event.getAgentId());
    state.setStatus(event.getStatus());
    state.setLastUpdated(event.getTimestamp());
    redisTemplate.opsForValue().set(key, state);
    
    // Update in-memory cache (if any)
    localCache.put(event.getAgentId(), state);
}
```

#### 4. **Redis Pub/Sub for Real-time Updates**

```
┌─────────────────────────────────────────────────────────┐
│         Redis Pub/Sub Flow                              │
└─────────────────────────────────────────────────────────┘

Instance 1:
├─ Updates agent state
├─ Publishes to channel: "agent:state:updates"
└─ Message: {agentId, status, timestamp}

All Other Instances:
├─ Subscribe to channel: "agent:state:updates"
├─ Receive update message
└─ Update local cache immediately
```

**Implementation:**

```java
@Service
public class AgentStateSyncService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer messageListener;
    
    @PostConstruct
    public void init() {
        messageListener.addMessageListener(
            new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {
                    AgentStateUpdate update = deserialize(message.getBody());
                    updateLocalCache(update);
                }
            },
            new ChannelTopic("agent:state:updates")
        );
    }
    
    public void publishStateUpdate(String agentId, AgentStatus status) {
        AgentStateUpdate update = new AgentStateUpdate(agentId, status, Instant.now());
        redisTemplate.convertAndSend("agent:state:updates", serialize(update));
    }
}
```

---

## Question 16: Walk me through the agent routing algorithm. How does it select the best agent?

### Answer

### Agent Routing Algorithm

#### 1. **Algorithm Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Agent Routing Algorithm Flow                   │
└─────────────────────────────────────────────────────────┘

1. Receive Conversation Request
   ├─ Extract requirements (skills, priority, language)
   └─ Get tenant context

2. Filter Available Agents
   ├─ Status: AVAILABLE
   ├─ Tenant match
   ├─ Skill match
   └─ Load < max capacity

3. Score Agents
   ├─ Current load (lower is better)
   ├─ Average response time (lower is better)
   ├─ Success rate (higher is better)
   └─ Skill match score

4. Select Best Agent
   ├─ Highest score
   └─ Or round-robin if tie

5. Update Agent State
   ├─ Status: BUSY
   ├─ Increment load
   └─ Emit event
```

#### 2. **Scoring Algorithm**

```java
@Service
public class AgentRoutingEngine {
    private static final double LOAD_WEIGHT = 0.4;
    private static final double RESPONSE_TIME_WEIGHT = 0.3;
    private static final double SUCCESS_RATE_WEIGHT = 0.2;
    private static final double SKILL_MATCH_WEIGHT = 0.1;
    
    public Agent selectAgent(List<Agent> agents, ConversationRequest request) {
        // Step 1: Filter agents
        List<Agent> candidates = agents.stream()
            .filter(agent -> agent.getStatus() == AgentStatus.AVAILABLE)
            .filter(agent -> agent.getTenantId().equals(request.getTenantId()))
            .filter(agent -> matchesSkills(agent, request))
            .filter(agent -> agent.getCurrentLoad() < agent.getMaxLoad())
            .collect(Collectors.toList());
        
        if (candidates.isEmpty()) {
            throw new NoAvailableAgentException();
        }
        
        // Step 2: Score agents
        Map<Agent, Double> scores = candidates.stream()
            .collect(Collectors.toMap(
                agent -> agent,
                agent -> calculateScore(agent, request)
            ));
        
        // Step 3: Select best agent
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new NoAvailableAgentException());
    }
    
    private double calculateScore(Agent agent, ConversationRequest request) {
        // Normalize values (0-1 scale)
        double loadScore = 1.0 - (agent.getCurrentLoad() / agent.getMaxLoad());
        double responseTimeScore = 1.0 / (1.0 + agent.getAverageResponseTime().toSeconds());
        double successRateScore = agent.getSuccessRate();
        double skillMatchScore = calculateSkillMatchScore(agent, request);
        
        // Weighted sum
        return (LOAD_WEIGHT * loadScore) +
               (RESPONSE_TIME_WEIGHT * responseTimeScore) +
               (SUCCESS_RATE_WEIGHT * successRateScore) +
               (SKILL_MATCH_WEIGHT * skillMatchScore);
    }
    
    private double calculateSkillMatchScore(Agent agent, ConversationRequest request) {
        Set<String> agentSkills = agent.getSkills();
        Set<String> requiredSkills = request.getRequiredSkills();
        
        long matchingSkills = agentSkills.stream()
            .filter(requiredSkills::contains)
            .count();
        
        return (double) matchingSkills / requiredSkills.size();
    }
}
```

#### 3. **Routing Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Routing Strategies                            │
└─────────────────────────────────────────────────────────┘

1. Load-Based:
   ├─ Select agent with lowest current load
   └─ Ensures even distribution

2. Performance-Based:
   ├─ Select agent with best response time
   └─ Ensures fast responses

3. Skill-Based:
   ├─ Select agent with best skill match
   └─ Ensures quality

4. Hybrid (Used):
   ├─ Combines all factors
   ├─ Weighted scoring
   └─ Best overall match
```

#### 4. **Fallback Mechanisms**

```java
@Service
public class AgentRoutingEngine {
    public Agent selectAgentWithFallback(List<Agent> agents, ConversationRequest request) {
        try {
            // Try primary selection
            return selectAgent(agents, request);
            
        } catch (NoAvailableAgentException e) {
            // Fallback 1: Relax skill requirements
            List<Agent> relaxedCandidates = agents.stream()
                .filter(agent -> agent.getStatus() == AgentStatus.AVAILABLE)
                .filter(agent -> matchesSkillsRelaxed(agent, request))
                .collect(Collectors.toList());
            
            if (!relaxedCandidates.isEmpty()) {
                return selectAgent(relaxedCandidates, request);
            }
            
            // Fallback 2: Queue conversation
            queueConversation(request);
            throw new NoAvailableAgentException("Conversation queued");
        }
    }
    
    private boolean matchesSkillsRelaxed(Agent agent, ConversationRequest request) {
        // Match at least 50% of required skills
        Set<String> agentSkills = agent.getSkills();
        Set<String> requiredSkills = request.getRequiredSkills();
        
        long matchingSkills = agentSkills.stream()
            .filter(requiredSkills::contains)
            .count();
        
        return matchingSkills >= (requiredSkills.size() / 2);
    }
}
```

---

## Question 17: What happens when multiple service instances try to update the same agent's state simultaneously?

### Answer

### Concurrent Update Scenario

#### 1. **Problem Scenario**

```
┌─────────────────────────────────────────────────────────┐
│         Concurrent Update Problem                      │
└─────────────────────────────────────────────────────────┘

Time    | Instance 1              | Instance 2
--------|--------------------------|--------------------------
T0      | Read agent-123           | Read agent-123
        | Status: AVAILABLE        | Status: AVAILABLE
        | Load: 5/10               | Load: 5/10
--------|--------------------------|--------------------------
T1      | Match to conversation-1 | Match to conversation-2
        | Update: BUSY, Load: 6    | Update: BUSY, Load: 6
--------|--------------------------|--------------------------
T2      | Save state               | Save state
        | Result: Load = 6         | Result: Load = 6
--------|--------------------------|--------------------------
Problem: Both conversations assigned, but load only incremented once
```

#### 2. **Solution: Distributed Locking**

```java
@Service
public class AgentStateManager {
    private final RedisTemplate<String, String> redisTemplate;
    
    public Agent matchAgentWithLock(String agentId, ConversationRequest request) {
        String lockKey = "lock:agent:" + agentId;
        String lockValue = UUID.randomUUID().toString();
        
        // Try to acquire lock with timeout
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));
        
        if (!lockAcquired) {
            // Another instance is updating, retry or fail
            throw new AgentStateLockedException("Agent state is being updated");
        }
        
        try {
            // Critical section: Read-Modify-Write
            AgentState state = getAgentState(agentId);
            
            // Validate state
            if (state.getStatus() != AgentStatus.AVAILABLE) {
                throw new AgentNotAvailableException();
            }
            if (state.getCurrentLoad() >= state.getMaxLoad()) {
                throw new AgentAtCapacityException();
            }
            
            // Update state atomically
            state.setStatus(AgentStatus.BUSY);
            state.setCurrentLoad(state.getCurrentLoad() + 1);
            state.setLastUpdated(Instant.now());
            
            // Save state
            saveAgentState(agentId, state);
            
            // Emit event
            emitAgentMatchedEvent(agentId, request);
            
            return state.toAgent();
            
        } finally {
            // Release lock (only if we own it)
            releaseLock(lockKey, lockValue);
        }
    }
    
    private void releaseLock(String lockKey, String lockValue) {
        // Lua script for atomic lock release
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

#### 3. **Optimistic Locking Alternative**

```java
@Service
public class AgentStateManager {
    @Transactional
    public Agent matchAgentOptimistic(String agentId, ConversationRequest request) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // Read current state
                AgentState state = agentStateRepository.findByAgentId(agentId);
                
                // Validate
                if (state.getStatus() != AgentStatus.AVAILABLE) {
                    throw new AgentNotAvailableException();
                }
                if (state.getCurrentLoad() >= state.getMaxLoad()) {
                    throw new AgentAtCapacityException();
                }
                
                // Update
                state.setStatus(AgentStatus.BUSY);
                state.setCurrentLoad(state.getCurrentLoad() + 1);
                state.setLastUpdated(Instant.now());
                
                // Save (version check happens here)
                agentStateRepository.save(state);
                
                // Success
                emitAgentMatchedEvent(agentId, request);
                return state.toAgent();
                
            } catch (OptimisticLockingFailureException e) {
                // Another instance updated, retry
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new ConcurrentModificationException(
                        "Failed to update agent state after retries", e);
                }
                // Wait before retry
                try {
                    Thread.sleep(100 * retryCount); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
        
        throw new RuntimeException("Should not reach here");
    }
}
```

#### 4. **Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         Locking Strategy Comparison                    │
└─────────────────────────────────────────────────────────┘

Distributed Locking:
├─ Pros:
│  ├─ Prevents concurrent updates
│  ├─ Simple to implement
│  └─ Guaranteed consistency
├─ Cons:
│  ├─ Lock contention
│  ├─ Deadlock risk
│  └─ Performance overhead
└─ Use Case: Critical updates

Optimistic Locking:
├─ Pros:
│  ├─ Better performance
│  ├─ No lock contention
│  └─ Retry on conflict
├─ Cons:
│  ├─ Retry overhead
│  ├─ May fail after retries
│  └─ More complex
└─ Use Case: High contention scenarios
```

---

## Question 18: How did you implement distributed locks in Redis? What are the potential issues?

### Answer

### Distributed Lock Implementation

#### 1. **Basic Implementation**

```java
@Service
public class DistributedLockService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean acquireLock(String lockKey, String lockValue, Duration timeout) {
        return redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, timeout);
    }
    
    public void releaseLock(String lockKey, String lockValue) {
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        if (lockValue.equals(currentValue)) {
            redisTemplate.delete(lockKey);
        }
    }
}
```

**Problem: Race Condition**

```
┌─────────────────────────────────────────────────────────┐
│         Race Condition Problem                         │
└─────────────────────────────────────────────────────────┘

Time    | Instance 1              | Instance 2
--------|--------------------------|--------------------------
T0      | Lock expires             | 
        | (timeout reached)        |
--------|--------------------------|--------------------------
T1      | Still processing...       | Acquires lock
        |                           | (lock was released)
--------|--------------------------|--------------------------
T2      | Finishes processing      | Processing...
        | Tries to release lock    |
--------|--------------------------|--------------------------
T3      | Releases lock            | 
        | (but Instance 2 owns it) |
--------|--------------------------|--------------------------
Problem: Instance 1 releases Instance 2's lock
```

#### 2. **Safe Implementation with Lua Script**

```java
@Service
public class DistributedLockService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean acquireLock(String lockKey, String lockValue, Duration timeout) {
        return redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, timeout);
    }
    
    public boolean releaseLock(String lockKey, String lockValue) {
        // Lua script for atomic lock release
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";
        
        Long result = redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(lockKey),
            lockValue
        );
        
        return result != null && result > 0;
    }
}
```

#### 3. **Lock with Auto-Renewal**

```java
@Service
public class DistributedLockService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ScheduledExecutorService scheduler;
    
    public LockHandle acquireLockWithRenewal(String lockKey, Duration timeout) {
        String lockValue = UUID.randomUUID().toString();
        
        // Acquire lock
        boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, timeout);
        
        if (!acquired) {
            return null;
        }
        
        // Start renewal task
        ScheduledFuture<?> renewalTask = scheduler.scheduleAtFixedRate(
            () -> renewLock(lockKey, lockValue, timeout),
            timeout.toMillis() / 2, // Renew at half-time
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
    
    public void releaseLock(LockHandle handle) {
        // Cancel renewal
        handle.getRenewalTask().cancel(false);
        
        // Release lock
        releaseLock(handle.getLockKey(), handle.getLockValue());
    }
}
```

#### 4. **Potential Issues and Solutions**

**Issue 1: Deadlock**

```
Problem:
├─ Instance 1 holds lock A, needs lock B
├─ Instance 2 holds lock B, needs lock A
└─ Both wait forever

Solution:
├─ Lock timeout
├─ Lock ordering (always acquire in same order)
└─ Deadlock detection
```

**Issue 2: Lock Expiration During Processing**

```
Problem:
├─ Lock expires while processing
├─ Another instance acquires lock
└─ Two instances process simultaneously

Solution:
├─ Lock renewal (heartbeat)
├─ Longer timeout
└─ Fast processing
```

**Issue 3: Network Partition**

```
Problem:
├─ Network partition occurs
├─ Lock appears held in one partition
├─ Released in another partition
└─ Two instances think they own lock

Solution:
├─ Fencing tokens (sequence numbers)
├─ Redlock algorithm (multiple Redis instances)
└─ Accept temporary inconsistency
```

**Issue 4: Performance Impact**

```
Problem:
├─ Lock contention causes delays
├─ High latency
└─ Reduced throughput

Solution:
├─ Fine-grained locking
├─ Lock-free algorithms where possible
├─ Optimistic locking for non-critical paths
└─ Lock timeout to prevent indefinite waits
```

---

## Question 19: Explain the event-driven state management approach. How does it ensure real-time synchronization?

### Answer

### Event-Driven State Management

#### 1. **Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Event-Driven State Management                  │
└─────────────────────────────────────────────────────────┘

State Update Flow:
1. Instance updates state in Redis
2. Instance emits event to Kafka
3. Kafka distributes event
4. All instances consume event
5. All instances update local cache
6. Real-time sync achieved
```

#### 2. **Event Types**

```java
// Agent State Change Event
public class AgentStateChangedEvent {
    private String agentId;
    private AgentStatus previousStatus;
    private AgentStatus newStatus;
    private Instant timestamp;
    private String reason;
    private Long version; // For ordering
}

// Agent Matched Event
public class AgentMatchedEvent {
    private String agentId;
    private String conversationId;
    private Instant timestamp;
    private AgentStatus previousState;
    private AgentStatus newState;
}
```

#### 3. **Event Publishing**

```java
@Service
public class AgentStateManager {
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    private final RedisTemplate<String, AgentState> redisTemplate;
    
    public void updateAgentState(String agentId, AgentStatus newStatus) {
        // 1. Read current state
        AgentState currentState = getAgentState(agentId);
        AgentStatus previousStatus = currentState.getStatus();
        
        // 2. Update state in Redis
        currentState.setStatus(newStatus);
        currentState.setLastUpdated(Instant.now());
        currentState.setVersion(currentState.getVersion() + 1);
        saveAgentState(agentId, currentState);
        
        // 3. Emit event to Kafka
        AgentStateChangedEvent event = AgentStateChangedEvent.builder()
            .agentId(agentId)
            .previousStatus(previousStatus)
            .newStatus(newStatus)
            .timestamp(Instant.now())
            .version(currentState.getVersion())
            .build();
        
        // Partition by agentId for ordering
        kafkaTemplate.send("agent-events", agentId, event);
    }
}
```

#### 4. **Event Consumption**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-match-service")
public void handleAgentStateChangedEvent(AgentStateChangedEvent event) {
    // Update local cache
    String key = "agent:state:" + event.getAgentId();
    AgentState state = getAgentState(event.getAgentId());
    
    // Apply event (idempotent)
    if (state.getVersion() < event.getVersion()) {
        state.setStatus(event.getNewStatus());
        state.setVersion(event.getVersion());
        state.setLastUpdated(event.getTimestamp());
        
        // Update Redis
        redisTemplate.opsForValue().set(key, state);
        
        // Update in-memory cache
        localCache.put(event.getAgentId(), state);
    }
}
```

#### 5. **Real-time Synchronization with Redis Pub/Sub**

```java
@Service
public class AgentStateSyncService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer messageListener;
    private final Map<String, AgentState> localCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // Subscribe to state update channel
        messageListener.addMessageListener(
            new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {
                    AgentStateUpdate update = deserialize(message.getBody());
                    updateLocalCacheImmediately(update);
                }
            },
            new ChannelTopic("agent:state:updates")
        );
    }
    
    public void updateAgentState(String agentId, AgentStatus status) {
        // 1. Update Redis
        AgentState state = getAgentState(agentId);
        state.setStatus(status);
        saveAgentState(agentId, state);
        
        // 2. Publish to Redis pub/sub for immediate sync
        AgentStateUpdate update = new AgentStateUpdate(agentId, status, Instant.now());
        redisTemplate.convertAndSend("agent:state:updates", serialize(update));
        
        // 3. Emit to Kafka for persistence
        emitKafkaEvent(agentId, status);
    }
    
    private void updateLocalCacheImmediately(AgentStateUpdate update) {
        // Update in-memory cache immediately (no Kafka delay)
        localCache.put(update.getAgentId(), 
            getAgentState(update.getAgentId()));
    }
}
```

#### 6. **Synchronization Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Synchronization Guarantees                    │
└─────────────────────────────────────────────────────────┘

Redis Pub/Sub:
├─ Real-time (milliseconds)
├─ Best effort delivery
├─ No persistence
└─ Immediate sync

Kafka Events:
├─ Guaranteed delivery
├─ Persistent
├─ Ordered per partition
└─ Eventual sync (seconds)

Combined Approach:
├─ Redis pub/sub for real-time
├─ Kafka for persistence
└─ Best of both worlds
```

---

## Question 20: What happens if Redis goes down? How do you handle agent state recovery?

### Answer

### Redis Failure Scenarios

#### 1. **Failure Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Redis Failure Detection                        │
└─────────────────────────────────────────────────────────┘

Health Checks:
├─ Ping Redis every 5 seconds
├─ Check connection pool
├─ Monitor response times
└─ Alert on failure

Failure Modes:
├─ Complete Redis failure
├─ Network partition
├─ Redis cluster split
└─ Performance degradation
```

#### 2. **Recovery Strategy**

```java
@Service
public class AgentStateManager {
    private final RedisTemplate<String, AgentState> redisTemplate;
    private final AgentStateRepository agentStateRepository; // PostgreSQL
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    
    public AgentState getAgentState(String agentId) {
        try {
            // Try Redis first
            return getFromRedis(agentId);
        } catch (RedisConnectionFailureException e) {
            // Fallback to database
            log.warn("Redis unavailable, falling back to database", e);
            return getFromDatabase(agentId);
        }
    }
    
    private AgentState getFromRedis(String agentId) {
        String key = "agent:state:" + agentId;
        AgentState state = redisTemplate.opsForValue().get(key);
        if (state == null) {
            // Not in cache, load from database and cache
            state = getFromDatabase(agentId);
            if (state != null) {
                redisTemplate.opsForValue().set(key, state, Duration.ofHours(1));
            }
        }
        return state;
    }
    
    private AgentState getFromDatabase(String agentId) {
        return agentStateRepository.findByAgentId(agentId)
            .orElse(AgentState.defaultState(agentId));
    }
}
```

#### 3. **State Recovery from Kafka**

```java
@Service
public class AgentStateRecoveryService {
    private final KafkaConsumer<String, AgentEvent> kafkaConsumer;
    private final RedisTemplate<String, AgentState> redisTemplate;
    
    public void recoverAgentStateFromEvents(String agentId) {
        // Read all events for this agent from Kafka
        List<AgentStateChangedEvent> events = readEventsFromKafka(agentId);
        
        // Rebuild state from events
        AgentState state = AgentState.defaultState(agentId);
        for (AgentStateChangedEvent event : events) {
            state = applyEvent(state, event);
        }
        
        // Restore to Redis
        String key = "agent:state:" + agentId;
        redisTemplate.opsForValue().set(key, state);
    }
    
    private AgentState applyEvent(AgentState state, AgentStateChangedEvent event) {
        state.setStatus(event.getNewStatus());
        state.setVersion(event.getVersion());
        state.setLastUpdated(event.getTimestamp());
        return state;
    }
}
```

#### 4. **Multi-Level Fallback**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Fallback Strategy                  │
└─────────────────────────────────────────────────────────┘

Level 1: Redis (Primary)
├─ Fast access
├─ Real-time updates
└─ Failure: Fall to Level 2

Level 2: Database (Secondary)
├─ Persistent storage
├─ Slower access
└─ Failure: Fall to Level 3

Level 3: Kafka Events (Recovery)
├─ Rebuild from events
├─ Complete history
└─ Last resort
```

#### 5. **Graceful Degradation**

```java
@Service
public class AgentStateManager {
    private final RedisTemplate<String, AgentState> redisTemplate;
    private final AgentStateRepository agentStateRepository;
    private volatile boolean redisAvailable = true;
    
    @Scheduled(fixedRate = 5000)
    public void checkRedisHealth() {
        try {
            redisTemplate.opsForValue().get("health:check");
            redisAvailable = true;
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis health check failed", e);
        }
    }
    
    public AgentState getAgentState(String agentId) {
        if (redisAvailable) {
            try {
                return getFromRedis(agentId);
            } catch (Exception e) {
                redisAvailable = false;
                return getFromDatabase(agentId);
            }
        } else {
            return getFromDatabase(agentId);
        }
    }
    
    public void updateAgentState(String agentId, AgentStatus status) {
        // Always update database first (source of truth)
        AgentState state = getFromDatabase(agentId);
        state.setStatus(status);
        agentStateRepository.save(state);
        
        // Try to update Redis if available
        if (redisAvailable) {
            try {
                String key = "agent:state:" + agentId;
                redisTemplate.opsForValue().set(key, state, Duration.ofHours(1));
            } catch (Exception e) {
                log.warn("Failed to update Redis, continuing with database", e);
            }
        }
        
        // Always emit event
        emitKafkaEvent(agentId, status);
    }
}
```

---

## Question 21: How does the routing engine handle agent skill matching?

### Answer

### Skill Matching Algorithm

#### 1. **Skill Model**

```java
public class Agent {
    private String agentId;
    private Set<String> skills; // ["billing", "technical", "spanish"]
    private Map<String, Integer> skillLevels; // {"billing": 5, "technical": 3}
    private Set<String> languages; // ["en", "es", "fr"]
}

public class ConversationRequest {
    private Set<String> requiredSkills; // ["billing", "technical"]
    private Set<String> preferredSkills; // ["spanish"]
    private String language; // "en"
    private Priority priority; // HIGH, NORMAL, LOW
}
```

#### 2. **Matching Algorithm**

```java
@Service
public class AgentRoutingEngine {
    public Agent selectAgent(List<Agent> agents, ConversationRequest request) {
        // Step 1: Filter by basic requirements
        List<Agent> candidates = agents.stream()
            .filter(agent -> agent.getStatus() == AgentStatus.AVAILABLE)
            .filter(agent -> matchesLanguage(agent, request))
            .filter(agent -> hasRequiredSkills(agent, request))
            .collect(Collectors.toList());
        
        if (candidates.isEmpty()) {
            // Fallback: Relax requirements
            return selectAgentWithRelaxedRequirements(agents, request);
        }
        
        // Step 2: Score by skill match
        Map<Agent, Double> scores = candidates.stream()
            .collect(Collectors.toMap(
                agent -> agent,
                agent -> calculateSkillMatchScore(agent, request)
            ));
        
        // Step 3: Select best match
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new NoAvailableAgentException());
    }
    
    private boolean matchesLanguage(Agent agent, ConversationRequest request) {
        return agent.getLanguages().contains(request.getLanguage());
    }
    
    private boolean hasRequiredSkills(Agent agent, ConversationRequest request) {
        return agent.getSkills().containsAll(request.getRequiredSkills());
    }
    
    private double calculateSkillMatchScore(Agent agent, ConversationRequest request) {
        double score = 0.0;
        
        // Required skills: 100% match required
        Set<String> requiredSkills = request.getRequiredSkills();
        Set<String> agentSkills = agent.getSkills();
        
        long matchingRequired = requiredSkills.stream()
            .filter(agentSkills::contains)
            .count();
        
        if (matchingRequired < requiredSkills.size()) {
            return 0.0; // Doesn't meet requirements
        }
        
        score += 1.0; // Base score for meeting requirements
        
        // Preferred skills: Bonus points
        Set<String> preferredSkills = request.getPreferredSkills();
        long matchingPreferred = preferredSkills.stream()
            .filter(agentSkills::contains)
            .count();
        
        score += (double) matchingPreferred / preferredSkills.size() * 0.5;
        
        // Skill levels: Higher level = better match
        double averageSkillLevel = agent.getSkillLevels().entrySet().stream()
            .filter(e -> requiredSkills.contains(e.getKey()))
            .mapToInt(Map.Entry::getValue)
            .average()
            .orElse(0.0);
        
        score += averageSkillLevel / 10.0 * 0.3; // Normalize to 0-0.3
        
        return score;
    }
}
```

#### 3. **Skill Matching Examples**

```
┌─────────────────────────────────────────────────────────┐
│         Skill Matching Examples                        │
└─────────────────────────────────────────────────────────┘

Request:
├─ Required: ["billing", "technical"]
├─ Preferred: ["spanish"]
└─ Language: "en"

Agent 1:
├─ Skills: ["billing", "technical", "spanish"]
├─ Languages: ["en", "es"]
└─ Score: 1.0 + 0.5 + 0.2 = 1.7

Agent 2:
├─ Skills: ["billing", "technical"]
├─ Languages: ["en"]
└─ Score: 1.0 + 0.0 + 0.15 = 1.15

Agent 3:
├─ Skills: ["billing"] (missing "technical")
├─ Languages: ["en"]
└─ Score: 0.0 (doesn't meet requirements)
```

---

## Question 22: What metrics do you track for agent matching performance?

### Answer

### Performance Metrics

#### 1. **Key Metrics**

```java
@Component
public class AgentMatchingMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordAgentMatch(String agentId, Duration matchTime, 
                                  boolean success, String reason) {
        // Match time histogram
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("agent.match.duration")
            .tag("success", String.valueOf(success))
            .register(meterRegistry));
        
        // Match success counter
        Counter.builder("agent.match.count")
            .tag("success", String.valueOf(success))
            .tag("reason", reason)
            .register(meterRegistry)
            .increment();
        
        // Agent utilization
        Gauge.builder("agent.utilization", agentId, 
            id -> getAgentUtilization(id))
            .register(meterRegistry);
    }
    
    public void recordRoutingDecision(String decisionType, Duration decisionTime) {
        Timer.builder("agent.routing.decision.duration")
            .tag("type", decisionType)
            .register(meterRegistry)
            .record(decisionTime);
    }
}
```

#### 2. **Metrics Dashboard**

```
┌─────────────────────────────────────────────────────────┐
│         Metrics Dashboard                              │
└─────────────────────────────────────────────────────────┘

Agent Matching Metrics:
├─ Match Rate: 95%
├─ Average Match Time: 50ms
├─ P95 Match Time: 100ms
├─ P99 Match Time: 200ms
└─ Failure Rate: 5%

Agent Utilization:
├─ Average Utilization: 70%
├─ Peak Utilization: 90%
├─ Idle Agents: 30%
└─ Overloaded Agents: 5%

Routing Performance:
├─ Skill Match Rate: 98%
├─ Language Match Rate: 99%
├─ Load Balance Score: 0.85
└─ Fallback Rate: 2%
```

---

## Question 23: How would you handle a scenario where all agents are busy?

### Answer

### All Agents Busy Scenario

#### 1. **Queue Management**

```java
@Service
public class ConversationQueueService {
    private final RedisTemplate<String, ConversationRequest> redisTemplate;
    
    public void queueConversation(ConversationRequest request) {
        // Add to priority queue
        String queueKey = "queue:conversations:" + request.getTenantId();
        
        // Score = priority (higher = more important) + timestamp
        double score = calculatePriorityScore(request);
        
        redisTemplate.opsForZSet().add(queueKey, 
            serialize(request), score);
        
        // Set TTL
        redisTemplate.expire(queueKey, Duration.ofHours(24));
    }
    
    private double calculatePriorityScore(ConversationRequest request) {
        double priorityMultiplier = switch (request.getPriority()) {
            case HIGH -> 1000.0;
            case NORMAL -> 100.0;
            case LOW -> 10.0;
        };
        
        // Older requests have higher score (FIFO within priority)
        long age = Instant.now().toEpochMilli() - 
                   request.getCreatedAt().toEpochMilli();
        
        return priorityMultiplier + (age / 1000.0);
    }
    
    public ConversationRequest dequeueConversation(String tenantId) {
        String queueKey = "queue:conversations:" + tenantId;
        
        // Get highest priority conversation
        Set<ZSetOperations.TypedTuple<String>> results = 
            redisTemplate.opsForZSet().popMax(queueKey, 1);
        
        if (results.isEmpty()) {
            return null;
        }
        
        ZSetOperations.TypedTuple<String> tuple = results.iterator().next();
        return deserialize(tuple.getValue());
    }
}
```

#### 2. **Agent Availability Monitoring**

```java
@Service
public class AgentAvailabilityMonitor {
    private final ScheduledExecutorService scheduler;
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void checkAgentAvailability() {
        List<Agent> availableAgents = getAvailableAgents();
        
        if (availableAgents.isEmpty()) {
            // All agents busy
            handleAllAgentsBusy();
        } else {
            // Process queued conversations
            processQueue();
        }
    }
    
    private void handleAllAgentsBusy() {
        // 1. Notify customers
        notifyCustomersInQueue();
        
        // 2. Escalate to managers
        escalateToManagers();
        
        // 3. Estimate wait time
        Duration estimatedWait = estimateWaitTime();
        broadcastWaitTime(estimatedWait);
    }
    
    private void processQueue() {
        List<ConversationRequest> queued = getQueuedConversations();
        
        for (ConversationRequest request : queued) {
            try {
                Agent agent = agentMatchService.matchAgent(request);
                if (agent != null) {
                    // Assign conversation
                    assignConversation(agent, request);
                    removeFromQueue(request);
                }
            } catch (NoAvailableAgentException e) {
                // Still no agents, keep in queue
                break;
            }
        }
    }
}
```

#### 3. **Customer Notification**

```java
@Service
public class CustomerNotificationService {
    public void notifyCustomerInQueue(ConversationRequest request, 
                                      int queuePosition, 
                                      Duration estimatedWait) {
        NotificationMessage message = NotificationMessage.builder()
            .type(NotificationType.QUEUE_POSITION)
            .message(String.format(
                "You are position %d in the queue. Estimated wait: %d minutes",
                queuePosition, estimatedWait.toMinutes()))
            .build();
        
        // Send via WebSocket
        webSocketService.sendToCustomer(request.getCustomerId(), message);
    }
    
    public void notifyAgentAvailable(ConversationRequest request, Agent agent) {
        NotificationMessage message = NotificationMessage.builder()
            .type(NotificationType.AGENT_AVAILABLE)
            .message(String.format("Agent %s is now available", agent.getName()))
            .build();
        
        webSocketService.sendToCustomer(request.getCustomerId(), message);
    }
}
```

---

## Summary

Agent Match Service answers cover:

1. **Stateless Design**: Enables horizontal scaling and fault tolerance
2. **State Consistency**: Distributed locks and optimistic locking
3. **Routing Algorithm**: Multi-factor scoring with skill matching
4. **Concurrent Updates**: Lock-based and optimistic approaches
5. **Event-Driven**: Real-time synchronization with Redis pub/sub and Kafka
6. **Failure Recovery**: Multi-level fallback (Redis → Database → Kafka)
7. **Skill Matching**: Required and preferred skills with scoring
8. **Metrics**: Performance tracking and monitoring
9. **Queue Management**: Handling all agents busy scenario
