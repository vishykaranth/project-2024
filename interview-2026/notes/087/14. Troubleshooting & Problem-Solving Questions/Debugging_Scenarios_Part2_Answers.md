# Debugging Scenarios - Part 2: Data & Consistency Issues

## Question 286: Position calculations are incorrect. How do you debug?

### Answer

### Position Calculation Debugging

#### 1. **Problem Identification**

```
┌─────────────────────────────────────────────────────────┐
│         Position Calculation Issues                    │
└─────────────────────────────────────────────────────────┘

Symptoms:
├─ Position balances don't match trades
├─ Reconciliation failures
├─ Customer complaints
└─ Financial discrepancies

Causes:
├─ Events processed out of order
├─ Missing events
├─ Duplicate events
└─ Calculation errors
```

#### 2. **Event Replay Analysis**

```java
@Service
public class PositionCalculationDebugger {
    private final EventRepository eventRepository;
    private final PositionService positionService;
    
    public void debugPositionCalculation(String accountId, String instrumentId) {
        // Get current position
        Position currentPosition = positionService.getCurrentPosition(accountId, instrumentId);
        
        // Replay events to recalculate
        Position recalculatedPosition = replayEvents(accountId, instrumentId);
        
        // Compare
        if (!currentPosition.equals(recalculatedPosition)) {
            log.error("Position mismatch detected!");
            log.error("Current: {}", currentPosition);
            log.error("Recalculated: {}", recalculatedPosition);
            
            // Find discrepancy
            findDiscrepancy(currentPosition, recalculatedPosition);
        }
    }
    
    private Position replayEvents(String accountId, String instrumentId) {
        // Get all events for this position
        List<PositionEvent> events = eventRepository
            .findByAccountIdAndInstrumentId(accountId, instrumentId);
        
        // Replay events
        Position position = Position.zero(accountId, instrumentId);
        
        for (PositionEvent event : events) {
            position = applyEvent(position, event);
        }
        
        return position;
    }
    
    private void findDiscrepancy(Position current, Position recalculated) {
        // Compare fields
        if (!current.getBalance().equals(recalculated.getBalance())) {
            log.error("Balance mismatch: current={}, recalculated={}", 
                current.getBalance(), recalculated.getBalance());
            
            // Find which events caused the difference
            findCausingEvents(current, recalculated);
        }
    }
}
```

#### 3. **Event Sequence Validation**

```java
@Service
public class EventSequenceValidator {
    public void validateEventSequence(String accountId, String instrumentId) {
        List<PositionEvent> events = getEvents(accountId, instrumentId);
        
        // Check for gaps in sequence
        for (int i = 1; i < events.size(); i++) {
            long currentSeq = events.get(i).getSequenceNumber();
            long previousSeq = events.get(i-1).getSequenceNumber();
            
            if (currentSeq != previousSeq + 1) {
                log.error("Sequence gap detected: expected {}, got {}", 
                    previousSeq + 1, currentSeq);
                investigateSequenceGap(accountId, instrumentId, previousSeq, currentSeq);
            }
        }
        
        // Check for duplicates
        Set<Long> sequenceNumbers = events.stream()
            .map(PositionEvent::getSequenceNumber)
            .collect(Collectors.toSet());
        
        if (sequenceNumbers.size() != events.size()) {
            log.error("Duplicate sequence numbers detected");
            findDuplicates(events);
        }
    }
}
```

#### 4. **Trade-to-Position Reconciliation**

```java
@Service
public class PositionReconciliationService {
    public void reconcilePosition(String accountId, String instrumentId) {
        // Get position from position service
        Position position = positionService.getCurrentPosition(accountId, instrumentId);
        
        // Calculate position from trades
        BigDecimal calculatedBalance = calculateFromTrades(accountId, instrumentId);
        
        // Compare
        if (!position.getBalance().equals(calculatedBalance)) {
            log.error("Reconciliation failure: position={}, calculated={}", 
                position.getBalance(), calculatedBalance);
            
            // Find missing or extra trades
            findTradeDiscrepancies(accountId, instrumentId, position, calculatedBalance);
        }
    }
    
    private BigDecimal calculateFromTrades(String accountId, String instrumentId) {
        List<Trade> trades = tradeRepository
            .findByAccountIdAndInstrumentId(accountId, instrumentId);
        
        BigDecimal balance = BigDecimal.ZERO;
        
        for (Trade trade : trades) {
            if (trade.getType() == TradeType.BUY) {
                balance = balance.add(trade.getQuantity());
            } else {
                balance = balance.subtract(trade.getQuantity());
            }
        }
        
        return balance;
    }
}
```

---

## Question 287: Events are lost. How do you recover?

### Answer

### Event Loss Recovery

#### 1. **Event Loss Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Event Loss Detection                          │
└─────────────────────────────────────────────────────────┘

Detection Methods:
├─ Sequence number gaps
├─ Reconciliation failures
├─ Missing state updates
└─ Consumer lag monitoring
```

#### 2. **Kafka Consumer Lag Monitoring**

```java
@Component
public class ConsumerLagMonitor {
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorConsumerLag() {
        Map<String, Long> lag = getConsumerLag();
        
        for (Map.Entry<String, Long> entry : lag.entrySet()) {
            String consumerGroup = entry.getKey();
            long lagValue = entry.getValue();
            
            if (lagValue > 1000) { // Threshold: 1000 messages
                log.warn("High consumer lag: {} - {} messages", consumerGroup, lagValue);
                alertService.highConsumerLag(consumerGroup, lagValue);
            }
            
            if (lagValue > 10000) {
                log.error("Very high consumer lag: {} - {} messages", consumerGroup, lagValue);
                investigateConsumerLag(consumerGroup);
            }
        }
    }
    
    private void investigateConsumerLag(String consumerGroup) {
        // Check consumer health
        checkConsumerHealth(consumerGroup);
        
        // Check processing rate
        double processingRate = getProcessingRate(consumerGroup);
        log.info("Processing rate: {} messages/second", processingRate);
        
        // Check for stuck consumers
        if (isConsumerStuck(consumerGroup)) {
            restartConsumer(consumerGroup);
        }
    }
}
```

#### 3. **Event Replay from Kafka**

```java
@Service
public class EventRecoveryService {
    private final KafkaConsumer<String, Event> kafkaConsumer;
    
    public void replayEvents(String entityId, long fromSequence, long toSequence) {
        // Seek to start position
        TopicPartition partition = getPartitionForEntity(entityId);
        kafkaConsumer.assign(Collections.singletonList(partition));
        
        // Find offset for sequence number
        long startOffset = findOffsetForSequence(partition, fromSequence);
        kafkaConsumer.seek(partition, startOffset);
        
        // Replay events
        List<Event> events = new ArrayList<>();
        ConsumerRecords<String, Event> records = kafkaConsumer.poll(Duration.ofSeconds(10));
        
        for (ConsumerRecord<String, Event> record : records) {
            Event event = record.value();
            
            if (event.getEntityId().equals(entityId) &&
                event.getSequenceNumber() >= fromSequence &&
                event.getSequenceNumber() <= toSequence) {
                events.add(event);
            }
            
            if (event.getSequenceNumber() > toSequence) {
                break;
            }
        }
        
        // Replay events
        for (Event event : events) {
            replayEvent(event);
        }
    }
    
    private void replayEvent(Event event) {
        // Replay event to rebuild state
        eventProcessor.processEvent(event);
    }
}
```

#### 4. **Database Backup Recovery**

```java
@Service
public class DatabaseBackupRecovery {
    public void recoverFromBackup(Instant backupTime) {
        // Restore database from backup
        restoreDatabaseFromBackup(backupTime);
        
        // Replay events from backup time to now
        replayEventsFromTime(backupTime, Instant.now());
    }
    
    private void replayEventsFromTime(Instant fromTime, Instant toTime) {
        // Get events from Kafka between times
        List<Event> events = getEventsBetweenTimes(fromTime, toTime);
        
        // Replay events
        for (Event event : events) {
            replayEvent(event);
        }
    }
}
```

---

## Question 288: System is experiencing memory leaks. How do you identify and fix?

### Answer

### Memory Leak Detection and Fix

#### 1. **Memory Leak Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Leak Indicators                          │
└─────────────────────────────────────────────────────────┘

Symptoms:
├─ Gradual memory increase
├─ Frequent GC cycles
├─ OOM errors
└─ Performance degradation
```

#### 2. **Heap Analysis**

```java
@Component
public class MemoryLeakDetector {
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void detectMemoryLeaks() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        double usagePercent = (double) used / max * 100;
        
        log.info("Heap usage: {}% ({} / {})", usagePercent, used, max);
        
        if (usagePercent > 80) {
            log.warn("High heap usage detected: {}%", usagePercent);
            
            // Take heap dump
            if (usagePercent > 90) {
                takeHeapDump();
            }
        }
        
        // Check for memory leak pattern
        if (isMemoryLeakPattern(heapUsage)) {
            log.error("Memory leak pattern detected");
            analyzeMemoryLeak();
        }
    }
    
    private boolean isMemoryLeakPattern(MemoryUsage heapUsage) {
        // Check if memory is continuously growing
        // Compare with previous measurements
        return false; // Simplified
    }
}
```

#### 3. **Heap Dump Analysis**

```java
@Service
public class HeapDumpAnalyzer {
    public void analyzeHeapDump(String heapDumpFile) {
        // Load heap dump
        HeapDump dump = loadHeapDump(heapDumpFile);
        
        // Analyze object counts
        Map<String, Long> objectCounts = dump.getObjectCounts();
        
        // Find suspicious objects
        objectCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(20)
            .forEach(entry -> 
                log.info("Object: {}, Count: {}, Size: {}", 
                    entry.getKey(), 
                    entry.getValue(),
                    dump.getObjectSize(entry.getKey()))
            );
        
        // Check for common leak patterns
        checkForCommonLeakPatterns(dump);
    }
    
    private void checkForCommonLeakPatterns(HeapDump dump) {
        // Check for growing collections
        if (dump.hasGrowingCollection("ArrayList")) {
            log.warn("Growing ArrayList detected - possible leak");
        }
        
        // Check for listeners not being removed
        if (dump.hasUnremovedListeners()) {
            log.warn("Unremoved listeners detected - possible leak");
        }
        
        // Check for static collections
        if (dump.hasStaticCollection()) {
            log.warn("Static collection detected - possible leak");
        }
    }
}
```

#### 4. **Common Memory Leak Patterns**

```java
// Pattern 1: Unclosed Resources
public class ResourceLeakExample {
    // ❌ BAD: Resource not closed
    public void processFile(String filename) {
        FileInputStream stream = new FileInputStream(filename);
        // Process file
        // Stream not closed - memory leak!
    }
    
    // ✅ GOOD: Resource properly closed
    public void processFileGood(String filename) {
        try (FileInputStream stream = new FileInputStream(filename)) {
            // Process file
        } // Automatically closed
    }
}

// Pattern 2: Static Collections
public class StaticCollectionLeak {
    // ❌ BAD: Static collection grows forever
    private static List<Object> cache = new ArrayList<>();
    
    public void addToCache(Object obj) {
        cache.add(obj); // Never removed - memory leak!
    }
    
    // ✅ GOOD: Bounded cache with eviction
    private static Cache<String, Object> cache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();
}

// Pattern 3: Listeners Not Removed
public class ListenerLeak {
    // ❌ BAD: Listener not removed
    public void registerListener(EventListener listener) {
        eventBus.addListener(listener);
        // Listener never removed - memory leak!
    }
    
    // ✅ GOOD: Listener properly removed
    public void registerListenerGood(EventListener listener) {
        eventBus.addListener(listener);
        // Store reference for removal
        registeredListeners.add(listener);
    }
    
    public void unregisterListener(EventListener listener) {
        eventBus.removeListener(listener);
        registeredListeners.remove(listener);
    }
}
```

#### 5. **Memory Leak Fixes**

```java
@Service
public class MemoryLeakFixer {
    public void fixMemoryLeak(String leakType) {
        switch (leakType) {
            case "UNCLOSED_RESOURCES":
                fixUnclosedResources();
                break;
            case "GROWING_COLLECTIONS":
                fixGrowingCollections();
                break;
            case "UNREMOVED_LISTENERS":
                fixUnremovedListeners();
                break;
            case "STATIC_COLLECTIONS":
                fixStaticCollections();
                break;
        }
    }
    
    private void fixGrowingCollections() {
        // Replace unbounded collections with bounded
        // Add eviction policies
        // Clear collections periodically
    }
}
```

---

## Question 289: Network partitions are causing issues. How do you handle?

### Answer

### Network Partition Handling

#### 1. **Partition Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Network Partition Detection                    │
└─────────────────────────────────────────────────────────┘

Symptoms:
├─ Service unavailability
├─ Timeout errors
├─ Split-brain scenarios
└─ Data inconsistency
```

#### 2. **Partition Detection**

```java
@Component
public class NetworkPartitionDetector {
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void detectPartitions() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            // Check connectivity
            if (!isReachable(service)) {
                log.warn("Service unreachable: {}", service.getName());
                handlePartition(service);
            }
            
            // Check for split-brain
            if (isSplitBrain(service)) {
                log.error("Split-brain detected for service: {}", service.getName());
                handleSplitBrain(service);
            }
        }
    }
    
    private boolean isReachable(Service service) {
        try {
            // Ping service
            return pingService(service, Duration.ofSeconds(2));
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isSplitBrain(Service service) {
        // Check if multiple instances think they're the leader
        List<ServiceInstance> instances = getServiceInstances(service);
        long leaderCount = instances.stream()
            .filter(ServiceInstance::isLeader)
            .count();
        
        return leaderCount > 1;
    }
}
```

#### 3. **Partition Handling Strategies**

```java
@Service
public class PartitionHandler {
    public void handlePartition(Service service) {
        // Strategy 1: Continue with degraded service
        if (canContinueDegraded(service)) {
            continueWithDegradedService(service);
            return;
        }
        
        // Strategy 2: Fail fast
        if (shouldFailFast(service)) {
            failFast(service);
            return;
        }
        
        // Strategy 3: Wait for partition to heal
        waitForPartitionHeal(service);
    }
    
    private void continueWithDegradedService(Service service) {
        // Use cached data
        // Return stale data
        // Queue requests for later
    }
    
    private void failFast(Service service) {
        // Return error immediately
        // Don't wait for timeout
        // Preserve resources
    }
}
```

#### 4. **Split-Brain Prevention**

```java
@Service
public class SplitBrainPrevention {
    public void preventSplitBrain(Service service) {
        // Use quorum-based leader election
        int quorum = (getInstanceCount(service) / 2) + 1;
        
        // Only allow operations if quorum is available
        if (getAvailableInstances(service).size() < quorum) {
            log.warn("Quorum not available, stopping operations");
            stopOperations(service);
        }
    }
    
    private void stopOperations(Service service) {
        // Stop accepting new requests
        // Complete in-flight requests
        // Wait for partition to heal
    }
}
```

---

## Question 290: Service is returning inconsistent results. How do you debug?

### Answer

### Inconsistent Results Debugging

#### 1. **Inconsistency Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Inconsistency Detection                         │
└─────────────────────────────────────────────────────────┘

Symptoms:
├─ Different results for same request
├─ Stale data returned
├─ Partial updates
└─ Race conditions
```

#### 2. **Request Tracing**

```java
@Service
public class InconsistencyDebugger {
    private final Tracer tracer;
    
    public Response processRequest(Request request) {
        // Create trace
        Span span = tracer.nextSpan()
            .name("process-request")
            .tag("request.id", request.getId())
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Process request
            Response response = doProcess(request);
            
            // Log for debugging
            log.debug("Request processed: id={}, result={}", 
                request.getId(), response);
            
            return response;
        } finally {
            span.end();
        }
    }
    
    public void debugInconsistency(String requestId) {
        // Get all traces for this request
        List<Trace> traces = getTracesForRequest(requestId);
        
        // Compare results
        Map<String, Object> results = new HashMap<>();
        for (Trace trace : traces) {
            String instance = trace.getServiceInstance();
            Object result = extractResult(trace);
            results.put(instance, result);
        }
        
        // Find inconsistencies
        if (results.values().stream().distinct().count() > 1) {
            log.error("Inconsistent results detected for request: {}", requestId);
            log.error("Results: {}", results);
            analyzeInconsistency(requestId, results);
        }
    }
}
```

#### 3. **State Consistency Check**

```java
@Service
public class StateConsistencyChecker {
    public void checkStateConsistency(String entityId) {
        // Get state from all instances
        Map<String, State> states = new HashMap<>();
        
        for (ServiceInstance instance : getAllInstances()) {
            State state = getStateFromInstance(instance, entityId);
            states.put(instance.getId(), state);
        }
        
        // Check consistency
        if (!areStatesConsistent(states)) {
            log.error("State inconsistency detected for entity: {}", entityId);
            log.error("States: {}", states);
            
            // Find source of inconsistency
            findInconsistencySource(entityId, states);
        }
    }
    
    private boolean areStatesConsistent(Map<String, State> states) {
        if (states.isEmpty()) {
            return true;
        }
        
        State firstState = states.values().iterator().next();
        return states.values().stream()
            .allMatch(state -> state.equals(firstState));
    }
}
```

#### 4. **Race Condition Detection**

```java
@Service
public class RaceConditionDetector {
    public void detectRaceConditions(String entityId) {
        // Monitor concurrent updates
        List<UpdateEvent> updates = getRecentUpdates(entityId);
        
        // Check for overlapping updates
        for (int i = 0; i < updates.size(); i++) {
            for (int j = i + 1; j < updates.size(); j++) {
                UpdateEvent event1 = updates.get(i);
                UpdateEvent event2 = updates.get(j);
                
                if (areOverlapping(event1, event2)) {
                    log.warn("Potential race condition: overlapping updates");
                    log.warn("Event 1: {}", event1);
                    log.warn("Event 2: {}", event2);
                    
                    analyzeRaceCondition(event1, event2);
                }
            }
        }
    }
    
    private boolean areOverlapping(UpdateEvent event1, UpdateEvent event2) {
        return event1.getStartTime().isBefore(event2.getEndTime()) &&
               event1.getEndTime().isAfter(event2.getStartTime());
    }
}
```

---

## Summary

Part 2 covers debugging scenarios for:

1. **Position Calculations**: Event replay, sequence validation, reconciliation
2. **Event Loss**: Detection, consumer lag monitoring, recovery from Kafka/backups
3. **Memory Leaks**: Detection, heap analysis, common patterns, fixes
4. **Network Partitions**: Detection, handling strategies, split-brain prevention
5. **Inconsistent Results**: Request tracing, state consistency, race conditions

Key principles:
- Use event replay for state reconstruction
- Monitor consumer lag to detect event loss
- Analyze heap dumps for memory leaks
- Handle network partitions gracefully
- Trace requests to find inconsistencies
