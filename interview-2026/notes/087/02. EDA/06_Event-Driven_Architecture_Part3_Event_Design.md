# Event-Driven Architecture Part 3: Event Design

## Question 146: How do you design event schemas?

### Answer

### Event Schema Design Principles

#### 1. **Schema Design Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Event Schema Design Principles                 │
└─────────────────────────────────────────────────────────┘

1. Immutability:
   ├─ Events are immutable
   ├─ Never modify existing events
   └─ Create new events for changes

2. Completeness:
   ├─ Include all necessary data
   ├─ Avoid external dependencies
   └─ Self-contained events

3. Versioning:
   ├─ Support schema evolution
   ├─ Backward compatibility
   └─ Version numbers

4. Clarity:
   ├─ Clear naming
   ├─ Well-documented
   └─ Type safety
```

#### 2. **Event Schema Structure**

```java
// Base Event Interface
public interface Event {
    String getEventId();
    String getEventType();
    Instant getTimestamp();
    Long getVersion();
    String getSource();
}

// Agent Matched Event
@JsonTypeName("AgentMatchedEvent")
public class AgentMatchedEvent implements Event {
    // Event Metadata
    private String eventId;
    private String eventType = "AgentMatchedEvent";
    private Instant timestamp;
    private Long version = 1L;
    private String source = "agent-match-service";
    
    // Event Data
    private String agentId;
    private String conversationId;
    private AgentStatus previousState;
    private AgentStatus newState;
    private String routingReason;
    private Map<String, String> metadata;
    
    // Getters and setters
}
```

#### 3. **Schema Best Practices**

```java
// Good: Complete event with all context
public class TradeCreatedEvent {
    private String tradeId;
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal price;
    private String currency;
    private TradeType type;
    private Instant timestamp;
    private String idempotencyKey;
    private Long sequenceNumber;
    private Map<String, String> metadata; // Extensible
}

// Bad: Incomplete event requiring external lookups
public class TradeCreatedEvent {
    private String tradeId; // Need to lookup trade details
    private Instant timestamp;
}
```

---

## Question 147: What's the event versioning strategy?

### Answer

### Event Versioning Strategy

#### 1. **Version Numbering**

```
┌─────────────────────────────────────────────────────────┐
│         Version Numbering Strategy                     │
└─────────────────────────────────────────────────────────┘

Semantic Versioning:
├─ Major: Breaking changes
├─ Minor: Backward compatible additions
└─ Patch: Bug fixes

Example:
├─ v1.0.0: Initial version
├─ v1.1.0: Added optional field (backward compatible)
├─ v2.0.0: Removed required field (breaking)
└─ v2.1.0: Added new optional field
```

#### 2. **Version in Event**

```java
public class AgentMatchedEvent {
    private Long version; // Schema version
    
    // Version 1 fields
    private String agentId;
    private String conversationId;
    private Instant timestamp;
    
    // Version 2 fields (optional for backward compatibility)
    private String priority; // Added in v2
    
    // Version 3 fields (optional)
    private Map<String, String> metadata; // Added in v3
}
```

#### 3. **Version Registry**

```java
@Service
public class EventVersionRegistry {
    private final Map<String, Integer> eventVersions = new HashMap<>();
    
    public int getLatestVersion(String eventType) {
        return eventVersions.getOrDefault(eventType, 1);
    }
    
    public void registerVersion(String eventType, int version) {
        eventVersions.put(eventType, version);
    }
    
    public boolean isCompatible(String eventType, int fromVersion, int toVersion) {
        // Check compatibility rules
        return compatibilityChecker.isCompatible(eventType, fromVersion, toVersion);
    }
}
```

---

## Question 148: How do you handle event schema evolution?

### Answer

### Schema Evolution Strategy

#### 1. **Evolution Rules**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Evolution Rules                         │
└─────────────────────────────────────────────────────────┘

Backward Compatible Changes:
├─ Add optional fields
├─ Remove optional fields (with default)
├─ Change field types (compatible)
└─ Reorder fields

Breaking Changes:
├─ Remove required fields
├─ Change required field types
├─ Add required fields
└─ Rename fields
```

#### 2. **Evolution Process**

```java
// Version 1: Initial
public class TradeCreatedEventV1 {
    private String tradeId;
    private String accountId;
    private BigDecimal quantity;
    private BigDecimal price;
}

// Version 2: Add optional field (backward compatible)
public class TradeCreatedEventV2 {
    private String tradeId;
    private String accountId;
    private BigDecimal quantity;
    private BigDecimal price;
    private String orderId; // New optional field
}

// Version 3: Add another optional field
public class TradeCreatedEventV3 {
    private String tradeId;
    private String accountId;
    private BigDecimal quantity;
    private BigDecimal price;
    private String orderId;
    private String strategy; // New optional field
}
```

#### 3. **Migration Strategy**

```java
@Service
public class EventSchemaMigrator {
    
    public TradeCreatedEvent migrateEvent(Object eventData, int fromVersion, int toVersion) {
        if (fromVersion == toVersion) {
            return (TradeCreatedEvent) eventData;
        }
        
        // Migrate step by step
        Object current = eventData;
        for (int version = fromVersion; version < toVersion; version++) {
            current = migrateVersion(current, version, version + 1);
        }
        
        return (TradeCreatedEvent) current;
    }
    
    private Object migrateVersion(Object event, int fromVersion, int toVersion) {
        switch (fromVersion) {
            case 1:
                if (toVersion == 2) {
                    return migrateV1ToV2((TradeCreatedEventV1) event);
                }
                break;
            case 2:
                if (toVersion == 3) {
                    return migrateV2ToV3((TradeCreatedEventV2) event);
                }
                break;
        }
        throw new UnsupportedMigrationException(fromVersion, toVersion);
    }
    
    private TradeCreatedEventV2 migrateV1ToV2(TradeCreatedEventV1 v1) {
        return TradeCreatedEventV2.builder()
            .tradeId(v1.getTradeId())
            .accountId(v1.getAccountId())
            .quantity(v1.getQuantity())
            .price(v1.getPrice())
            .orderId(null) // Default for new field
            .build();
    }
}
```

---

## Question 149: What's the difference between event types (agent events, conversation events, trade events)?

### Answer

### Event Type Comparison

#### 1. **Event Type Categories**

```
┌─────────────────────────────────────────────────────────┐
│         Event Type Categories                          │
└─────────────────────────────────────────────────────────┘

Domain Events:
├─ Agent events
├─ Conversation events
├─ Trade events
└─ Position events

System Events:
├─ Service health events
├─ Deployment events
└─ Configuration events

Integration Events:
├─ External system events
├─ Webhook events
└─ API events
```

#### 2. **Event Type Characteristics**

```java
// Agent Event: State change event
public class AgentStateChangedEvent {
    private String agentId;
    private AgentStatus previousStatus;
    private AgentStatus newStatus;
    private Instant timestamp;
    
    // Characteristics:
    // - Low frequency
    // - State change focus
    // - Partition by agentId
}

// Conversation Event: Lifecycle event
public class ConversationStartedEvent {
    private String conversationId;
    private String customerId;
    private String agentId;
    private String channel;
    private Instant timestamp;
    
    // Characteristics:
    // - Medium frequency
    // - Lifecycle focus
    // - Partition by conversationId
}

// Trade Event: Transaction event
public class TradeCreatedEvent {
    private String tradeId;
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal price;
    private Instant timestamp;
    
    // Characteristics:
    // - High frequency
    // - Transaction focus
    // - Partition by accountId
    // - Requires ordering
}
```

#### 3. **Comparison Table**

```
┌─────────────────────────────────────────────────────────┐
│         Event Type Comparison                         │
└─────────────────────────────────────────────────────────┘

Feature              | Agent Events | Conversation Events | Trade Events
---------------------|--------------|---------------------|---------------
Frequency            | Low          | Medium              | High
Partition Key        | agentId       | conversationId      | accountId
Ordering Required    | Yes          | Yes                 | Yes (critical)
Retention            | 7 days       | 7 days              | 30 days
Consumers            | 2-3          | 3-4                 | 4-5
Criticality          | Medium       | Medium              | High
```

---

## Question 150: How do you ensure event backward compatibility?

### Answer

### Backward Compatibility Strategy

#### 1. **Compatibility Rules**

```
┌─────────────────────────────────────────────────────────┐
│         Backward Compatibility Rules                  │
└─────────────────────────────────────────────────────────┘

Rule 1: Add Optional Fields Only
├─ New fields must be optional
├─ Provide default values
└─ Old consumers ignore new fields

Rule 2: Don't Remove Fields
├─ Mark as deprecated
├─ Keep in schema
└─ Provide default values

Rule 3: Don't Change Field Types
├─ Keep same types
├─ Or provide converters
└─ Maintain compatibility
```

#### 2. **Implementation**

```java
// Version 1: Initial
public class AgentMatchedEventV1 {
    private String agentId;
    private String conversationId;
    private Instant timestamp;
}

// Version 2: Backward compatible (adds optional field)
public class AgentMatchedEventV2 {
    // Keep all V1 fields
    private String agentId;
    private String conversationId;
    private Instant timestamp;
    
    // Add optional field with default
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String priority; // Optional, defaults to null
}

// Consumer handles both versions
@Service
public class AgentEventConsumer {
    @KafkaListener(topics = "agent-events")
    public void handleEvent(AgentMatchedEvent event) {
        // Works with both V1 and V2
        String agentId = event.getAgentId();
        String conversationId = event.getConversationId();
        
        // Handle optional field
        String priority = event.getPriority() != null 
            ? event.getPriority() 
            : "NORMAL"; // Default
    }
}
```

#### 3. **Compatibility Testing**

```java
@Test
public void testBackwardCompatibility() {
    // Create V1 event
    AgentMatchedEventV1 v1Event = new AgentMatchedEventV1();
    v1Event.setAgentId("agent-123");
    v1Event.setConversationId("conv-001");
    
    // Serialize V1
    String json = objectMapper.writeValueAsString(v1Event);
    
    // Deserialize as V2 (should work)
    AgentMatchedEventV2 v2Event = objectMapper.readValue(
        json, 
        AgentMatchedEventV2.class
    );
    
    // Assert V1 fields still work
    assertEquals("agent-123", v2Event.getAgentId());
    assertEquals("conv-001", v2Event.getConversationId());
    
    // Assert new field has default
    assertNull(v2Event.getPriority()); // Optional field
}
```

---

## Question 151: What's the event payload size limit?

### Answer

### Event Payload Size Management

#### 1. **Size Limits**

```
┌─────────────────────────────────────────────────────────┐
│         Event Payload Size Limits                      │
└─────────────────────────────────────────────────────────┘

Kafka Limits:
├─ Max message size: 1MB (default)
├─ Configurable: message.max.bytes
└─ Recommended: < 100KB

Best Practices:
├─ Keep events small (< 10KB)
├─ Reference large data
├─ Use external storage
└─ Compress if needed
```

#### 2. **Size Optimization**

```java
// Bad: Large payload
public class ConversationEvent {
    private String conversationId;
    private List<Message> allMessages; // Could be large
    private List<Attachment> attachments; // Could be very large
}

// Good: Reference large data
public class ConversationEvent {
    private String conversationId;
    private String messageStoreReference; // Reference to external store
    private String attachmentStoreReference; // Reference to S3
    private ConversationSummary summary; // Small summary
}
```

#### 3. **Compression**

```java
@Configuration
public class KafkaProducerConfig {
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        
        // Enable compression
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        return new DefaultKafkaProducerFactory<>(props);
    }
}
```

---

## Question 152: How do you handle large events?

### Answer

### Large Event Handling

#### 1. **Event Chunking**

```java
// Large event split into chunks
public class LargeEventChunk {
    private String eventId;
    private int chunkNumber;
    private int totalChunks;
    private byte[] chunkData;
    private String checksum;
}

@Service
public class LargeEventProcessor {
    
    public void publishLargeEvent(LargeEvent event) {
        // Split into chunks
        List<byte[]> chunks = splitIntoChunks(event, 100 * 1024); // 100KB chunks
        
        for (int i = 0; i < chunks.size(); i++) {
            LargeEventChunk chunk = LargeEventChunk.builder()
                .eventId(event.getEventId())
                .chunkNumber(i)
                .totalChunks(chunks.size())
                .chunkData(chunks.get(i))
                .checksum(calculateChecksum(chunks.get(i)))
                .build();
            
            kafkaTemplate.send("large-events", event.getEventId(), chunk);
        }
    }
    
    @KafkaListener(topics = "large-events")
    public void handleChunk(LargeEventChunk chunk) {
        // Reassemble chunks
        eventReassembler.addChunk(chunk);
        
        if (eventReassembler.isComplete(chunk.getEventId())) {
            LargeEvent event = eventReassembler.reassemble(chunk.getEventId());
            processEvent(event);
        }
    }
}
```

#### 2. **External Storage**

```java
@Service
public class LargeEventPublisher {
    private final S3Service s3Service;
    
    public void publishLargeEvent(LargeEvent event) {
        // Store large data in S3
        String s3Key = "events/" + event.getEventId();
        s3Service.putObject(s3Key, serialize(event));
        
        // Publish small reference event
        EventReference reference = EventReference.builder()
            .eventId(event.getEventId())
            .storageLocation("s3://bucket/" + s3Key)
            .size(event.getSize())
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("event-references", event.getEventId(), reference);
    }
    
    @KafkaListener(topics = "event-references")
    public void handleEventReference(EventReference reference) {
        // Fetch from S3
        LargeEvent event = s3Service.getObject(reference.getStorageLocation());
        processEvent(event);
    }
}
```

---

## Question 153: What's the event retention policy?

### Answer

### Event Retention Policy

#### 1. **Retention Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Event Retention Policy                         │
└─────────────────────────────────────────────────────────┘

Conversational AI Platform:
├─ agent-events: 7 days
├─ conversation-events: 7 days
├─ message-events: 7 days
└─ analytics-events: 30 days

Prime Broker System:
├─ trade-events: 30 days
├─ position-events: 30 days
├─ ledger-events: 7 years (compliance)
└─ settlement-events: 7 years (compliance)
```

#### 2. **Retention Configuration**

```java
@Configuration
public class KafkaTopicConfig {
    
    @Bean
    public NewTopic agentEventsTopic() {
        return TopicBuilder.name("agent-events")
            .partitions(10)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, 
                String.valueOf(Duration.ofDays(7).toMillis()))
            .build();
    }
    
    @Bean
    public NewTopic ledgerEventsTopic() {
        return TopicBuilder.name("ledger-events")
            .partitions(10)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, 
                String.valueOf(Duration.ofDays(7 * 365).toMillis())) // 7 years
            .build();
    }
}
```

#### 3. **Archival Strategy**

```java
@Service
public class EventArchivalService {
    private final S3Service s3Service;
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void archiveOldEvents() {
        // Archive events older than retention period
        List<String> topics = Arrays.asList(
            "agent-events",
            "conversation-events",
            "message-events"
        );
        
        for (String topic : topics) {
            archiveTopic(topic);
        }
    }
    
    private void archiveTopic(String topic) {
        // Read events from Kafka
        List<Event> events = readEventsOlderThan(topic, Duration.ofDays(7));
        
        // Archive to S3
        String s3Key = "archive/" + topic + "/" + LocalDate.now();
        s3Service.putObject(s3Key, serialize(events));
        
        // Delete from Kafka (handled by retention policy)
    }
}
```

---

## Summary

Event Design covers:

1. **Schema Design**: Immutability, completeness, versioning, clarity
2. **Versioning**: Semantic versioning with version numbers
3. **Schema Evolution**: Backward compatible changes, migration strategy
4. **Event Types**: Domain, system, and integration events
5. **Backward Compatibility**: Optional fields, no removals, type consistency
6. **Payload Size**: Keep events small, use references for large data
7. **Large Events**: Chunking and external storage strategies
8. **Retention**: Policy based on business and compliance requirements
