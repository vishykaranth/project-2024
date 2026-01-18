# Event-Driven Architecture (Project 1) - Detailed Answers

## Question 36: Explain the Kafka event bus architecture. How many topics did you use?

### Answer

### Kafka Event Bus Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Kafka Cluster                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  agent-      │  │ conversation-│  │  message-    │  │
│  │  events      │  │  events      │  │  events      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  session-    │  │  bot-        │  │  analytics-   │  │
│  │  events      │  │  events      │  │  events      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Topics Used

#### 1. **Topic List**

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Topics                                   │
└─────────────────────────────────────────────────────────┘

1. agent-events
   ├─ Agent state changes
   ├─ Agent matched events
   └─ Agent availability updates

2. conversation-events
   ├─ Conversation started
   ├─ Conversation ended
   └─ Conversation state changes

3. message-events
   ├─ Message sent
   ├─ Message received
   └─ Message delivery status

4. session-events
   ├─ Session created
   ├─ Session expired
   └─ Session state changes

5. bot-events
   ├─ Bot responses
   ├─ Bot state changes
   └─ Bot interactions

6. analytics-events
   ├─ User actions
   ├─ Performance metrics
   └─ Business events
```

#### 2. **Topic Configuration**

```java
@Configuration
public class KafkaTopicConfiguration {
    @Bean
    public NewTopic agentEventsTopic() {
        return TopicBuilder.name("agent-events")
            .partitions(10)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days
            .build();
    }
    
    @Bean
    public NewTopic conversationEventsTopic() {
        return TopicBuilder.name("conversation-events")
            .partitions(10)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000")
            .build();
    }
    
    @Bean
    public NewTopic messageEventsTopic() {
        return TopicBuilder.name("message-events")
            .partitions(20) // More partitions for high volume
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "259200000") // 3 days
            .build();
    }
}
```

---

## Question 37: How do you ensure event ordering in Kafka?

### Answer

### Event Ordering Strategy

#### 1. **Partitioning by Key**

```
┌─────────────────────────────────────────────────────────┐
│         Event Ordering Strategy                        │
└─────────────────────────────────────────────────────────┘

Key Strategy:
├─ Partition key = Entity ID (agentId, conversationId)
├─ Same entity → Same partition
├─ Same partition → Ordered processing
└─ Different partitions → Parallel processing

Example:
├─ agent-123 events → Partition 3
├─ agent-456 events → Partition 7
└─ Both processed in parallel, each in order
```

#### 2. **Implementation**

```java
@Service
public class EventPublisher {
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    
    public void publishAgentEvent(AgentEvent event) {
        // Partition by agentId to ensure ordering
        kafkaTemplate.send("agent-events", event.getAgentId(), event);
    }
    
    public void publishConversationEvent(ConversationEvent event) {
        // Partition by conversationId to ensure ordering
        kafkaTemplate.send("conversation-events", 
            event.getConversationId(), event);
    }
}
```

#### 3. **Consumer Configuration**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-match-service")
public void handleAgentEvent(AgentEvent event) {
    // Events for same agentId are processed in order
    // Single consumer per partition ensures ordering
    processAgentEvent(event);
}

@Configuration
public class KafkaConsumerConfiguration {
    @Bean
    public ConsumerFactory<String, AgentEvent> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "agent-match-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Ensure ordering
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1); // Process one at a time
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
```

---

## Question 38: What's your strategy for handling event schema evolution?

### Answer

### Schema Evolution Strategy

#### 1. **Schema Registry**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Evolution Strategy                      │
└─────────────────────────────────────────────────────────┘

Schema Registry (Confluent):
├─ Centralized schema management
├─ Version control
├─ Compatibility checking
└─ Schema evolution rules

Compatibility Modes:
├─ BACKWARD: New schema can read old data
├─ FORWARD: Old schema can read new data
└─ FULL: Both directions
```

#### 2. **Versioned Events**

```java
// Version 1
public class AgentEventV1 {
    private String agentId;
    private AgentStatus status;
    private Instant timestamp;
}

// Version 2 (backward compatible)
public class AgentEventV2 {
    private String agentId;
    private AgentStatus status;
    private Instant timestamp;
    private String reason; // New optional field
    private Map<String, Object> metadata; // New optional field
}
```

#### 3. **Schema Evolution Rules**

```java
@Configuration
public class SchemaEvolutionConfiguration {
    public void configureSchemaEvolution() {
        // Use Avro with schema registry
        // BACKWARD compatibility: New consumers can read old events
        // FORWARD compatibility: Old consumers can read new events
        
        // Add optional fields (backward compatible)
        // Remove optional fields (forward compatible)
        // Never remove required fields
        // Never change field types
    }
}
```

---

## Question 39: How do you handle event processing failures?

### Answer

### Event Processing Failure Handling

#### 1. **Retry Strategy**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-match-service")
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2),
    dltStrategy = DltStrategy.FAIL_ON_ERROR
)
public void handleAgentEvent(AgentEvent event) {
    try {
        processAgentEvent(event);
    } catch (Exception e) {
        log.error("Failed to process agent event", e);
        throw e; // Retry
    }
}
```

#### 2. **Dead Letter Queue**

```java
@KafkaListener(topics = "agent-events.DLT", groupId = "agent-match-service-dlt")
public void handleDeadLetterEvent(AgentEvent event) {
    // Events that failed after all retries
    log.error("Event failed after retries: {}", event);
    
    // Option 1: Manual review
    alertService.sendAlert("Event processing failed", event);
    
    // Option 2: Store for later processing
    failedEventRepository.save(event);
}
```

#### 3. **Error Handling**

```java
@Service
public class EventProcessor {
    public void processEvent(AgentEvent event) {
        try {
            // Process event
            processAgentEvent(event);
            
        } catch (TransientException e) {
            // Retryable error
            throw new RetryableException(e);
            
        } catch (PermanentException e) {
            // Non-retryable error
            log.error("Permanent error processing event", e);
            sendToDeadLetterQueue(event, e);
            
        } catch (Exception e) {
            // Unknown error
            log.error("Unknown error processing event", e);
            throw new RetryableException(e); // Retry by default
        }
    }
}
```

---

## Question 40: Explain the partitioning strategy for Kafka topics.

### Answer

### Partitioning Strategy

#### 1. **Partition Key Selection**

```
┌─────────────────────────────────────────────────────────┐
│         Partitioning Strategy                          │
└─────────────────────────────────────────────────────────┘

Partition Key = Entity ID:
├─ agent-events → agentId
├─ conversation-events → conversationId
├─ message-events → conversationId
└─ session-events → sessionId

Benefits:
├─ Events for same entity → same partition
├─ Ordered processing per entity
├─ Parallel processing across entities
└─ Load distribution
```

#### 2. **Partition Count**

```java
@Configuration
public class KafkaPartitioningStrategy {
    public int calculatePartitions(String topic, int expectedThroughput) {
        // Target: 1 partition per 1MB/s throughput
        // With 10MB/s expected throughput → 10 partitions
        
        // Consider:
        // - Expected message rate
        // - Consumer parallelism
        // - Replication factor
        
        return Math.max(1, expectedThroughput / 1000000); // 1 partition per MB/s
    }
}
```

#### 3. **Custom Partitioner**

```java
@Component
public class TenantAwarePartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, 
                        Object value, byte[] valueBytes, Cluster cluster) {
        // Extract tenant ID from key or value
        String tenantId = extractTenantId(key, value);
        
        // Hash tenant ID to partition
        int partitionCount = cluster.partitionCountForTopic(topic);
        return Math.abs(tenantId.hashCode()) % partitionCount;
    }
}
```

---

## Question 41: What's the difference between agent events and conversation events?

### Answer

### Event Type Differences

#### 1. **Agent Events**

```java
public class AgentEvent {
    private String agentId;
    private AgentEventType type; // MATCHED, STATE_CHANGED, AVAILABLE
    private AgentStatus previousStatus;
    private AgentStatus newStatus;
    private String conversationId; // Optional
    private Instant timestamp;
}

// Examples:
// - AgentMatchedEvent: Agent assigned to conversation
// - AgentStateChangedEvent: Agent status changed (AVAILABLE → BUSY)
// - AgentAvailableEvent: Agent became available
```

#### 2. **Conversation Events**

```java
public class ConversationEvent {
    private String conversationId;
    private ConversationEventType type; // STARTED, ENDED, TRANSFERRED
    private String customerId;
    private String agentId; // Optional
    private String channel;
    private Instant timestamp;
}

// Examples:
// - ConversationStartedEvent: New conversation created
// - ConversationEndedEvent: Conversation closed
// - ConversationTransferredEvent: Conversation moved to different agent
```

#### 3. **Usage Differences**

```
┌─────────────────────────────────────────────────────────┐
│         Event Usage Differences                        │
└─────────────────────────────────────────────────────────┘

Agent Events:
├─ Consumers: Agent Match Service, Analytics Service
├─ Purpose: Agent state management, routing decisions
├─ Frequency: Medium (agent state changes)
└─ Partition: By agentId

Conversation Events:
├─ Consumers: Conversation Service, Analytics Service, Reporting
├─ Purpose: Conversation lifecycle, metrics, reporting
├─ Frequency: Low (conversation lifecycle events)
└─ Partition: By conversationId
```

---

## Question 42: How do you ensure idempotency in event handlers?

### Answer

### Idempotency Strategy

#### 1. **Idempotent Event Processing**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-match-service")
public void handleAgentEvent(AgentEvent event) {
    // Check if event already processed
    String eventId = event.getEventId();
    if (isEventProcessed(eventId)) {
        log.debug("Event {} already processed, skipping", eventId);
        return;
    }
    
    // Process event
    processAgentEvent(event);
    
    // Mark as processed
    markEventAsProcessed(eventId);
}

private boolean isEventProcessed(String eventId) {
    // Check in Redis or database
    return redisTemplate.opsForValue()
        .get("event:processed:" + eventId) != null;
}

private void markEventAsProcessed(String eventId) {
    // Store in Redis with TTL
    redisTemplate.opsForValue().set(
        "event:processed:" + eventId,
        "true",
        Duration.ofDays(7) // Keep for 7 days
    );
}
```

#### 2. **Idempotent Operations**

```java
@Service
public class AgentStateManager {
    public void updateAgentState(AgentEvent event) {
        // Idempotent operation: Same event → Same result
        AgentState state = getAgentState(event.getAgentId());
        
        // Check version to prevent duplicate processing
        if (state.getVersion() >= event.getVersion()) {
            log.debug("Event version {} already processed, current version {}", 
                event.getVersion(), state.getVersion());
            return; // Already processed
        }
        
        // Apply event
        state.setStatus(event.getNewStatus());
        state.setVersion(event.getVersion());
        saveAgentState(event.getAgentId(), state);
    }
}
```

---

## Question 43: What happens if an event is processed multiple times?

### Answer

### Duplicate Event Handling

#### 1. **Duplicate Detection**

```java
@Service
public class EventDeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isDuplicate(String eventId) {
        String key = "event:processed:" + eventId;
        return redisTemplate.hasKey(key);
    }
    
    public void markAsProcessed(String eventId) {
        String key = "event:processed:" + eventId;
        redisTemplate.opsForValue().set(key, "true", Duration.ofDays(7));
    }
}
```

#### 2. **Idempotent Processing**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-match-service")
public void handleAgentEvent(AgentEvent event) {
    // Idempotent processing
    // Processing same event multiple times → Same result
    
    if (isDuplicate(event.getEventId())) {
        return; // Skip duplicate
    }
    
    // Process (idempotent operation)
    updateAgentState(event);
    
    // Mark as processed
    markAsProcessed(event.getEventId());
}
```

---

## Question 44: How do you handle event processing latency during peak hours?

### Answer

### Latency Handling

#### 1. **Consumer Scaling**

```java
@Configuration
public class KafkaConsumerConfiguration {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AgentEvent> 
        kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AgentEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(10); // 10 concurrent consumers
        return factory;
    }
}
```

#### 2. **Batch Processing**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-match-service")
public void handleAgentEvents(List<AgentEvent> events) {
    // Process batch
    events.parallelStream()
        .forEach(this::processAgentEvent);
}
```

#### 3. **Async Processing**

```java
@Service
public class EventProcessor {
    private final ExecutorService executorService;
    
    @KafkaListener(topics = "agent-events", groupId = "agent-match-service")
    public void handleAgentEvent(AgentEvent event) {
        // Process asynchronously
        executorService.submit(() -> {
            processAgentEvent(event);
        });
    }
}
```

---

## Question 45: Explain the event sourcing pattern used in the system.

### Answer

### Event Sourcing Pattern

#### 1. **Event Sourcing Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Pattern                        │
└─────────────────────────────────────────────────────────┘

State = Sum of Events:
├─ Current state derived from events
├─ Events are source of truth
├─ Replay events to rebuild state
└─ Complete audit trail

Example - Agent State:
├─ Event 1: AgentCreated (agent-123, AVAILABLE)
├─ Event 2: AgentMatched (agent-123, BUSY)
├─ Event 3: AgentStateChanged (agent-123, AWAY)
└─ Current State: agent-123, AWAY
```

#### 2. **State Reconstruction**

```java
@Service
public class AgentStateReconstructionService {
    public AgentState reconstructState(String agentId) {
        // Read all events for agent
        List<AgentEvent> events = readEventsFromKafka(agentId);
        
        // Start with initial state
        AgentState state = AgentState.defaultState(agentId);
        
        // Apply events in order
        for (AgentEvent event : events) {
            state = applyEvent(state, event);
        }
        
        return state;
    }
    
    private AgentState applyEvent(AgentState state, AgentEvent event) {
        return switch (event.getType()) {
            case AGENT_CREATED -> state.withStatus(AgentStatus.AVAILABLE);
            case AGENT_MATCHED -> state.withStatus(AgentStatus.BUSY)
                                       .withConversationId(event.getConversationId());
            case AGENT_STATE_CHANGED -> state.withStatus(event.getNewStatus());
            default -> state;
        };
    }
}
```

#### 3. **Snapshot Strategy**

```java
@Service
public class AgentStateSnapshotService {
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void createSnapshot() {
        // Create snapshot of all agent states
        Map<String, AgentState> states = getAllAgentStates();
        
        AgentStateSnapshot snapshot = new AgentStateSnapshot(
            Instant.now(),
            states
        );
        
        // Save snapshot
        snapshotRepository.save(snapshot);
    }
    
    public AgentState reconstructFromSnapshot(String agentId, Instant timestamp) {
        // Get snapshot before timestamp
        AgentStateSnapshot snapshot = snapshotRepository
            .findLatestBefore(timestamp);
        
        // Get state from snapshot
        AgentState state = snapshot.getStates().get(agentId);
        
        // Apply events after snapshot
        List<AgentEvent> events = readEventsAfter(agentId, snapshot.getTimestamp());
        for (AgentEvent event : events) {
            state = applyEvent(state, event);
        }
        
        return state;
    }
}
```

---

## Summary

Event-Driven Architecture (Project 1) answers cover:

1. **Kafka Architecture**: 6 topics with proper partitioning
2. **Event Ordering**: Partitioning by entity ID
3. **Schema Evolution**: Schema registry with compatibility modes
4. **Failure Handling**: Retry, dead letter queue, error handling
5. **Partitioning Strategy**: Entity-based partitioning
6. **Event Types**: Agent vs conversation events
7. **Idempotency**: Duplicate detection and idempotent operations
8. **Duplicate Handling**: Event deduplication
9. **Latency Handling**: Consumer scaling, batch processing, async
10. **Event Sourcing**: State reconstruction from events, snapshots
