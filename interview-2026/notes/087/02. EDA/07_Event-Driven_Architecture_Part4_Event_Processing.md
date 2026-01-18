# Event-Driven Architecture Part 4: Event Processing

## Question 154: How do you ensure event ordering?

### Answer

### Event Ordering Strategy

#### 1. **Partition-Based Ordering**

```
┌─────────────────────────────────────────────────────────┐
│         Partition-Based Ordering                       │
└─────────────────────────────────────────────────────────┘

Key Principle:
├─ Events with same partition key → same partition
├─ Events in partition → processed in order
├─ Single consumer per partition
└─ Ordering guaranteed per partition

Implementation:
├─ Partition key = entity ID (agentId, conversationId, accountId)
├─ Hash-based partitioning
└─ Consumer concurrency = partition count
```

#### 2. **Sequence Numbers**

```java
@Service
public class EventOrderingService {
    private final RedisTemplate<String, Long> redisTemplate;
    
    @KafkaListener(topics = "trade-events")
    public void handleTradeEvent(TradeCreatedEvent event) {
        String accountId = event.getAccountId();
        String sequenceKey = "sequence:account:" + accountId;
        
        // Get expected sequence
        Long expectedSequence = redisTemplate.opsForValue().get(sequenceKey);
        if (expectedSequence == null) {
            expectedSequence = 0L;
        }
        
        // Validate ordering
        if (event.getSequenceNumber() != expectedSequence + 1) {
            handleOutOfOrderEvent(event, expectedSequence);
            return;
        }
        
        // Process in order
        processEvent(event);
        redisTemplate.opsForValue().set(sequenceKey, event.getSequenceNumber());
    }
}
```

#### 3. **Consumer Configuration**

```java
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TradeEvent> 
        kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TradeEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        
        // One thread per partition ensures ordering
        factory.setConcurrency(10); // Match partition count
        
        // Process one at a time
        factory.getContainerProperties().setIdleBetweenPolls(100);
        
        return factory;
    }
}
```

---

## Question 155: What's the partitioning strategy for events?

### Answer

### Event Partitioning Strategy

#### 1. **Partition Key Selection**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Key Strategy                         │
└─────────────────────────────────────────────────────────┘

Key Selection Criteria:
├─ Events needing ordering → same partition
├─ High cardinality → even distribution
├─ Related events → same partition
└─ Business logic alignment

Examples:
├─ agent-events: agentId
├─ conversation-events: conversationId
├─ trade-events: accountId
└─ message-events: conversationId
```

#### 2. **Partition Count Calculation**

```java
@Service
public class PartitionStrategy {
    /**
     * Calculate optimal partition count
     */
    public int calculatePartitions(long eventsPerSecond, int consumers) {
        // Target: 10K events/second per partition
        int basePartitions = (int) Math.ceil(eventsPerSecond / 10000.0);
        
        // At least one partition per consumer
        int minPartitions = Math.max(basePartitions, consumers);
        
        // Add 50% buffer for growth
        int partitionsWithBuffer = (int) (minPartitions * 1.5);
        
        // Round to power of 2
        return nextPowerOfTwo(partitionsWithBuffer);
    }
}
```

#### 3. **Custom Partitioning**

```java
@Component
public class CustomPartitioner implements Partitioner {
    
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, 
                        Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        
        if (key == null) {
            return ThreadLocalRandom.current().nextInt(numPartitions);
        }
        
        // Tenant-based partitioning
        if (key instanceof String) {
            String keyStr = (String) key;
            if (keyStr.contains("tenant-")) {
                String tenantId = extractTenantId(keyStr);
                return Math.abs(tenantId.hashCode()) % numPartitions;
            }
        }
        
        // Default: hash-based
        return Math.abs(key.hashCode()) % numPartitions;
    }
}
```

---

## Question 156: How do you handle event processing failures?

### Answer

### Event Processing Failure Handling

#### 1. **Failure Classification**

```
┌─────────────────────────────────────────────────────────┐
│         Failure Classification                         │
└─────────────────────────────────────────────────────────┘

Transient Failures:
├─ Network timeouts
├─ Temporary service unavailability
├─ Database connection issues
└─ Retry with backoff

Permanent Failures:
├─ Invalid event data
├─ Business rule violations
├─ Data corruption
└─ Dead letter queue

Critical Failures:
├─ Position calculation errors
├─ Ledger entry failures
└─ Immediate alert
```

#### 2. **Retry Strategy**

```java
@Service
public class EventProcessor {
    private static final int MAX_RETRIES = 3;
    
    @KafkaListener(topics = "trade-events")
    public void handleEvent(TradeCreatedEvent event) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            try {
                processEvent(event);
                return; // Success
                
            } catch (TransientException e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    sendToDeadLetterQueue(event, e);
                    return;
                }
                
                // Exponential backoff
                long delay = (long) Math.pow(2, retryCount) * 1000;
                Thread.sleep(delay);
                
            } catch (PermanentException e) {
                sendToDeadLetterQueue(event, e);
                return;
            }
        }
    }
}
```

#### 3. **Dead Letter Queue**

```java
@Service
public class DeadLetterQueueService {
    private final KafkaTemplate<String, FailedEvent> kafkaTemplate;
    
    public void sendToDeadLetterQueue(Event event, Exception error) {
        FailedEvent failedEvent = FailedEvent.builder()
            .originalEvent(event)
            .errorMessage(error.getMessage())
            .errorType(error.getClass().getName())
            .timestamp(Instant.now())
            .retryCount(3)
            .build();
        
        kafkaTemplate.send("events-dlq", event.getEventId(), failedEvent);
        
        // Alert
        alertService.sendAlert("Event processing failed", failedEvent);
    }
}
```

---

## Question 157: What's the retry strategy for failed events?

### Answer

### Retry Strategy

#### 1. **Exponential Backoff**

```java
@Service
public class EventRetryService {
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_DELAY_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    
    public void retryEvent(Event event, int attemptNumber) {
        if (attemptNumber >= MAX_RETRIES) {
            sendToDeadLetterQueue(event);
            return;
        }
        
        // Calculate delay
        long delay = (long) (INITIAL_DELAY_MS * 
            Math.pow(BACKOFF_MULTIPLIER, attemptNumber));
        
        // Schedule retry
        scheduler.schedule(() -> {
            try {
                processEvent(event);
            } catch (Exception e) {
                retryEvent(event, attemptNumber + 1);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
```

#### 2. **Retry Timeline**

```
┌─────────────────────────────────────────────────────────┐
│         Retry Timeline                                  │
└─────────────────────────────────────────────────────────┘

Attempt 1: Immediate
    │
    ├─► Fails
    │
    ▼
Wait: 1 second

Attempt 2: After 1s
    │
    ├─► Fails
    │
    ▼
Wait: 2 seconds

Attempt 3: After 2s
    │
    ├─► Fails
    │
    ▼
Wait: 4 seconds

Attempt 4: After 4s
    │
    ├─► Fails
    │
    ▼
Wait: 8 seconds

Attempt 5: After 8s
    │
    └─► Success or DLQ
```

---

## Question 158: How do you ensure idempotency in event processing?

### Answer

### Idempotency Strategy

#### 1. **Idempotency Keys**

```java
@Service
public class IdempotentEventProcessor {
    private final RedisTemplate<String, String> redisTemplate;
    
    @KafkaListener(topics = "trade-events")
    public void handleEvent(TradeCreatedEvent event) {
        // Check idempotency
        String idempotencyKey = "event:processed:" + event.getEventId();
        
        Boolean alreadyProcessed = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, "processed", Duration.ofDays(7));
        
        if (!alreadyProcessed) {
            log.info("Event already processed: {}", event.getEventId());
            return;
        }
        
        // Process event
        processEvent(event);
    }
}
```

#### 2. **Version-Based Idempotency**

```java
@Service
public class VersionBasedEventProcessor {
    
    @KafkaListener(topics = "position-events")
    public void handleEvent(PositionUpdatedEvent event) {
        Position position = getPosition(
            event.getAccountId(), 
            event.getInstrumentId()
        );
        
        // Check version
        if (position.getVersion() >= event.getVersion()) {
            log.info("Event already applied: version {}", event.getVersion());
            return;
        }
        
        // Apply event
        applyEvent(position, event);
        position.setVersion(event.getVersion());
        savePosition(position);
    }
}
```

#### 3. **Idempotent Operations**

```java
@Service
public class IdempotentPositionService {
    
    public void updatePosition(TradeCreatedEvent event) {
        Position position = getPosition(
            event.getAccountId(), 
            event.getInstrumentId()
        );
        
        // Check if trade already applied
        if (position.getProcessedTrades().contains(event.getTradeId())) {
            return; // Already processed
        }
        
        // Apply trade (idempotent)
        PositionChange change = calculateChange(event);
        Position newPosition = position.apply(change);
        newPosition.getProcessedTrades().add(event.getTradeId());
        
        savePosition(newPosition);
    }
}
```

---

## Question 159: What's the exactly-once processing guarantee?

### Answer

### Exactly-Once Processing

#### 1. **Kafka Exactly-Once Semantics**

```
┌─────────────────────────────────────────────────────────┐
│         Exactly-Once Semantics                         │
└─────────────────────────────────────────────────────────┘

Kafka Features:
├─ Idempotent producer
├─ Transactional producer
├─ Read-committed isolation
└─ Exactly-once semantics

Configuration:
├─ enable.idempotence=true
├─ transactional.id=<unique-id>
└─ isolation.level=read_committed
```

#### 2. **Transactional Processing**

```java
@Configuration
public class KafkaTransactionConfig {
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "transactional-producer");
        
        return new DefaultKafkaProducerFactory<>(props);
    }
    
    @Bean
    public KafkaTransactionManager<String, Object> kafkaTransactionManager() {
        return new KafkaTransactionManager<>(producerFactory());
    }
}

@Service
public class TransactionalEventProcessor {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Transactional
    @KafkaListener(topics = "trade-events")
    public void handleEvent(TradeCreatedEvent event) {
        // Process event
        updatePosition(event);
        createLedgerEntry(event);
        
        // Emit new events (within transaction)
        kafkaTemplate.send("position-events", new PositionUpdatedEvent(event));
        kafkaTemplate.send("ledger-events", new LedgerEntryCreatedEvent(event));
        
        // All or nothing
    }
}
```

#### 3. **Idempotency + Transactions**

```java
@Service
public class ExactlyOnceProcessor {
    private final RedisTemplate<String, String> redisTemplate;
    
    @Transactional
    @KafkaListener(topics = "trade-events")
    public void handleEvent(TradeCreatedEvent event) {
        // Idempotency check
        String idempotencyKey = "event:processed:" + event.getEventId();
        Boolean alreadyProcessed = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, "processed", Duration.ofDays(7));
        
        if (!alreadyProcessed) {
            return; // Already processed
        }
        
        // Process event (idempotent operations)
        updatePosition(event);
        createLedgerEntry(event);
        
        // Transaction ensures atomicity
    }
}
```

---

## Question 160: How do you handle duplicate events?

### Answer

### Duplicate Event Handling

#### 1. **Deduplication**

```java
@Service
public class EventDeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isDuplicate(String eventId) {
        String key = "event:dedup:" + eventId;
        
        Boolean isNew = redisTemplate.opsForValue()
            .setIfAbsent(key, "processed", Duration.ofDays(7));
        
        return !isNew;
    }
    
    @KafkaListener(topics = "agent-events")
    public void handleEvent(AgentMatchedEvent event) {
        if (isDuplicate(event.getEventId())) {
            log.warn("Duplicate event: {}", event.getEventId());
            return;
        }
        
        processEvent(event);
    }
}
```

#### 2. **Version-Based Deduplication**

```java
@Service
public class VersionBasedDeduplication {
    
    @KafkaListener(topics = "position-events")
    public void handleEvent(PositionUpdatedEvent event) {
        Position position = getPosition(
            event.getAccountId(), 
            event.getInstrumentId()
        );
        
        // Check version
        if (position.getVersion() >= event.getVersion()) {
            log.info("Duplicate or old event: version {}", event.getVersion());
            return;
        }
        
        // Process event
        applyEvent(position, event);
    }
}
```

---

## Question 161: What's the dead letter queue strategy?

### Answer

### Dead Letter Queue Strategy

#### 1. **DLQ Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Dead Letter Queue Architecture                 │
└─────────────────────────────────────────────────────────┘

Event Processing:
├─ Process event
├─ Retry on failure
└─ Send to DLQ after max retries

DLQ Processing:
├─ Store failed events
├─ Alert operations
├─ Manual intervention
└─ Analysis and fix
```

#### 2. **DLQ Implementation**

```java
@Service
public class DeadLetterQueueService {
    private final KafkaTemplate<String, FailedEvent> kafkaTemplate;
    private final FailedEventRepository repository;
    
    public void sendToDeadLetterQueue(Event event, Exception error) {
        FailedEvent failedEvent = FailedEvent.builder()
            .originalEvent(event)
            .errorMessage(error.getMessage())
            .errorType(error.getClass().getName())
            .stackTrace(getStackTrace(error))
            .timestamp(Instant.now())
            .retryCount(3)
            .build();
        
        // Send to DLQ topic
        kafkaTemplate.send("events-dlq", event.getEventId(), failedEvent);
        
        // Store in database
        repository.save(failedEvent);
        
        // Alert
        alertService.sendCriticalAlert("Event failed", failedEvent);
    }
    
    @KafkaListener(topics = "events-dlq", groupId = "dlq-processor")
    public void processDeadLetterEvent(FailedEvent failedEvent) {
        // Log for analysis
        log.error("DLQ event: {}", failedEvent);
        
        // Store for manual review
        repository.save(failedEvent);
        
        // Attempt manual fix if possible
        if (canAutoFix(failedEvent)) {
            attemptAutoFix(failedEvent);
        }
    }
}
```

#### 3. **DLQ Monitoring**

```java
@Component
public class DLQMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorDLQ() {
        long dlqSize = getDLQSize();
        
        Gauge.builder("events.dlq.size")
            .register(meterRegistry)
            .set(dlqSize);
        
        if (dlqSize > 100) {
            alertService.sendAlert("High DLQ size", dlqSize);
        }
    }
}
```

---

## Question 162: How do you monitor event processing latency?

### Answer

### Latency Monitoring

#### 1. **Latency Metrics**

```java
@Component
public class EventProcessingMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordProcessingTime(String eventType, Duration duration) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("event.processing.duration")
            .tag("event.type", eventType)
            .register(meterRegistry));
    }
    
    public void recordConsumerLag(String topic, String partition, long lag) {
        Gauge.builder("kafka.consumer.lag")
            .tag("topic", topic)
            .tag("partition", partition)
            .register(meterRegistry)
            .set(lag);
    }
}
```

#### 2. **Latency Tracking**

```java
@Service
public class EventProcessor {
    private final EventProcessingMetrics metrics;
    
    @KafkaListener(topics = "trade-events")
    public void handleEvent(TradeCreatedEvent event) {
        Instant start = Instant.now();
        
        try {
            processEvent(event);
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            metrics.recordProcessingTime("TradeCreatedEvent", duration);
        }
    }
}
```

#### 3. **Alerting**

```java
@Component
public class LatencyMonitor {
    @Scheduled(fixedRate = 10000)
    public void checkLatency() {
        // Check P95 latency
        double p95Latency = getP95Latency();
        
        if (p95Latency > 1000) { // 1 second
            alertService.sendAlert("High event processing latency", p95Latency);
        }
        
        // Check consumer lag
        long maxLag = getMaxConsumerLag();
        if (maxLag > 10000) {
            alertService.sendAlert("High consumer lag", maxLag);
        }
    }
}
```

---

## Question 163: What happens if event processing falls behind?

### Answer

### Lag Handling Strategy

#### 1. **Lag Detection**

```java
@Component
public class ConsumerLagMonitor {
    private final KafkaConsumer<String, Object> consumer;
    
    @Scheduled(fixedRate = 10000)
    public void monitorLag() {
        Map<TopicPartition, Long> lag = getConsumerLag();
        
        lag.forEach((partition, lagValue) -> {
            if (lagValue > 10000) {
                handleHighLag(partition, lagValue);
            }
        });
    }
    
    private void handleHighLag(TopicPartition partition, long lag) {
        // Alert
        alertService.sendAlert("High consumer lag", 
            Map.of("partition", partition, "lag", lag));
        
        // Scale consumers
        if (lag > 50000) {
            scaleConsumers(partition.topic());
        }
    }
}
```

#### 2. **Scaling Strategy**

```java
@Service
public class ConsumerScalingService {
    
    public void scaleConsumers(String topic) {
        // Increase consumer instances
        int currentConsumers = getConsumerCount(topic);
        int targetConsumers = calculateTargetConsumers(topic);
        
        if (targetConsumers > currentConsumers) {
            scaleUpConsumers(topic, targetConsumers);
        }
    }
    
    private int calculateTargetConsumers(String topic) {
        long lag = getTotalLag(topic);
        long eventsPerSecond = getEventsPerSecond(topic);
        
        // Target: Process lag in 5 minutes
        long targetThroughput = lag / 300; // 5 minutes
        int consumersNeeded = (int) Math.ceil(targetThroughput / eventsPerSecond);
        
        return Math.max(consumersNeeded, getPartitionCount(topic));
    }
}
```

#### 3. **Catch-Up Strategy**

```java
@Service
public class EventCatchUpService {
    
    public void catchUp(String topic, String partition) {
        // Increase batch size
        increaseBatchSize(topic, partition);
        
        // Process faster
        reduceProcessingTime(topic, partition);
        
        // Parallel processing
        enableParallelProcessing(topic, partition);
    }
}
```

---

## Summary

Event Processing covers:

1. **Event Ordering**: Partition-based with sequence numbers
2. **Partitioning**: Hash-based with custom strategies
3. **Failure Handling**: Classification, retry, DLQ
4. **Retry Strategy**: Exponential backoff with max retries
5. **Idempotency**: Keys, versions, and idempotent operations
6. **Exactly-Once**: Transactions and idempotency
7. **Duplicate Handling**: Deduplication and version checks
8. **Dead Letter Queue**: Storage, alerting, and processing
9. **Latency Monitoring**: Metrics, tracking, and alerting
10. **Lag Handling**: Detection, scaling, and catch-up strategies
