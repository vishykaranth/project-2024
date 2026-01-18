# State Management - Part 1: Fundamentals & Storage

## Question 221: How do you manage state in stateless services?

### Answer

### State Management in Stateless Services

#### 1. **External State Storage**

```
┌─────────────────────────────────────────────────────────┐
│         State Storage Strategy                         │
└─────────────────────────────────────────────────────────┘

Stateless Service:
├─ No in-memory state
├─ State in external stores
└─ Any instance can handle request

State Storage Options:
├─ Redis: Fast, shared state
├─ Database: Persistent state
├─ Kafka: Event state
└─ External services: Distributed state
```

#### 2. **Implementation Pattern**

```java
@Service
public class StatelessStateService {
    // No instance variables for state
    private final RedisTemplate<String, State> redisTemplate;
    private final StateRepository stateRepository;
    
    public State getState(String entityId) {
        // Read from external store
        return getStateFromExternalStore(entityId);
    }
    
    public void updateState(String entityId, State newState) {
        // Write to external store
        saveStateToExternalStore(entityId, newState);
    }
    
    private State getStateFromExternalStore(String entityId) {
        // Try Redis first
        State state = redisTemplate.opsForValue().get("state:" + entityId);
        if (state != null) {
            return state;
        }
        
        // Fallback to database
        return stateRepository.findByEntityId(entityId)
            .orElse(State.defaultState(entityId));
    }
}
```

---

## Question 222: What's the state storage strategy (Redis, Database, In-memory)?

### Answer

### Multi-Tier State Storage

#### 1. **Storage Tier Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         State Storage Tiers                            │
└─────────────────────────────────────────────────────────┘

Tier 1: In-Memory (Local Cache)
├─ Fastest access (< 1ms)
├─ Limited size
├─ Lost on restart
└─ Per-instance

Tier 2: Redis (Distributed Cache)
├─ Fast access (5-10ms)
├─ Large size
├─ Shared across instances
└─ Persistent

Tier 3: Database (Source of Truth)
├─ Slowest access (50-100ms)
├─ Unlimited size
├─ Persistent
└─ ACID guarantees
```

#### 2. **Storage Selection Logic**

```java
@Service
public class TieredStateStorage {
    private final Cache<String, State> localCache;
    private final RedisTemplate<String, State> redisTemplate;
    private final StateRepository repository;
    
    public State getState(String entityId) {
        // Tier 1: Local cache
        State state = localCache.getIfPresent(entityId);
        if (state != null) {
            return state;
        }
        
        // Tier 2: Redis
        state = redisTemplate.opsForValue().get("state:" + entityId);
        if (state != null) {
            localCache.put(entityId, state);
            return state;
        }
        
        // Tier 3: Database
        state = repository.findByEntityId(entityId).orElse(null);
        if (state != null) {
            // Populate caches
            redisTemplate.opsForValue().set("state:" + entityId, state);
            localCache.put(entityId, state);
        }
        
        return state;
    }
}
```

---

## Question 223: How do you ensure state consistency across instances?

### Answer

### Cross-Instance State Consistency

#### 1. **Event-Driven Synchronization**

```java
@Service
public class ConsistentStateService {
    private final RedisTemplate<String, State> redisTemplate;
    private final KafkaTemplate<String, StateEvent> kafkaTemplate;
    
    public void updateState(String entityId, State newState) {
        // Update in Redis (shared state)
        redisTemplate.opsForValue().set(
            "state:" + entityId, 
            newState
        );
        
        // Emit event for other instances
        StateUpdatedEvent event = new StateUpdatedEvent(entityId, newState);
        kafkaTemplate.send("state-events", entityId, event);
    }
    
    @KafkaListener(topics = "state-events", groupId = "state-service")
    public void handleStateUpdate(StateUpdatedEvent event) {
        // Update local cache from event
        localCache.put(
            event.getEntityId(), 
            event.getNewState()
        );
    }
}
```

#### 2. **Redis Pub/Sub for Real-time Sync**

```java
@Service
public class RealtimeStateSync {
    private final RedisTemplate<String, String> redisTemplate;
    
    @PostConstruct
    public void subscribeToUpdates() {
        RedisMessageListenerContainer container = 
            new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.addMessageListener(
            new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {
                    StateUpdate update = deserialize(message.getBody());
                    updateLocalState(update);
                }
            },
            new ChannelTopic("state:updates")
        );
        container.start();
    }
}
```

---

## Question 224: What's the state synchronization mechanism?

### Answer

### State Synchronization Mechanisms

#### 1. **Pull-Based Synchronization**

```java
@Service
public class PullBasedSync {
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void syncState() {
        // Poll for state changes
        List<String> changedEntities = getChangedEntities();
        
        for (String entityId : changedEntities) {
            State latestState = getLatestState(entityId);
            updateLocalState(entityId, latestState);
        }
    }
}
```

#### 2. **Push-Based Synchronization**

```java
@Service
public class PushBasedSync {
    public void updateState(String entityId, State newState) {
        // Update source
        saveState(entityId, newState);
        
        // Push to all instances
        pushToAllInstances(entityId, newState);
    }
}
```

---

## Question 225: How do you handle state recovery after failures?

### Answer

### State Recovery Strategies

#### 1. **Recovery from Database**

```java
@Service
public class StateRecoveryService {
    private final StateRepository repository;
    private final RedisTemplate<String, State> redisTemplate;
    
    public void recoverState(String entityId) {
        // Load from database (source of truth)
        State state = repository.findByEntityId(entityId)
            .orElse(State.defaultState(entityId));
        
        // Restore to Redis
        redisTemplate.opsForValue().set(
            "state:" + entityId, 
            state
        );
    }
}
```

#### 2. **Recovery from Events**

```java
@Service
public class EventBasedRecovery {
    private final EventStore eventStore;
    
    public State recoverStateFromEvents(String entityId) {
        // Replay all events for entity
        List<StateEvent> events = eventStore.getEvents(entityId);
        
        // Rebuild state from events
        State state = State.defaultState(entityId);
        for (StateEvent event : events) {
            state = applyEvent(state, event);
        }
        
        return state;
    }
}
```

---

## Summary

Part 1 covers:

1. **Stateless State Management**: External storage, no in-memory state
2. **Storage Strategy**: Multi-tier (Local → Redis → Database)
3. **Cross-Instance Consistency**: Events and pub/sub
4. **Synchronization**: Pull and push mechanisms
5. **Recovery**: Database and event-based recovery

Key principles:
- Store state externally for stateless services
- Use multi-tier storage for performance
- Synchronize state via events
- Recover from source of truth
