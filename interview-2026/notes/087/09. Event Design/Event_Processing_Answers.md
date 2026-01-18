# Event Processing - Detailed Answers

## Question 154: How do you ensure event ordering?

### Answer

### Event Ordering Strategy

#### 1. **Kafka Partitioning for Ordering**

```
┌─────────────────────────────────────────────────────────┐
│         Event Ordering with Kafka Partitioning         │
└─────────────────────────────────────────────────────────┘

Key Principle:
├─ Events with same partition key → Same partition
├─ Single consumer per partition
├─ Sequential processing within partition
└─ Guaranteed ordering per partition

Partition Key Strategy:
├─ Agent Events: Partition by agentId
├─ Conversation Events: Partition by conversationId
├─ Trade Events: Partition by accountId
└─ Position Events: Partition by accountId:instrumentId
```

**Implementation:**

```java
@Service
public class EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishAgentEvent(String agentId, AgentEvent event) {
        // Partition key = agentId ensures all events for same agent
        // go to same partition and are processed in order
        kafkaTemplate.send("agent-events", agentId, event);
    }
    
    public void publishConversationEvent(String conversationId, ConversationEvent event) {
        // Partition key = conversationId ensures all events for same conversation
        // go to same partition and are processed in order
        kafkaTemplate.send("conversation-events", conversationId, event);
    }
    
    public void publishTradeEvent(String accountId, TradeEvent event) {
        // Partition key = accountId ensures all trades for same account
        // are processed in order (critical for position calculations)
        kafkaTemplate.send("trade-events", accountId, event);
    }
}
```

#### 2. **Consumer Configuration**

```java
@KafkaListener(
    topics = "agent-events",
    groupId = "agent-match-service",
    concurrency = "10" // 10 consumers for 10 partitions
)
public void handleAgentEvent(AgentEvent event) {
    // Events are processed sequentially per partition
    // Multiple partitions processed in parallel
    processAgentEvent(event);
}

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "agent-match-service");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Enable auto-commit for simplicity (or manual commit for exactly-once)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        
        // Process one message at a time per partition (max.poll.records = 1)
        // for strict ordering
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
```

#### 3. **Sequence Numbers for Validation**

```java
public class TradeEvent {
    private String tradeId;
    private String accountId;
    private Long sequenceNumber; // Monotonically increasing per account
    private Instant timestamp;
    // ... other fields
}

@Service
public class TradeEventProcessor {
    private final Map<String, Long> lastProcessedSequence = new ConcurrentHashMap<>();
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        String accountId = event.getAccountId();
        Long currentSequence = event.getSequenceNumber();
        Long lastSequence = lastProcessedSequence.getOrDefault(accountId, -1L);
        
        // Validate ordering
        if (currentSequence <= lastSequence) {
            log.warn("Out-of-order event detected: accountId={}, current={}, last={}",
                accountId, currentSequence, lastSequence);
            
            // Option 1: Reject and wait for correct order
            // Option 2: Buffer and reorder
            // Option 3: Accept if within tolerance window
            handleOutOfOrderEvent(event, lastSequence);
            return;
        }
        
        // Process event
        processTradeEvent(event);
        
        // Update last processed sequence
        lastProcessedSequence.put(accountId, currentSequence);
    }
    
    private void handleOutOfOrderEvent(TradeEvent event, Long lastSequence) {
        // Buffer events that arrive out of order
        String accountId = event.getAccountId();
        eventBuffer.computeIfAbsent(accountId, k -> new TreeMap<>())
            .put(event.getSequenceNumber(), event);
        
        // Process buffered events in order
        processBufferedEvents(accountId, lastSequence);
    }
}
```

#### 4. **Ordering Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Ordering Guarantees                            │
└─────────────────────────────────────────────────────────┘

Per-Partition Ordering:
├─ Events in same partition processed in order
├─ Single consumer per partition
└─ Guaranteed FIFO within partition

Cross-Partition Ordering:
├─ No guarantee across partitions
├─ Parallel processing
└─ Acceptable for independent entities

Example:
├─ Agent-123 events: Ordered (same partition)
├─ Agent-456 events: Ordered (same partition)
└─ Agent-123 vs Agent-456: Not ordered (different partitions)
```

---

## Question 155: What's the partitioning strategy for events?

### Answer

### Partitioning Strategy

#### 1. **Partition Key Selection**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Key Strategy                         │
└─────────────────────────────────────────────────────────┘

Criteria for Partition Key:
├─ Must ensure ordering for related events
├─ Should distribute load evenly
├─ Should align with business logic
└─ Should prevent hot partitions

Partition Key Examples:
├─ Agent Events: agentId
├─ Conversation Events: conversationId
├─ Trade Events: accountId
├─ Position Events: accountId:instrumentId
└─ Message Events: conversationId
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
     * - Consumer parallelism: Number of consumers
     * - Ordering requirements: Per entity ordering
     * - Future growth: 2-3x current load
     */
    public int calculatePartitionCount(String topic, int expectedThroughput) {
        // Target: 1 partition per 10K events/second
        int basePartitions = expectedThroughput / 10_000;
        
        // Round up to nearest power of 2 for better distribution
        int partitions = (int) Math.pow(2, Math.ceil(Math.log(basePartitions) / Math.log(2)));
        
        // Minimum 3 partitions for replication
        // Maximum 100 partitions (Kafka recommendation)
        return Math.max(3, Math.min(100, partitions));
    }
    
    /**
     * Example calculations:
     * - 50K events/sec → 5 base → 8 partitions
     * - 200K events/sec → 20 base → 32 partitions
     * - 1M events/sec → 100 base → 100 partitions (max)
     */
}
```

#### 3. **Partition Distribution**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Distribution Example                 │
└─────────────────────────────────────────────────────────┘

Topic: trade-events
Partitions: 10

Partition Key: accountId
Hash Function: hash(accountId) % 10

Distribution:
├─ account-001 → Partition 3
├─ account-002 → Partition 7
├─ account-003 → Partition 1
└─ account-004 → Partition 3 (same as account-001)

Result:
├─ All events for account-001 → Partition 3 (ordered)
├─ All events for account-002 → Partition 7 (ordered)
└─ Events for different accounts → Different partitions (parallel)
```

#### 4. **Hot Partition Prevention**

```java
@Service
public class PartitionBalancer {
    /**
     * Prevent hot partitions by:
     * 1. Using composite keys for high-volume entities
     * 2. Adding random component for load distribution
     * 3. Monitoring partition lag
     */
    public String generatePartitionKey(String entityId, boolean highVolume) {
        if (highVolume) {
            // Add random component for high-volume entities
            int randomComponent = ThreadLocalRandom.current().nextInt(0, 100);
            return entityId + ":" + randomComponent;
        } else {
            return entityId;
        }
    }
    
    /**
     * Monitor partition lag and rebalance if needed
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorPartitionLag() {
        Map<Integer, Long> lagByPartition = getPartitionLag();
        
        long maxLag = lagByPartition.values().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0);
        
        long minLag = lagByPartition.values().stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(0);
        
        // Alert if lag difference > 50%
        if (maxLag > 0 && (maxLag - minLag) / maxLag > 0.5) {
            alertService.sendAlert("Partition lag imbalance detected");
        }
    }
}
```

---

## Question 156: How do you handle event processing failures?

### Answer

### Event Processing Failure Handling

#### 1. **Failure Types and Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Failure Handling Strategy                      │
└─────────────────────────────────────────────────────────┘

Transient Failures:
├─ Network timeouts
├─ Temporary service unavailability
├─ Rate limiting
└─ Strategy: Retry with backoff

Permanent Failures:
├─ Invalid event data
├─ Business rule violations
├─ Data corruption
└─ Strategy: Dead letter queue

Partial Failures:
├─ Some events succeed, some fail
├─ Batch processing failures
└─ Strategy: Idempotent processing + retry
```

#### 2. **Retry Strategy**

```java
@Service
public class EventProcessor {
    private final RetryTemplate retryTemplate;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        retryTemplate.execute(context -> {
            try {
                processTradeEvent(event);
                return null;
            } catch (TransientException e) {
                // Retry with exponential backoff
                throw e;
            } catch (PermanentException e) {
                // Don't retry, send to DLQ
                sendToDeadLetterQueue(event, e);
                return null;
            }
        });
    }
    
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Exponential backoff: 1s, 2s, 4s, 8s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Retry up to 3 times
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
}
```

#### 3. **Dead Letter Queue (DLQ)**

```java
@Service
public class DeadLetterQueueService {
    private final KafkaTemplate<String, FailedEvent> kafkaTemplate;
    
    public void sendToDeadLetterQueue(Object event, Exception error) {
        FailedEvent failedEvent = FailedEvent.builder()
            .originalEvent(event)
            .errorMessage(error.getMessage())
            .errorType(error.getClass().getName())
            .stackTrace(getStackTrace(error))
            .timestamp(Instant.now())
            .retryCount(getRetryCount(event))
            .build();
        
        // Send to DLQ topic
        kafkaTemplate.send("dlq-events", failedEvent);
        
        // Also store in database for manual review
        failedEventRepository.save(failedEvent);
    }
    
    /**
     * Process DLQ events manually or with special handling
     */
    @KafkaListener(topics = "dlq-events", groupId = "dlq-processor")
    public void handleDeadLetterEvent(FailedEvent failedEvent) {
        // Log for manual review
        log.error("DLQ Event: {}", failedEvent);
        
        // Attempt to fix and reprocess
        if (canAutoFix(failedEvent)) {
            Object fixedEvent = fixEvent(failedEvent);
            reprocessEvent(fixedEvent);
        }
    }
}
```

#### 4. **Idempotent Processing**

```java
@Service
public class IdempotentEventProcessor {
    private final RedisTemplate<String, String> redisTemplate;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        String idempotencyKey = "event:processed:" + event.getEventId();
        
        // Check if already processed
        String processed = redisTemplate.opsForValue().get(idempotencyKey);
        if (processed != null) {
            log.info("Event already processed: {}", event.getEventId());
            return; // Idempotent: skip if already processed
        }
        
        try {
            // Process event
            processTradeEvent(event);
            
            // Mark as processed (TTL: 7 days)
            redisTemplate.opsForValue().set(
                idempotencyKey, 
                "processed", 
                Duration.ofDays(7)
            );
            
        } catch (Exception e) {
            // On failure, don't mark as processed
            // Will retry on next consumption
            throw e;
        }
    }
}
```

#### 5. **Error Handling Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Error Handling Flow                            │
└─────────────────────────────────────────────────────────┘

Event Received
    │
    ▼
Process Event
    │
    ├─► Success → Commit offset → Done
    │
    └─► Failure
        │
        ├─► Transient Error?
        │   │
        │   ├─► Yes → Retry (exponential backoff)
        │   │   │
        │   │   ├─► Success → Commit → Done
        │   │   │
        │   │   └─► Still Fails → Max Retries?
        │   │       │
        │   │       ├─► Yes → DLQ
        │   │       │
        │   │       └─► No → Retry again
        │   │
        │   └─► No → Permanent Error → DLQ
        │
        └─► DLQ → Manual Review → Fix & Reprocess
```

---

## Question 157: What's the retry strategy for failed events?

### Answer

### Retry Strategy

#### 1. **Retry Configuration**

```java
@Configuration
public class EventRetryConfig {
    @Bean
    public RetryTemplate eventRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Retry Policy: Max 3 attempts
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // Backoff Policy: Exponential backoff
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 second
        backOffPolicy.setMultiplier(2.0); // Double each time
        backOffPolicy.setMaxInterval(10000); // Max 10 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
}
```

#### 2. **Retry Timeline**

```
┌─────────────────────────────────────────────────────────┐
│         Retry Timeline                                 │
└─────────────────────────────────────────────────────────┘

Attempt 1: t=0s
    │
    ├─► Fails
    │
    ▼
Wait: 1 second

Attempt 2: t=1s
    │
    ├─► Fails
    │
    ▼
Wait: 2 seconds (1 * 2.0)

Attempt 3: t=3s
    │
    ├─► Fails
    │
    ▼
Wait: 4 seconds (2 * 2.0, but max is 10s)

Attempt 4: t=7s
    │
    └─► Success or Final Failure → DLQ
```

#### 3. **Selective Retry**

```java
@Service
public class SelectiveRetryProcessor {
    private final RetryTemplate retryTemplate;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        retryTemplate.execute(context -> {
            try {
                processTradeEvent(event);
                return null;
                
            } catch (TransientException e) {
                // Retry transient errors
                log.warn("Transient error, will retry: {}", e.getMessage());
                throw e;
                
            } catch (ValidationException e) {
                // Don't retry validation errors
                log.error("Validation error, sending to DLQ: {}", e.getMessage());
                sendToDeadLetterQueue(event, e);
                return null;
                
            } catch (BusinessRuleException e) {
                // Don't retry business rule violations
                log.error("Business rule violation, sending to DLQ: {}", e.getMessage());
                sendToDeadLetterQueue(event, e);
                return null;
            }
        });
    }
    
    private boolean isTransientException(Exception e) {
        return e instanceof TimeoutException ||
               e instanceof ConnectException ||
               e instanceof SocketTimeoutException ||
               (e instanceof HttpServerErrorException && 
                ((HttpServerErrorException) e).getStatusCode().is5xxServerError());
    }
}
```

#### 4. **Circuit Breaker Integration**

```java
@Service
public class ResilientEventProcessor {
    private final CircuitBreaker circuitBreaker;
    private final RetryTemplate retryTemplate;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        // Check circuit breaker first
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            // Circuit is open, send to DLQ immediately
            sendToDeadLetterQueue(event, new CircuitBreakerOpenException());
            return;
        }
        
        // Process with retry
        retryTemplate.execute(context -> {
            return circuitBreaker.executeSupplier(() -> {
                processTradeEvent(event);
                return null;
            });
        });
    }
    
    @Bean
    public CircuitBreaker circuitBreaker() {
        return CircuitBreaker.of("event-processor", CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open after 50% failures
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before half-open
            .slidingWindowSize(10) // Last 10 calls
            .build());
    }
}
```

---

## Question 158: How do you ensure idempotency in event processing?

### Answer

### Idempotency Strategy

#### 1. **Idempotency Key**

```java
public class TradeEvent {
    private String eventId; // Unique event ID
    private String tradeId;
    private String accountId;
    private Long sequenceNumber;
    // ... other fields
}

@Service
public class IdempotentEventProcessor {
    private final RedisTemplate<String, String> redisTemplate;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        String idempotencyKey = "event:processed:" + event.getEventId();
        
        // Check if already processed
        String processed = redisTemplate.opsForValue().get(idempotencyKey);
        if (processed != null) {
            log.info("Event already processed, skipping: {}", event.getEventId());
            return; // Idempotent: return same result
        }
        
        try {
            // Process event
            processTradeEvent(event);
            
            // Mark as processed
            redisTemplate.opsForValue().set(
                idempotencyKey,
                "processed",
                Duration.ofDays(7) // Keep for 7 days
            );
            
        } catch (Exception e) {
            // On failure, don't mark as processed
            // Will retry on next consumption
            log.error("Event processing failed: {}", event.getEventId(), e);
            throw e;
        }
    }
}
```

#### 2. **Idempotent Operations**

```java
@Service
public class PositionService {
    /**
     * Idempotent position update:
     * - Same event processed multiple times → Same result
     * - Uses event ID to track processed events
     */
    @Transactional
    public void updatePosition(TradeEvent event) {
        // Check if this event was already applied
        String eventId = event.getEventId();
        PositionEventLog existingLog = positionEventLogRepository
            .findByEventId(eventId);
        
        if (existingLog != null) {
            // Already processed, return existing result
            log.info("Position update already applied for event: {}", eventId);
            return;
        }
        
        // Calculate position change
        PositionChange change = calculatePositionChange(event);
        
        // Update position
        Position position = getCurrentPosition(event.getAccountId(), event.getInstrumentId());
        Position newPosition = position.apply(change);
        positionRepository.save(newPosition);
        
        // Log event as processed
        PositionEventLog log = new PositionEventLog();
        log.setEventId(eventId);
        log.setTradeId(event.getTradeId());
        log.setTimestamp(Instant.now());
        positionEventLogRepository.save(log);
    }
}
```

#### 3. **Idempotency with Database**

```java
@Entity
@Table(name = "processed_events", 
       uniqueConstraints = @UniqueConstraint(columnNames = "event_id"))
public class ProcessedEvent {
    @Id
    private String eventId;
    private Instant processedAt;
    private String eventType;
    private String result; // Success/Failure
}

@Service
public class DatabaseIdempotencyService {
    private final ProcessedEventRepository repository;
    
    public boolean isProcessed(String eventId) {
        return repository.existsById(eventId);
    }
    
    @Transactional
    public void markAsProcessed(String eventId, String eventType, String result) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(eventId);
        processedEvent.setEventType(eventType);
        processedEvent.setResult(result);
        processedEvent.setProcessedAt(Instant.now());
        
        try {
            repository.save(processedEvent);
        } catch (DataIntegrityViolationException e) {
            // Already exists, idempotent
            log.debug("Event already marked as processed: {}", eventId);
        }
    }
}
```

---

## Question 159: What's the exactly-once processing guarantee?

### Answer

### Exactly-Once Processing

#### 1. **Processing Semantics**

```
┌─────────────────────────────────────────────────────────┐
│         Processing Semantics                           │
└─────────────────────────────────────────────────────────┘

At-Most-Once:
├─ Event may be lost
├─ No duplicates
├─ Simple implementation
└─ Use case: Non-critical events

At-Least-Once:
├─ Event may be processed multiple times
├─ No loss
├─ Requires idempotency
└─ Use case: Most common, with idempotent handlers

Exactly-Once:
├─ Event processed exactly once
├─ No loss, no duplicates
├─ Complex implementation
└─ Use case: Financial systems, critical operations
```

#### 2. **Kafka Exactly-Once Configuration**

```java
@Configuration
public class ExactlyOnceKafkaConfig {
    @Bean
    public ConsumerFactory<String, Object> exactlyOnceConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "position-service");
        
        // Enable idempotent producer
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        
        // Transactional producer
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "position-service-producer");
        
        // Consumer isolation level
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        
        // Disable auto-commit, manual commit
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public KafkaTransactionManager<String, Object> kafkaTransactionManager() {
        return new KafkaTransactionManager<>(producerFactory());
    }
}
```

#### 3. **Transactional Processing**

```java
@Service
public class ExactlyOnceEventProcessor {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PositionRepository positionRepository;
    
    @Transactional(transactionManager = "kafkaTransactionManager")
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        // 1. Process event (update position)
        updatePosition(event);
        
        // 2. Emit result event (in same transaction)
        PositionUpdatedEvent resultEvent = new PositionUpdatedEvent(event);
        kafkaTemplate.send("position-events", resultEvent);
        
        // 3. Commit offset (automatic with @Transactional)
        // If any step fails, entire transaction rolls back
    }
}
```

#### 4. **Idempotency + Transactions**

```java
@Service
public class ExactlyOnceWithIdempotency {
    private final RedisTemplate<String, String> redisTemplate;
    private final PositionRepository positionRepository;
    
    @Transactional
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        String idempotencyKey = "event:processed:" + event.getEventId();
        
        // Check idempotency (in transaction)
        String processed = redisTemplate.opsForValue().get(idempotencyKey);
        if (processed != null) {
            return; // Already processed
        }
        
        try {
            // Process event
            updatePosition(event);
            
            // Mark as processed (in same transaction)
            redisTemplate.opsForValue().set(idempotencyKey, "processed");
            
            // Commit transaction
            // If commit fails, retry will see unprocessed event
            
        } catch (Exception e) {
            // Rollback transaction
            // Event not marked as processed, will retry
            throw e;
        }
    }
}
```

---

## Question 160: How do you handle duplicate events?

### Answer

### Duplicate Event Handling

#### 1. **Duplicate Detection**

```java
@Service
public class DuplicateEventDetector {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isDuplicate(String eventId) {
        String key = "event:seen:" + eventId;
        Boolean exists = redisTemplate.hasKey(key);
        
        if (exists != null && exists) {
            return true; // Duplicate
        }
        
        // Mark as seen (TTL: 7 days)
        redisTemplate.opsForValue().set(key, "seen", Duration.ofDays(7));
        return false; // Not duplicate
    }
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        if (isDuplicate(event.getEventId())) {
            log.warn("Duplicate event detected, skipping: {}", event.getEventId());
            return; // Skip duplicate
        }
        
        processTradeEvent(event);
    }
}
```

#### 2. **Deduplication Window**

```java
@Service
public class TimeWindowDeduplication {
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration DEDUP_WINDOW = Duration.ofHours(24);
    
    public boolean isDuplicate(String eventId, Instant eventTimestamp) {
        String key = "event:dedup:" + eventId;
        
        // Check if seen
        String seenTimestamp = redisTemplate.opsForValue().get(key);
        if (seenTimestamp != null) {
            Instant seen = Instant.parse(seenTimestamp);
            if (eventTimestamp.isAfter(seen.minus(DEDUP_WINDOW))) {
                return true; // Duplicate within window
            }
        }
        
        // Mark as seen
        redisTemplate.opsForValue().set(
            key, 
            eventTimestamp.toString(), 
            DEDUP_WINDOW
        );
        
        return false;
    }
}
```

#### 3. **Bloom Filter for Deduplication**

```java
@Service
public class BloomFilterDeduplication {
    private final RedisTemplate<String, byte[]> redisTemplate;
    private final BloomFilter<String> bloomFilter;
    
    @PostConstruct
    public void init() {
        // Initialize Bloom filter with expected 10M events
        // False positive rate: 0.01 (1%)
        this.bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(Charset.defaultCharset()),
            10_000_000,
            0.01
        );
    }
    
    public boolean mightBeDuplicate(String eventId) {
        // Bloom filter: fast check, may have false positives
        if (!bloomFilter.mightContain(eventId)) {
            bloomFilter.put(eventId);
            return false; // Definitely not duplicate
        }
        
        // Might be duplicate, check Redis for confirmation
        return redisTemplate.hasKey("event:processed:" + eventId);
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

Event Processing Flow:
1. Event received from main topic
2. Processing attempted
3. On failure after max retries:
   ├─ Send to DLQ topic
   ├─ Store in database
   └─ Alert operations team

DLQ Processing:
1. Monitor DLQ topic
2. Analyze failures
3. Fix and reprocess
4. Or mark as permanently failed
```

#### 2. **DLQ Implementation**

```java
@Service
public class DeadLetterQueueService {
    private final KafkaTemplate<String, FailedEvent> kafkaTemplate;
    private final FailedEventRepository repository;
    
    public void sendToDeadLetterQueue(Object event, Exception error, int retryCount) {
        FailedEvent failedEvent = FailedEvent.builder()
            .eventId(generateEventId(event))
            .originalEvent(serialize(event))
            .errorMessage(error.getMessage())
            .errorType(error.getClass().getName())
            .stackTrace(getStackTrace(error))
            .retryCount(retryCount)
            .timestamp(Instant.now())
            .status(FailedEventStatus.PENDING)
            .build();
        
        // Send to DLQ topic
        kafkaTemplate.send("dlq-events", failedEvent);
        
        // Store in database for persistence
        repository.save(failedEvent);
        
        // Alert operations
        alertService.sendAlert("Event sent to DLQ: " + failedEvent.getEventId());
    }
}
```

#### 3. **DLQ Processing**

```java
@Service
public class DeadLetterQueueProcessor {
    @KafkaListener(topics = "dlq-events", groupId = "dlq-processor")
    public void handleDeadLetterEvent(FailedEvent failedEvent) {
        log.error("DLQ Event received: {}", failedEvent);
        
        // Analyze failure
        FailureAnalysis analysis = analyzeFailure(failedEvent);
        
        if (analysis.isAutoFixable()) {
            // Attempt auto-fix
            Object fixedEvent = autoFixEvent(failedEvent);
            reprocessEvent(fixedEvent);
        } else {
            // Manual review required
            notifyManualReview(failedEvent, analysis);
        }
    }
    
    private Object autoFixEvent(FailedEvent failedEvent) {
        Object originalEvent = deserialize(failedEvent.getOriginalEvent());
        
        // Example: Fix missing fields
        if (failedEvent.getErrorType().contains("NullPointerException")) {
            return fixNullFields(originalEvent);
        }
        
        // Example: Retry with different configuration
        if (failedEvent.getErrorType().contains("TimeoutException")) {
            return retryWithLongerTimeout(originalEvent);
        }
        
        return originalEvent;
    }
}
```

---

## Question 162: How do you monitor event processing latency?

### Answer

### Event Processing Latency Monitoring

#### 1. **Latency Metrics**

```java
@Component
public class EventProcessingMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordProcessingLatency(String eventType, Duration latency) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("event.processing.latency")
            .tag("event.type", eventType)
            .register(meterRegistry));
    }
    
    public void recordEndToEndLatency(String eventId, Instant eventTime) {
        Duration latency = Duration.between(eventTime, Instant.now());
        Timer.builder("event.end.to.end.latency")
            .register(meterRegistry)
            .record(latency);
    }
}
```

#### 2. **Latency Tracking**

```java
@Service
public class LatencyTrackingProcessor {
    private final EventProcessingMetrics metrics;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeEvent event) {
        Instant startTime = Instant.now();
        
        try {
            processTradeEvent(event);
            
            // Record processing latency
            Duration processingLatency = Duration.between(startTime, Instant.now());
            metrics.recordProcessingLatency("trade-event", processingLatency);
            
            // Record end-to-end latency (from event creation)
            metrics.recordEndToEndLatency(event.getEventId(), event.getTimestamp());
            
        } catch (Exception e) {
            // Record failure latency
            Duration failureLatency = Duration.between(startTime, Instant.now());
            metrics.recordProcessingLatency("trade-event-failed", failureLatency);
            throw e;
        }
    }
}
```

#### 3. **Latency Dashboard**

```
┌─────────────────────────────────────────────────────────┐
│         Latency Metrics Dashboard                      │
└─────────────────────────────────────────────────────────┘

Event Processing Latency:
├─ P50: 50ms
├─ P95: 200ms
├─ P99: 500ms
└─ Max: 2s

End-to-End Latency:
├─ P50: 100ms
├─ P95: 300ms
├─ P99: 1s
└─ Max: 5s

Partition Lag:
├─ Average: 100 events
├─ Max: 1000 events
└─ Alert threshold: 5000 events
```

---

## Question 163: What happens if event processing falls behind?

### Answer

### Handling Processing Lag

#### 1. **Lag Detection**

```java
@Service
public class LagMonitor {
    private final KafkaConsumer<String, Object> consumer;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorLag() {
        Map<TopicPartition, Long> lagByPartition = getConsumerLag();
        
        for (Map.Entry<TopicPartition, Long> entry : lagByPartition.entrySet()) {
            Long lag = entry.getValue();
            
            if (lag > 1000) {
                // High lag detected
                alertService.sendAlert(
                    String.format("High lag on partition %s: %d events", 
                        entry.getKey(), lag));
            }
            
            if (lag > 10000) {
                // Critical lag
                alertService.sendCriticalAlert(
                    String.format("Critical lag on partition %s: %d events", 
                        entry.getKey(), lag));
                
                // Scale up consumers
                scaleUpConsumers(entry.getKey());
            }
        }
    }
    
    private Map<TopicPartition, Long> getConsumerLag() {
        Map<TopicPartition, Long> lag = new HashMap<>();
        
        for (TopicPartition partition : consumer.assignment()) {
            long endOffset = consumer.endOffsets(Collections.singleton(partition))
                .get(partition);
            long currentOffset = consumer.position(partition);
            lag.put(partition, endOffset - currentOffset);
        }
        
        return lag;
    }
}
```

#### 2. **Auto-Scaling Consumers**

```java
@Service
public class ConsumerAutoScaler {
    private final KafkaListenerEndpointRegistry registry;
    
    public void scaleUpConsumers(TopicPartition partition) {
        // Increase consumer concurrency
        int currentConcurrency = getCurrentConcurrency();
        int newConcurrency = Math.min(currentConcurrency * 2, getMaxConcurrency());
        
        if (newConcurrency > currentConcurrency) {
            updateConsumerConcurrency(newConcurrency);
            log.info("Scaled up consumers from {} to {}", 
                currentConcurrency, newConcurrency);
        }
    }
    
    public void scaleDownConsumers() {
        Map<TopicPartition, Long> lag = getConsumerLag();
        long maxLag = lag.values().stream().mapToLong(Long::longValue).max().orElse(0);
        
        if (maxLag < 100) {
            // Low lag, can scale down
            int currentConcurrency = getCurrentConcurrency();
            int newConcurrency = Math.max(currentConcurrency / 2, getMinConcurrency());
            
            if (newConcurrency < currentConcurrency) {
                updateConsumerConcurrency(newConcurrency);
                log.info("Scaled down consumers from {} to {}", 
                    currentConcurrency, newConcurrency);
            }
        }
    }
}
```

#### 3. **Batch Processing for Catch-up**

```java
@Service
public class BatchEventProcessor {
    @KafkaListener(
        topics = "trade-events",
        groupId = "position-service",
        containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void handleBatchTradeEvents(List<TradeEvent> events) {
        // Process batch for efficiency
        List<TradeEvent> processedEvents = new ArrayList<>();
        
        for (TradeEvent event : events) {
            try {
                processTradeEvent(event);
                processedEvents.add(event);
            } catch (Exception e) {
                log.error("Failed to process event in batch: {}", event.getEventId(), e);
                // Continue with other events
            }
        }
        
        // Commit only processed events
        // Failed events will be retried
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> 
        batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true); // Enable batch processing
        factory.setConcurrency(10); // 10 consumers
        return factory;
    }
}
```

---

## Summary

Event Processing answers cover:

1. **Event Ordering**: Kafka partitioning ensures ordering per entity
2. **Partitioning Strategy**: Key selection and distribution
3. **Failure Handling**: Retry, DLQ, and error classification
4. **Retry Strategy**: Exponential backoff with max attempts
5. **Idempotency**: Event ID tracking and idempotent operations
6. **Exactly-Once**: Transactional processing with idempotency
7. **Duplicate Handling**: Deduplication with Redis and Bloom filters
8. **Dead Letter Queue**: Failed event handling and reprocessing
9. **Latency Monitoring**: Metrics and dashboards
10. **Lag Handling**: Auto-scaling and batch processing
