# Deep Technical Answers - Part 11: Event Processing (Questions 51-55)

## Question 51: You "architected Prime Broker system with Kafka event bus." How do you ensure event ordering?

### Answer

### Event Ordering in Kafka

#### 1. **Event Ordering Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Event Ordering                           │
└─────────────────────────────────────────────────────────┘

Ordering Guarantees:
├─ Within partition: Guaranteed order
├─ Across partitions: No guarantee
├─ Key-based partitioning: Same key → same partition
└─ Single consumer per partition: Sequential processing
```

#### 2. **Key-Based Partitioning**

```java
@Service
public class TradeEventProducer {
    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;
    
    public void publishTradeEvent(TradeEvent event) {
        // Partition by accountId ensures ordering per account
        String key = event.getAccountId();
        kafkaTemplate.send("trade-events", key, event);
        
        // All events for same account go to same partition
        // Processed in order by single consumer
    }
}
```

#### 3. **Consumer Configuration**

```java
@KafkaListener(topics = "trade-events")
public void handleTradeEvent(TradeEvent event) {
    // Single consumer per partition
    // Events processed sequentially
    processTradeEvent(event);
}

// Consumer configuration
@Bean
public ConsumerFactory<String, TradeEvent> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "trade-processor");
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1); // Process one at a time
    return new DefaultKafkaConsumerFactory<>(props);
}
```

---

## Question 52: How do you handle event processing failures?

### Answer

### Event Processing Failure Handling

#### 1. **Failure Handling Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Event Processing Failure Handling            │
└─────────────────────────────────────────────────────────┘

Handling Mechanisms:
├─ Retry with backoff
├─ Dead letter queue
├─ Error handling
└─ Monitoring and alerting
```

#### 2. **Retry with Dead Letter Queue**

```java
@KafkaListener(topics = "trade-events")
public void handleTradeEvent(TradeEvent event) {
    try {
        processTradeEvent(event);
    } catch (Exception e) {
        // Retry logic
        if (shouldRetry(e)) {
            retryEvent(event);
        } else {
            // Send to dead letter queue
            sendToDeadLetterQueue(event, e);
        }
    }
}

private void sendToDeadLetterQueue(TradeEvent event, Exception e) {
    DeadLetterEvent dlqEvent = new DeadLetterEvent(event, e);
    kafkaTemplate.send("trade-events-dlq", dlqEvent);
}
```

---

## Question 53: What's your approach to exactly-once processing?

### Answer

### Exactly-Once Processing

#### 1. **Exactly-Once Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Exactly-Once Processing                        │
└─────────────────────────────────────────────────────────┘

Mechanisms:
├─ Idempotency keys
├─ Transactional producers
├─ Idempotent consumers
└─ Deduplication
```

#### 2. **Idempotent Consumer**

```java
@Service
public class IdempotentEventProcessor {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void processEvent(TradeEvent event) {
        String eventId = event.getEventId();
        
        // Check if already processed
        String processed = redisTemplate.opsForValue()
            .get("event:processed:" + eventId);
        
        if (processed != null) {
            // Already processed, skip
            return;
        }
        
        // Process event
        processTradeEvent(event);
        
        // Mark as processed
        redisTemplate.opsForValue().set(
            "event:processed:" + eventId,
            "true",
            Duration.ofDays(7)
        );
    }
}
```

---

## Question 54: How do you handle event schema evolution?

### Answer

### Event Schema Evolution

#### 1. **Schema Evolution Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Evolution                               │
└─────────────────────────────────────────────────────────┘

Approaches:
├─ Schema registry (Confluent, Avro)
├─ Versioned schemas
├─ Backward compatibility
└─ Forward compatibility
```

#### 2. **Schema Registry**

```java
// Use Confluent Schema Registry
@Configuration
public class KafkaConfig {
    @Bean
    public KafkaAvroSerializer avroSerializer() {
        Map<String, Object> props = new HashMap<>();
        props.put("schema.registry.url", "http://schema-registry:8081");
        return new KafkaAvroSerializer();
    }
}

// Schema evolution handled by registry
// Backward compatible: Old consumers can read new events
// Forward compatible: New consumers can read old events
```

---

## Question 55: What's your strategy for event replay?

### Answer

### Event Replay Strategy

#### 1. **Event Replay**

```
┌─────────────────────────────────────────────────────────┐
│         Event Replay Strategy                          │
└─────────────────────────────────────────────────────────┘

Replay Scenarios:
├─ Reprocess failed events
├─ Rebuild state from events
├─ Data migration
└─ Testing and debugging
```

#### 2. **Replay Implementation**

```java
@Service
public class EventReplayService {
    public void replayEvents(String topic, long fromOffset, long toOffset) {
        // Create consumer with specific offset
        KafkaConsumer<String, TradeEvent> consumer = createConsumer();
        consumer.seek(new TopicPartition(topic, 0), fromOffset);
        
        // Process events
        while (true) {
            ConsumerRecords<String, TradeEvent> records = 
                consumer.poll(Duration.ofMillis(100));
            
            for (ConsumerRecord<String, TradeEvent> record : records) {
                if (record.offset() > toOffset) {
                    break;
                }
                
                // Replay event
                processTradeEvent(record.value());
            }
        }
    }
}
```

---

## Summary

Part 11 covers questions 51-55 on Event Processing:

51. **Event Ordering**: Key-based partitioning, single consumer per partition
52. **Event Processing Failures**: Retry, dead letter queue
53. **Exactly-Once Processing**: Idempotency, transactional producers
54. **Schema Evolution**: Schema registry, backward/forward compatibility
55. **Event Replay**: Offset-based replay, state rebuilding

Key techniques:
- Key-based partitioning for ordering
- Dead letter queues for failures
- Idempotency for exactly-once
- Schema registry for evolution
- Event replay for recovery
