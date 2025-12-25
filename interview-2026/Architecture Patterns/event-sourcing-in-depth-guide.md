# Event Sourcing In-Depth Guide: Event Store, Replay & Snapshots

## Table of Contents
1. [Event Sourcing Overview](#event-sourcing-overview)
2. [Core Concepts](#core-concepts)
3. [Event Store](#event-store)
4. [Event Replay](#event-replay)
5. [Snapshots](#snapshots)
6. [Implementation Patterns](#implementation-patterns)
7. [Event Sourcing vs Traditional CRUD](#event-sourcing-vs-traditional-crud)
8. [Best Practices](#best-practices)
9. [Common Challenges](#common-challenges)
10. [Interview Questions & Answers](#interview-questions--answers)

---

## Event Sourcing Overview

### What is Event Sourcing?

**Event Sourcing** is:
- **Event-Driven Architecture**: Store events instead of current state
- **Immutable History**: Complete audit trail of all changes
- **Time Travel**: Reconstruct state at any point in time
- **Event Store**: Central repository for all events
- **Replay Capability**: Rebuild state by replaying events

### Key Principles

1. **Events as Source of Truth**: Events are the primary data
2. **Immutable Events**: Events cannot be changed or deleted
3. **Append-Only**: Only append new events, never modify
4. **State Reconstruction**: Current state derived from events
5. **Event Replay**: Rebuild state by replaying events

### Benefits

**Advantages:**
1. **Complete Audit Trail**: Every change is recorded
2. **Time Travel**: Query state at any point in time
3. **Debugging**: Understand what happened and why
4. **Scalability**: Append-only writes are fast
5. **Event Replay**: Rebuild state from scratch
6. **Temporal Queries**: "What was the state on date X?"

### Drawbacks

**Challenges:**
1. **Complexity**: More complex than CRUD
2. **Event Schema Evolution**: Handling schema changes
3. **Storage Growth**: Events accumulate over time
4. **Replay Performance**: Can be slow for large histories
5. **Eventual Consistency**: May require eventual consistency
6. **Learning Curve**: Team needs to understand the pattern

---

## Core Concepts

### Events

**Event Definition:**

```java
public interface DomainEvent {
    String getEventId();
    String getAggregateId();
    Instant getTimestamp();
    String getEventType();
}

public class OrderCreatedEvent implements DomainEvent {
    private String eventId;
    private String orderId;
    private Instant timestamp;
    private String customerId;
    private BigDecimal totalAmount;
    private List<OrderItem> items;
    
    // Getters and setters
}

public class OrderItemAddedEvent implements DomainEvent {
    private String eventId;
    private String orderId;
    private Instant timestamp;
    private String productId;
    private int quantity;
    private BigDecimal price;
    
    // Getters and setters
}

public class OrderShippedEvent implements DomainEvent {
    private String eventId;
    private String orderId;
    private Instant timestamp;
    private String trackingNumber;
    private Address shippingAddress;
    
    // Getters and setters
}
```

### Aggregates

**Aggregate Root:**

```java
public class Order {
    private String orderId;
    private String customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItem> items;
    private List<DomainEvent> uncommittedEvents;
    
    public Order(String orderId, String customerId, List<OrderItem> items) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.CREATED;
        this.totalAmount = calculateTotal(items);
        this.uncommittedEvents = new ArrayList<>();
        
        // Raise event
        raiseEvent(new OrderCreatedEvent(
            UUID.randomUUID().toString(),
            orderId,
            Instant.now(),
            customerId,
            totalAmount,
            items
        ));
    }
    
    public void addItem(String productId, int quantity, BigDecimal price) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot add items to " + status + " order");
        }
        
        OrderItem item = new OrderItem(productId, quantity, price);
        items.add(item);
        totalAmount = calculateTotal(items);
        
        // Raise event
        raiseEvent(new OrderItemAddedEvent(
            UUID.randomUUID().toString(),
            orderId,
            Instant.now(),
            productId,
            quantity,
            price
        ));
    }
    
    public void ship(String trackingNumber, Address address) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before shipping");
        }
        
        this.status = OrderStatus.SHIPPED;
        
        // Raise event
        raiseEvent(new OrderShippedEvent(
            UUID.randomUUID().toString(),
            orderId,
            Instant.now(),
            trackingNumber,
            address
        ));
    }
    
    // Reconstruct from events
    public static Order fromEvents(List<DomainEvent> events) {
        Order order = null;
        for (DomainEvent event : events) {
            order = applyEvent(order, event);
        }
        return order;
    }
    
    private static Order applyEvent(Order order, DomainEvent event) {
        if (event instanceof OrderCreatedEvent) {
            OrderCreatedEvent e = (OrderCreatedEvent) event;
            order = new Order(e.getOrderId(), e.getCustomerId(), e.getItems());
        } else if (event instanceof OrderItemAddedEvent) {
            OrderItemAddedEvent e = (OrderItemAddedEvent) event;
            order.addItem(e.getProductId(), e.getQuantity(), e.getPrice());
        } else if (event instanceof OrderShippedEvent) {
            OrderShippedEvent e = (OrderShippedEvent) event;
            order.ship(e.getTrackingNumber(), e.getShippingAddress());
        }
        return order;
    }
    
    private void raiseEvent(DomainEvent event) {
        uncommittedEvents.add(event);
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }
    
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

### Event Store

**Event Store Interface:**

```java
public interface EventStore {
    void appendEvents(String aggregateId, List<DomainEvent> events, long expectedVersion);
    List<DomainEvent> getEvents(String aggregateId);
    List<DomainEvent> getEvents(String aggregateId, long fromVersion);
    List<DomainEvent> getEventsByType(String eventType);
    List<DomainEvent> getEventsSince(Instant timestamp);
}
```

**Event Store Implementation:**

```java
@Repository
public class PostgresEventStore implements EventStore {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    public PostgresEventStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void appendEvents(String aggregateId, List<DomainEvent> events, long expectedVersion) {
        // Optimistic locking check
        Long currentVersion = jdbcTemplate.queryForObject(
            "SELECT MAX(version) FROM events WHERE aggregate_id = ?",
            Long.class,
            aggregateId
        );
        
        if (currentVersion != null && currentVersion != expectedVersion) {
            throw new ConcurrencyException("Expected version " + expectedVersion + 
                " but was " + currentVersion);
        }
        
        // Insert events
        for (int i = 0; i < events.size(); i++) {
            DomainEvent event = events.get(i);
            long version = (currentVersion == null ? 0 : currentVersion) + i + 1;
            
            jdbcTemplate.update(
                "INSERT INTO events (event_id, aggregate_id, event_type, event_data, version, timestamp) " +
                "VALUES (?, ?, ?, ?::jsonb, ?, ?)",
                event.getEventId(),
                aggregateId,
                event.getEventType(),
                objectMapper.writeValueAsString(event),
                version,
                event.getTimestamp()
            );
        }
    }
    
    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return jdbcTemplate.query(
            "SELECT event_data, event_type FROM events " +
            "WHERE aggregate_id = ? ORDER BY version ASC",
            new Object[]{aggregateId},
            (rs, rowNum) -> {
                String eventType = rs.getString("event_type");
                String eventData = rs.getString("event_data");
                return deserializeEvent(eventType, eventData);
            }
        );
    }
    
    @Override
    public List<DomainEvent> getEvents(String aggregateId, long fromVersion) {
        return jdbcTemplate.query(
            "SELECT event_data, event_type FROM events " +
            "WHERE aggregate_id = ? AND version >= ? ORDER BY version ASC",
            new Object[]{aggregateId, fromVersion},
            (rs, rowNum) -> {
                String eventType = rs.getString("event_type");
                String eventData = rs.getString("event_data");
                return deserializeEvent(eventType, eventData);
            }
        );
    }
    
    private DomainEvent deserializeEvent(String eventType, String eventData) {
        try {
            Class<?> eventClass = Class.forName(eventType);
            return (DomainEvent) objectMapper.readValue(eventData, eventClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }
}
```

**Event Store Schema:**

```sql
CREATE TABLE events (
    event_id VARCHAR(255) PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_data JSONB NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    metadata JSONB,
    UNIQUE(aggregate_id, version)
);

CREATE INDEX idx_events_aggregate_id ON events(aggregate_id);
CREATE INDEX idx_events_event_type ON events(event_type);
CREATE INDEX idx_events_timestamp ON events(timestamp);
CREATE INDEX idx_events_aggregate_version ON events(aggregate_id, version);
```

---

## Event Store

### Event Store Architecture

**Components:**

1. **Event Storage**: Persistent storage for events
2. **Event Stream**: Sequence of events for an aggregate
3. **Event Versioning**: Optimistic locking with version numbers
4. **Event Metadata**: Additional information about events
5. **Event Indexing**: Fast retrieval of events

### Event Storage Options

**Database (PostgreSQL):**

```java
// Advantages:
// - ACID transactions
// - Strong consistency
// - SQL queries
// - JSON support

// Schema
CREATE TABLE events (
    event_id UUID PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_data JSONB NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    metadata JSONB
);
```

**NoSQL (MongoDB):**

```java
// Advantages:
// - Flexible schema
// - Horizontal scaling
// - Document storage

// Document structure
{
    "_id": "event-id",
    "aggregateId": "order-123",
    "eventType": "OrderCreated",
    "eventData": { ... },
    "version": 1,
    "timestamp": "2024-01-01T00:00:00Z"
}
```

**Event Store (EventStore DB):**

```java
// Advantages:
// - Purpose-built for events
// - Stream support
// - Built-in projections
// - Optimized for event sourcing

// Usage
EventStoreConnection connection = EventStoreConnectionBuilder.create()
    .connectTo("localhost", 1113)
    .build();

EventData eventData = EventData.newBuilder()
    .eventId(UUID.randomUUID())
    .eventType("OrderCreated")
    .data(ByteBuffer.wrap(jsonBytes))
    .build();

connection.appendToStream("order-123", ExpectedVersion.any(), eventData);
```

**Message Queue (Kafka):**

```java
// Advantages:
// - Distributed
// - High throughput
// - Event streaming
// - Replay capability

// Usage
KafkaProducer<String, String> producer = new KafkaProducer<>(props);
ProducerRecord<String, String> record = new ProducerRecord<>(
    "events",
    aggregateId,
    jsonEvent
);
producer.send(record);
```

### Event Versioning

**Optimistic Locking:**

```java
public class EventStore {
    
    public void appendEvents(String aggregateId, List<DomainEvent> events, long expectedVersion) {
        // Check current version
        long currentVersion = getCurrentVersion(aggregateId);
        
        if (currentVersion != expectedVersion) {
            throw new ConcurrencyException(
                "Expected version " + expectedVersion + 
                " but was " + currentVersion
            );
        }
        
        // Append events with incremented versions
        for (int i = 0; i < events.size(); i++) {
            long version = currentVersion + i + 1;
            saveEvent(aggregateId, events.get(i), version);
        }
    }
    
    private long getCurrentVersion(String aggregateId) {
        return jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(version), 0) FROM events WHERE aggregate_id = ?",
            Long.class,
            aggregateId
        );
    }
}
```

### Event Metadata

**Metadata Structure:**

```java
public class EventMetadata {
    private String eventId;
    private String aggregateId;
    private String eventType;
    private long version;
    private Instant timestamp;
    private String userId;
    private String correlationId;
    private String causationId;
    private Map<String, String> customMetadata;
    
    // Getters and setters
}

// Store with event
{
    "eventId": "evt-123",
    "aggregateId": "order-456",
    "eventType": "OrderCreated",
    "eventData": { ... },
    "metadata": {
        "userId": "user-789",
        "correlationId": "req-abc",
        "causationId": "cmd-xyz",
        "ipAddress": "192.168.1.1"
    }
}
```

---

## Event Replay

### Replay Basics

**Reconstructing State:**

```java
public class OrderRepository {
    
    private final EventStore eventStore;
    
    public OrderRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    public Order findById(String orderId) {
        // Get all events for this aggregate
        List<DomainEvent> events = eventStore.getEvents(orderId);
        
        if (events.isEmpty()) {
            return null;
        }
        
        // Reconstruct aggregate from events
        return Order.fromEvents(events);
    }
    
    public void save(Order order) {
        // Get current version
        List<DomainEvent> existingEvents = eventStore.getEvents(order.getOrderId());
        long currentVersion = existingEvents.size();
        
        // Append new events
        List<DomainEvent> newEvents = order.getUncommittedEvents();
        eventStore.appendEvents(
            order.getOrderId(),
            newEvents,
            currentVersion
        );
        
        // Mark events as committed
        order.markEventsAsCommitted();
    }
}
```

### Replay Strategies

**Full Replay:**

```java
public class Order {
    
    public static Order fromEvents(List<DomainEvent> events) {
        Order order = null;
        
        // Replay all events from beginning
        for (DomainEvent event : events) {
            order = applyEvent(order, event);
        }
        
        return order;
    }
    
    private static Order applyEvent(Order order, DomainEvent event) {
        if (event instanceof OrderCreatedEvent) {
            OrderCreatedEvent e = (OrderCreatedEvent) event;
            order = new Order(e.getOrderId(), e.getCustomerId(), e.getItems());
        } else if (event instanceof OrderItemAddedEvent) {
            OrderItemAddedEvent e = (OrderItemAddedEvent) event;
            order.addItem(e.getProductId(), e.getQuantity(), e.getPrice());
        } else if (event instanceof OrderShippedEvent) {
            OrderShippedEvent e = (OrderShippedEvent) event;
            order.ship(e.getTrackingNumber(), e.getShippingAddress());
        }
        return order;
    }
}
```

**Incremental Replay:**

```java
public class Order {
    
    private long lastProcessedVersion;
    
    public void replayEvents(List<DomainEvent> events, long fromVersion) {
        // Only replay events after last processed version
        for (DomainEvent event : events) {
            if (event.getVersion() > lastProcessedVersion) {
                applyEvent(event);
                lastProcessedVersion = event.getVersion();
            }
        }
    }
}
```

### Replay Performance

**Optimization Strategies:**

1. **Snapshots**: Use snapshots to reduce replay time
2. **Parallel Replay**: Replay multiple aggregates in parallel
3. **Caching**: Cache reconstructed aggregates
4. **Lazy Loading**: Load events on demand
5. **Batch Processing**: Process events in batches

**Parallel Replay:**

```java
public class OrderRepository {
    
    public List<Order> findAll(List<String> orderIds) {
        return orderIds.parallelStream()
                .map(this::findById)
                .collect(Collectors.toList());
    }
}
```

---

## Snapshots

### What are Snapshots?

**Snapshots** are:
- **State Checkpoints**: Saved state at specific points
- **Performance Optimization**: Reduce replay time
- **Periodic Creation**: Created at intervals
- **Base for Replay**: Start replay from snapshot

### Snapshot Strategy

**When to Create Snapshots:**

1. **Event Count**: After N events
2. **Time Interval**: Every X hours/days
3. **Size Threshold**: When aggregate size exceeds limit
4. **On Demand**: Manual snapshot creation

**Snapshot Creation:**

```java
public class SnapshotService {
    
    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private static final int SNAPSHOT_INTERVAL = 100; // Every 100 events
    
    public void createSnapshotIfNeeded(String aggregateId) {
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        long eventCount = events.size();
        
        // Check if snapshot needed
        if (eventCount > 0 && eventCount % SNAPSHOT_INTERVAL == 0) {
            // Reconstruct aggregate
            Order order = Order.fromEvents(events);
            
            // Create snapshot
            Snapshot snapshot = new Snapshot(
                aggregateId,
                eventCount,
                serialize(order),
                Instant.now()
            );
            
            // Save snapshot
            snapshotStore.save(snapshot);
        }
    }
}
```

### Snapshot Store

**Snapshot Store Interface:**

```java
public interface SnapshotStore {
    void save(Snapshot snapshot);
    Snapshot getLatest(String aggregateId);
    void delete(String aggregateId, long version);
}
```

**Snapshot Store Implementation:**

```java
@Repository
public class PostgresSnapshotStore implements SnapshotStore {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public void save(Snapshot snapshot) {
        jdbcTemplate.update(
            "INSERT INTO snapshots (aggregate_id, version, snapshot_data, created_at) " +
            "VALUES (?, ?, ?::jsonb, ?) " +
            "ON CONFLICT (aggregate_id) DO UPDATE SET " +
            "version = EXCLUDED.version, " +
            "snapshot_data = EXCLUDED.snapshot_data, " +
            "created_at = EXCLUDED.created_at",
            snapshot.getAggregateId(),
            snapshot.getVersion(),
            objectMapper.writeValueAsString(snapshot.getData()),
            snapshot.getCreatedAt()
        );
    }
    
    @Override
    public Snapshot getLatest(String aggregateId) {
        return jdbcTemplate.queryForObject(
            "SELECT aggregate_id, version, snapshot_data, created_at " +
            "FROM snapshots WHERE aggregate_id = ?",
            new Object[]{aggregateId},
            (rs, rowNum) -> {
                return new Snapshot(
                    rs.getString("aggregate_id"),
                    rs.getLong("version"),
                    deserialize(rs.getString("snapshot_data")),
                    rs.getTimestamp("created_at").toInstant()
                );
            }
        );
    }
}
```

**Snapshot Schema:**

```sql
CREATE TABLE snapshots (
    aggregate_id VARCHAR(255) PRIMARY KEY,
    version BIGINT NOT NULL,
    snapshot_data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_snapshots_aggregate_version ON snapshots(aggregate_id, version);
```

### Replay with Snapshots

**Optimized Replay:**

```java
public class OrderRepository {
    
    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    
    public Order findById(String orderId) {
        // Try to get latest snapshot
        Snapshot snapshot = snapshotStore.getLatest(orderId);
        
        Order order;
        List<DomainEvent> eventsToReplay;
        
        if (snapshot != null) {
            // Deserialize snapshot
            order = deserialize(snapshot.getData());
            
            // Get events after snapshot
            eventsToReplay = eventStore.getEvents(orderId, snapshot.getVersion() + 1);
        } else {
            // No snapshot, start from beginning
            order = null;
            eventsToReplay = eventStore.getEvents(orderId);
        }
        
        // Replay remaining events
        for (DomainEvent event : eventsToReplay) {
            order = applyEvent(order, event);
        }
        
        return order;
    }
}
```

### Snapshot Cleanup

**Cleanup Strategy:**

```java
public class SnapshotCleanupService {
    
    private final SnapshotStore snapshotStore;
    private static final int KEEP_SNAPSHOTS = 10; // Keep last 10 snapshots
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupOldSnapshots() {
        // Get all aggregates with snapshots
        List<String> aggregateIds = snapshotStore.getAllAggregateIds();
        
        for (String aggregateId : aggregateIds) {
            // Get all snapshots for aggregate
            List<Snapshot> snapshots = snapshotStore.getAll(aggregateId);
            
            if (snapshots.size() > KEEP_SNAPSHOTS) {
                // Sort by version descending
                snapshots.sort((a, b) -> Long.compare(b.getVersion(), a.getVersion()));
                
                // Delete old snapshots
                for (int i = KEEP_SNAPSHOTS; i < snapshots.size(); i++) {
                    snapshotStore.delete(aggregateId, snapshots.get(i).getVersion());
                }
            }
        }
    }
}
```

---

## Implementation Patterns

### CQRS with Event Sourcing

**Command Side (Write):**

```java
@RestController
public class OrderCommandController {
    
    private final OrderRepository orderRepository;
    
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderCommand command) {
        Order order = new Order(
            UUID.randomUUID().toString(),
            command.getCustomerId(),
            command.getItems()
        );
        
        orderRepository.save(order);
        
        return ResponseEntity.ok(order);
    }
}
```

**Query Side (Read):**

```java
@RestController
public class OrderQueryController {
    
    private final OrderReadModelRepository readModelRepository;
    
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderView> getOrder(@PathVariable String id) {
        OrderView order = readModelRepository.findById(id);
        return ResponseEntity.ok(order);
    }
}
```

**Read Model Projection:**

```java
@Component
public class OrderProjection {
    
    private final EventStore eventStore;
    private final OrderReadModelRepository readModelRepository;
    
    @EventListener
    public void handle(OrderCreatedEvent event) {
        OrderView view = new OrderView();
        view.setOrderId(event.getOrderId());
        view.setCustomerId(event.getCustomerId());
        view.setTotalAmount(event.getTotalAmount());
        view.setStatus("CREATED");
        readModelRepository.save(view);
    }
    
    @EventListener
    public void handle(OrderShippedEvent event) {
        OrderView view = readModelRepository.findById(event.getOrderId());
        view.setStatus("SHIPPED");
        view.setTrackingNumber(event.getTrackingNumber());
        readModelRepository.save(view);
    }
}
```

### Event Handlers

**Domain Event Handlers:**

```java
@Component
public class OrderEventHandler {
    
    private final EmailService emailService;
    private final InventoryService inventoryService;
    
    @EventListener
    public void handle(OrderCreatedEvent event) {
        // Send confirmation email
        emailService.sendOrderConfirmation(event.getCustomerId(), event.getOrderId());
        
        // Update inventory
        for (OrderItem item : event.getItems()) {
            inventoryService.reserveItem(item.getProductId(), item.getQuantity());
        }
    }
    
    @EventListener
    public void handle(OrderShippedEvent event) {
        // Send shipping notification
        emailService.sendShippingNotification(
            event.getCustomerId(),
            event.getOrderId(),
            event.getTrackingNumber()
        );
    }
}
```

---

## Event Sourcing vs Traditional CRUD

### Comparison

| Aspect | Event Sourcing | Traditional CRUD |
|--------|---------------|------------------|
| Storage | Events | Current state |
| History | Complete | Lost |
| Audit Trail | Built-in | Manual |
| Time Travel | Yes | No |
| Replay | Yes | No |
| Complexity | High | Low |
| Performance | Replay overhead | Direct reads |
| Storage | Growing | Fixed |

### When to Use Event Sourcing

**Use Event Sourcing When:**
1. **Audit Requirements**: Need complete audit trail
2. **Temporal Queries**: Need to query past state
3. **Complex Business Logic**: Need to understand why
4. **Compliance**: Regulatory requirements
5. **Debugging**: Need to debug complex issues
6. **Event-Driven**: Event-driven architecture

**Don't Use Event Sourcing When:**
1. **Simple CRUD**: Simple create/read/update/delete
2. **No History Needed**: Don't need past state
3. **High Read Performance**: Need very fast reads
4. **Small Team**: Limited expertise
5. **Simple Domain**: Simple business logic

---

## Best Practices

### Event Design

1. **Immutable Events**: Events should be immutable
2. **Rich Events**: Include all necessary data
3. **Event Versioning**: Version events for schema evolution
4. **Event Naming**: Use clear, descriptive names
5. **Event Size**: Keep events reasonably sized

### Performance

1. **Snapshots**: Use snapshots to reduce replay time
2. **Caching**: Cache reconstructed aggregates
3. **Batch Processing**: Process events in batches
4. **Parallel Replay**: Replay multiple aggregates in parallel
5. **Event Archiving**: Archive old events

### Schema Evolution

**Event Versioning:**

```java
public class OrderCreatedEventV1 implements DomainEvent {
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
}

public class OrderCreatedEventV2 implements DomainEvent {
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private String currency; // New field
    private Map<String, String> metadata; // New field
}

// Migration
public class EventMigrator {
    public DomainEvent migrate(DomainEvent event) {
        if (event instanceof OrderCreatedEventV1) {
            OrderCreatedEventV1 v1 = (OrderCreatedEventV1) event;
            return new OrderCreatedEventV2(
                v1.getOrderId(),
                v1.getCustomerId(),
                v1.getTotalAmount(),
                "USD", // Default currency
                Collections.emptyMap() // Default metadata
            );
        }
        return event;
    }
}
```

---

## Common Challenges

### Challenge 1: Event Schema Evolution

**Problem:** Events change over time, need to handle old events.

**Solution:**
- Version events
- Migrate events during replay
- Support multiple versions

### Challenge 2: Replay Performance

**Problem:** Replaying many events is slow.

**Solution:**
- Use snapshots
- Cache reconstructed aggregates
- Parallel replay
- Incremental replay

### Challenge 3: Eventual Consistency

**Problem:** Read models may be stale.

**Solution:**
- Accept eventual consistency
- Use read models for queries
- Event handlers update read models
- Clear consistency boundaries

### Challenge 4: Storage Growth

**Problem:** Events accumulate over time.

**Solution:**
- Archive old events
- Compress events
- Delete old events (if allowed)
- Use time-based partitioning

---

## Interview Questions & Answers

### Q1: What is Event Sourcing?

**Answer:**
- **Event Sourcing**: Store events instead of current state
- **Immutable History**: Complete audit trail of all changes
- **Time Travel**: Reconstruct state at any point in time
- **Event Store**: Central repository for all events
- **Replay Capability**: Rebuild state by replaying events

### Q2: What is an Event Store?

**Answer:**
- **Event Store**: Central repository for all events
- **Append-Only**: Only append new events, never modify
- **Event Stream**: Sequence of events for an aggregate
- **Versioning**: Optimistic locking with version numbers
- **Storage**: Can use database, NoSQL, or specialized event store

### Q3: What is Event Replay?

**Answer:**
- **Event Replay**: Reconstructing state by replaying events
- **Full Replay**: Replay all events from beginning
- **Incremental Replay**: Replay events after snapshot
- **Performance**: Can be slow for large histories
- **Optimization**: Use snapshots to reduce replay time

### Q4: What are Snapshots?

**Answer:**
- **Snapshots**: Saved state at specific points
- **Performance Optimization**: Reduce replay time
- **Periodic Creation**: Created at intervals (e.g., every 100 events)
- **Base for Replay**: Start replay from snapshot
- **Storage**: Store snapshots separately from events

### Q5: How do you handle event schema evolution?

**Answer:**
- **Version Events**: Version events for schema changes
- **Migration**: Migrate events during replay
- **Multiple Versions**: Support multiple event versions
- **Backward Compatibility**: Maintain backward compatibility
- **Event Migrators**: Use migrators to transform events

### Q6: What is the difference between Event Sourcing and CQRS?

**Answer:**
- **Event Sourcing**: Store events instead of current state
- **CQRS**: Separate read and write models
- **Combination**: Often used together
- **Event Sourcing**: Focus on storage
- **CQRS**: Focus on separation of concerns

### Q7: How do you ensure consistency in Event Sourcing?

**Answer:**
- **Optimistic Locking**: Use version numbers
- **Eventual Consistency**: Accept eventual consistency for read models
- **Strong Consistency**: Use transactions for event store
- **Read Models**: Update read models asynchronously
- **Consistency Boundaries**: Define clear boundaries

### Q8: What are the benefits of Event Sourcing?

**Answer:**
1. **Complete Audit Trail**: Every change is recorded
2. **Time Travel**: Query state at any point in time
3. **Debugging**: Understand what happened and why
4. **Scalability**: Append-only writes are fast
5. **Event Replay**: Rebuild state from scratch
6. **Temporal Queries**: "What was the state on date X?"

### Q9: What are the drawbacks of Event Sourcing?

**Answer:**
1. **Complexity**: More complex than CRUD
2. **Event Schema Evolution**: Handling schema changes
3. **Storage Growth**: Events accumulate over time
4. **Replay Performance**: Can be slow for large histories
5. **Eventual Consistency**: May require eventual consistency
6. **Learning Curve**: Team needs to understand the pattern

### Q10: When should you use Event Sourcing?

**Answer:**
- **Audit Requirements**: Need complete audit trail
- **Temporal Queries**: Need to query past state
- **Complex Business Logic**: Need to understand why
- **Compliance**: Regulatory requirements
- **Debugging**: Need to debug complex issues
- **Event-Driven**: Event-driven architecture

---

## Summary

**Key Takeaways:**
1. **Event Sourcing**: Store events instead of current state
2. **Event Store**: Central repository for all events
3. **Event Replay**: Reconstruct state by replaying events
4. **Snapshots**: Optimize replay performance
5. **CQRS**: Often combined with Event Sourcing
6. **Schema Evolution**: Handle event versioning

**Complete Coverage:**
- Event Sourcing overview and principles
- Core concepts (events, aggregates, event store)
- Event store implementation
- Event replay strategies
- Snapshot implementation
- Implementation patterns
- Best practices and common challenges
- Interview Q&A

---

**Guide Complete** - Ready for interview preparation!

