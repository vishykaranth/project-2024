# Event-Driven Architecture Part 5: Event Sourcing

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
├─ Store all events (changes)
├─ Rebuild state from events
└─ Complete history
```

#### 2. **Why Event Sourcing?**

```
┌─────────────────────────────────────────────────────────┐
│         Benefits of Event Sourcing                     │
└─────────────────────────────────────────────────────────┘

1. Complete Audit Trail:
   ├─ Every change recorded
   ├─ Who, what, when
   └─ Compliance requirement

2. Time Travel:
   ├─ Rebuild state at any point
   ├─ Debugging capability
   └─ Historical analysis

3. Event Replay:
   ├─ Rebuild state from scratch
   ├─ Disaster recovery
   └─ Testing scenarios

4. Event-Driven Architecture:
   ├─ Natural fit
   ├─ Events as source of truth
   └─ Loose coupling
```

#### 3. **Event Sourcing Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Architecture                    │
└─────────────────────────────────────────────────────────┘

Event Store (Kafka):
├─ All events stored
├─ Immutable log
├─ Partitioned by entity
└─ Retention policy

State Reconstruction:
├─ Read all events
├─ Apply events in order
└─ Rebuild current state

Snapshots:
├─ Periodic state snapshots
├─ Faster reconstruction
└─ Start from snapshot + events
```

---

## Question 165: How do you rebuild state from events?

### Answer

### State Reconstruction

#### 1. **Reconstruction Process**

```java
@Service
public class AgentStateReconstructionService {
    private final KafkaConsumer<String, AgentEvent> kafkaConsumer;
    
    public AgentState reconstructState(String agentId) {
        // Step 1: Read all events for this agent
        List<AgentEvent> events = readEventsFromKafka(agentId);
        
        // Step 2: Start with initial state
        AgentState state = AgentState.initialState(agentId);
        
        // Step 3: Apply all events in order
        for (AgentEvent event : events) {
            state = applyEvent(state, event);
        }
        
        return state;
    }
    
    private List<AgentEvent> readEventsFromKafka(String agentId) {
        // Read from partition for this agent
        List<AgentEvent> events = new ArrayList<>();
        
        // Seek to beginning
        kafkaConsumer.seekToBeginning(
            Collections.singletonList(
                new TopicPartition("agent-events", getPartition(agentId))
            )
        );
        
        // Read all events
        ConsumerRecords<String, AgentEvent> records = kafkaConsumer.poll(
            Duration.ofSeconds(10)
        );
        
        for (ConsumerRecord<String, AgentEvent> record : records) {
            if (record.key().equals(agentId)) {
                events.add(record.value());
            }
        }
        
        return events;
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

#### 2. **Reconstruction Flow**

```
┌─────────────────────────────────────────────────────────┐
│         State Reconstruction Flow                     │
└─────────────────────────────────────────────────────────┘

1. Load Events:
   ├─ Read from Kafka
   ├─ Filter by entity ID
   └─ Sort by timestamp/sequence

2. Initialize State:
   ├─ Create initial state
   └─ Version = 0

3. Apply Events:
   ├─ For each event:
   │  ├─ Validate event
   │  ├─ Apply to state
   │  └─ Update version
   └─ Continue until all events processed

4. Return State:
   └─ Current state after all events
```

#### 3. **Optimized Reconstruction**

```java
@Service
public class OptimizedStateReconstruction {
    
    public AgentState reconstructState(String agentId) {
        // Try to use snapshot first
        AgentStateSnapshot snapshot = getLatestSnapshot(agentId);
        
        if (snapshot != null) {
            // Rebuild from snapshot
            return reconstructFromSnapshot(agentId, snapshot);
        }
        
        // Fallback: Rebuild from all events
        return reconstructFromAllEvents(agentId);
    }
    
    private AgentState reconstructFromSnapshot(String agentId, 
                                                AgentStateSnapshot snapshot) {
        // Get events after snapshot
        List<AgentEvent> events = readEventsAfterVersion(
            agentId, 
            snapshot.getVersion()
        );
        
        // Start from snapshot state
        AgentState state = snapshot.getState();
        
        // Apply new events
        for (AgentEvent event : events) {
            state = applyEvent(state, event);
        }
        
        return state;
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
│         Why Snapshots?                                 │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Many events over time
├─ Slow reconstruction
└─ High memory usage

Solution:
├─ Periodic snapshots
├─ Faster reconstruction
└─ Start from snapshot + recent events
```

#### 2. **Snapshot Creation**

```java
@Service
public class AgentStateSnapshotService {
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void createSnapshots() {
        List<String> agentIds = getAllActiveAgentIds();
        
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
}
```

#### 3. **Snapshot Storage**

```java
@Entity
public class AgentStateSnapshot {
    @Id
    private String snapshotId;
    
    private String agentId;
    
    @Lob
    private AgentState state; // Serialized state
    
    private Long version;
    private Instant timestamp;
    
    // Getters and setters
}
```

#### 4. **Snapshot Usage**

```java
@Service
public class SnapshotBasedReconstruction {
    
    public AgentState reconstructState(String agentId) {
        // Get latest snapshot
        AgentStateSnapshot snapshot = snapshotRepository
            .findLatestByAgentId(agentId)
            .orElse(null);
        
        if (snapshot == null) {
            // No snapshot, rebuild from all events
            return reconstructFromAllEvents(agentId);
        }
        
        // Get events after snapshot
        List<AgentEvent> events = readEventsAfterVersion(
            agentId, 
            snapshot.getVersion()
        );
        
        // Start from snapshot
        AgentState state = snapshot.getState();
        
        // Apply new events
        for (AgentEvent event : events) {
            state = applyEvent(state, event);
        }
        
        return state;
    }
}
```

#### 5. **Snapshot Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Snapshot Strategy                              │
└─────────────────────────────────────────────────────────┘

Frequency:
├─ Hourly for active entities
├─ Daily for inactive entities
└─ On-demand for critical entities

Retention:
├─ Keep last 7 snapshots
├─ Delete older snapshots
└─ Archive to cold storage

Trigger Conditions:
├─ Time-based (scheduled)
├─ Event count threshold
├─ State size threshold
└─ Manual trigger
```

---

## Question 167: How do you handle event replay?

### Answer

### Event Replay Strategy

#### 1. **Replay Scenarios**

```
┌─────────────────────────────────────────────────────────┐
│         Replay Scenarios                              │
└─────────────────────────────────────────────────────────┘

1. State Recovery:
   ├─ Rebuild state from events
   ├─ After system failure
   └─ Disaster recovery

2. Testing:
   ├─ Replay historical events
   ├─ Test scenarios
   └─ Debugging

3. Migration:
   ├─ Replay events to new system
   ├─ Data migration
   └─ System upgrade
```

#### 2. **Replay Implementation**

```java
@Service
public class EventReplayService {
    private final KafkaConsumer<String, AgentEvent> kafkaConsumer;
    
    public void replayEvents(String agentId, Instant fromTime, Instant toTime) {
        // Read events in time range
        List<AgentEvent> events = readEventsInTimeRange(
            agentId, 
            fromTime, 
            toTime
        );
        
        // Replay events
        AgentState state = getStateAtTime(agentId, fromTime);
        
        for (AgentEvent event : events) {
            state = applyEvent(state, event);
        }
        
        // Save final state
        saveState(agentId, state);
    }
    
    public void replayEventsFromOffset(String agentId, long fromOffset) {
        // Seek to offset
        TopicPartition partition = new TopicPartition(
            "agent-events", 
            getPartition(agentId)
        );
        
        kafkaConsumer.seek(partition, fromOffset);
        
        // Read and replay
        ConsumerRecords<String, AgentEvent> records = kafkaConsumer.poll(
            Duration.ofSeconds(10)
        );
        
        AgentState state = getCurrentState(agentId);
        
        for (ConsumerRecord<String, AgentEvent> record : records) {
            if (record.key().equals(agentId)) {
                state = applyEvent(state, record.value());
            }
        }
        
        saveState(agentId, state);
    }
}
```

#### 3. **Replay Modes**

```java
public enum ReplayMode {
    FULL,           // Replay all events
    INCREMENTAL,    // Replay from last processed
    TIME_RANGE,     // Replay events in time range
    VERSION_RANGE   // Replay events in version range
}

@Service
public class EventReplayService {
    
    public void replayEvents(String agentId, ReplayMode mode, 
                             Object... params) {
        switch (mode) {
            case FULL:
                replayAllEvents(agentId);
                break;
            case INCREMENTAL:
                replayFromLastProcessed(agentId);
                break;
            case TIME_RANGE:
                Instant from = (Instant) params[0];
                Instant to = (Instant) params[1];
                replayEventsInTimeRange(agentId, from, to);
                break;
            case VERSION_RANGE:
                Long fromVersion = (Long) params[0];
                Long toVersion = (Long) params[1];
                replayEventsInVersionRange(agentId, fromVersion, toVersion);
                break;
        }
    }
}
```

---

## Question 168: What's the performance impact of event sourcing?

### Answer

### Performance Impact

#### 1. **Performance Considerations**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Impact                            │
└─────────────────────────────────────────────────────────┘

Read Performance:
├─ Reconstructing state: Slow (many events)
├─ With snapshots: Fast
└─ Caching: Very fast

Write Performance:
├─ Appending events: Fast
├─ No updates needed
└─ Better than traditional updates

Storage:
├─ More storage (all events)
├─ Compression helps
└─ Retention policies
```

#### 2. **Optimization Strategies**

```java
@Service
public class OptimizedEventSourcing {
    
    // Strategy 1: Snapshots
    public AgentState getState(String agentId) {
        AgentStateSnapshot snapshot = getLatestSnapshot(agentId);
        if (snapshot != null) {
            return reconstructFromSnapshot(agentId, snapshot);
        }
        return reconstructFromAllEvents(agentId);
    }
    
    // Strategy 2: Caching
    @Cacheable("agent-state")
    public AgentState getCachedState(String agentId) {
        return getState(agentId);
    }
    
    // Strategy 3: Lazy Loading
    public AgentState getStateLazy(String agentId) {
        // Return cached if available
        AgentState cached = getCachedState(agentId);
        if (cached != null && isRecent(cached)) {
            return cached;
        }
        
        // Reconstruct in background
        CompletableFuture.supplyAsync(() -> reconstructState(agentId))
            .thenAccept(state -> cacheState(agentId, state));
        
        // Return stale if needed
        return cached != null ? cached : reconstructState(agentId);
    }
}
```

#### 3. **Performance Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Metrics                            │
└─────────────────────────────────────────────────────────┘

State Reconstruction:
├─ Without snapshot: 500ms (1000 events)
├─ With snapshot: 50ms (100 events after snapshot)
└─ With cache: 1ms (cached state)

Event Append:
├─ Single event: 5ms
├─ Batch events: 50ms (100 events)
└─ Throughput: 10K events/second
```

---

## Question 169: How do you handle event history growth?

### Answer

### Event History Management

#### 1. **History Growth Problem**

```
┌─────────────────────────────────────────────────────────┐
│         History Growth Problem                        │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Events accumulate over time
├─ Storage grows
├─ Reconstruction slows
└─ Memory usage increases

Example:
├─ 1M events per day
├─ 365M events per year
├─ 2.5GB per year (assuming 7 bytes/event)
└─ Need management strategy
```

#### 2. **Retention Policies**

```java
@Configuration
public class EventRetentionConfig {
    
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
        // Archive events older than retention
        List<String> topics = Arrays.asList(
            "agent-events",
            "conversation-events"
        );
        
        for (String topic : topics) {
            archiveTopic(topic);
        }
    }
    
    private void archiveTopic(String topic) {
        // Read events older than retention
        List<Event> oldEvents = readEventsOlderThan(
            topic, 
            Duration.ofDays(7)
        );
        
        // Archive to S3
        String s3Key = "archive/" + topic + "/" + LocalDate.now();
        s3Service.putObject(s3Key, serialize(oldEvents));
        
        // Delete from Kafka (handled by retention policy)
    }
}
```

#### 4. **Compaction Strategy**

```java
@Configuration
public class EventCompactionConfig {
    
    @Bean
    public NewTopic compactedTopic() {
        return TopicBuilder.name("agent-state-compacted")
            .partitions(10)
            .replicas(3)
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, "compact")
            .config(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG, "0.5")
            .build();
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

Kafka (Hot Storage):
├─ Recent events (7-30 days)
├─ Fast access
└─ Active processing

S3 (Cold Storage):
├─ Archived events
├─ Long-term retention
└─ Compliance storage

Retrieval:
├─ Recent: From Kafka
├─ Archived: From S3
└─ On-demand retrieval
```

#### 2. **Archival Process**

```java
@Service
public class EventArchivalService {
    private final S3Service s3Service;
    private final KafkaConsumer<String, Event> kafkaConsumer;
    
    @Scheduled(cron = "0 0 2 * * *") // Daily
    public void archiveEvents() {
        // Archive events older than retention
        LocalDate archiveDate = LocalDate.now().minusDays(7);
        
        List<String> topics = getTopicsToArchive();
        
        for (String topic : topics) {
            archiveTopic(topic, archiveDate);
        }
    }
    
    private void archiveTopic(String topic, LocalDate archiveDate) {
        // Read events before archive date
        List<Event> events = readEventsBeforeDate(topic, archiveDate);
        
        if (events.isEmpty()) {
            return;
        }
        
        // Group by date for efficient storage
        Map<LocalDate, List<Event>> eventsByDate = events.stream()
            .collect(Collectors.groupingBy(
                event -> LocalDate.from(event.getTimestamp())
            ));
        
        // Archive each day
        for (Map.Entry<LocalDate, List<Event>> entry : eventsByDate.entrySet()) {
            String s3Key = String.format(
                "archive/%s/%s/%s.json.gz",
                topic,
                entry.getKey().getYear(),
                entry.getKey()
            );
            
            // Compress and upload
            byte[] compressed = compress(serialize(entry.getValue()));
            s3Service.putObject(s3Key, compressed);
        }
    }
}
```

#### 3. **Retrieval Strategy**

```java
@Service
public class ArchivedEventRetrievalService {
    private final S3Service s3Service;
    
    public List<Event> retrieveArchivedEvents(String topic, 
                                               LocalDate date) {
        String s3Key = String.format(
            "archive/%s/%d/%s.json.gz",
            topic,
            date.getYear(),
            date
        );
        
        // Download and decompress
        byte[] compressed = s3Service.getObject(s3Key);
        byte[] decompressed = decompress(compressed);
        
        return deserialize(decompressed);
    }
    
    public List<Event> retrieveArchivedEventsRange(String topic,
                                                    LocalDate fromDate,
                                                    LocalDate toDate) {
        List<Event> allEvents = new ArrayList<>();
        
        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            List<Event> dayEvents = retrieveArchivedEvents(topic, current);
            allEvents.addAll(dayEvents);
            current = current.plusDays(1);
        }
        
        return allEvents;
    }
}
```

#### 4. **Archival Benefits**

```
┌─────────────────────────────────────────────────────────┐
│         Archival Benefits                             │
└─────────────────────────────────────────────────────────┘

Storage Cost:
├─ Kafka: Expensive (SSD)
├─ S3: Cheap (object storage)
└─ 90% cost reduction

Compliance:
├─ Long-term retention
├─ Immutable storage
└─ Audit trail

Performance:
├─ Kafka: Fast (recent events)
├─ S3: Slower (archived events)
└─ Best of both worlds
```

---

## Summary

Event Sourcing covers:

1. **Event Sourcing Pattern**: Store events, rebuild state, complete history
2. **State Reconstruction**: Read events, apply in order, rebuild state
3. **Snapshots**: Periodic snapshots for faster reconstruction
4. **Event Replay**: Replay scenarios, recovery, testing
5. **Performance Impact**: Optimizations with snapshots and caching
6. **History Growth**: Retention policies and management
7. **Event Archival**: Archive to S3, retrieve on demand

Event sourcing provides complete audit trail, time travel, and event replay capabilities while managing performance and storage costs through snapshots and archival strategies.
