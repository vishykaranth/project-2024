# Event-Driven Architecture Part 2: Project 2 - Prime Broker System

## Question 107: Explain the Kafka event bus for the Prime Broker System.

### Answer

### Prime Broker Event Bus Architecture

#### 1. **Event Bus Overview**

```
┌─────────────────────────────────────────────────────────┐
│                    Kafka Cluster                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Broker 1    │  │  Broker 2    │  │  Broker 3    │  │
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

#### 2. **Event Topics**

```
┌─────────────────────────────────────────────────────────┐
│         Event Topics (5 Topics)                         │
└─────────────────────────────────────────────────────────┘

1. trade-events
   ├─ Partitions: 20
   ├─ Replication: 3
   ├─ Retention: 30 days
   └─ Events: Trade created, updated, cancelled

2. position-events
   ├─ Partitions: 20
   ├─ Replication: 3
   ├─ Retention: 30 days
   └─ Events: Position changes, updates

3. ledger-events
   ├─ Partitions: 10
   ├─ Replication: 3
   ├─ Retention: 7 years (compliance)
   └─ Events: Ledger entries created

4. settlement-events
   ├─ Partitions: 10
   ├─ Replication: 3
   ├─ Retention: 7 years
   └─ Events: Settlement status changes

5. instrument-events
   ├─ Partitions: 5
   ├─ Replication: 3
   ├─ Retention: 30 days
   └─ Events: Instrument updates, price changes
```

#### 3. **Event Schema**

```java
// Trade Created Event
public class TradeCreatedEvent {
    private String tradeId;
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal price;
    private String currency;
    private TradeType type; // BUY, SELL
    private Instant timestamp;
    private String idempotencyKey;
    private Long sequenceNumber; // For ordering
}

// Position Updated Event
public class PositionUpdatedEvent {
    private String accountId;
    private String instrumentId;
    private PositionChange change;
    private Position newPosition;
    private Instant timestamp;
    private String tradeId; // Source trade
    private Long sequenceNumber;
}

// Ledger Entry Created Event
public class LedgerEntryCreatedEvent {
    private String ledgerEntryId;
    private String tradeId;
    private LedgerEntry debitEntry;
    private LedgerEntry creditEntry;
    private Instant timestamp;
    private Long sequenceNumber;
}
```

---

## Question 108: How do you ensure event ordering for trade processing?

### Answer

### Event Ordering Strategy

#### 1. **Partitioning by Account ID**

```java
@Service
public class TradeEventPublisher {
    private final KafkaTemplate<String, TradeCreatedEvent> kafkaTemplate;
    
    public void publishTradeCreatedEvent(TradeCreatedEvent event) {
        // Partition key = accountId ensures all trades for same account
        // go to same partition and maintain order
        String partitionKey = event.getAccountId();
        
        kafkaTemplate.send("trade-events", partitionKey, event);
    }
}
```

#### 2. **Sequence Numbers**

```java
@Service
public class TradeEventProcessor {
    private final RedisTemplate<String, Long> redisTemplate;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeCreatedEvent event) {
        String accountId = event.getAccountId();
        String sequenceKey = "sequence:account:" + accountId;
        
        // Get expected sequence number
        Long expectedSequence = redisTemplate.opsForValue()
            .get(sequenceKey);
        
        if (expectedSequence == null) {
            expectedSequence = 0L;
        }
        
        // Check sequence number
        if (event.getSequenceNumber() < expectedSequence) {
            // Out of order or duplicate
            log.warn("Out of order event: expected {}, got {}", 
                expectedSequence, event.getSequenceNumber());
            return;
        }
        
        if (event.getSequenceNumber() > expectedSequence) {
            // Missing events, wait or request replay
            log.error("Missing events: expected {}, got {}", 
                expectedSequence, event.getSequenceNumber());
            requestEventReplay(accountId, expectedSequence, event.getSequenceNumber());
            return;
        }
        
        // Process event
        processTradeEvent(event);
        
        // Update sequence number
        redisTemplate.opsForValue().set(
            sequenceKey, 
            expectedSequence + 1,
            Duration.ofDays(7)
        );
    }
}
```

#### 3. **Single Consumer Per Partition**

```java
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TradeCreatedEvent> 
        tradeEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TradeCreatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(tradeEventConsumerFactory());
        
        // One thread per partition ensures ordering
        factory.setConcurrency(20); // Match partition count
        
        return factory;
    }
}
```

#### 4. **Ordering Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Ordering Guarantees                            │
└─────────────────────────────────────────────────────────┘

Per-Account Ordering:
├─ All trades for account-123 → same partition
├─ Processed sequentially
└─ Position calculations correct

Cross-Account Ordering:
├─ Trades for different accounts → different partitions
├─ Processed in parallel
└─ No ordering guarantee (not needed)
```

---

## Question 109: What's the partitioning strategy for trade events?

### Answer

### Trade Event Partitioning

#### 1. **Partition Key Strategy**

```java
@Service
public class TradeEventPublisher {
    private final KafkaTemplate<String, TradeCreatedEvent> kafkaTemplate;
    
    public void publishTradeEvent(TradeCreatedEvent event) {
        // Partition key = accountId
        // Ensures all trades for same account in same partition
        String partitionKey = event.getAccountId();
        
        kafkaTemplate.send("trade-events", partitionKey, event);
    }
}
```

#### 2. **Partition Count Calculation**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Count Calculation                    │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ 1M trades/day
├─ Peak: 100K trades/hour
├─ Per-partition throughput: 10K events/second
└─ Need: 20 partitions

Calculation:
├─ Peak rate: 100K/hour = ~28 trades/second
├─ With 10x buffer: 280 trades/second
├─ Partitions needed: 280 / 10 = 28
└─ Round to 20 (power of 2, sufficient)
```

#### 3. **Partition Distribution**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Distribution Example                 │
└─────────────────────────────────────────────────────────┘

20 Partitions:
├─ account-001 → hash("account-001") % 20 = 1
├─ account-002 → hash("account-002") % 20 = 8
├─ account-003 → hash("account-003") % 20 = 15
└─ Even distribution across partitions
```

---

## Question 110: How do you handle event processing failures?

### Answer

### Event Processing Failure Handling

#### 1. **Failure Types**

```
┌─────────────────────────────────────────────────────────┐
│         Failure Types                                   │
└─────────────────────────────────────────────────────────┘

Transient Failures:
├─ Database connection issues
├─ Temporary service unavailability
├─ Network timeouts
└─ Retry with backoff

Permanent Failures:
├─ Invalid trade data
├─ Business rule violations
├─ Data corruption
└─ Dead letter queue

Critical Failures:
├─ Position calculation errors
├─ Ledger entry failures
└─ Require immediate attention
```

#### 2. **Retry Strategy**

```java
@Service
public class TradeEventProcessor {
    private static final int MAX_RETRIES = 5;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeCreatedEvent event) {
        int retryCount = 0;
        
        while (retryCount < MAX_RETRIES) {
            try {
                // Process event
                updatePosition(event);
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
                // Don't retry
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
    private final KafkaTemplate<String, FailedTradeEvent> kafkaTemplate;
    
    public void sendToDeadLetterQueue(TradeCreatedEvent event, Exception error) {
        FailedTradeEvent failedEvent = FailedTradeEvent.builder()
            .originalEvent(event)
            .errorMessage(error.getMessage())
            .errorType(error.getClass().getName())
            .timestamp(Instant.now())
            .retryCount(5)
            .build();
        
        kafkaTemplate.send("trade-events-dlq", event.getAccountId(), failedEvent);
        
        // Alert operations team
        alertService.sendCriticalAlert(
            "Trade event processing failed",
            failedEvent
        );
    }
}
```

---

## Question 111: What happens if an event is processed out of order?

### Answer

### Out-of-Order Event Handling

#### 1. **Detection**

```java
@Service
public class TradeEventProcessor {
    private final RedisTemplate<String, Long> redisTemplate;
    
    @KafkaListener(topics = "trade-events")
    public void handleTradeEvent(TradeCreatedEvent event) {
        String accountId = event.getAccountId();
        String sequenceKey = "sequence:account:" + accountId;
        
        // Get last processed sequence
        Long lastSequence = redisTemplate.opsForValue().get(sequenceKey);
        if (lastSequence == null) {
            lastSequence = 0L;
        }
        
        // Check if out of order
        if (event.getSequenceNumber() <= lastSequence) {
            // Out of order or duplicate
            log.warn("Out of order event detected: account={}, expected={}, got={}", 
                accountId, lastSequence + 1, event.getSequenceNumber());
            handleOutOfOrderEvent(event, lastSequence);
            return;
        }
        
        if (event.getSequenceNumber() > lastSequence + 1) {
            // Missing events
            log.error("Missing events: account={}, expected={}, got={}", 
                accountId, lastSequence + 1, event.getSequenceNumber());
            handleMissingEvents(accountId, lastSequence + 1, event.getSequenceNumber());
            return;
        }
        
        // Process in order
        processEvent(event);
        redisTemplate.opsForValue().set(sequenceKey, event.getSequenceNumber());
    }
}
```

#### 2. **Handling Strategy**

```java
@Service
public class OutOfOrderEventHandler {
    
    public void handleOutOfOrderEvent(TradeCreatedEvent event, Long lastSequence) {
        if (event.getSequenceNumber() < lastSequence) {
            // Duplicate or very old event
            log.info("Skipping duplicate/old event: {}", event.getTradeId());
            return;
        }
        
        if (event.getSequenceNumber() == lastSequence) {
            // Duplicate - check if already processed
            if (isTradeAlreadyProcessed(event.getTradeId())) {
                log.info("Trade already processed: {}", event.getTradeId());
                return;
            }
            
            // Reprocess (idempotent)
            processEvent(event);
        }
    }
    
    public void handleMissingEvents(String accountId, Long fromSequence, Long toSequence) {
        // Request replay of missing events
        eventReplayService.replayEvents(accountId, fromSequence, toSequence);
    }
}
```

#### 3. **Event Replay**

```java
@Service
public class EventReplayService {
    
    public void replayEvents(String accountId, Long fromSequence, Long toSequence) {
        // Read events from Kafka for this account
        List<TradeCreatedEvent> events = readEventsFromKafka(
            accountId, 
            fromSequence, 
            toSequence
        );
        
        // Process in order
        for (TradeCreatedEvent event : events) {
            processEvent(event);
        }
    }
}
```

---

## Question 112: How do you ensure exactly-once processing of events?

### Answer

### Exactly-Once Processing

#### 1. **Idempotency Keys**

```java
@Service
public class TradeEventProcessor {
    private final RedisTemplate<String, String> redisTemplate;
    
    @KafkaListener(topics = "trade-events")
    public void handleTradeEvent(TradeCreatedEvent event) {
        // Check idempotency key
        String idempotencyKey = "event:processed:" + event.getIdempotencyKey();
        
        Boolean alreadyProcessed = redisTemplate.opsForValue()
            .setIfAbsent(idempotencyKey, "processed", Duration.ofDays(7));
        
        if (!alreadyProcessed) {
            log.info("Event already processed: {}", event.getIdempotencyKey());
            return; // Skip duplicate
        }
        
        // Process event
        processEvent(event);
    }
}
```

#### 2. **Transactional Processing**

```java
@Service
public class TradeEventProcessor {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Transactional
    @KafkaListener(topics = "trade-events")
    public void handleTradeEvent(TradeCreatedEvent event) {
        // Process event
        updatePosition(event);
        createLedgerEntry(event);
        
        // Emit new events (within transaction)
        kafkaTemplate.send("position-events", 
            new PositionUpdatedEvent(event));
        kafkaTemplate.send("ledger-events", 
            new LedgerEntryCreatedEvent(event));
        
        // Transaction commits all or nothing
    }
}
```

#### 3. **Idempotent Operations**

```java
@Service
public class PositionService {
    
    public void updatePosition(TradeCreatedEvent event) {
        // Idempotent operation
        Position position = getPosition(event.getAccountId(), event.getInstrumentId());
        
        // Check if trade already applied
        if (position.getProcessedTrades().contains(event.getTradeId())) {
            log.info("Trade already applied to position: {}", event.getTradeId());
            return; // Already processed
        }
        
        // Apply trade
        PositionChange change = calculateChange(event);
        Position newPosition = position.apply(change);
        newPosition.getProcessedTrades().add(event.getTradeId());
        
        savePosition(newPosition);
    }
}
```

---

## Question 113: Explain the sequence number mechanism for events.

### Answer

### Sequence Number Mechanism

#### 1. **Sequence Number Generation**

```java
@Service
public class TradeEventPublisher {
    private final RedisTemplate<String, Long> redisTemplate;
    
    public void publishTradeCreatedEvent(Trade trade) {
        String accountId = trade.getAccountId();
        String sequenceKey = "sequence:account:" + accountId;
        
        // Generate sequence number (atomic increment)
        Long sequenceNumber = redisTemplate.opsForValue().increment(sequenceKey);
        
        // Create event with sequence number
        TradeCreatedEvent event = TradeCreatedEvent.builder()
            .tradeId(trade.getTradeId())
            .accountId(accountId)
            .instrumentId(trade.getInstrumentId())
            .quantity(trade.getQuantity())
            .price(trade.getPrice())
            .sequenceNumber(sequenceNumber)
            .timestamp(Instant.now())
            .build();
        
        // Publish event
        kafkaTemplate.send("trade-events", accountId, event);
    }
}
```

#### 2. **Sequence Number Validation**

```java
@Service
public class TradeEventProcessor {
    private final RedisTemplate<String, Long> redisTemplate;
    
    @KafkaListener(topics = "trade-events")
    public void handleTradeEvent(TradeCreatedEvent event) {
        String accountId = event.getAccountId();
        String sequenceKey = "sequence:processed:" + accountId;
        
        // Get last processed sequence
        Long lastProcessed = redisTemplate.opsForValue().get(sequenceKey);
        if (lastProcessed == null) {
            lastProcessed = 0L;
        }
        
        // Validate sequence
        Long expectedSequence = lastProcessed + 1;
        
        if (event.getSequenceNumber() < expectedSequence) {
            // Out of order or duplicate
            log.warn("Out of order event: expected {}, got {}", 
                expectedSequence, event.getSequenceNumber());
            return;
        }
        
        if (event.getSequenceNumber() > expectedSequence) {
            // Missing events
            log.error("Missing events: expected {}, got {}", 
                expectedSequence, event.getSequenceNumber());
            requestReplay(accountId, expectedSequence, event.getSequenceNumber());
            return;
        }
        
        // Process event
        processEvent(event);
        
        // Update processed sequence
        redisTemplate.opsForValue().set(sequenceKey, event.getSequenceNumber());
    }
}
```

#### 3. **Sequence Number Recovery**

```java
@Service
public class SequenceNumberRecoveryService {
    
    public void recoverSequenceNumbers(String accountId) {
        // Read all events from Kafka
        List<TradeCreatedEvent> events = readAllEventsFromKafka(accountId);
        
        // Find max sequence number
        Long maxSequence = events.stream()
            .mapToLong(TradeCreatedEvent::getSequenceNumber)
            .max()
            .orElse(0L);
        
        // Update sequence counter
        String sequenceKey = "sequence:account:" + accountId;
        redisTemplate.opsForValue().set(sequenceKey, maxSequence + 1);
    }
}
```

---

## Question 114: What's the event schema evolution strategy?

### Answer

### Schema Evolution Strategy

#### 1. **Backward Compatibility**

```java
// Version 1: Initial schema
public class TradeCreatedEventV1 {
    private String tradeId;
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal price;
    private Instant timestamp;
}

// Version 2: Added optional field (backward compatible)
public class TradeCreatedEventV2 {
    private String tradeId;
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal price;
    private Instant timestamp;
    private String orderId; // New optional field
    private String strategy; // New optional field
}
```

#### 2. **Schema Registry**

```java
@Configuration
public class SchemaRegistryConfig {
    
    @Bean
    public SchemaRegistryClient schemaRegistryClient() {
        return new CachedSchemaRegistryClient(
            "http://schema-registry:8081",
            100
        );
    }
    
    @Bean
    public KafkaAvroSerializer avroSerializer() {
        Map<String, Object> props = new HashMap<>();
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, 
            "http://schema-registry:8081");
        return new KafkaAvroSerializer(schemaRegistryClient());
    }
}
```

#### 3. **Version Handling**

```java
@Service
public class TradeEventProcessor {
    
    @KafkaListener(topics = "trade-events")
    public void handleTradeEvent(EventWrapper wrapper) {
        int schemaVersion = wrapper.getSchemaVersion();
        
        // Deserialize based on version
        TradeCreatedEvent event = deserializeEvent(wrapper, schemaVersion);
        
        // Process event
        processEvent(event);
    }
    
    private TradeCreatedEvent deserializeEvent(EventWrapper wrapper, int version) {
        switch (version) {
            case 1:
                return deserializeV1(wrapper.getData());
            case 2:
                return deserializeV2(wrapper.getData());
            default:
                throw new UnsupportedSchemaVersionException(version);
        }
    }
}
```

---

## Question 115: How do you handle high event volume (1M+ trades/day)?

### Answer

### High Volume Handling

#### 1. **Scaling Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Scaling Strategy                                │
└─────────────────────────────────────────────────────────┘

Partitioning:
├─ 20 partitions for trade-events
├─ Parallel processing
└─ Even distribution

Consumer Scaling:
├─ 20 consumer instances
├─ One per partition
└─ Maximum parallelism

Batch Processing:
├─ Process events in batches
├─ Reduce overhead
└─ Improve throughput
```

#### 2. **Batch Processing**

```java
@KafkaListener(topics = "trade-events", groupId = "position-service")
public void handleTradeEvents(
    @Payload List<TradeCreatedEvent> events,
    @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions) {
    
    // Process batch
    List<CompletableFuture<Void>> futures = events.stream()
        .map(event -> CompletableFuture.runAsync(() -> processEvent(event)))
        .collect(Collectors.toList());
    
    // Wait for all
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .join();
}
```

#### 3. **Performance Optimization**

```java
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConsumerFactory<String, TradeCreatedEvent> tradeEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "position-service");
        
        // Performance tuning
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024 * 1024); // 1MB
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // 500ms
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Batch size
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5min
        
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
```

#### 4. **Throughput Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Throughput Metrics                            │
└─────────────────────────────────────────────────────────┘

Targets:
├─ 1M trades/day = ~12 trades/second average
├─ Peak: 100K trades/hour = ~28 trades/second
├─ Per partition: ~1.4 trades/second
└─ Well within capacity (10K events/second per partition)

Actual Performance:
├─ Average: 15 trades/second
├─ Peak: 35 trades/second
├─ P95 latency: 50ms
└─ P99 latency: 100ms
```

---

## Summary

Event-Driven Architecture Part 2 covers:

1. **Event Bus**: 5 topics for Prime Broker System
2. **Event Ordering**: Account-based partitioning with sequence numbers
3. **Partitioning**: 20 partitions for high throughput
4. **Failure Handling**: Retry, DLQ, and critical alerts
5. **Out-of-Order**: Sequence number validation and replay
6. **Exactly-Once**: Idempotency keys and transactional processing
7. **Sequence Numbers**: Atomic generation and validation
8. **Schema Evolution**: Backward compatibility with Schema Registry
9. **High Volume**: Batch processing and consumer scaling
