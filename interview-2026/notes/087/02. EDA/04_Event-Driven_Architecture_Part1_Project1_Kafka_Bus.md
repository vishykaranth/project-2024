# Event-Driven Architecture Part 1: Project 1 - Kafka Event Bus

## Question 36: Explain the Kafka event bus architecture. How many topics did you use?

### Answer

### Kafka Event Bus Architecture

#### 1. **Overall Architecture**

```
┌─────────────────────────────────────────────────────────┐
│                    Kafka Cluster                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Broker 1    │  │  Broker 2    │  │  Broker 3    │  │
│  │  (Leader)    │  │  (Replica)   │  │  (Replica)   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
        ┌─────────────┐ ┌──────────┐ ┌──────────┐
        │  Producer   │ │ Consumer │ │ Consumer │
        │  Services   │ │ Groups   │ │ Groups   │
        └─────────────┘ └──────────┘ └──────────┘
```

#### 2. **Topic Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Topics (6 Topics)                        │
└─────────────────────────────────────────────────────────┘

1. agent-events
   ├─ Partitions: 10
   ├─ Replication: 3
   ├─ Retention: 7 days
   └─ Events: Agent state changes, matches

2. conversation-events
   ├─ Partitions: 10
   ├─ Replication: 3
   ├─ Retention: 7 days
   └─ Events: Conversation started, ended, updated

3. message-events
   ├─ Partitions: 20
   ├─ Replication: 3
   ├─ Retention: 7 days
   └─ Events: Messages sent, received, delivered

4. session-events
   ├─ Partitions: 10
   ├─ Replication: 3
   ├─ Retention: 7 days
   └─ Events: Session created, expired, updated

5. bot-events
   ├─ Partitions: 10
   ├─ Replication: 3
   ├─ Retention: 7 days
   └─ Events: Bot responses, intents, entities

6. analytics-events
   ├─ Partitions: 5
   ├─ Replication: 3
   ├─ Retention: 30 days
   └─ Events: Metrics, KPIs, business events
```

#### 3. **Topic Configuration**

```java
@Configuration
public class KafkaTopicConfig {
    
    @Bean
    public NewTopic agentEventsTopic() {
        return TopicBuilder.name("agent-events")
            .partitions(10)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
            .build();
    }
    
    @Bean
    public NewTopic conversationEventsTopic() {
        return TopicBuilder.name("conversation-events")
            .partitions(10)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
            .build();
    }
    
    @Bean
    public NewTopic messageEventsTopic() {
        return TopicBuilder.name("message-events")
            .partitions(20) // Higher partitions for message volume
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(7).toMillis()))
            .build();
    }
}
```

#### 4. **Event Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Event Flow Architecture                         │
└─────────────────────────────────────────────────────────┘

Producer Flow:
1. Service generates event
2. Serialize event to JSON/Avro
3. Determine partition key
4. Send to Kafka broker
5. Broker replicates to replicas
6. Acknowledge to producer

Consumer Flow:
1. Consumer polls Kafka
2. Receive batch of events
3. Deserialize events
4. Process events
5. Commit offset
6. Continue polling
```

---

## Question 37: How do you ensure event ordering in Kafka?

### Answer

### Event Ordering Strategy

#### 1. **Partition-Based Ordering**

```
┌─────────────────────────────────────────────────────────┐
│         Partition-Based Ordering                       │
└─────────────────────────────────────────────────────────┘

Key Principle:
├─ Events with same key → same partition
├─ Events in partition → processed in order
├─ Different partitions → processed in parallel
└─ Ordering guaranteed per partition

Example:
├─ agent-123 events → Partition 3
├─ agent-456 events → Partition 7
└─ Both processed in order within their partitions
```

#### 2. **Partition Key Strategy**

```java
@Service
public class EventPublisher {
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    
    public void publishAgentEvent(AgentEvent event) {
        // Partition key = agentId ensures all events for same agent
        // go to same partition and maintain order
        String partitionKey = event.getAgentId();
        
        kafkaTemplate.send("agent-events", partitionKey, event);
    }
    
    public void publishConversationEvent(ConversationEvent event) {
        // Partition key = conversationId ensures all events for same
        // conversation go to same partition
        String partitionKey = event.getConversationId();
        
        kafkaTemplate.send("conversation-events", partitionKey, event);
    }
    
    public void publishMessageEvent(MessageEvent event) {
        // Partition key = conversationId ensures message ordering
        String partitionKey = event.getConversationId();
        
        kafkaTemplate.send("message-events", partitionKey, event);
    }
}
```

#### 3. **Partition Assignment**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Assignment                            │
└─────────────────────────────────────────────────────────┘

Hash-Based Partitioning:
├─ hash(key) % num_partitions = partition
├─ Consistent hashing
└─ Even distribution

Example:
├─ agent-123 → hash("agent-123") % 10 = 3
├─ agent-456 → hash("agent-456") % 10 = 7
└─ agent-789 → hash("agent-789") % 10 = 3 (same as agent-123)
```

#### 4. **Consumer Group Configuration**

```java
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConsumerFactory<String, AgentEvent> agentEventConsumerFactory() {
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
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AgentEvent> 
        agentEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AgentEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(agentEventConsumerFactory());
        factory.setConcurrency(10); // One thread per partition
        return factory;
    }
}
```

#### 5. **Ordering Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Ordering Guarantees                            │
└─────────────────────────────────────────────────────────┘

Per-Partition Ordering:
├─ Events in same partition → processed in order
├─ Single consumer per partition
└─ Sequential processing

Cross-Partition Ordering:
├─ Events in different partitions → no ordering guarantee
├─ Parallel processing
└─ Acceptable for most use cases

Example:
├─ agent-123: Event1, Event2, Event3 → Processed in order
├─ agent-456: Event1, Event2, Event3 → Processed in order
└─ But agent-123 Event2 may process before agent-456 Event1
```

---

## Question 38: What's your strategy for handling event schema evolution?

### Answer

### Event Schema Evolution Strategy

#### 1. **Schema Registry**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Registry Architecture                   │
└─────────────────────────────────────────────────────────┘

Components:
├─ Confluent Schema Registry
├─ Avro schemas
├─ Version management
└─ Compatibility checking

Flow:
1. Producer registers schema
2. Schema Registry validates compatibility
3. Schema version assigned
4. Events include schema version
5. Consumer uses schema version to deserialize
```

#### 2. **Schema Versioning**

```java
// Version 1: Initial schema
@AvroSchema("""
{
  "type": "record",
  "name": "AgentMatchedEvent",
  "namespace": "com.example.events",
  "fields": [
    {"name": "agentId", "type": "string"},
    {"name": "conversationId", "type": "string"},
    {"name": "timestamp", "type": "long"}
  ]
}
""")
public class AgentMatchedEventV1 {
    private String agentId;
    private String conversationId;
    private Long timestamp;
}

// Version 2: Added field (backward compatible)
@AvroSchema("""
{
  "type": "record",
  "name": "AgentMatchedEvent",
  "namespace": "com.example.events",
  "fields": [
    {"name": "agentId", "type": "string"},
    {"name": "conversationId", "type": "string"},
    {"name": "timestamp", "type": "long"},
    {"name": "priority", "type": ["null", "string"], "default": null}
  ]
}
""")
public class AgentMatchedEventV2 {
    private String agentId;
    private String conversationId;
    private Long timestamp;
    private String priority; // New optional field
}
```

#### 3. **Compatibility Modes**

```
┌─────────────────────────────────────────────────────────┐
│         Compatibility Modes                            │
└─────────────────────────────────────────────────────────┘

BACKWARD (Default):
├─ New schema can read old data
├─ Old consumers can read new data (if fields optional)
└─ Can add optional fields

FORWARD:
├─ Old schema can read new data
├─ New consumers can read old data
└─ Can remove optional fields

FULL:
├─ Both backward and forward compatible
└─ Most restrictive

NONE:
├─ No compatibility checking
└─ Use with caution
```

#### 4. **Schema Evolution Implementation**

```java
@Service
public class EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SchemaRegistryClient schemaRegistry;
    
    public void publishAgentEvent(AgentMatchedEvent event) {
        // Get latest schema version
        int schemaVersion = schemaRegistry.getLatestSchemaVersion(
            "agent-matched-event");
        
        // Include schema version in event
        EventWrapper wrapper = EventWrapper.builder()
            .schemaVersion(schemaVersion)
            .event(event)
            .build();
        
        kafkaTemplate.send("agent-events", event.getAgentId(), wrapper);
    }
}

@Service
public class EventConsumer {
    @KafkaListener(topics = "agent-events")
    public void handleAgentEvent(EventWrapper wrapper) {
        int schemaVersion = wrapper.getSchemaVersion();
        
        // Deserialize based on schema version
        AgentMatchedEvent event = deserializeEvent(
            wrapper.getEvent(), 
            schemaVersion
        );
        
        processEvent(event);
    }
    
    private AgentMatchedEvent deserializeEvent(Object data, int version) {
        switch (version) {
            case 1:
                return deserializeV1(data);
            case 2:
                return deserializeV2(data);
            default:
                throw new UnsupportedSchemaVersionException(version);
        }
    }
}
```

#### 5. **Migration Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Migration Strategy                      │
└─────────────────────────────────────────────────────────┘

Phase 1: Deploy New Schema (Backward Compatible)
├─ Deploy consumers with new schema support
├─ Old consumers continue working
└─ New fields optional

Phase 2: Update Producers
├─ Start producing events with new schema
├─ Both old and new events in topic
└─ Consumers handle both

Phase 3: Deprecate Old Schema
├─ All producers using new schema
├─ Remove old schema support from consumers
└─ Clean up old code

Rollback Plan:
├─ Keep old schema support
├─ Feature flags for new schema
└─ Gradual rollout
```

---

## Question 39: How do you handle event processing failures?

### Answer

### Event Processing Failure Handling

#### 1. **Failure Types**

```
┌─────────────────────────────────────────────────────────┐
│         Failure Types                                  │
└─────────────────────────────────────────────────────────┘

Transient Failures:
├─ Network issues
├─ Temporary service unavailability
├─ Timeout errors
└─ Retry with backoff

Permanent Failures:
├─ Invalid event data
├─ Business logic errors
├─ Data corruption
└─ Dead letter queue

Processing Failures:
├─ Consumer crashes
├─ Memory issues
├─ Infinite loops
└─ Restart consumer
```

#### 2. **Retry Strategy**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-match-service")
public void handleAgentEvent(AgentMatchedEvent event) {
    int maxRetries = 3;
    int retryCount = 0;
    
    while (retryCount < maxRetries) {
        try {
            processEvent(event);
            return; // Success
            
        } catch (TransientException e) {
            retryCount++;
            if (retryCount >= maxRetries) {
                sendToDeadLetterQueue(event, e);
                return;
            }
            
            // Exponential backoff
            long delay = (long) Math.pow(2, retryCount) * 1000; // 1s, 2s, 4s
            Thread.sleep(delay);
            
        } catch (PermanentException e) {
            // Don't retry, send to DLQ immediately
            sendToDeadLetterQueue(event, e);
            return;
        }
    }
}
```

#### 3. **Dead Letter Queue**

```java
@Service
public class DeadLetterQueueService {
    private final KafkaTemplate<String, FailedEvent> kafkaTemplate;
    
    public void sendToDeadLetterQueue(AgentEvent event, Exception error) {
        FailedEvent failedEvent = FailedEvent.builder()
            .originalEvent(event)
            .errorMessage(error.getMessage())
            .errorType(error.getClass().getName())
            .timestamp(Instant.now())
            .retryCount(3)
            .build();
        
        kafkaTemplate.send("agent-events-dlq", event.getAgentId(), failedEvent);
        
        // Also log for monitoring
        log.error("Event sent to DLQ: {}", event, error);
    }
    
    @KafkaListener(topics = "agent-events-dlq", groupId = "dlq-processor")
    public void processDeadLetterEvent(FailedEvent failedEvent) {
        // Manual intervention required
        alertService.sendAlert(
            "Event processing failed",
            failedEvent
        );
        
        // Store for analysis
        failedEventRepository.save(failedEvent);
    }
}
```

#### 4. **Error Handling Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Error Handling Patterns                        │
└─────────────────────────────────────────────────────────┘

Pattern 1: Retry with Backoff
├─ Transient failures
├─ Exponential backoff
└─ Max retry limit

Pattern 2: Dead Letter Queue
├─ Permanent failures
├─ Manual intervention
└─ Analysis and fix

Pattern 3: Circuit Breaker
├─ Repeated failures
├─ Stop processing temporarily
└─ Resume after cooldown

Pattern 4: Skip and Continue
├─ Non-critical events
├─ Log error
└─ Continue processing
```

#### 5. **Consumer Error Handling**

```java
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AgentEvent> 
        kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AgentEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // Error handling
        factory.setCommonErrorHandler(new DefaultErrorHandler(
            new FixedBackOff(1000L, 3L) // Retry 3 times with 1s delay
        ));
        
        // Custom error handler
        factory.setErrorHandler((exception, data) -> {
            log.error("Error processing event: {}", data, exception);
            // Send to DLQ
            deadLetterQueueService.sendToDLQ(data, exception);
        });
        
        return factory;
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
│         Partition Key Strategy                         │
└─────────────────────────────────────────────────────────┘

Key Principles:
├─ Events needing ordering → same partition
├─ Even distribution → avoid hotspots
├─ Related events → same partition
└─ High cardinality → good distribution

Partition Keys:
├─ agent-events: agentId
├─ conversation-events: conversationId
├─ message-events: conversationId
└─ session-events: sessionId
```

#### 2. **Partition Count Calculation**

```java
@Service
public class PartitionStrategy {
    /**
     * Calculate optimal partition count
     * 
     * Factors:
     * - Throughput: Events per second
     * - Consumer parallelism: Consumers per topic
     * - Ordering requirements: Per-entity ordering
     * - Growth: Future scaling
     */
    public int calculatePartitions(String topic, long eventsPerSecond) {
        // Target: 10,000 events/second per partition
        int basePartitions = (int) Math.ceil(eventsPerSecond / 10000.0);
        
        // Add buffer for growth (50%)
        int partitionsWithBuffer = (int) (basePartitions * 1.5);
        
        // Round up to nearest power of 2 for better distribution
        return nextPowerOfTwo(partitionsWithBuffer);
    }
    
    private int nextPowerOfTwo(int n) {
        return (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
    }
}
```

#### 3. **Partition Assignment**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Assignment Examples                  │
└─────────────────────────────────────────────────────────┘

agent-events (10 partitions):
├─ agent-123 → hash("agent-123") % 10 = 3
├─ agent-456 → hash("agent-456") % 10 = 7
├─ agent-789 → hash("agent-789") % 10 = 3
└─ agent-123 and agent-789 → same partition (ordering maintained)

conversation-events (10 partitions):
├─ conv-001 → hash("conv-001") % 10 = 1
├─ conv-002 → hash("conv-002") % 10 = 8
└─ Different conversations → different partitions (parallel processing)
```

#### 4. **Custom Partitioning**

```java
@Component
public class CustomPartitioner implements Partitioner {
    
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, 
                        Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        
        if (key == null) {
            // Round-robin for null keys
            return ThreadLocalRandom.current().nextInt(numPartitions);
        }
        
        // Custom logic based on key
        if (key instanceof String) {
            String keyStr = (String) key;
            
            // Tenant-based partitioning
            if (keyStr.startsWith("tenant-")) {
                String tenantId = extractTenantId(keyStr);
                return Math.abs(tenantId.hashCode()) % numPartitions;
            }
            
            // Default: hash-based
            return Math.abs(keyStr.hashCode()) % numPartitions;
        }
        
        return 0;
    }
}
```

#### 5. **Partition Rebalancing**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Rebalancing                         │
└─────────────────────────────────────────────────────────┘

Scenarios:
├─ Consumer joins group
├─ Consumer leaves group
├─ Partition count changes
└─ Rebalance triggered

Strategy:
├─ Cooperative rebalancing (preferred)
├─ Minimal disruption
├─ Resume from last committed offset
└─ No duplicate processing
```

---

## Question 41: What's the difference between agent events and conversation events?

### Answer

### Event Type Comparison

#### 1. **Agent Events**

```java
// Agent State Changed Event
public class AgentStateChangedEvent {
    private String agentId;
    private AgentStatus previousStatus;
    private AgentStatus newStatus;
    private Instant timestamp;
    private String reason;
    private Long version;
}

// Agent Matched Event
public class AgentMatchedEvent {
    private String agentId;
    private String conversationId;
    private Instant timestamp;
    private AgentStatus previousState;
    private AgentStatus newState;
    private String routingReason;
}

// Agent Availability Event
public class AgentAvailabilityEvent {
    private String agentId;
    private boolean available;
    private Instant timestamp;
    private String reason;
}
```

**Characteristics:**
- **Partition Key**: `agentId`
- **Ordering**: Per agent (all events for agent-123 in order)
- **Frequency**: Lower (state changes)
- **Consumers**: Agent Match Service, Analytics Service
- **Retention**: 7 days

#### 2. **Conversation Events**

```java
// Conversation Started Event
public class ConversationStartedEvent {
    private String conversationId;
    private String customerId;
    private String agentId;
    private String channel;
    private Instant timestamp;
    private Map<String, String> metadata;
}

// Conversation Ended Event
public class ConversationEndedEvent {
    private String conversationId;
    private String reason;
    private Duration duration;
    private Instant timestamp;
    private ConversationSummary summary;
}

// Conversation Updated Event
public class ConversationUpdatedEvent {
    private String conversationId;
    private ConversationStatus status;
    private Instant timestamp;
    private String updateReason;
}
```

**Characteristics:**
- **Partition Key**: `conversationId`
- **Ordering**: Per conversation (all events for conv-001 in order)
- **Frequency**: Medium (lifecycle events)
- **Consumers**: Conversation Service, Analytics Service, Reporting Service
- **Retention**: 7 days

#### 3. **Comparison Table**

```
┌─────────────────────────────────────────────────────────┐
│         Event Type Comparison                         │
└─────────────────────────────────────────────────────────┘

Feature              | Agent Events    | Conversation Events
---------------------|-----------------|-------------------
Partition Key        | agentId         | conversationId
Ordering Scope       | Per agent       | Per conversation
Event Frequency      | Low             | Medium
Event Types          | State changes   | Lifecycle events
Primary Consumers    | Agent Service   | Conversation Service
Secondary Consumers  | Analytics       | Analytics, Reporting
Retention            | 7 days          | 7 days
Partition Count      | 10              | 10
```

#### 4. **Event Flow Example**

```
┌─────────────────────────────────────────────────────────┐
│         Event Flow Example                             │
└─────────────────────────────────────────────────────────┘

Scenario: Agent matches to conversation

1. Agent Match Service:
   ├─ Publishes AgentMatchedEvent
   └─ Partition: agent-123

2. Conversation Service:
   ├─ Publishes ConversationStartedEvent
   └─ Partition: conv-001

3. Both events processed:
   ├─ Agent events → Update agent state
   └─ Conversation events → Update conversation state
```

---

## Question 42: How do you ensure idempotency in event handlers?

### Answer

### Idempotency Strategy

#### 1. **Idempotency Keys**

```java
public class AgentMatchedEvent {
    private String eventId; // Unique event ID
    private String agentId;
    private String conversationId;
    private Instant timestamp;
    private Long version; // For ordering
}

@Service
public class AgentEventProcessor {
    private final RedisTemplate<String, String> redisTemplate;
    
    @KafkaListener(topics = "agent-events")
    public void handleAgentMatchedEvent(AgentMatchedEvent event) {
        // Check if event already processed
        String idempotencyKey = "event:processed:" + event.getEventId();
        
        Boolean alreadyProcessed = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, "processed", Duration.ofDays(7));
        
        if (!alreadyProcessed) {
            // Event already processed, skip
            log.info("Event {} already processed, skipping", event.getEventId());
            return;
        }
        
        // Process event
        processEvent(event);
    }
    
    private void processEvent(AgentMatchedEvent event) {
        // Idempotent operation
        AgentState state = getAgentState(event.getAgentId());
        
        // Check if state already reflects this event
        if (state.getVersion() >= event.getVersion()) {
            log.info("Event {} already applied, skipping", event.getEventId());
            return;
        }
        
        // Apply event
        state.setStatus(AgentStatus.BUSY);
        state.setVersion(event.getVersion());
        saveAgentState(event.getAgentId(), state);
    }
}
```

#### 2. **Version-Based Idempotency**

```java
@Service
public class ConversationEventProcessor {
    @KafkaListener(topics = "conversation-events")
    public void handleConversationEvent(ConversationEvent event) {
        Conversation conversation = getConversation(event.getConversationId());
        
        // Check version
        if (conversation.getVersion() >= event.getVersion()) {
            // Event already applied
            return;
        }
        
        // Apply event (idempotent)
        applyEvent(conversation, event);
        conversation.setVersion(event.getVersion());
        saveConversation(conversation);
    }
    
    private void applyEvent(Conversation conversation, ConversationEvent event) {
        // Idempotent operations
        switch (event.getType()) {
            case STARTED:
                if (conversation.getStatus() != ConversationStatus.ACTIVE) {
                    conversation.setStatus(ConversationStatus.ACTIVE);
                }
                break;
            case ENDED:
                if (conversation.getStatus() != ConversationStatus.ENDED) {
                    conversation.setStatus(ConversationStatus.ENDED);
                }
                break;
        }
    }
}
```

#### 3. **Idempotency Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Idempotency Patterns                           │
└─────────────────────────────────────────────────────────┘

Pattern 1: Idempotency Key
├─ Unique event ID
├─ Store in Redis/DB
├─ Check before processing
└─ Skip if already processed

Pattern 2: Version Number
├─ Event version
├─ Compare with current state
├─ Skip if version <= current
└─ Apply if version > current

Pattern 3: Idempotent Operations
├─ UPSERT operations
├─ Check-before-write
└─ No side effects on replay

Pattern 4: Deduplication Window
├─ Store processed events
├─ TTL-based cleanup
└─ Memory efficient
```

---

## Question 43: What happens if an event is processed multiple times?

### Answer

### Duplicate Event Handling

#### 1. **Duplicate Scenarios**

```
┌─────────────────────────────────────────────────────────┐
│         Duplicate Event Scenarios                      │
└─────────────────────────────────────────────────────────┘

Scenario 1: Consumer Restart
├─ Event processed but offset not committed
├─ Consumer restarts
├─ Re-processes same event
└─ Solution: Idempotency

Scenario 2: Network Retry
├─ Producer retries on timeout
├─ Event sent multiple times
├─ Consumer receives duplicates
└─ Solution: Deduplication

Scenario 3: Rebalance
├─ Consumer rebalances
├─ Events reprocessed
└─ Solution: Idempotency + offset management
```

#### 2. **Deduplication Strategy**

```java
@Service
public class EventDeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isDuplicate(String eventId) {
        String key = "event:dedup:" + eventId;
        
        // Set with TTL (7 days)
        Boolean isNew = redisTemplate.opsForValue()
            .setIfAbsent(key, "processed", Duration.ofDays(7));
        
        return !isNew; // true if duplicate
    }
    
    @KafkaListener(topics = "agent-events")
    public void handleEvent(AgentMatchedEvent event) {
        // Check for duplicates
        if (isDuplicate(event.getEventId())) {
            log.warn("Duplicate event detected: {}", event.getEventId());
            return; // Skip duplicate
        }
        
        // Process event
        processEvent(event);
    }
}
```

#### 3. **Idempotent Processing**

```java
@Service
public class IdempotentEventProcessor {
    
    @KafkaListener(topics = "agent-events")
    public void handleAgentMatchedEvent(AgentMatchedEvent event) {
        // Idempotent processing
        AgentState state = getAgentState(event.getAgentId());
        
        // Check if event already applied
        if (isEventApplied(state, event)) {
            log.info("Event {} already applied to agent {}", 
                event.getEventId(), event.getAgentId());
            return;
        }
        
        // Apply event (idempotent operation)
        applyEventIdempotently(state, event);
    }
    
    private boolean isEventApplied(AgentState state, AgentMatchedEvent event) {
        // Check version
        if (state.getVersion() >= event.getVersion()) {
            return true;
        }
        
        // Check state
        if (state.getStatus() == AgentStatus.BUSY && 
            state.getCurrentConversationId().equals(event.getConversationId())) {
            return true; // Already matched to this conversation
        }
        
        return false;
    }
    
    private void applyEventIdempotently(AgentState state, AgentMatchedEvent event) {
        // Idempotent operations only
        if (state.getStatus() != AgentStatus.BUSY) {
            state.setStatus(AgentStatus.BUSY);
        }
        
        if (!event.getConversationId().equals(state.getCurrentConversationId())) {
            state.setCurrentConversationId(event.getConversationId());
        }
        
        state.setVersion(event.getVersion());
        saveAgentState(state);
    }
}
```

---

## Question 44: How do you handle event processing latency during peak hours?

### Answer

### Latency Handling Strategy

#### 1. **Consumer Scaling**

```
┌─────────────────────────────────────────────────────────┐
│         Consumer Scaling Strategy                     │
└─────────────────────────────────────────────────────────┘

Horizontal Scaling:
├─ Increase consumer instances
├─ More partitions = more parallelism
└─ Auto-scaling based on lag

Example:
├─ Normal: 5 consumers, 10 partitions
├─ Peak: 20 consumers, 10 partitions
└─ Each partition processed by 2 consumers (round-robin)
```

#### 2. **Batch Processing**

```java
@KafkaListener(topics = "message-events", groupId = "message-processor")
public void handleMessageEvents(
    @Payload List<MessageEvent> events,
    @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
    @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
    
    // Process batch
    List<CompletableFuture<Void>> futures = events.stream()
        .map(event -> CompletableFuture.runAsync(() -> processEvent(event)))
        .collect(Collectors.toList());
    
    // Wait for all to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .join();
}
```

#### 3. **Async Processing**

```java
@Service
public class AsyncEventProcessor {
    private final ExecutorService executorService;
    
    @KafkaListener(topics = "agent-events")
    public void handleAgentEvent(AgentMatchedEvent event) {
        // Process asynchronously
        executorService.submit(() -> {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Error processing event", e);
                sendToDLQ(event, e);
            }
        });
        
        // Return immediately (don't block consumer)
    }
    
    private void processEvent(AgentMatchedEvent event) {
        // Long-running operation
        updateAgentState(event);
        notifySystems(event);
        updateAnalytics(event);
    }
}
```

#### 4. **Lag Monitoring**

```java
@Component
public class ConsumerLagMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void monitorConsumerLag() {
        Map<TopicPartition, Long> lag = getConsumerLag();
        
        lag.forEach((partition, lagValue) -> {
            Gauge.builder("kafka.consumer.lag")
                .tag("topic", partition.topic())
                .tag("partition", String.valueOf(partition.partition()))
                .register(meterRegistry)
                .set(lagValue);
            
            // Alert if lag too high
            if (lagValue > 10000) {
                alertService.sendAlert(
                    "High consumer lag detected",
                    Map.of("partition", partition, "lag", lagValue)
                );
            }
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

Traditional Approach:
├─ Store current state
├─ Update state directly
└─ Lose history

Event Sourcing:
├─ Store all events
├─ Rebuild state from events
└─ Complete history
```

#### 2. **State Reconstruction**

```java
@Service
public class AgentStateReconstructionService {
    
    public AgentState reconstructState(String agentId) {
        // Read all events for this agent
        List<AgentEvent> events = readEventsFromKafka(agentId);
        
        // Start with initial state
        AgentState state = AgentState.initialState(agentId);
        
        // Apply all events in order
        for (AgentEvent event : events) {
            state = applyEvent(state, event);
        }
        
        return state;
    }
    
    private AgentState applyEvent(AgentState state, AgentEvent event) {
        switch (event.getType()) {
            case STATE_CHANGED:
                state.setStatus(event.getNewStatus());
                break;
            case MATCHED:
                state.setStatus(AgentStatus.BUSY);
                state.setCurrentConversationId(event.getConversationId());
                break;
            case AVAILABILITY_CHANGED:
                state.setAvailable(event.isAvailable());
                break;
        }
        
        state.setVersion(event.getVersion());
        state.setLastUpdated(event.getTimestamp());
        return state;
    }
}
```

#### 3. **Snapshot Strategy**

```java
@Service
public class AgentStateSnapshotService {
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void createSnapshot() {
        List<String> agentIds = getAllAgentIds();
        
        for (String agentId : agentIds) {
            // Reconstruct current state
            AgentState currentState = reconstructState(agentId);
            
            // Create snapshot
            AgentStateSnapshot snapshot = AgentStateSnapshot.builder()
                .agentId(agentId)
                .state(currentState)
                .version(currentState.getVersion())
                .timestamp(Instant.now())
                .build();
            
            // Save snapshot
            snapshotRepository.save(snapshot);
        }
    }
    
    public AgentState reconstructFromSnapshot(String agentId) {
        // Get latest snapshot
        AgentStateSnapshot snapshot = snapshotRepository
            .findLatestByAgentId(agentId)
            .orElse(null);
        
        if (snapshot == null) {
            // No snapshot, rebuild from all events
            return reconstructState(agentId);
        }
        
        // Get events after snapshot
        List<AgentEvent> events = readEventsAfterVersion(
            agentId, 
            snapshot.getVersion()
        );
        
        // Apply events to snapshot
        AgentState state = snapshot.getState();
        for (AgentEvent event : events) {
            state = applyEvent(state, event);
        }
        
        return state;
    }
}
```

#### 4. **Event Store**

```
┌─────────────────────────────────────────────────────────┐
│         Event Store Architecture                      │
└─────────────────────────────────────────────────────────┘

Kafka as Event Store:
├─ All events stored in Kafka
├─ Retention: 7 days (configurable)
├─ Replay capability
└─ Partitioned by entity ID

Benefits:
├─ Complete audit trail
├─ Time travel (replay to any point)
├─ Debugging capability
└─ Compliance
```

---

## Summary

Event-Driven Architecture Part 1 covers:

1. **Kafka Event Bus**: 6 topics with proper partitioning and replication
2. **Event Ordering**: Partition-based ordering per entity
3. **Schema Evolution**: Schema Registry with backward compatibility
4. **Failure Handling**: Retry, DLQ, and error recovery
5. **Partitioning**: Hash-based partitioning for even distribution
6. **Event Types**: Agent events vs conversation events
7. **Idempotency**: Event ID and version-based deduplication
8. **Latency**: Scaling, batching, and async processing
9. **Event Sourcing**: State reconstruction from events with snapshots
