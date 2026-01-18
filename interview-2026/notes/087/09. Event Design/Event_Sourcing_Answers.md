# Event Sourcing - Detailed Answers

## Question 164: Explain the event sourcing pattern. Why did you use it?

### Answer

### Event Sourcing Pattern

#### 1. **What is Event Sourcing?**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Pattern                         │
└─────────────────────────────────────────────────────────┘

Traditional Approach:
├─ Store current state
├─ Update state directly
└─ Lose history

Event Sourcing:
├─ Store all events (immutable log)
├─ State = Replay of events
├─ Complete history preserved
└─ Audit trail built-in
```

#### 2. **Event Sourcing Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Architecture                    │
└─────────────────────────────────────────────────────────┘

Event Store (Kafka):
├─ All events stored
├─ Immutable log
├─ Append-only
└─ Complete history

State Reconstruction:
├─ Load all events
├─ Replay in order
├─ Rebuild current state
└─ Or rebuild state at any point in time

Current State (Redis/Database):
├─ Cached state
├─ Fast access
├─ Rebuilt from events if needed
└─ Snapshot for performance
```

#### 3. **Why Event Sourcing?**

**Audit Trail:**
```
Financial System Requirements:
├─ Complete audit trail
├─ Regulatory compliance
├─ Traceability
└─ Event sourcing provides this automatically
```

**State Recovery:**
```
Disaster Recovery:
├─ Rebuild state from events
├─ Point-in-time recovery
├─ Debugging capability
└─ Event sourcing enables this
```

**Temporal Queries:**
```
Historical Analysis:
├─ "What was the position at time T?"
├─ "How did the state change over time?"
├─ "What events led to this state?"
└─ Event sourcing enables temporal queries
```

**Event Replay:**
```
Testing and Debugging:
├─ Replay events to reproduce bugs
├─ Test with historical data
├─ Debug state transitions
└─ Event sourcing enables replay
```

#### 4. **Event Sourcing Implementation**

```java
// Event Store Interface
public interface EventStore {
    void appendEvent(String aggregateId, DomainEvent event);
    List<DomainEvent> getEvents(String aggregateId);
    List<DomainEvent> getEvents(String aggregateId, Instant from, Instant to);
}

// Domain Event
public abstract class DomainEvent {
    private String eventId;
    private String aggregateId;
    private Instant timestamp;
    private Long version;
    
    // Getters and setters
}

// Aggregate Root
public class Position {
    private String accountId;
    private String instrumentId;
    private BigDecimal balance;
    private Long version;
    private List<DomainEvent> uncommittedEvents = new ArrayList<>();
    
    // Apply event to state
    public void apply(DomainEvent event) {
        if (event instanceof TradeExecutedEvent) {
            TradeExecutedEvent tradeEvent = (TradeExecutedEvent) event;
            this.balance = this.balance.add(tradeEvent.getQuantity());
        }
        this.version++;
    }
    
    // Rebuild from events
    public static Position fromEvents(List<DomainEvent> events) {
        Position position = new Position();
        for (DomainEvent event : events) {
            position.apply(event);
        }
        return position;
    }
}
```

---

## Question 165: How do you rebuild state from events?

### Answer

### State Reconstruction

#### 1. **Reconstruction Process**

```
┌─────────────────────────────────────────────────────────┐
│         State Reconstruction Flow                     │
└─────────────────────────────────────────────────────────┘

1. Load Events
   ├─ Query event store
   ├─ Filter by aggregate ID
   └─ Order by version/timestamp

2. Initialize State
   ├─ Create empty aggregate
   └─ Set initial values

3. Replay Events
   ├─ For each event in order:
   │  ├─ Apply event to state
   │  └─ Update version
   └─ Continue until all events processed

4. Current State
   └─ Final state after all events
```

#### 2. **Implementation**

```java
@Service
public class PositionService {
    private final EventStore eventStore;
    private final PositionRepository positionRepository;
    
    public Position getCurrentPosition(String accountId, String instrumentId) {
        String aggregateId = accountId + ":" + instrumentId;
        
        // Try cache first
        Position cached = getCachedPosition(aggregateId);
        if (cached != null) {
            return cached;
        }
        
        // Rebuild from events
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        Position position = Position.fromEvents(events);
        
        // Cache for future access
        cachePosition(aggregateId, position);
        
        return position;
    }
    
    public Position getPositionAtTime(String accountId, String instrumentId, Instant atTime) {
        String aggregateId = accountId + ":" + instrumentId;
        
        // Get events up to specified time
        List<DomainEvent> events = eventStore.getEvents(aggregateId, 
            Instant.EPOCH, atTime);
        
        // Rebuild state at that time
        return Position.fromEvents(events);
    }
}
```

#### 3. **Event Application**

```java
public class Position {
    private String accountId;
    private String instrumentId;
    private BigDecimal balance;
    private Long version;
    
    // Apply event to rebuild state
    public void apply(DomainEvent event) {
        if (event instanceof TradeExecutedEvent) {
            apply((TradeExecutedEvent) event);
        } else if (event instanceof PositionAdjustedEvent) {
            apply((PositionAdjustedEvent) event);
        } else if (event instanceof PositionResetEvent) {
            apply((PositionResetEvent) event);
        }
        this.version = event.getVersion();
    }
    
    private void apply(TradeExecutedEvent event) {
        if (event.getTradeType() == TradeType.BUY) {
            this.balance = this.balance.add(event.getQuantity());
        } else {
            this.balance = this.balance.subtract(event.getQuantity());
        }
    }
    
    private void apply(PositionAdjustedEvent event) {
        this.balance = event.getNewBalance();
    }
    
    private void apply(PositionResetEvent event) {
        this.balance = BigDecimal.ZERO;
    }
    
    // Rebuild from events
    public static Position fromEvents(List<DomainEvent> events) {
        Position position = new Position();
        for (DomainEvent event : events) {
            position.apply(event);
        }
        return position;
    }
}
```

#### 4. **Performance Optimization**

```java
@Service
public class OptimizedPositionService {
    private final EventStore eventStore;
    private final SnapshotRepository snapshotRepository;
    
    public Position getCurrentPosition(String accountId, String instrumentId) {
        String aggregateId = accountId + ":" + instrumentId;
        
        // Try snapshot first
        PositionSnapshot snapshot = snapshotRepository
            .findLatest(aggregateId);
        
        if (snapshot != null) {
            // Replay events after snapshot
            List<DomainEvent> events = eventStore.getEvents(
                aggregateId, 
                snapshot.getTimestamp(), 
                Instant.now()
            );
            
            Position position = snapshot.getPosition();
            for (DomainEvent event : events) {
                position.apply(event);
            }
            
            return position;
        }
        
        // No snapshot, rebuild from all events
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        return Position.fromEvents(events);
    }
}
```

---

## Question 166: What's the snapshot strategy?

### Answer

### Snapshot Strategy

#### 1. **Why Snapshots?**

```
┌─────────────────────────────────────────────────────────┐
│         Snapshot Benefits                              │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Long event history
├─ Slow state reconstruction
├─ High memory usage
└─ Poor performance

Solution: Snapshots
├─ Periodic state snapshots
├─ Replay only recent events
├─ Faster reconstruction
└─ Better performance
```

#### 2. **Snapshot Strategy**

```java
@Entity
public class PositionSnapshot {
    @Id
    private String id;
    private String aggregateId; // accountId:instrumentId
    private Position position; // Serialized state
    private Instant timestamp;
    private Long version; // Event version at snapshot
}

@Service
public class SnapshotService {
    private final SnapshotRepository snapshotRepository;
    private final EventStore eventStore;
    
    /**
     * Create snapshot periodically
     * Strategy: Every N events or every T time
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void createSnapshots() {
        List<String> aggregateIds = getAllAggregateIds();
        
        for (String aggregateId : aggregateIds) {
            // Check if snapshot needed
            if (shouldCreateSnapshot(aggregateId)) {
                createSnapshot(aggregateId);
            }
        }
    }
    
    private boolean shouldCreateSnapshot(String aggregateId) {
        // Get last snapshot
        PositionSnapshot lastSnapshot = snapshotRepository
            .findLatest(aggregateId);
        
        if (lastSnapshot == null) {
            return true; // No snapshot exists
        }
        
        // Get events since last snapshot
        List<DomainEvent> events = eventStore.getEvents(
            aggregateId,
            lastSnapshot.getTimestamp(),
            Instant.now()
        );
        
        // Create snapshot if more than 1000 events
        return events.size() > 1000;
    }
    
    private void createSnapshot(String aggregateId) {
        // Rebuild current state
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        Position position = Position.fromEvents(events);
        
        // Create snapshot
        PositionSnapshot snapshot = new PositionSnapshot();
        snapshot.setId(UUID.randomUUID().toString());
        snapshot.setAggregateId(aggregateId);
        snapshot.setPosition(position);
        snapshot.setTimestamp(Instant.now());
        snapshot.setVersion((long) events.size());
        
        snapshotRepository.save(snapshot);
        
        // Cleanup old snapshots (keep last 10)
        cleanupOldSnapshots(aggregateId);
    }
}
```

#### 3. **Snapshot-Based Reconstruction**

```java
@Service
public class SnapshotBasedPositionService {
    public Position getCurrentPosition(String accountId, String instrumentId) {
        String aggregateId = accountId + ":" + instrumentId;
        
        // Get latest snapshot
        PositionSnapshot snapshot = snapshotRepository.findLatest(aggregateId);
        
        if (snapshot == null) {
            // No snapshot, rebuild from all events
            return rebuildFromAllEvents(aggregateId);
        }
        
        // Start from snapshot
        Position position = snapshot.getPosition();
        
        // Replay events after snapshot
        List<DomainEvent> events = eventStore.getEvents(
            aggregateId,
            snapshot.getTimestamp(),
            Instant.now()
        );
        
        for (DomainEvent event : events) {
            position.apply(event);
        }
        
        return position;
    }
    
    public Position getPositionAtTime(String accountId, String instrumentId, Instant atTime) {
        String aggregateId = accountId + ":" + instrumentId;
        
        // Find snapshot before target time
        PositionSnapshot snapshot = snapshotRepository
            .findLatestBefore(aggregateId, atTime);
        
        Position position;
        Instant fromTime;
        
        if (snapshot != null && snapshot.getTimestamp().isBefore(atTime)) {
            // Start from snapshot
            position = snapshot.getPosition();
            fromTime = snapshot.getTimestamp();
        } else {
            // No snapshot, start from beginning
            position = new Position(accountId, instrumentId);
            fromTime = Instant.EPOCH;
        }
        
        // Replay events up to target time
        List<DomainEvent> events = eventStore.getEvents(
            aggregateId,
            fromTime,
            atTime
        );
        
        for (DomainEvent event : events) {
            position.apply(event);
        }
        
        return position;
    }
}
```

#### 4. **Snapshot Cleanup**

```java
@Service
public class SnapshotCleanupService {
    /**
     * Cleanup old snapshots
     * Keep: Last snapshot per aggregate
     * Keep: Snapshots from last 30 days
     * Delete: Older snapshots
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void cleanupOldSnapshots() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));
        
        List<PositionSnapshot> oldSnapshots = snapshotRepository
            .findOlderThan(cutoff);
        
        // Group by aggregate
        Map<String, List<PositionSnapshot>> byAggregate = oldSnapshots.stream()
            .collect(Collectors.groupingBy(PositionSnapshot::getAggregateId));
        
        for (Map.Entry<String, List<PositionSnapshot>> entry : byAggregate.entrySet()) {
            List<PositionSnapshot> snapshots = entry.getValue();
            
            // Keep latest snapshot
            snapshots.sort(Comparator.comparing(PositionSnapshot::getTimestamp).reversed());
            PositionSnapshot latest = snapshots.get(0);
            
            // Delete others
            for (int i = 1; i < snapshots.size(); i++) {
                snapshotRepository.delete(snapshots.get(i));
            }
        }
    }
}
```

---

## Question 167: How do you handle event replay?

### Answer

### Event Replay

#### 1. **Replay Scenarios**

```
┌─────────────────────────────────────────────────────────┐
│         Event Replay Scenarios                         │
└─────────────────────────────────────────────────────────┘

1. State Recovery:
   ├─ Rebuild state after failure
   ├─ Recover from backup
   └─ Restore to last known good state

2. Testing:
   ├─ Replay historical events
   ├─ Test with real data
   └─ Reproduce bugs

3. Migration:
   ├─ Replay events to new system
   ├─ Data migration
   └─ System upgrade

4. Debugging:
   ├─ Replay events to reproduce issue
   ├─ Step through state changes
   └─ Understand state transitions
```

#### 2. **Replay Implementation**

```java
@Service
public class EventReplayService {
    private final EventStore eventStore;
    private final PositionRepository positionRepository;
    
    /**
     * Replay all events for an aggregate
     */
    public void replayEvents(String aggregateId) {
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        
        Position position = new Position();
        for (DomainEvent event : events) {
            position.apply(event);
        }
        
        // Save reconstructed state
        positionRepository.save(position);
    }
    
    /**
     * Replay events in time range
     */
    public void replayEvents(String aggregateId, Instant from, Instant to) {
        List<DomainEvent> events = eventStore.getEvents(aggregateId, from, to);
        
        // Get state before replay
        Position position = getPositionAtTime(aggregateId, from);
        
        // Replay events
        for (DomainEvent event : events) {
            position.apply(event);
        }
        
        // Save state
        positionRepository.save(position);
    }
    
    /**
     * Replay events with side effects disabled
     * (for testing/debugging)
     */
    public Position replayEventsDryRun(String aggregateId) {
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        
        Position position = new Position();
        for (DomainEvent event : events) {
            // Apply event without side effects
            position.apply(event);
        }
        
        // Return without saving
        return position;
    }
}
```

#### 3. **Bulk Replay**

```java
@Service
public class BulkReplayService {
    /**
     * Replay events for all aggregates
     * Used for system recovery or migration
     */
    public void replayAllEvents() {
        List<String> aggregateIds = getAllAggregateIds();
        
        for (String aggregateId : aggregateIds) {
            try {
                replayEvents(aggregateId);
                log.info("Replayed events for: {}", aggregateId);
            } catch (Exception e) {
                log.error("Failed to replay events for: {}", aggregateId, e);
                // Continue with other aggregates
            }
        }
    }
    
    /**
     * Parallel replay for performance
     */
    public void replayAllEventsParallel() {
        List<String> aggregateIds = getAllAggregateIds();
        
        aggregateIds.parallelStream().forEach(aggregateId -> {
            try {
                replayEvents(aggregateId);
            } catch (Exception e) {
                log.error("Failed to replay events for: {}", aggregateId, e);
            }
        });
    }
}
```

#### 4. **Replay with Validation**

```java
@Service
public class ValidatedReplayService {
    /**
     * Replay events with validation
     * Detects inconsistencies
     */
    public ReplayResult replayEventsWithValidation(String aggregateId) {
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        
        Position position = new Position();
        List<ValidationError> errors = new ArrayList<>();
        
        for (DomainEvent event : events) {
            try {
                // Validate event before applying
                validateEvent(event, position);
                
                // Apply event
                position.apply(event);
                
            } catch (ValidationException e) {
                errors.add(new ValidationError(event, e));
                log.warn("Validation error for event: {}", event.getEventId(), e);
            }
        }
        
        return new ReplayResult(position, errors);
    }
    
    private void validateEvent(DomainEvent event, Position currentState) {
        if (event instanceof TradeExecutedEvent) {
            TradeExecutedEvent tradeEvent = (TradeExecutedEvent) event;
            
            // Validate: Can't sell more than you have
            if (tradeEvent.getTradeType() == TradeType.SELL) {
                if (currentState.getBalance().compareTo(tradeEvent.getQuantity()) < 0) {
                    throw new ValidationException("Insufficient balance for sell");
                }
            }
        }
    }
}
```

---

## Question 168: What's the performance impact of event sourcing?

### Answer

### Performance Considerations

#### 1. **Performance Challenges**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Challenges                         │
└─────────────────────────────────────────────────────────┘

1. State Reconstruction:
   ├─ Loading all events
   ├─ Replaying events
   └─ Slow for long histories

2. Event Storage:
   ├─ Large event log
   ├─ Disk I/O
   └─ Network overhead

3. Memory Usage:
   ├─ Loading events into memory
   ├─ Rebuilding state
   └─ High memory consumption
```

#### 2. **Optimization Strategies**

**Snapshots:**
```java
// Reduce events to replay
// Before: Replay 10,000 events
// After: Replay 100 events (since last snapshot)
// Improvement: 100x faster
```

**Caching:**
```java
// Cache reconstructed state
// Before: Rebuild from events every time
// After: Cache current state, invalidate on new events
// Improvement: 1000x faster for reads
```

**Lazy Loading:**
```java
// Load events on demand
// Before: Load all events upfront
// After: Load events only when needed
// Improvement: Reduced memory usage
```

#### 3. **Performance Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Metrics                             │
└─────────────────────────────────────────────────────────┘

Without Snapshots:
├─ State reconstruction: 500ms (10K events)
├─ Memory usage: 100MB per aggregate
└─ Disk I/O: High

With Snapshots:
├─ State reconstruction: 5ms (100 events)
├─ Memory usage: 10MB per aggregate
└─ Disk I/O: Low

With Caching:
├─ State read: 1ms (cache hit)
├─ Memory usage: 50MB (cached states)
└─ Disk I/O: Minimal
```

#### 4. **Optimized Implementation**

```java
@Service
public class OptimizedPositionService {
    private final EventStore eventStore;
    private final SnapshotRepository snapshotRepository;
    private final Cache<String, Position> positionCache;
    
    public Position getCurrentPosition(String accountId, String instrumentId) {
        String aggregateId = accountId + ":" + instrumentId;
        
        // L1: Cache
        Position cached = positionCache.getIfPresent(aggregateId);
        if (cached != null) {
            return cached;
        }
        
        // L2: Snapshot + Recent Events
        PositionSnapshot snapshot = snapshotRepository.findLatest(aggregateId);
        if (snapshot != null) {
            Position position = rebuildFromSnapshot(snapshot);
            positionCache.put(aggregateId, position);
            return position;
        }
        
        // L3: Full Replay (slow, but rare)
        Position position = rebuildFromAllEvents(aggregateId);
        positionCache.put(aggregateId, position);
        return position;
    }
}
```

---

## Question 169: How do you handle event history growth?

### Answer

### Event History Management

#### 1. **History Growth Problem**

```
┌─────────────────────────────────────────────────────────┐
│         History Growth                                  │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Events accumulate over time
├─ Event log grows indefinitely
├─ Slower state reconstruction
├─ Higher storage costs
└─ Performance degradation

Example:
├─ 1M events per day
├─ 365M events per year
├─ 3.65B events in 10 years
└─ Replay time: Hours
```

#### 2. **Retention Strategy**

```java
@Service
public class EventRetentionService {
    /**
     * Retention Policy:
     * - Keep all events for 1 year
     * - Archive events older than 1 year
     * - Delete events older than 7 years (compliance)
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void archiveOldEvents() {
        Instant oneYearAgo = Instant.now().minus(Duration.ofDays(365));
        Instant sevenYearsAgo = Instant.now().minus(Duration.ofDays(2555));
        
        // Archive events older than 1 year
        List<DomainEvent> eventsToArchive = eventStore
            .getEventsOlderThan(oneYearAgo);
        
        for (DomainEvent event : eventsToArchive) {
            // Move to archive storage (cheaper, slower)
            archiveEvent(event);
            
            // Remove from primary storage
            eventStore.deleteEvent(event.getEventId());
        }
        
        // Delete events older than 7 years
        List<DomainEvent> eventsToDelete = eventStore
            .getEventsOlderThan(sevenYearsAgo);
        
        for (DomainEvent event : eventsToDelete) {
            eventStore.deleteEvent(event.getEventId());
        }
    }
}
```

#### 3. **Compaction Strategy**

```java
@Service
public class EventCompactionService {
    /**
     * Compact events by creating snapshots
     * Replace many events with single snapshot
     */
    public void compactEvents(String aggregateId) {
        // Get all events
        List<DomainEvent> events = eventStore.getEvents(aggregateId);
        
        if (events.size() < 1000) {
            return; // Not enough events to compact
        }
        
        // Create snapshot from events
        Position position = Position.fromEvents(events);
        PositionSnapshot snapshot = createSnapshot(aggregateId, position);
        
        // Delete events before snapshot
        Instant snapshotTime = snapshot.getTimestamp();
        List<DomainEvent> eventsToDelete = events.stream()
            .filter(e -> e.getTimestamp().isBefore(snapshotTime))
            .collect(Collectors.toList());
        
        for (DomainEvent event : eventsToDelete) {
            eventStore.deleteEvent(event.getEventId());
        }
        
        log.info("Compacted {} events into snapshot for {}", 
            eventsToDelete.size(), aggregateId);
    }
}
```

#### 4. **Partitioning by Time**

```java
@Service
public class TimePartitionedEventStore {
    /**
     * Partition events by time
     * Easier to archive/delete old partitions
     */
    public void storeEvent(DomainEvent event) {
        String partition = getPartitionForTime(event.getTimestamp());
        String topic = "events-" + partition; // e.g., "events-2024-01"
        
        kafkaTemplate.send(topic, event.getAggregateId(), event);
    }
    
    private String getPartitionForTime(Instant timestamp) {
        LocalDate date = LocalDate.ofInstant(timestamp, ZoneId.systemDefault());
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
    
    /**
     * Archive entire partition
     */
    public void archivePartition(String partition) {
        String topic = "events-" + partition;
        // Archive all events in this topic
        archiveTopic(topic);
    }
}
```

---

## Question 170: What's the event archival strategy?

### Answer

### Event Archival Strategy

#### 1. **Archival Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Event Archival Architecture                    │
└─────────────────────────────────────────────────────────┘

Primary Storage (Kafka):
├─ Recent events (last 30 days)
├─ Fast access
├─ High performance
└─ Expensive storage

Archive Storage (S3/Glacier):
├─ Older events (30+ days)
├─ Slower access
├─ Lower performance
└─ Cheap storage

Retrieval:
├─ Query archive when needed
├─ Restore to primary if frequent access
└─ Point-in-time queries
```

#### 2. **Archival Implementation**

```java
@Service
public class EventArchivalService {
    private final EventStore eventStore;
    private final ArchiveStorage archiveStorage; // S3, Glacier, etc.
    
    /**
     * Archive events older than retention period
     */
    @Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
    public void archiveOldEvents() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));
        
        List<DomainEvent> eventsToArchive = eventStore
            .getEventsOlderThan(cutoff);
        
        // Group by aggregate for efficient archiving
        Map<String, List<DomainEvent>> byAggregate = eventsToArchive.stream()
            .collect(Collectors.groupingBy(DomainEvent::getAggregateId));
        
        for (Map.Entry<String, List<DomainEvent>> entry : byAggregate.entrySet()) {
            String aggregateId = entry.getKey();
            List<DomainEvent> events = entry.getValue();
            
            // Archive events
            archiveStorage.archiveEvents(aggregateId, events);
            
            // Remove from primary storage
            for (DomainEvent event : events) {
                eventStore.deleteEvent(event.getEventId());
            }
            
            log.info("Archived {} events for aggregate: {}", 
                events.size(), aggregateId);
        }
    }
}
```

#### 3. **Archive Retrieval**

```java
@Service
public class ArchiveRetrievalService {
    private final ArchiveStorage archiveStorage;
    
    /**
     * Retrieve archived events
     * Used for historical queries or state recovery
     */
    public List<DomainEvent> retrieveArchivedEvents(String aggregateId, 
                                                     Instant from, 
                                                     Instant to) {
        // Query archive storage
        return archiveStorage.retrieveEvents(aggregateId, from, to);
    }
    
    /**
     * Restore archived events to primary storage
     * If frequently accessed
     */
    public void restoreToPrimary(String aggregateId, Instant from, Instant to) {
        List<DomainEvent> events = archiveStorage.retrieveEvents(
            aggregateId, from, to);
        
        // Restore to primary storage
        for (DomainEvent event : events) {
            eventStore.appendEvent(aggregateId, event);
        }
        
        log.info("Restored {} events for aggregate: {}", 
            events.size(), aggregateId);
    }
}
```

#### 4. **Tiered Storage**

```java
@Service
public class TieredEventStorage {
    /**
     * Tiered Storage Strategy:
     * - Hot: Last 7 days (Kafka)
     * - Warm: 7-30 days (Kafka with lower retention)
     * - Cold: 30-365 days (S3)
     * - Archive: 365+ days (Glacier)
     */
    public void storeEvent(DomainEvent event) {
        Instant eventTime = event.getTimestamp();
        Instant now = Instant.now();
        Duration age = Duration.between(eventTime, now);
        
        if (age.toDays() < 7) {
            // Hot: Primary Kafka
            primaryEventStore.appendEvent(event);
        } else if (age.toDays() < 30) {
            // Warm: Secondary Kafka
            warmEventStore.appendEvent(event);
        } else if (age.toDays() < 365) {
            // Cold: S3
            coldStorage.archiveEvent(event);
        } else {
            // Archive: Glacier
            archiveStorage.archiveEvent(event);
        }
    }
    
    public List<DomainEvent> getEvents(String aggregateId, Instant from, Instant to) {
        List<DomainEvent> events = new ArrayList<>();
        
        // Query all tiers
        events.addAll(primaryEventStore.getEvents(aggregateId, from, to));
        events.addAll(warmEventStore.getEvents(aggregateId, from, to));
        events.addAll(coldStorage.retrieveEvents(aggregateId, from, to));
        events.addAll(archiveStorage.retrieveEvents(aggregateId, from, to));
        
        // Sort by timestamp
        events.sort(Comparator.comparing(DomainEvent::getTimestamp));
        
        return events;
    }
}
```

---

## Summary

Event Sourcing answers cover:

1. **Event Sourcing Pattern**: Immutable event log, state reconstruction
2. **State Reconstruction**: Replay events to rebuild state
3. **Snapshots**: Performance optimization for long histories
4. **Event Replay**: Recovery, testing, debugging scenarios
5. **Performance Impact**: Challenges and optimization strategies
6. **History Growth**: Retention, compaction, partitioning
7. **Event Archival**: Tiered storage, archive retrieval

Key benefits:
- Complete audit trail
- State recovery capability
- Temporal queries
- Debugging and testing
- Compliance and regulatory requirements
