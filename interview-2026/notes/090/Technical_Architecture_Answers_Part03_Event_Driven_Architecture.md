# Technical Architecture Answers - Part 3: Event-Driven Architecture

## Question 11: You've extensively used event-driven architecture. What are the benefits?

### Answer

### Event-Driven Architecture Benefits

#### 1. **Key Benefits**

```
┌─────────────────────────────────────────────────────────┐
│         Event-Driven Architecture Benefits             │
└─────────────────────────────────────────────────────────┘

1. Loose Coupling:
├─ Services don't know about each other
├─ Communicate through events
├─ Independent evolution
└─ Easy to add/remove services

2. Scalability:
├─ Parallel event processing
├─ Independent scaling
├─ High throughput
└─ Handle spikes

3. Resilience:
├─ Failure isolation
├─ Event replay capability
├─ Eventual consistency
└─ No cascading failures

4. Audit Trail:
├─ Complete event history
├─ Event sourcing support
├─ Compliance
└─ Debugging capability
```

#### 2. **Loose Coupling**

```java
// Loose coupling through events
// Service A doesn't know about Service B
@Service
public class OrderService {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public Order createOrder(OrderRequest request) {
        Order order = createOrder(request);
        
        // Publish event - doesn't know who consumes it
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        kafkaTemplate.send("order-events", order.getId(), event);
        
        return order;
    }
}

// Service B subscribes to events
@KafkaListener(topics = "order-events", groupId = "inventory-service")
public void handleOrderCreated(OrderCreatedEvent event) {
    // React to event
    inventoryService.reserveInventory(event.getOrderId(), event.getItems());
}
```

**Benefits:**
- Services can evolve independently
- Easy to add new consumers
- No direct dependencies
- Technology diversity

#### 3. **Scalability**

```
┌─────────────────────────────────────────────────────────┐
│         Scalability Benefits                           │
└─────────────────────────────────────────────────────────┘

Parallel Processing:
├─ Multiple consumers process events
├─ Consumer groups for load distribution
├─ Partition-based parallelism
└─ High throughput

Independent Scaling:
├─ Scale consumers independently
├─ Scale producers independently
├─ Scale event bus independently
└─ No coordination needed
```

**Implementation:**
- Kafka consumer groups enable parallel processing
- Each partition processed by one consumer
- Add more consumers to increase throughput
- Scale based on event volume

#### 4. **Resilience**

```java
// Resilience through event replay
@Service
public class ResilientEventProcessor {
    private final KafkaConsumer<String, Event> consumer;
    
    public void processEvents() {
        while (true) {
            ConsumerRecords<String, Event> records = consumer.poll(Duration.ofMillis(100));
            
            for (ConsumerRecord<String, Event> record : records) {
                try {
                    processEvent(record.value());
                    // Commit offset only after successful processing
                    consumer.commitSync();
                } catch (Exception e) {
                    // Event processing failed
                    // Don't commit offset - will retry
                    log.error("Event processing failed", e);
                    // Can also send to dead letter queue
                }
            }
        }
    }
}
```

**Benefits:**
- Events can be replayed on failure
- No data loss
- Automatic retry capability
- Dead letter queue for failed events

---

## Question 12: How do you design event schemas for event-driven systems?

### Answer

### Event Schema Design

#### 1. **Event Schema Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Event Schema Principles                        │
└─────────────────────────────────────────────────────────┘

1. Immutability:
├─ Events are immutable
├─ Never modify existing events
└─ Create new events for changes

2. Versioning:
├─ Version event schemas
├─ Backward compatibility
└─ Migration strategy

3. Completeness:
├─ Include all necessary data
├─ Include metadata
└─ Include context

4. Clarity:
├─ Clear naming
├─ Well-documented
└─ Self-describing
```

#### 2. **Event Schema Structure**

```java
// Event schema design
public class OrderCreatedEvent {
    // Event metadata
    private String eventId;
    private String eventType;
    private Long eventVersion;
    private Instant timestamp;
    private String source;
    
    // Event data
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String currency;
    
    // Context
    private String correlationId;
    private String causationId;
    private Map<String, String> metadata;
    
    // Getters and setters
}
```

#### 3. **Event Versioning**

```java
// Event versioning strategy
public class OrderCreatedEventV1 {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    // V1 fields
}

public class OrderCreatedEventV2 {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private String shippingAddress; // New field in V2
    // V2 fields
}

// Event converter
@Service
public class EventConverter {
    public OrderCreatedEventV2 convertV1ToV2(OrderCreatedEventV1 v1) {
        OrderCreatedEventV2 v2 = new OrderCreatedEventV2();
        v2.setOrderId(v1.getOrderId());
        v2.setCustomerId(v1.getCustomerId());
        v2.setItems(v1.getItems());
        v2.setShippingAddress(null); // Default for V1 events
        return v2;
    }
}
```

#### 4. **Schema Registry**

```java
// Using Schema Registry (Confluent)
@Configuration
public class SchemaRegistryConfig {
    @Bean
    public KafkaAvroSerializer kafkaAvroSerializer() {
        Map<String, Object> props = new HashMap<>();
        props.put("schema.registry.url", "http://schema-registry:8081");
        return new KafkaAvroSerializer();
    }
}

// Avro schema definition
@AvroSchema("""
    {
      "type": "record",
      "name": "OrderCreatedEvent",
      "fields": [
        {"name": "orderId", "type": "string"},
        {"name": "customerId", "type": "string"},
        {"name": "items", "type": {"type": "array", "items": "OrderItem"}},
        {"name": "totalAmount", "type": "double"},
        {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"}
      ]
    }
    """)
public class OrderCreatedEvent {
    // Avro-generated class
}
```

#### 5. **Event Naming Conventions**

```java
// Event naming conventions
public class EventNamingConventions {
    // Pattern: {Entity}{Action}Event
    // Examples:
    // - OrderCreatedEvent
    // - OrderCancelledEvent
    // - PaymentProcessedEvent
    // - InventoryReservedEvent
    
    // Event types
    public enum EventType {
        CREATED,    // Entity created
        UPDATED,    // Entity updated
        DELETED,    // Entity deleted
        PROCESSED,  // Process completed
        FAILED,     // Process failed
        CANCELLED   // Process cancelled
    }
}
```

---

## Question 13: You've used Kafka extensively. How do you ensure event ordering?

### Answer

### Event Ordering in Kafka

#### 1. **Kafka Ordering Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Ordering Guarantees                     │
└─────────────────────────────────────────────────────────┘

Ordering Guarantees:
├─ Ordering per partition
├─ No ordering across partitions
└─ Partition key determines ordering

Strategy:
├─ Use partition key for ordering
├─ Same key → same partition → ordered
└─ Different keys → different partitions → unordered
```

#### 2. **Partition Key Strategy**

```java
// Partition key for ordering
@Service
public class KafkaEventPublisher {
    private final KafkaTemplate<String, Event> kafkaTemplate;
    
    public void publishOrderEvent(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        
        // Partition key: orderId
        // All events for same order go to same partition
        // Ensures ordering per order
        kafkaTemplate.send("order-events", order.getId(), event);
    }
    
    public void publishConversationEvent(Conversation conversation) {
        ConversationCreatedEvent event = new ConversationCreatedEvent(conversation);
        
        // Partition key: conversationId
        // All events for same conversation go to same partition
        // Ensures ordering per conversation
        kafkaTemplate.send("conversation-events", conversation.getId(), event);
    }
}
```

#### 3. **Ordering Requirements**

```java
// Different ordering requirements
public class OrderingRequirements {
    // Requirement 1: Order events per order
    // Solution: Partition by orderId
    public void publishOrderEvents(String orderId) {
        kafkaTemplate.send("order-events", orderId, new OrderCreatedEvent());
        kafkaTemplate.send("order-events", orderId, new OrderUpdatedEvent());
        // Both events go to same partition → ordered
    }
    
    // Requirement 2: Conversation events per conversation
    // Solution: Partition by conversationId
    public void publishConversationEvents(String conversationId) {
        kafkaTemplate.send("conversation-events", conversationId, 
            new ConversationCreatedEvent());
        kafkaTemplate.send("conversation-events", conversationId,
            new MessageReceivedEvent());
        // Both events go to same partition → ordered
    }
    
    // Requirement 3: Global ordering (all events)
    // Solution: Single partition (not recommended for scale)
    public void publishGlobalEvents(Event event) {
        kafkaTemplate.send("global-events", "global-key", event);
        // Single partition → global ordering (but limited throughput)
    }
}
```

#### 4. **Consumer Ordering**

```java
// Consumer ordering
@KafkaListener(topics = "order-events", groupId = "order-processor")
public void processOrderEvents(ConsumerRecord<String, OrderEvent> record) {
    // Process events in order (per partition)
    // Single consumer per partition ensures ordering
    processEvent(record.value());
}

// Multiple consumers for parallel processing
// Each consumer handles one partition
// Ordering maintained per partition
```

#### 5. **Sequence Numbers**

```java
// Sequence numbers for ordering validation
public class OrderEvent {
    private String orderId;
    private Long sequenceNumber; // Sequence per order
    private EventType eventType;
    private Instant timestamp;
}

@Service
public class OrderEventProcessor {
    private final Map<String, Long> lastSequence = new ConcurrentHashMap<>();
    
    public void processEvent(OrderEvent event) {
        String orderId = event.getOrderId();
        Long currentSequence = event.getSequenceNumber();
        Long lastSequence = this.lastSequence.get(orderId);
        
        // Validate sequence
        if (lastSequence != null && currentSequence <= lastSequence) {
            // Out of order event
            log.warn("Out of order event: orderId={}, sequence={}, lastSequence={}",
                orderId, currentSequence, lastSequence);
            // Handle out-of-order event
            handleOutOfOrderEvent(event, lastSequence);
            return;
        }
        
        // Process event
        processEvent(event);
        
        // Update last sequence
        this.lastSequence.put(orderId, currentSequence);
    }
}
```

---

## Question 14: How do you handle event schema evolution?

### Answer

### Event Schema Evolution

#### 1. **Schema Evolution Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Evolution Strategies                    │
└─────────────────────────────────────────────────────────┘

1. Backward Compatibility:
├─ New fields optional
├─ Don't remove fields
├─ Don't change field types
└─ Use default values

2. Forward Compatibility:
├─ Consumers ignore unknown fields
├─ Consumers handle missing fields
└─ Use optional fields

3. Versioning:
├─ Version event schemas
├─ Multiple versions coexist
└─ Migration strategy
```

#### 2. **Backward Compatible Changes**

```java
// Backward compatible schema evolution
// V1: Original schema
public class OrderCreatedEventV1 {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
}

// V2: Add new optional field (backward compatible)
public class OrderCreatedEventV2 {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String shippingAddress; // New optional field
}

// V1 consumers can still read V2 events (ignore new field)
// V2 consumers can read V1 events (use default for missing field)
```

#### 3. **Schema Registry Evolution**

```java
// Using Schema Registry for evolution
@Service
public class SchemaEvolutionService {
    private final SchemaRegistryClient schemaRegistry;
    
    public void evolveSchema(String topic, Schema newSchema) {
        // Register new schema version
        SchemaMetadata metadata = schemaRegistry.register(topic, newSchema);
        
        // Schema Registry ensures compatibility
        // - Backward compatibility: New schema can read old data
        // - Forward compatibility: Old schema can read new data
        // - Full compatibility: Both directions
    }
}

// Avro schema evolution rules
@AvroSchema("""
    {
      "type": "record",
      "name": "OrderCreatedEvent",
      "fields": [
        {"name": "orderId", "type": "string"},
        {"name": "customerId", "type": "string"},
        {"name": "items", "type": {"type": "array", "items": "OrderItem"}},
        {"name": "totalAmount", "type": "double"},
        {"name": "shippingAddress", "type": ["null", "string"], "default": null}
      ]
    }
    """)
public class OrderCreatedEvent {
    // New field with default (backward compatible)
}
```

#### 4. **Event Transformation**

```java
// Event transformation for schema evolution
@Service
public class EventTransformer {
    public OrderCreatedEventV2 transformV1ToV2(OrderCreatedEventV1 v1) {
        OrderCreatedEventV2 v2 = new OrderCreatedEventV2();
        v2.setOrderId(v1.getOrderId());
        v2.setCustomerId(v1.getCustomerId());
        v2.setItems(v1.getItems());
        v2.setTotalAmount(v1.getTotalAmount());
        v2.setShippingAddress(null); // Default for V1 events
        return v2;
    }
    
    public OrderCreatedEventV1 transformV2ToV1(OrderCreatedEventV2 v2) {
        OrderCreatedEventV1 v1 = new OrderCreatedEventV1();
        v1.setOrderId(v2.getOrderId());
        v1.setCustomerId(v2.getCustomerId());
        v1.setItems(v2.getItems());
        v1.setTotalAmount(v2.getTotalAmount());
        // Ignore shippingAddress (V1 doesn't have it)
        return v1;
    }
}
```

#### 5. **Migration Strategy**

```java
// Gradual migration strategy
@Service
public class EventMigrationService {
    public void migrateEvents(String topic) {
        // Phase 1: Publish both V1 and V2 events
        publishBothVersions(topic);
        
        // Phase 2: Migrate consumers to V2
        migrateConsumersToV2();
        
        // Phase 3: Stop publishing V1 events
        stopPublishingV1(topic);
        
        // Phase 4: Transform old V1 events to V2
        transformOldEvents(topic);
    }
    
    private void publishBothVersions(String topic) {
        // Publish both versions during migration
        OrderCreatedEventV1 v1 = createV1Event();
        OrderCreatedEventV2 v2 = createV2Event();
        
        kafkaTemplate.send(topic + "-v1", v1);
        kafkaTemplate.send(topic + "-v2", v2);
    }
}
```

---

## Question 15: What's your approach to event sourcing?

### Answer

### Event Sourcing Approach

#### 1. **Event Sourcing Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Principles                      │
└─────────────────────────────────────────────────────────┘

Core Concept:
├─ Store all changes as events
├─ State = Sum of events
├─ Rebuild state by replaying events
└─ Events are source of truth

Benefits:
├─ Complete audit trail
├─ Time travel (state at any point)
├─ Event replay capability
└─ Debugging and analysis
```

#### 2. **Event Sourcing Implementation**

```java
// Event sourcing implementation
@Entity
public class Order {
    @Id
    private String orderId;
    private String customerId;
    private OrderStatus status;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    
    // Apply event to rebuild state
    public void apply(OrderEvent event) {
        if (event instanceof OrderCreatedEvent) {
            apply((OrderCreatedEvent) event);
        } else if (event instanceof OrderUpdatedEvent) {
            apply((OrderUpdatedEvent) event);
        } else if (event instanceof OrderCancelledEvent) {
            apply((OrderCancelledEvent) event);
        }
    }
    
    private void apply(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        this.customerId = event.getCustomerId();
        this.items = event.getItems();
        this.totalAmount = event.getTotalAmount();
        this.status = OrderStatus.CREATED;
    }
    
    private void apply(OrderUpdatedEvent event) {
        this.items = event.getItems();
        this.totalAmount = event.getTotalAmount();
        this.status = OrderStatus.UPDATED;
    }
    
    private void apply(OrderCancelledEvent event) {
        this.status = OrderStatus.CANCELLED;
    }
}

// Event store
@Service
public class EventStore {
    private final EventRepository eventRepository;
    
    public void append(String aggregateId, Event event) {
        EventRecord record = new EventRecord(
            aggregateId,
            event.getClass().getName(),
            serialize(event),
            Instant.now()
        );
        eventRepository.save(record);
    }
    
    public List<Event> getEvents(String aggregateId) {
        List<EventRecord> records = eventRepository.findByAggregateIdOrderBySequence(
            aggregateId);
        return records.stream()
            .map(this::deserialize)
            .collect(Collectors.toList());
    }
    
    public Order rebuildOrder(String orderId) {
        List<Event> events = getEvents(orderId);
        Order order = new Order();
        for (Event event : events) {
            order.apply(event);
        }
        return order;
    }
}
```

#### 3. **Snapshots**

```java
// Snapshots for performance
@Service
public class SnapshotService {
    private final EventStore eventStore;
    private final SnapshotRepository snapshotRepository;
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void createSnapshots() {
        List<String> orderIds = getAllOrderIds();
        
        for (String orderId : orderIds) {
            // Rebuild current state
            Order order = eventStore.rebuildOrder(orderId);
            
            // Create snapshot
            Snapshot snapshot = new Snapshot(
                orderId,
                order,
                eventStore.getLastSequence(orderId),
                Instant.now()
            );
            
            snapshotRepository.save(snapshot);
        }
    }
    
    public Order getOrder(String orderId) {
        // Try snapshot first
        Snapshot snapshot = snapshotRepository.findLatestByOrderId(orderId);
        
        if (snapshot != null) {
            // Replay events after snapshot
            List<Event> events = eventStore.getEventsAfter(
                orderId, snapshot.getLastSequence());
            
            Order order = snapshot.getOrder();
            for (Event event : events) {
                order.apply(event);
            }
            return order;
        } else {
            // No snapshot, rebuild from all events
            return eventStore.rebuildOrder(orderId);
        }
    }
}
```

#### 4. **Event Sourcing Benefits**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Benefits                        │
└─────────────────────────────────────────────────────────┘

Audit Trail:
├─ Complete history of changes
├─ Who, what, when
└─ Compliance requirements

Time Travel:
├─ Rebuild state at any point
├─ Historical analysis
└─ Debugging

Event Replay:
├─ Rebuild state from events
├─ Disaster recovery
└─ Testing
```

---

## Summary

Part 3 covers:
1. **Event-Driven Architecture Benefits**: Loose coupling, scalability, resilience, audit trail
2. **Event Schema Design**: Principles, structure, versioning, naming
3. **Event Ordering in Kafka**: Partition keys, ordering guarantees, sequence numbers
4. **Schema Evolution**: Backward compatibility, Schema Registry, migration
5. **Event Sourcing**: Principles, implementation, snapshots, benefits

Key takeaways:
- Event-driven architecture enables loose coupling and scalability
- Design event schemas with versioning and backward compatibility
- Use partition keys to ensure event ordering
- Plan for schema evolution from the start
- Event sourcing provides complete audit trail and time travel
